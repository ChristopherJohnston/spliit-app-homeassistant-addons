const { createServer } = require('http')
// var http = require('http');
var fs = require('fs');
const { parse } = require('url')
const next = require('next')

// var options = {
//   key: fs.readFileSync(`${process.env.KEYFILE}`),
//   cert: fs.readFileSync(`${process.env.CERTFILE}`)
// };

const dev = process.env.NODE_ENV !== 'production'
const hostname = 'localhost'
const port = 3000

const app = next({ dev, hostname, port })
const handle = app.getRequestHandler()

app.prepare().then(() => {
  createServer(async (req, res) => {
    try {
      // Be sure to pass `true` as the second argument to `url.parse`.
      // This tells it to parse the query portion of the URL.
      const parsedUrl = parse(req.url, true)
      const { pathname, query } = parsedUrl

      console.log(`x-ingress-path: ${req.headers["x-ingress-path"]}`)
      console.log(`x-remote-user-id: ${req.headers['x-remote-user-id']}`)
      console.log(`x-remote-user-name: ${req.headers['x-remote-user-name']}`)
      console.log(`x-remote-user-display-name: ${req.headers['x-remote-user-display-name']}`)

      console.log(parsedUrl)
      console.log(pathname)

      if (pathname === '/groups') {
        await app.render(req, res, '/groups', query)
      } else if (pathname === '/b') {
        await app.render(req, res, '/b', query)
      } else {
        await handle(req, res, parsedUrl)
      }
    } catch (err) {
      console.error('Error occurred handling', req.url, err)
      res.statusCode = 500
      res.end('internal server error')
    }
  })
    .once('error', (err) => {
      console.error(err)
      process.exit(1)
    })
    .listen(port, () => {
      console.log(`> Ready on https://${hostname}:${port}`)
    })
})