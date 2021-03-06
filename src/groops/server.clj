(ns groops.server
  (:require [cemerick.piggieback]
            [clojure.tools.nrepl.server :as nrepl]
            [cider.nrepl :as cider]
            [groops.async :as async]
            [groops.web :refer [app]]
            [org.httpkit.server :refer [run-server]]))

(defonce nrepl-port 8030)
(defonce http-port  8080)

(defonce webserver (atom nil))
(defonce nrepl-server (atom nil))

(defn start-nrepl []
  (let [handler (apply nrepl/default-handler
                       #'cemerick.piggieback/wrap-cljs-repl
                       (map resolve cider/cider-middleware))]
    (reset! nrepl-server  (nrepl/start-server :port nrepl-port
                                              :bind "127.0.0.1"
                                              :handler handler))))

(defn start-webserver []
  (reset! webserver (run-server #'app {:port http-port
                                       :ip "127.0.0.1"})))

(defn stop-webserver []
  (when-not (nil? @webserver)
    (@webserver :timeout 100)
    (reset! webserver nil)))

(defn restart []
  (do
    (stop-webserver)
    (start-webserver)))

(defn -main [& args]
  (try
    (println "Starting nrepl on port" nrepl-port)
    (start-nrepl)

    (println "Starting webserver on port" http-port)
    (start-webserver)

    (catch Throwable t
      (.printStackTrace t)
      (System/exit 1))))
