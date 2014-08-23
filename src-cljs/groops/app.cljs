(ns groops.app
  (:require [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!) 

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
          (reset! view-atom data))))

(defn init [template] 
  (fn [data]
    (om/component (template data))))

(def app-state (atom {})) 

(defn happy-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
               (dom/div #js {:className "jumbotron"}
                        (dom/div #js {:className "container"}
                                 (dom/h1 nil "Websocket Test")))
               (dom/div #js {:className "container"}
                        (dom/div #js {:className "progress"}

                                 (if-let [level (:level data)]
                                   (dom/div #js {:className "progress-bar progress-bar-success"
                                                 :style #js {:width (str level "%")}}
                                            level)
                                   (dom/div #js {:className "progress-bar progress-bar-warning"
                                                 :style #js {:width "100%"}}
                                            "Loading..."))))))))

;;; We can manipulate these templates once we begin storing data
(deftemplate intro "public/intro.html" [data] {})

(deftemplate join "public/join.html" [data] {})

(deftemplate room "public/room.html" [data] {})

(when-let [pathname (.-pathname (.-location js/window))]
  (case pathname
    "/join" (om/root (init join) app-state {:target (.getElementById js/document "om")})
    "/room" (om/root (init room) app-state {:target (.getElementById js/document "om")})
    "/intro" (om/root (init intro) app-state {:target (.getElementById js/document "om")})
    "/" (om/root (init intro) app-state {:target (.getElementById js/document "om")})
    "/test" (om/root happy-view view-atom {:target (.getElementById js/document "om")})))

(comment (when-let  [pathname (.-pathname (.-location js/window))]
           (if (= pathname "/")
             (om/root (init join) app-state {:target (.getElementById js/document "om")}))))
