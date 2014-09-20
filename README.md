groops
======

This project will implement a Clojure version of the [Groops](https://github.com/groops/examples) application for the [Bleeding Edge Web](http://www.meetup.com/bleeding-edge-web/) meetup.  There are [loose specifications](https://github.com/groops/examples/wiki/Application-Specs) for the app, but we are free to implement that however we see fit. 

Keeping with the spirit of living on the Bleeding edge, the plan is to implement this using the "Immutable Stack" (coined by [Kitchen Table Coders](https://twitter.com/ktcoders)):

* Datomic
* Clojure
* ClojureScript
* Om

# Usage

Start the web application server with:
```
$ lein server
```

If you'd like a more interactive web development session then start the FigWheel server with:
```
$ lein figwheel
```

If you'd like a BREPL session then start austin by launching your Clojure REPL (cider recommended) and then launching the preconfigured BREPL
```
user> (ns groops.web)
;; => nil

groops.web> (brepl)
Browser-REPL ready @ http://localhost:XXXXX/XXX/repl/start
Type `:cljs/quit` to stop the ClojureScript REPL
;; => nil

cljs.user> (js/alert "hi")
;; => nil
;; you may need to refresh your browser at this point

cljs.user> (ns groops.app)
;; => nil

groops.app> @app-state
;; => {:user {:name "John Smith", :email "John.Smith@gmail.com", :twitter "@JohnSmith"}, :gravatar-url "http://www.gravatar.com/avatar/1c874909e198bf87d38b50ef7e4d3163", :room-count-map {:Alpha 0, :Beta 0, :Delta 0, :Gamma 0}}

groops.app> 
```
