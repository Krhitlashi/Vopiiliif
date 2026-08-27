package aih.iikrhia.vopiiliif

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aih.iikrhia.vopiiliif.network.WikiNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import aih.iikrhia.vopiiliif.R

sealed class WikiState {
    object Idle : WikiState()
    object Loading : WikiState()
    data class SuccessSearch(val results: List<aih.iikrhia.vopiiliif.network.SearchResult>, val isWiktionary: Boolean) : WikiState()
    data class SuccessDetail(
        val title: String,
        val blocks: List<aih.iikrhia.vopiiliif.network.WikiBlock>,
        val isWiktionary: Boolean,
        val langCode: String = "en",
        val langLinks: List<aih.iikrhia.vopiiliif.network.LangLink> = emptyList()
    ) : WikiState()
    data class Error(val message: String) : WikiState()
}

data class BookmarkItem(
    val title: String,
    val langCode: String,
    val isWiktionary: Boolean = false
) {
    fun toStorageString(): String = "$title|$langCode|$isWiktionary"

    companion object {
        fun fromStorageString(str: String): BookmarkItem {
            if (!str.contains("|")) {
                return BookmarkItem(title = str, langCode = "en", isWiktionary = false)
            }
            val parts = str.split("|")
            val title = parts.getOrElse(0) { str }
            val lang = parts.getOrElse(1) { "en" }
            val isWik = parts.getOrNull(2)?.lowercase()?.toBooleanStrictOrNull() ?: false
            return BookmarkItem(title = title, langCode = lang, isWiktionary = isWik)
        }
    }
}

class WikiViewModel : ViewModel() {
    private val _state = MutableStateFlow<WikiState>(WikiState.Idle)
    val state: StateFlow<WikiState> = _state.asStateFlow()

    private val _isWiktionary = MutableStateFlow(false)
    val isWiktionary: StateFlow<Boolean> = _isWiktionary.asStateFlow()

    private val _langCode = MutableStateFlow("en")
    val langCode: StateFlow<String> = _langCode.asStateFlow()

    private val _availableLanguages = MutableStateFlow<List<aih.iikrhia.vopiiliif.network.LangLink>>(emptyList())
    val availableLanguages: StateFlow<List<aih.iikrhia.vopiiliif.network.LangLink>> = _availableLanguages.asStateFlow()

    private val _siteLanguages = MutableStateFlow<List<aih.iikrhia.vopiiliif.network.LangLink>>(emptyList())
    val siteLanguages: StateFlow<List<aih.iikrhia.vopiiliif.network.LangLink>> = _siteLanguages.asStateFlow()

    private val _recentLanguages = MutableStateFlow<List<String>>(emptyList())
    val recentLanguages: StateFlow<List<String>> = _recentLanguages.asStateFlow()

    fun loadRecentLanguages(context: android.content.Context) {
        val prefs = context.getSharedPreferences("wiki_settings", android.content.Context.MODE_PRIVATE)
        val csv = prefs.getString("recent_langs", "") ?: ""
        if (csv.isNotBlank()) {
            val list = csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (list.isNotEmpty()) {
                _recentLanguages.value = list
            }
        }
    }

