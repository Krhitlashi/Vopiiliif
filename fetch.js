const https = require('https');
const options = {
  hostname: 'en.wikipedia.org',
  port: 443,
  path: '/w/api.php?action=parse&format=json&disableeditsection=true&page=Cat',
  method: 'GET',
  headers: {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
  }
};
https.get(options, (resp) => {
    let data = '';
    resp.on('data', (chunk) => { data += chunk; });
    resp.on('end', () => {
        const fs = require('fs');
        const text = JSON.parse(data).parse.text['*'];
        fs.writeFileSync('sample.html', text);
        const cheerio = require('cheerio');
        const $ = cheerio.load(text);
        const topLevels = $('.mw-parser-output').children().map((i, el) => el.tagName + (el.attribs.class ? '.' + el.attribs.class.split(' ').join('.') : '')).get();
        console.log(topLevels.slice(0, 20));
    });
}).on("error", (err) => {
    console.log("Error: " + err.message);
});
