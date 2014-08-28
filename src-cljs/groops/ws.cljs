(ns groops.ws)

(def ws-url (str "ws://" (.-host js/location) "/ws"))
(def socket-atom (atom nil))

(defn start-ws! []
  (println "WebSocket destination is" ws-url)

  ;; ugly js mutability
  (let [socket (js/WebSocket. ws-url)]
    (set! (.-onmessage socket)
          (fn [event]
            (let [json-data (.parse js/JSON (.-data event))
                  data      (js->clj json-data :keywordize-keys true)]
              (println "MESSAGE" data))))
    (set! (.-onerror socket)
          (fn [event]
            (println "ERROR" event)))
    (set! (.-onopen socket)
          (fn [event]
            (println "OPEN" event)))
    (set! (.-onclose socket)
          (fn [event]
            (println "CLOSE" event)))
    (reset! socket-atom socket)))

(defn serialize [obj]
  (js/JSON.stringify (clj->js obj)))

(defn send-message [topic data]
  (when-let [socket @socket-atom]
    (let [message {:topic topic :data data}]
      (println "X" message)
      (.send socket (serialize message)))))