    fun addRecentLanguage(context: android.content.Context?, code: String) {
        if (code.isBlank()) return
        val current = _recentLanguages.value.toMutableList()
        current.remove(code)
        current.add(0, code)
        val trimmed = current.take(5)
        _recentLanguages.value = trimmed
        if (context != null) {
            val prefs = context.getSharedPreferences("wiki_settings", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("recent_langs", trimmed.joinToString(",")).apply()
        }
    }

    init {
        fetchSiteLanguages()
    }

    fun fetchSiteLanguages() {
        viewModelScope.launch {
            try {
                val api = WikiNetwork.getService(_langCode.value, _isWiktionary.value)
                val response = api.getSiteLanguages()
                val list = response.query?.languages?.map {
                    aih.iikrhia.vopiiliif.network.LangLink(
                        lang = it.code,
                        langname = "${it.name} ( ${it.code.uppercase()} )",
                        title = ""
                    )
                } ?: emptyList()
                if (list.isNotEmpty()) {
                    _siteLanguages.value = list
                } else {
                    useFallbackLanguages()
                }
            } catch (e: Exception) {
                useFallbackLanguages()
            }
        }
    }

    private fun useFallbackLanguages() {
        _siteLanguages.value = lingvoNomoj.map { (code, name) ->
            aih.iikrhia.vopiiliif.network.LangLink(
                lang = code,
                langname = "$name ( ${code.uppercase()} )",
                title = ""
            )
        }
    }

    private val _showFullArticle = MutableStateFlow(true)
    val showFullArticle: StateFlow<Boolean> = _showFullArticle.asStateFlow()

    private val _showSearchSuggestions = MutableStateFlow(true)
    val showSearchSuggestions: StateFlow<Boolean> = _showSearchSuggestions.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _appLanguage = MutableStateFlow("aih")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _fontType = MutableStateFlow("default")
    val fontType: StateFlow<String> = _fontType.asStateFlow()

    private val _importedFontName = MutableStateFlow<String?>(null)
    val importedFontName: StateFlow<String?> = _importedFontName.asStateFlow()

    private val _customFontFamily = MutableStateFlow<androidx.compose.ui.text.font.FontFamily?>(null)
    val customFontFamily: StateFlow<androidx.compose.ui.text.font.FontFamily?> = _customFontFamily.asStateFlow()

    private fun isValidFont(file: java.io.File): Boolean {
        if (!file.exists() || file.length() == 0L) return false
        return try {
            val font = androidx.compose.ui.text.font.Font(file)
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun createFontFamily(file: java.io.File): androidx.compose.ui.text.font.FontFamily {
        return androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(file))
    }

    fun loadStoredFont(context: android.content.Context) {
        val sharedPrefs = context.getSharedPreferences("wiki_settings", android.content.Context.MODE_PRIVATE)
        val type = sharedPrefs.getString("font_type", "default") ?: "default"
        val name = sharedPrefs.getString("imported_font_name", null)

        val fontFile = java.io.File(context.filesDir, "custom_font.ttf")
        if (fontFile.exists() && fontFile.length() > 0L) {
            if (isValidFont(fontFile)) {
                try {
                    _customFontFamily.value = createFontFamily(fontFile)
                    _fontType.value = type
                    _importedFontName.value = name
                } catch (t: Throwable) {
                    _fontType.value = "default"
                    _importedFontName.value = null
                    _customFontFamily.value = null
                    fontFile.delete()
                }
            } else {
                _fontType.value = "default"
                _importedFontName.value = null
                _customFontFamily.value = null
                try { fontFile.delete() } catch (ignored: Throwable) {}
                sharedPrefs.edit()
                    .putString("font_type", "default")
                    .remove("imported_font_name")
                    .apply()
            }
        } else {
            _fontType.value = "default"
            _importedFontName.value = null
            _customFontFamily.value = null
        }
    }

    fun setFontType(context: android.content.Context, type: String) {
        _fontType.value = type
        val sharedPrefs = context.getSharedPreferences("wiki_settings", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("font_type", type).apply()
    }

    fun importFont(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var fileName = "imported_font.ttf"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }

                val fontFile = java.io.File(context.filesDir, "custom_font.ttf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    fontFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (isValidFont(fontFile)) {
                    val newFontFamily = createFontFamily(fontFile)

                    _customFontFamily.value = newFontFamily
                    _importedFontName.value = fileName
                    _fontType.value = "imported"

                    val sharedPrefs = context.getSharedPreferences("wiki_settings", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit()
                        .putString("font_type", "imported")
                        .putString("imported_font_name", fileName)
                        .apply()

                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, context.getString(R.string.font_import_success, fileName), android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (fontFile.exists()) {
                        try { fontFile.delete() } catch (ignored: Throwable) {}
                    }
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Invalid font file structure", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            } catch (t: Throwable) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Failed to load font - " + (t.localizedMessage ?: t.message), android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun deleteImportedFont(context: android.content.Context) {
        _fontType.value = "default"
        _importedFontName.value = null
        _customFontFamily.value = null

        val fontFile = java.io.File(context.filesDir, "custom_font.ttf")
        if (fontFile.exists()) {
            fontFile.delete()
        }

        val sharedPrefs = context.getSharedPreferences("wiki_settings", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("font_type", "default")
            .remove("imported_font_name")
            .apply()
    }

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
    }

    fun toggleSearchSuggestions(show: Boolean) {
        _showSearchSuggestions.value = show
        if (!show) {
            _suggestions.value = emptyList()
        }
    }

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    fun getSuggestions(query: String) {
        if (!_showSearchSuggestions.value || query.isBlank()) {
            suggestionsJob?.cancel()
            _suggestions.value = emptyList()
            return
        }
        // Cancel the previous lookup so a stale response can't clobber the
        // suggestion list while the user is still typing.
        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch {
            try {
                val api = WikiNetwork.getService(_langCode.value, _isWiktionary.value)
                val response = api.search(query)
                val titles = response.query?.search?.map { it.title } ?: emptyList()
                _suggestions.value = titles.distinct().take(16)
            } catch (e: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    private var lastSearchResults: List<aih.iikrhia.vopiiliif.network.SearchResult> = emptyList()
    private var currentSearchQuery: String = ""
    private val detailHistory = mutableListOf<WikiState.SuccessDetail>()
    private var detailLoadJob: kotlinx.coroutines.Job? = null
    private var searchJob: kotlinx.coroutines.Job? = null
    private var suggestionsJob: kotlinx.coroutines.Job? = null

    private val lingvoNomoj = linkedMapOf(
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "ja" to "日本語",
        "zh" to "中文",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "ar" to "العربية",
        "hi" to "हिन्दी",
        "he" to "עברית",
        "la" to "Latina",
        "eo" to "Esperanto",
        "ko" to "한국어",
        "nl" to "Nederlands",
        "sv" to "Svenska",
        "pl" to "Polski",
        "vi" to "Tiếng Việt",
        "uk" to "Українська",
        "tr" to "Türkçe"
    )

    private fun getLanguageReadableName(code: String): String {
        return lingvoNomoj[code.lowercase()] ?: code.uppercase()
    }

    fun switchArticleLanguage(context: android.content.Context? = null, code: String, translatedTitle: String) {
        if (code.isNotBlank()) {
            addRecentLanguage(context, code)
        }
        _langCode.value = code
        if (translatedTitle.isNotBlank()) {
            loadExtract(translatedTitle)
        } else {
            val currentState = state.value
            if (currentState is WikiState.SuccessDetail) {
                loadExtract(currentState.title)
            }
        }
    }

    fun toggleSource() {
        _isWiktionary.value = !_isWiktionary.value
        fetchSiteLanguages()
        // If we have an active search, re-run with new source
        if (state.value is WikiState.SuccessSearch && currentSearchQuery.isNotBlank()) {
            search(currentSearchQuery)
        } else {
            clear()
        }
    }

    fun setLanguage(context: android.content.Context? = null, code: String) {
        if (code.isNotBlank()) {
            addRecentLanguage(context, code)
        }
        _langCode.value = code
        val currentState = state.value
        if (currentState is WikiState.SuccessDetail) {
            // Reload the same page but in the new language!
            loadExtract(currentState.title)
        } else if (currentState is WikiState.SuccessSearch && currentSearchQuery.isNotBlank()) {
            // Re-run the active search in the new language!
            search(currentSearchQuery)
        } else {
            clear()
        }
    }

    fun toggleShowFullArticle(full: Boolean) {
        _showFullArticle.value = full
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun toggleSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun search(query: String) {
        if (query.isBlank()) return
        currentSearchQuery = query
        _state.value = WikiState.Loading
        // Cancel any in-flight search so a stale response cannot overwrite a newer one
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val api = WikiNetwork.getService(_langCode.value, _isWiktionary.value)
                val response = api.search(query)
                val results = response.query?.search ?: emptyList()
                lastSearchResults = results
                _state.value = WikiState.SuccessSearch(results, _isWiktionary.value)
            } catch (e: Exception) {
                // Formatting instructions: use " - " instead of colons in strings, and standard parenthesis format
                _state.value = WikiState.Error("Failed to fetch results - ${e.localizedMessage ?: e.message}")
            }
        }
    }

    // Convert the plain-text lead extract into simple paragraph blocks so the
    // article shows content almost instantly while the full page still loads.
    private fun parseLeadToBlocks(text: String): List<aih.iikrhia.vopiiliif.network.WikiBlock> {
        if (text.isBlank()) return emptyList()
        return text.split("\n\n").mapNotNull { p ->
            val t = p.trim()
            if (t.isNotBlank()) {
                aih.iikrhia.vopiiliif.network.WikiBlock.Paragraph(t.replace('\n', ' '))
            } else {
                null
            }
        }
    }

    fun loadExtract(
        title: String,
        pushToHistory: Boolean = false,
        overrideWiktionary: Boolean? = null,
        overrideLangCode: String? = null
    ) {
        if (overrideWiktionary != null) {
            _isWiktionary.value = overrideWiktionary
        }
        if (!overrideLangCode.isNullOrBlank()) {
            _langCode.value = overrideLangCode
        }

        if (pushToHistory) {
            val currentState = _state.value
            if (currentState is WikiState.SuccessDetail) {
                detailHistory.add(currentState)
            }
        } else {
            detailHistory.clear()
        }
        
        _state.value = WikiState.Loading
        detailLoadJob?.cancel()
        detailLoadJob = viewModelScope.launch {
            try {
                val currentLang = _langCode.value
                val currentIsWik = _isWiktionary.value
                val api = WikiNetwork.getService(currentLang, currentIsWik)
                val domain = if (currentIsWik) "wiktionary" else "wikipedia"
                val baseUri = "https://$currentLang.$domain.org"

                val currentLink = aih.iikrhia.vopiiliif.network.LangLink(
                    lang = currentLang,
                    langname = getLanguageReadableName(currentLang),
                    title = title
                )
                _availableLanguages.value = listOf(currentLink)

                // 1. Lead-first (official-app pattern): show the intro extract as soon
                //    as it arrives (a small, fast response) so the page feels instant.
                //    The full parse below replaces it; the UI swaps directly because
                //    the animated-content key is the article identity.
                var leadShown = false
                try {
                    val extractResponse = api.getExtract(title, 1)
                    val leadText = extractResponse.query?.pages?.values?.firstOrNull()?.extract ?: ""
                    val leadBlocks = parseLeadToBlocks(leadText)
                    if (leadBlocks.isNotEmpty()) {
                        leadShown = true
                        _state.value = WikiState.SuccessDetail(title, leadBlocks, currentIsWik, currentLang, listOf(currentLink))
                    }
                } catch (e: Exception) {
                    // Lead is best-effort; the full parse below still loads the page.
                }

                // 2. Full page (parsed off the main thread).
                val response = api.parsePage(title)
                val html = response.parse?.text?.html ?: ""
                val blocks = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    aih.iikrhia.vopiiliif.network.parseHtmlToBlocks(html, baseUri)
                }
                // Keep the lead if the full parse produced nothing.
                if (blocks.isNotEmpty() || !leadShown) {
                    _state.value = WikiState.SuccessDetail(title, blocks, currentIsWik, currentLang, listOf(currentLink))
                }

                launch {
                    try {
                        val langLinksResponse = api.getLangLinks(title)
                        val list = langLinksResponse.query?.pages?.values?.firstOrNull()?.langlinks ?: emptyList()
                        val pageLangLinks = listOf(currentLink) + list
                        _availableLanguages.value = pageLangLinks
                        val latestState = _state.value
                        if (latestState is WikiState.SuccessDetail && latestState.title == title && latestState.langCode == currentLang) {
                            _state.value = latestState.copy(langLinks = pageLangLinks)
                        }
                    } catch (e: Exception) {
                        // Keep only the current language link when the lookup fails
                    }
                }
            } catch (e: Exception) {
                _state.value = WikiState.Error("Failed to fetch article details - ${e.localizedMessage ?: e.message}")
            }
        }
    }

    fun navigateBack() {
        if (detailHistory.isNotEmpty()) {
            val prevDetail = detailHistory.removeAt(detailHistory.lastIndex)
            _isWiktionary.value = prevDetail.isWiktionary
            _langCode.value = prevDetail.langCode
            _availableLanguages.value = prevDetail.langLinks
            _state.value = prevDetail
        } else {
            goBackToSearch()
        }
    }

    fun goBackToSearch() {
        detailHistory.clear()
        if (lastSearchResults.isNotEmpty()) {
            _state.value = WikiState.SuccessSearch(lastSearchResults, _isWiktionary.value)
        } else {
            _state.value = WikiState.Idle
        }
    }

    private val _savedArticles = MutableStateFlow<List<BookmarkItem>>(emptyList())
    val savedArticles: StateFlow<List<BookmarkItem>> = _savedArticles.asStateFlow()

    private val _showToc = MutableStateFlow(false)
    val showToc: StateFlow<Boolean> = _showToc.asStateFlow()

    private val _showBookmarks = MutableStateFlow(false)
    val showBookmarks: StateFlow<Boolean> = _showBookmarks.asStateFlow()

    fun toggleToc(show: Boolean) {
        _showToc.value = show
    }

    fun toggleBookmarks(show: Boolean) {
        _showBookmarks.value = show
    }

    fun loadSavedArticles(context: android.content.Context) {
        val prefs = context.getSharedPreferences("wiki_saved", android.content.Context.MODE_PRIVATE)
        val set = prefs.getStringSet("articles", emptySet()) ?: emptySet()
        val list = set.map { BookmarkItem.fromStorageString(it) }.sortedBy { it.title }
        _savedArticles.value = list
    }

    fun toggleBookmark(
        context: android.content.Context,
        title: String,
        langCode: String = _langCode.value,
        isWiktionary: Boolean = _isWiktionary.value
    ) {
        val prefs = context.getSharedPreferences("wiki_saved", android.content.Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet("articles", emptySet())?.toMutableSet() ?: mutableSetOf()
        val items = currentSet.map { BookmarkItem.fromStorageString(it) }.toMutableList()
        val existing = items.find { it.title == title && it.langCode == langCode && it.isWiktionary == isWiktionary }
        if (existing != null) {
            items.remove(existing)
        } else {
            items.add(BookmarkItem(title, langCode, isWiktionary))
        }
        val newSet = items.map { it.toStorageString() }.toSet()
        prefs.edit().putStringSet("articles", newSet).apply()
        _savedArticles.value = items.sortedBy { it.title }
    }

    fun clear() {
        detailHistory.clear()
        lastSearchResults = emptyList()
        _state.value = WikiState.Idle
    }
}
