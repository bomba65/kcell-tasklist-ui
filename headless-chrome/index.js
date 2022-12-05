const http = require('http');
const puppeteer = require('puppeteer');

const PORT = 8800;

async function main() {
  const browser = await puppeteer.launch({
    headless: true,
    ignoreHTTPSErrors: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const page = await browser.newPage();

  const requestHandler = async (request, response) => {
    if (request.url === '/topdf' && request.method === 'POST') {
      let body = '';
      request.on('data', (data) => {
        body += data;
      });
      request.on('end', async () => {
        try {
          await page.setContent(body);

          const buffer = await page.pdf({
            printBackground: true,
            format: 'a4',
            PreferCSSPageSize: true,
            margin: {
              top: '20px',
              bottom: '20px'
            }
          });

          response.setHeader("Content-Type", "application/pdf");
          response.end(buffer);
        } catch (e) {
          response.writeHead(500, 'Internal Server Error');
          response.end(e);
        }
      });
    } else {
      response.writeHead(404, 'Not found');
      response.end();
    }
  };

  const server = http.createServer(requestHandler);

  server.listen(PORT, (err) => {
    if (err) {
      return console.log('Something went wrong: ', err)
    }

    console.log(`server is listening on ${PORT}`)
  });

  server.addListener('close', async () => {
    if (page) await page.close();
    if (browser) await browser.close();
  });
}

(async () => {
  try {
    await main();
  } catch (e) {
    return console.log('Something went wrong: ', e)
  }
})();
