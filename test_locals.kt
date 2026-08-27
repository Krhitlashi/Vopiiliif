import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistryOwner
import android.content.Context
import android.content.ContextWrapper

class LocalizedContextWrapper(base: Context, val configContext: Context) : ContextWrapper(base) {
    override fun getResources() = configContext.resources
}
