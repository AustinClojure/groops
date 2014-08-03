(ns groops.server
  (:require [groops.async :as async]
            [groops.web :as web]
            [ring.middleware.content-type :refer [wrap-content-type]]

            [ring.middleware.reload :refer [wrap-reload]]
            [org.httpkit.server :refer [run-server]]
            [clojure.tools.nrepl.server :as nrepl]
            [cider.nrepl :as cider]))

(def nrepl-port 8030)
(def http-port  8080)


(defn start-nrepl []
    (nrepl/start-server :port nrepl-port
                        :bind "127.0.0.1"
                        :handler cider/cider-nrepl-handler))

(defn start-webserver []
  (let [app (-> web/app
                wrap-content-type
                wrap-reload)]
    (run-server app {:port http-port})))

(defn -main [& args]
  (try
    (println "Starting nrepl on port" nrepl-port)
    (start-nrepl)

    (println "Starting websocket client loop")
    (async/send-loop)

    (println "Starting webserver on port" http-port)
    (start-webserver)

    (catch Throwable t
      (.printStackTrace t)
      (System/exit 1))))
