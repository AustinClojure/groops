(defproject groops "0.1.0-SNAPSHOT"
  :description "Austin Clojure Meetup groops project"
  :url "https://github.com/AustinClojure/groops"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cheshire "5.3.1"]
                 [compojure "1.1.8"]
                 [http-kit "2.1.16"]
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [org.clojure/clojurescript "0.0-2280"]
                 [om "0.7.0"]
                 [kioo "0.4.1-SNAPSHOT"]
                 [com.facebook/react "0.11.1"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [cider/cider-nrepl "0.7.0-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.0.3"]]
  :resource-paths ["resources"]
  :cljsbuild {:builds
              [{:source-paths ["src-cljs"]
                :compiler {
                           :output-to "resources/generated/js/groops.js"
                           :optimizations :whitespace
                           ;;:source-map true
                           :pretty-print true}}]}

  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}}

  :aliases {"server"  ["trampoline" "run" "-m" "groops.server"]})
