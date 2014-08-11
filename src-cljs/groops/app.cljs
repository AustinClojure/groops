(ns groops.app
  (:require [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!) 

(def view-atom (atom nil))

(def ws-url (str "ws://" (.-host js/location) "/ws"))
(println "WebSocket destination is" ws-url)
(def socket (js/WebSocket. ws-url))

(set! (.-onmessage socket)
      (fn [event]
        (let [json-data (.parse js/JSON (.-data event))
              data      (js->clj json-data :keywordize-keys true)]
          (reset! view-atom data))))

;;; We can manipulate these templates once we begin storing data
(deftemplate intro "public/intro.html" [data] {})

(deftemplate join "public/join.html" [data] {})

(deftemplate room "public/room.html" [data] {})

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

(when-let [pathname (.-pathname (.-location js/window))]
  (case pathname
    "/join" (om/root (init join) app-state {:target (.-body js/document)})
    "/room" (om/root (init room) app-state {:target (.-body js/document)})
    "/" (om/root (init intro) app-state {:target (.-body js/document)})
    "/test" (om/root happy-view view-atom {:target (.getElementById js/document "om")})))
 
