(defproject groops "0.1.0-SNAPSHOT"
  :description "Austin Clojure Meetup groops project"
  :url "https://github.com/AustinClojure/groops"

  :dependencies [[org.clojure/clojure "1.6.0"]

                 ;; server
                 [cheshire "5.3.1"]
                 [compojure "1.1.8"]
                 [http-kit "2.1.16"]
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-json "0.3.1"]
                 [liberator "0.12.1"]
                 [enlive "1.1.5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; clojurescript
                 [org.clojure/clojurescript "0.0-2280"]
                 [om "0.7.3"]
                 [kioo "0.4.1-SNAPSHOT"]
                 [cljs-ajax "0.2.6"]
                 [com.facebook/react "0.11.1"]
                 [cljs-hash "0.0.2"]

                 ;; dev
                 [org.clojure/tools.nrepl "0.2.3"]
                 [cider/cider-nrepl "0.8.0-snapshot"]
                 [figwheel "0.1.4-SNAPSHOT"]]

  :plugins [[com.cemerick/austin "0.1.5"]
            [lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.8"]
            [lein-figwheel "0.1.4-SNAPSHOT"]]

  :resource-paths ["resources"
                   "resources/generated"]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src-cljs"]
                :compiler {
                           :output-dir "resources/generated/public/js"
                           :output-to  "resources/generated/public/js/groops.js"
                           :source-map true
                           :optimizations :none #_:whitespace
                           :pretty-print true}}]}

  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]
                   :repl-options {:init-ns groops.web}}}

  :aliases {"server" ["trampoline" "run" "-m" "groops.server"]}

  :figwheel {:http-server-root "public"
             :server-port 3449})
