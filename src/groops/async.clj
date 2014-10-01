(ns groops.async
  (:require [org.httpkit.server :as ws]
            [cheshire.core :as json]))

(defonce room-users (atom {}))
(defonce clients (atom {}))
(defonce chat-clients (atom {}))

;; ----------------------------------------

(defn send-message [message-map room]
  (doseq [[user channel] (get @room-users room)]
    (let [message (json/generate-string message-map)]
      (println "SENDING" message "to" user)
      (ws/send! channel message false))))

;; ----------------------------------------
(defmulti dispatch-message (fn [channel topic data] topic))

(defmethod dispatch-message :default [channel topic data]
  (println "UNKNOWN TOPIC" topic channel))

(defmethod dispatch-message "join" [channel topic data]
  (println "JOIN" data)
  (swap! room-users (fn [clients]
                   (assoc-in clients [(:room data) (:user data)] channel))))


;; ----------------------------------------

(defn send-to-room [room message]
  (doseq [[user channel] (get @room-users room)]
    (ws/send! channel (json/generate-string {:topic "joined" :data message}))))

;; ----------------------------------------
(defn ws-chat [req]
  (ws/with-channel req channel
    (println channel "A client connected!")

    (ws/on-receive channel
                (fn [data]
                  (let [received (json/parse-string data true)]
                    (dispatch-message channel (:topic received) (:data received)))))

    (ws/on-close channel
              (fn [status]
                (println channel "disconnected. status: " status)))))



