# Web-Dev Environment


## In a REPL

Start a ClojureScript figwheel-main REPL building dev.

    M-x cider-jack-in-cljs

    C-c M-S-j

    $ lein cljs-dev-repl

The REPL should pull up a webpage displaying the application. After a
short delay, you should see `cljs.user=>` appear as your prompt. This
means that figwheel connected with the front-end, and the REPL can now
be used to evaluate ClojureScript.
