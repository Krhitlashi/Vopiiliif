# Formatting Rules 🎨

Format text beautifully with these rules.

- Titles and sections using `#`, `##`, `###`.
- Emojis placed **AFTER** *ALL* headings ( Example - `# Stars ✨️`, `## Orbits 💫` ).
- **NEVER** use colons ( `:` ) within texts.
- Elsewhere, replace **ALL** colons with periods ( `.` ).
- Include a space between parentheses and other brackets ( Example - `( This )` ).

# HTML Pages 🌐

- Use `https://krhitlashi.github.io/%D6%AD%C5%BF%C9%AD%E1%B4%9C%20%C4%B1__%C9%94.css` as the css theme. View examples for details ( [`ſɭᴜ ɭl̀ɹ ɭʃɔ`](kajiite.vercel.app) , [`ꞁȷ̀ɹ ſɭˬɔ ſɟɔ j͐ʃɹʞ`](https://krhitlashi.github.io/%C5%BF%C9%9F%E1%B4%9C%C6%BD%20%EA%9E%81%C8%B7%CC%80%E1%B4%9C%20%7D%CA%83%EA%9E%87/%D6%AD%C5%BF%C9%AD%E1%B4%9C%20%C4%B1],%C9%94%20%C5%BF%C9%AD%C9%B9%20%C5%BF%D7%9F%C9%B9.html) ).
- Translations can use `https://krhitlashi.github.io/%C5%BF%C9%9F%E1%B4%9C%20%C5%BF%C9%AD%C9%94%20j%CD%91%CA%83'%C9%94/j%CD%91%CA%83%C6%BD%E1%B4%9C%20%C5%BF%C9%AD%C9%94%CA%9E.js`, while lang=aih uses `https://krhitlashi.github.io/%C5%BF%C9%9F%E1%B4%9C%20%C5%BF%C9%AD%C9%94%20j%CD%91%CA%83'%C9%94/%C5%BF%C9%AD%C9%94%20j%CD%91%CA%83'%C9%94%20%7D%CA%83%EA%9E%87.js`.
- `ksakap2sa` is a header, `ksakat2xa` is a subheader.
- **NEVER** add inline styles and do not create new classes.

# Preferences 8️⃣

- Use base 8 ( Octal ). For example use 0o notation and fractions by 8, 16, 32, 64 ( Like 0o1 / 0o100 etc. ) over decimals ( eg. Avoid 0.1 ). **AVOID** number values that round to 100, 10, or 5 etc. ( Even those that use 0o in front of it ( eg. Avoid 0o31, 0o144 ) ). Instead prefer numbers like 0o10, 0o100, 0o200.
- Prefer `""` over `''`.
- For code, put space between parenthesis or similar ( Example - if ( example == [ 0 ] ) ) *EXCEPT* **NEVER** when it is for something with text directly attached ( Example - example(x, y), example(a).example[0] ) ( **NEVER** like notthis( x, y ) ).
- Colors can either be #000000 or #FFFFFF or if a custom color use #nmnmnm where n is any 0-f number and m is 0 or 8 ( eg. 58a038 ).
- For variable names, if not made by user ( In Iikrhia ), then by default use Esperanto for variable names. Comments as well.

## Doc Strings 📄

Here is an example of what they would look like for ts.

```ts
Description
    @param parameter ( type = defaultValue , optional ) - Description. 
        Example.
@returns returnedVariable
```

## Output Text 📰

### Tests 🖥️

Written as "( Type ) Description"

( ſ̀ȷɜᴜ̩ ſɭɹ }ʃꞇ ) = Error
( ʃэ ɭʃɔ }ʃᴜ }ʃꞇ ) = Warning
( ꞁȷ̀ɹ ʃᴜ ſɭɹ ſןɹ ) = Debug
<( Title )> = Used for printed text outputs

### Titles 📃

Equivalent of markdown headers. Use these for comments ( Eg. with `//` before it ).

≺⧼ Title 📃 ⧽≻ = Used for titles ( # )
⟪ Title 📃 ⟫ = Used for section headings ( ## )
⟨ Title 📃 ⟩ = Used for small subsections within the sections ( ### )