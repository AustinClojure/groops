(ns groops.screens.room
  (:require [ajax.core :as ajax]
            [cljs.reader :refer [read-string]]
            [groops.uri :as uri]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate component]]))

;; The keys are all ints, so sort them such that :10 > :2
(defn msg-comparator [key1 key2] (compare (read-string (name key1))
                                          (read-string (name key2))))

(defn order-messages [msg-vector]
  (into (sorted-map-by msg-comparator) msg-vector))

(defsnippet chat-message-snippet "templates/room.html" [:tr.chat-message]
  [[_ message]]
  {[:img]          (set-attr :src (:gravatar-uri message))
   [:span.author]  (content (:author message))
   [:span.message] (content (:message message))})

(defn room-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (ajax/GET (str "api/room/messages/" (:selected-room data))
                {:format (ajax/json-format {:keywords? true})
                 :error-handler (fn [response]
                                  (println "get-message ERROR!" response))
                 :handler (fn [response]
                            (let [messages (order-messages (:msg-vect response))]
                              (println "MESSAGES!" messages)
                              (om/update! data :messages messages)))}))

    om/IRenderState
    (render-state [_ state]
      (letfn [(post-message [room username gravatar message]
                (ajax/POST (str "/api/room/message")
                           {:params {:room room
                                     :user username
                                     :message (:message state)
                                     :gravatar-uri gravatar}
                            :format (ajax/json-format {:keywords? true})
                            :handler (fn [resp]
                                       (println "POST-MESSAGE resp" resp))
                            :error-handler (fn [response]
                                             (println "POST-MESSAGE ERROR!:" response))}))
              (send-message []
                (let [user (:user @data)
                      name (:name user)
                      gravatar-uri (uri/gravatar-uri (:email user))
                      room (:selected-room @data)
                      message (:message state)]
                  (when (not-empty message)
                    (om/set-state! owner :message "")
                    (post-message room name gravatar-uri message))))]

        (component
         "templates/room.html"
         {[:button.back-btn]
          (listen :onClick #(om/update! data :selected-room nil))

          [:span#room-name]
          (content (:selected-room data))

          [:tr.chat-message]
          (content (map chat-message-snippet (:messages data)))

          [:button#send]
          (listen :onClick send-message)

          [:input#message]
          (do->
           (set-attr "value" (:message state))
           (listen :on-change #(om/set-state! owner :message (.. % -target -value)))
           (listen :onKeyDown #(when (= (.-key %) "Enter")
                                 (send-message))))})))))













