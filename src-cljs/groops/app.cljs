(ns groops.app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def view-atom (atom nil))

(def socket (js/WebSocket. "ws://localhost:8080/ws"))

(set! (.-onmessage socket)
      (fn [event]
        (let [json-data (.parse js/JSON (.-data event))
              data      (js->clj json-data :keywordize-keys true)]
          (reset! view-atom data))))


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

(when-let [target (.getElementById js/document "om")]
  (om/root happy-view
           view-atom
           {:target target}))

