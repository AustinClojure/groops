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
               (if data
                 (str "Happiness: " (:happiness data))
                 "Determining happiness")))))

(when-let [target (.getElementById js/document "om")]
  (om/root happy-view
           view-atom
           {:target target}))

