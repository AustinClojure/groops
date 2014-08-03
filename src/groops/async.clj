(ns groops.async
  (:require [org.httpkit.server :refer [with-channel on-close send!]])
  (:require [cheshire.core :refer [generate-string]]))

(def clients (atom {}))

(defn ws [req]
  (with-channel req channel
    (swap! clients assoc channel true)
    (println channel "connected")
    (on-close channel
              (fn [status]
                (swap! clients dissoc channel)
                (println channel "disconnected. status: " status)))))


(defn send-level []
  (let [level          (int (rand 100))
        message        (generate-string {:level level})
        active-clients (keys @clients)]
    (when (seq active-clients)
      (println "sending level" level "to" (count active-clients) "clients")
      (doseq [client active-clients]
        (send! client message false)))))


;; TODO this should be core.async
(defn send-loop []
  (future (loop []
            (send-level)
            (Thread/sleep 5000)
            (recur))))

