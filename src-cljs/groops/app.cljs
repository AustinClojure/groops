(ns groops.app
  (:require [ajax.core :as ajax]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-hash.md5 :refer [md5]]
            [clojure.string :refer [trim lower-case replace replace-first]])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

(comment
  (def view-atom (atom nil))

  (println "The current path is:" (.-pathname (.-location js/window)) )

  (def ws-url (str "ws://" (.-host js/location) "/ws"))
  (println "WebSocket destination is" ws-url)
  (def socket (js/WebSocket. ws-url))

  (set! (.-onmessage socket)
        (fn [event]
          (let [json-data (.parse js/JSON (.-data event))
                data      (js->clj json-data :keywordize-keys true)]
            ;;(println "socket.onmessage->json-data:" json-data)
            ;;(println "socket.onmessage->data:" data)
            (reset! view-atom data)))))


;; ----------------------------------------
(def app-state (atom {}))

;; ----------------------------------------
(defn post-user [name email twitter]
  (ajax/POST "/api/user"
             {:params {:name name
                       :email email
                       :twitter twitter}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [user]
                         (swap! app-state assoc :user user))
              :error-handler (fn [response]
                               (println "ERROR!" response))}))

(defn get-rooms []
  (ajax/GET "/api/rooms"
            {:format (ajax/json-format {:keywords? true})
             :error-handler (fn [response]
                              (println "get-rooms ERROR!" response))
             :handler (fn [response]
                        ;;(println "GET ROOMS" response)
                        ;;(println "--" (:rooms-list response))
                        (swap! app-state assoc 
                               :room-count-map (:room-count-map response))
                        #_(println "APP STATE IS " app-state))}))

(defn get-messages []
  (ajax/GET "api/room/messages/Alpha" 
            {:format (ajax/json-format {:keywords? true})
             :error-handler (fn [response]
                              (println "get-message ERROR!" response))
             :handler (fn [response]
                       ;; (println ":get-messages :selected-room" (:selected-room @app-state))
                       ;; (println "GET MESSAGES:" response)
                       ;; (println "--" (:msg-vect response))
                        (swap! app-state assoc :msg-vect (:msg-vect response)))}))

(defn post-room [room-name]
  (ajax/POST "/api/room"
             {:params {:room-name room-name}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [resp]
                         (println "POST-ROOMS resp" resp)
                         (get-rooms))
              :error-handler (fn [response]
                               (println "post-room ERROR!" response))}))

(defn post-message [message]
  (ajax/POST (str "/api/room/message")
             {:params {:room (:selected-room @app-state)
                       :user (get-in @app-state [:user :name])
                       :message message}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [resp]
                         (println "POST-MESSAGE resp" resp)
                         (get-messages))
              :error-handler (fn [response]
                               (println "POST-MESSAGE ERROR!:" response))}))

;; ----------------------------------------
(defn login-user []
  (let [name (.-value (.getElementById js/document "name"))
        email (.-value (.getElementById js/document "email"))
        twitter (.-value (.getElementById js/document "twitter"))]
    (if (and (not (= name "")) (not (= email "")))
      (post-user name email twitter)
      (js/alert "Please complete the name and email fields"))))

(defn create-room []
  (let [room-name (.-value (.getElementById js/document "room-name"))]
    (post-room room-name)))

(defn join-room [room-name]
  (do
    (println "Joining Room: " room-name))
    (swap! app-state assoc :selected-room room-name))

(defn exit-room []
  (do
    (println "Exiting Room!")
    (swap! app-state dissoc :selected-room)))

(defn send-message []
  (let [message (.-value (.getElementById js/document "message"))]
    (post-message message)))

(defn logout-user [user]
  (swap! app-state dissoc :user :email :twitter))

(defn get-gravatar [email]
  (if email (str "http://www.gravatar.com/avatar/"
                 (-> email
                     (trim)
                     (lower-case)
                     (md5) ))
      ("http://http://www.gravatar.com/avatar/00000000000000000000000000000000")))

;; ----------------------------------------
(defn init [template]
  (fn [data]
    (om/component (template data))))

(defn default-action [action]
  (fn [e]
    (.preventDefault e)
    (action)))

(defn keyword-to-string [keyword]
  (replace-first (str keyword) ":" ""))

;; ----------------------------------------
(defsnippet room-item-snippet "public/join.html" [:.row-item]
  [room-vect]
  {[:td.room-name] (content (keyword-to-string (first room-vect)))
   [:td.user-count] (content (second room-vect))
   [:a.join-btn]  (listen :onClick #(do (.preventDefault %)
                                        (join-room (keyword-to-string (first room-vect)))))})

(defsnippet chat-message-snippet "public/room.html" [:tr.chat-message]
  [msg-vect]
  {[:span.author] (content (:author (second msg-vect)))
   [:span.message] (content (:message (second msg-vect)))})
;; ----------------------------------------
(deftemplate intro "public/intro.html" [data]
  {[:#submit-btn] (listen :onClick (default-action login-user))})

(deftemplate join "public/join.html" [data]
  {[:span.username] (content (get-in data [:user :name]))
   [:span.email] (content (get-in data [:user :email]))
   [:a#twitter] (do-> (content (get-in data [:user :twitter]))
                      (set-attr :href (str "https://twitter.com/"
                                           (replace (trim
                                                     (get-in data
                                                             [:user :twitter])) "@" ""))))
   [:img#gravatar] (set-attr :src (get-gravatar (get-in data [:user :email])))
   [:#create-room-btn] (listen :onClick (default-action create-room))
   [:tbody.room-table] (substitute (map room-item-snippet (:room-count-map data)))})

(deftemplate room "public/room.html" [data] 
  {[:a.back-btn] (listen :onClick (default-action exit-room))
   [:span#room-name] (content (:selected-room data))
   [:tr.chat-message] (substitute (map chat-message-snippet (:msg-vect data)))
   [:button#send] (listen :onClick (default-action send-message))})

(defn join-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (get-rooms))
    om/IRender
    (render [_]
      (println "ROOMS data is" data)
      (om/build (init join) data))))

(defn room-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (get-messages))
    om/IRender
    (render [_]
      (println "Messages are " data)
      (om/build (init room) data))))

(defn page-view [data owner]
  (reify
    om/IRender
    (render [_]
      (if (:selected-room data)
        (om/build room-view data)
        (if (:user data)
          (om/build join-view data)
          (om/build (init intro) data) )))))


(om/root page-view app-state {:target (.getElementById js/document "om")})
