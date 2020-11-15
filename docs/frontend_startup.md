# Web-Dev Environment

## Front-end Setup

To get an interactive development environment run:

    npm install
    lein figwheel

To console.log CLJS data-structures, make sure you enable [devtools in
Chrome](https://developers.google.com/web/tools/chrome-devtools).

Start a REPL in terminal

    lein cljs-repl

Start a REPL specifically using dev build

    lein cljs-dev-repl

## Back-end Setup

Start the back-end web-server and pull up webpage

    lein ring server

Start the back-end web-server but don't pull up webpage

    lein ring server-headless
