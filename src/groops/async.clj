(ns groops.async
  (:require [org.httpkit.server :refer [with-channel on-close on-receive send!]]
            [cheshire.core :refer [generate-string]]))



(def clients (atom {}))
(def chat-clients (atom {}))

;; chat-clients
;; { {:channel channel0 :username user0 :email email0 :room room0}
;;   {:channel channel1 :username user1 :email email1 :room room1} }


(defn chat-ws [req]
  (with-channel req channel
    (swap! chat-clients assoc channel {:name nil :email nil :room nil})
    (println channel "connected")
    (on-close channel
              (fn [status]
                (swap! chat-clients dissoc channel)
                (println channel "disconnected. status: " status)))
    (on-receive channel (fn [data]
                         (println "on-receive channel:" channel " data:" data)
                         (swap! chat-clients assoc-in [channel] (read-string data))
                         (println "chat-ws chat-clients" @chat-clients)))))

(defn send-message [message-map room]
  (let [client-filter-fn (fn [room] (fn [client] (if (= room (:room (val client))) true false)))
        clients-in-room (fn [room clients] (filter (client-filter-fn room) clients))
        channels-to-room (keys (clients-in-room room @chat-clients))
        message-string (generate-string message-map)]
    (when (seq channels-to-room)
      (println "sending message: " message-map "to" (count channels-to-room) "channels")
      (doseq [channel channels-to-room]
        (send! channel message-string false)))))

