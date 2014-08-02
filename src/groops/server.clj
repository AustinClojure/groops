(ns groops.server
  (:require [groops.handler :as handler]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :refer [run-server]]

            [cider.nrepl.middleware.classpath]
            [cider.nrepl.middleware.complete]
            [cider.nrepl.middleware.info]
            [cider.nrepl.middleware.inspect]
            [cider.nrepl.middleware.stacktrace]
            [cider.nrepl.middleware.trace]

            [clojure.tools.nrepl.server :as nrepl]))

(def nrepl-middlewares [cider.nrepl.middleware.classpath/wrap-classpath
                        cider.nrepl.middleware.complete/wrap-complete
                        cider.nrepl.middleware.info/wrap-info
                        cider.nrepl.middleware.inspect/wrap-inspect
                        cider.nrepl.middleware.stacktrace/wrap-stacktrace
                        cider.nrepl.middleware.trace/wrap-trace])

(defn -main [& args]
  (println "Starting nrepl on port 8030")
  (nrepl/start-server :port 8030
                      :bind "127.0.0.1"
                      :handler (apply nrepl/default-handler nrepl-middlewares))

  (let [app (-> handler/app
                 reload/wrap-reload)]
    (run-server app {:port 8080})))

