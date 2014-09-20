(ns groops.app
  (:require  [clojure.browser.repl]
             [ajax.core :as ajax]
             [kioo.om :refer [content set-style set-attr do-> substitute listen]]
             [kioo.core :refer [handle-wrapper]]
             [om.core :as om :include-macros true]
             [om.dom :as dom :include-macros true]
             [cljs-hash.md5 :refer [md5]]
             [clojure.string :refer [trim lower-case replace replace-first]]
             [cljs.reader :refer [read-string]]
             [figwheel.client :as fw :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

(fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (print "reloaded")))


;; ----------------------------------------
(def app-state (atom {}))

;; ----------------------------------------

(def ws-url (str "ws://" (.-host js/location) "/ws/chat"))
(def socket (js/WebSocket. ws-url))

(defn update-socket [data]
  (let [name (get-in data [:user :name])
        email (get-in data [:user :email])
        room (:selected-room data)]
    (.send socket {:name name :email email :room room})))

(set! (.-onopen socket)
      (fn [event]
        (println "WebSocket connected. Destination: " ws-url)))

;; The keys are all ints, so sort them such that :10 > :2
(defn msg-comparator [key1 key2] (compare (read-string (name key1))
                                          (read-string (name key2))))

(set! (.-onmessage socket)
      (fn [event]
        (let [json-data (.parse js/JSON (.-data event))
              data (js->clj json-data :keywordize-keys true)
              sorted-message-map (into (sorted-map-by msg-comparator)
                                       (conj (:msg-vect @app-state) data))]
          ;;(println "socket.onmessage data:" data)
          (swap! app-state assoc :msg-vect sorted-message-map))))

;; ----------------------------------------
(defn post-user [name email twitter]
  (ajax/POST "/api/user"
             {:params {:name name
                       :email email
                       :twitter twitter}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [user]
                         (do
                           (swap! app-state assoc :user user)
                           (update-socket @app-state)))
              :error-handler (fn [response]
                               (println "ERROR!" response))}))

(defn get-rooms []
  (ajax/GET "/api/rooms"
            {:format (ajax/json-format {:keywords? true})
             :error-handler (fn [response]
                              (println "get-rooms ERROR!" response))
             :handler (fn [response]
                        (swap! app-state assoc
                               :room-count-map (:room-count-map response)))}))

(defn get-messages []
  (ajax/GET (str  "api/room/messages/" (:selected-room @app-state))
            {:format (ajax/json-format {:keywords? true})
             :error-handler (fn [response]
                              (println "get-message ERROR!" response))
             :handler (fn [response]
                         (let [msg-vector (:msg-vect response)
                               sorted-message-map (into (sorted-map-by msg-comparator) msg-vector)]
                          (swap! app-state assoc :msg-vect sorted-message-map)))}))

(defn post-room [room-name]
  (ajax/POST "/api/room"
             {:params {:room-name room-name}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [resp]
                         (get-rooms))
              :error-handler (fn [response]
                               (println "post-room ERROR!" response))}))

(defn post-message [message]
  (ajax/POST (str "/api/room/message")
             {:params {:room (:selected-room @app-state)
                       :user (get-in @app-state [:user :name])
                       :message message
                       :gravatar-url (:gravatar-url @app-state)}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [resp]
                         (println "POST-MESSAGE resp" resp))
              :error-handler (fn [response]
                               (println "POST-MESSAGE ERROR!:" response))}))

;; ----------------------------------------
(defn login-user []
  (let [name (.-value (.getElementById js/document "name"))
        email (.-value (.getElementById js/document "email"))
        twitter (.-value (.getElementById js/document "twitter"))]
    (if (and (not (= name "")) (not (= email "")))
      (do
        (post-user name email twitter))
      (js/alert "Please complete the name and email fields"))))

(defn create-room []
  (let [room-name (.-value (.getElementById js/document "room-name"))]
    (post-room room-name)))

(defn join-room [room-name]
  (do
    (println "Joining Room: " room-name)
    (swap! app-state assoc :selected-room room-name)
    (update-socket @app-state)))

(defn exit-room []
  (do
    (println "Exiting Room!")
    (swap! app-state dissoc :selected-room)
    (update-socket @app-state)))

(defn send-message []
  (let [message (.-value (.getElementById js/document "message"))]
    (post-message message)))

(defn logout-user [user]
  (do
    (swap! app-state dissoc :user :email :twitter)
    (update-socket @app-state)))

(defn gravatar [email]
  (if email
    (do
      (swap! app-state assoc :gravatar-url (str "http://www.gravatar.com/avatar/"
                                                (-> email
                                                    (trim)
                                                    (lower-case)
                                                    (md5))))
      (str "http://www.gravatar.com/avatar/"
           (-> email
               (trim)
               (lower-case)
               (md5))))
    (do
      (swap! app-state assoc :gravatar-url
             ("http://www.gravatar.com/avatar/00000000000000000000000000000000"))
      ("http://www.gravatar.com/avatar/00000000000000000000000000000000"))))

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

(defn set-focus-on-id [id]
  (.focus (.getElementById js/document id)))

;; ----------------------------------------
(defsnippet room-item-snippet "public/join.html" [:.row-item]
  [room-vect]
  {[:td.room-name] (content (keyword-to-string (first room-vect)))
   [:td.user-count] (content (second room-vect))
   [:a.join-btn]  (listen :onClick #(do (.preventDefault %)
                                        (join-room (keyword-to-string (first room-vect)))))})

(defsnippet chat-message-snippet "public/room.html" [:tr.chat-message]
  [msg-vect]
  {[:img] (set-attr :src (:gravatar-url (second msg-vect)))
   [:span.author] (content (:author (second msg-vect)))
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
   [:img#gravatar] (set-attr :src (gravatar (get-in data [:user :email])))
   [:#create-room-btn] (listen :onClick (default-action create-room))
   [:tbody.room-table] (substitute (map room-item-snippet (:room-count-map data)))})

(deftemplate room "public/room.html" [data]
  {[:a.back-btn] (listen :onClick (default-action exit-room))
   [:span#room-name] (content (:selected-room data))
   [:tr.chat-message] (substitute (map chat-message-snippet (:msg-vect data)))
   [:button#send] (listen :onClick (default-action send-message))
   [:input#message] (listen :onKeyDown #(when (= (.-key %) "Enter")
                                          (send-message)))})

(defn join-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (get-rooms))
    om/IRender
    (render [_]
      ;;(println "ROOMS data is" data)
      (om/build (init join) data))))

(defn room-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (get-messages))
    om/IRender
    (render [_]
      ;;(println "Messages are " data)
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
