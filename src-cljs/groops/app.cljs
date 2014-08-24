(ns groops.app
  (:require [ajax.core :as ajax]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-hash.md5 :refer [md5]]
            [clojure.string :refer [trim lower-case replace]])
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
                          (println "GET ROOMS" response)
                          (println "--" (:rooms-list response))
                          (swap! app-state assoc :rooms-list (:rooms-list response))
                          (println "APP STATE IS " app-state))}))

(defn post-room [room-name]
  (ajax/POST "/api/room"
             {:params {:room-name room-name}
              :format (ajax/json-format {:keywords? true})
              :handler (fn [resp]
                         (println "POST-ROOMS resp" resp)
                         (get-rooms))
              :error-handler (fn [response]
                               (println "post-room ERROR!" response))}))




;; ----------------------------------------
(defn login-user []
  (let [name (.-value (.getElementById js/document "name"))
        email (.-value (.getElementById js/document "email"))
        twitter (.-value (.getElementById js/document "twitter"))]
    (post-user name email twitter)))

(defn create-room []
  (let [room-name (.-value (.getElementById js/document "room-name"))]
    (post-room room-name)))

#_(defn join-room [room-name]
    (swap! app-state :selected-room room-name))

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

(defsnippet room-item-snippet "public/join.html" [:.row-item]
  [room-name]
  {[:span.roomname] (content room-name)
   ;; [:td.user-count] (content user-count)
   [:td.join-btn]  (listen onClick #(do (.preventDefault %)
                                        (join-room room-name)))})

;;; We can manipulate these templates once we begin storing data
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
   [:tbody.room-table] (substitute (map room-item-snippet (:rooms-list data)))})

(deftemplate room "public/room.html" [data] {})

(defn join-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (get-rooms))

    om/IRender
    (render [_]
      (println "ROOMS data is" data)
      (om/build (init join) data))))

(defn page-view [data owner]
  (reify
    om/IRender
    (render [_]
      (if (:user data)
        (om/build join-view data)
        (om/build (init intro) data) ))))


(om/root page-view app-state {:target (.getElementById js/document "om")})
