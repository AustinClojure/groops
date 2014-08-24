(ns groops.app
  (:require [kioo.om :refer [content set-style set-attr do-> substitute listen]]
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
(defn init [template]
  (fn [data]
    (om/component (template data))))

(def app-state (atom {}))

(defn login-user [] 
  (let [name (.-value (.getElementById js/document "name"))
        email (.-value (.getElementById js/document "email"))
        twitter (.-value (.getElementById js/document "twitter"))]
    (swap! app-state assoc :user name :email email :twitter twitter)))  

(defn logout-user [user]
  (swap! app-state dissoc :user :email :twitter))

(defn get-gravatar [email]
  (if email (str "http://www.gravatar.com/avatar/"  
                 (-> email
                     (trim)
                     (lower-case)
                     (md5) ))
      ("http://http://www.gravatar.com/avatar/00000000000000000000000000000000")))

;;; We can manipulate these templates once we begin storing data
(deftemplate intro "public/intro.html" [data] 
  {[:#submit-btn] (listen :onClick #(do (.preventDefault %) (login-user)))})
(deftemplate join "public/join.html" [data]
  {[:span.username] (content (:user data))   
   [:span.email] (content (:email data))   
   [:a#twitter] (do-> (content (:twitter data))
                         (set-attr :href (str "https://twitter.com/" 
                                              (replace (trim (:twitter data)) "@" ""))))
   [:img.grp-gravatar] (set-attr :src (get-gravatar (:email data)))})

(deftemplate room "public/room.html" [data] {})

(defn page-view [data owner]
  (reify
    om/IRender
    (render [_]
      (if (:user data)
        (om/build (init join)  data)
        (do (println "@app-state: " @app-state) (om/build (init intro) data) )))))


(om/root page-view app-state {:target (.getElementById js/document "om")})



