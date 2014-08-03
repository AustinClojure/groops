(ns groops.async
  (:require [org.httpkit.server :refer [with-channel on-close send!]])
  (:require [cheshire.core :refer [generate-string]]))

(def clients (atom {}))

(defn ws [req]
  (with-channel req con
    (swap! clients assoc con true)
    (println con " connected")
    (on-close con (fn [status]
                    (swap! clients dissoc con)
                    (println con " disconnected. status: " status)))))


(defn send-level []
  (let [level          (int (rand 100))
        message        (generate-string {:level level})
        active-clients (keys @clients)]
    (when (seq active-clients)
      (println "sending level " level)
      (doseq [client active-clients]
        (send! client message false)))))

(defn send-loop []
  (future (loop []
            (send-level)
            (Thread/sleep 5000)
            (recur))))

