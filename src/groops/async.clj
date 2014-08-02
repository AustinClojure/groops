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


(defn send-happiness []
  (println "clients: " (count @clients))
  (doseq [client @clients]
    (send! (key client) (generate-string
                         {:happiness (rand 10)})
           false)))

(defn send-loop []
  (future (loop []
            (println "....")

            (Thread/sleep 5000)
            (recur))))

