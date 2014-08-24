(ns groops.app
  (:require [ajax.core :as ajax]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
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

(defn login-user [user]
  (swap! app-state assoc :user {:name user}))

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

;;; We can manipulate these templates once we begin storing data
(deftemplate intro "public/intro.html" [data] {})
(deftemplate join "public/join.html" [data]
  {[:span.username]      (content (get-in data [:user :name]))
   [:span.email]         (content (get-in data [:user :email]))
   [:span.twitterhandle] (content (get-in data [:user :twitter]))})

(deftemplate room "public/room.html" [data] {})

(defn page-view [data owner]
  (reify
    om/IRender
    (render [_]
      (if (:user data)
        (om/build (init join)  data)
        (om/build (init intro) data)))))


(om/root page-view app-state {:target (.getElementById js/document "om")})
