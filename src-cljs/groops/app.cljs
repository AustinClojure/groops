(ns groops.app
  (:require [ajax.core :as ajax]
            [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [<! >! chan]]
            [figwheel.client :as fw :include-macros true]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [groops.screens.intro]
            [groops.screens.join]
            [groops.screens.room]
            [groops.socket :as socket])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]
                   [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

;; ----------------------------------------

(defonce app-state (atom {}))

;; The keys are all ints, so sort them such that :10 > :2
(defn msg-comparator [key1 key2] (compare (read-string (name key1)) ;; TODO - FIX BAD
                                          (read-string (name key2))))
(defn app-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (om/set-state! owner :socket (socket/make-socket
                                    (fn [event]
                                      (.log js.console "EVENT" event)
                                      (let [json-data (.parse js/JSON (.-data event))
                                            event-data (js->clj json-data :keywordize-keys true)]
                                        (om/transact! data :messages
                                                      (fn [current]
                                                        (into (sorted-map-by msg-comparator)
                                                              (conj current event-data))) )))))
      (when-let [ch (om/get-shared owner :chan)]
        (go (loop []
              (when-let [v (<! ch)]
                (.send (om/get-state owner :socket) v)
                (recur))))))

    om/IWillUnmount
    (will-unmount [_]
      (println "TODO should remove socket!" (om/get-state owner :socket)))

    om/IRender
    (render [_]
      (cond
       (:selected-room data)
       (om/build groops.screens.room/room-view data)

       (:user data)
       (om/build groops.screens.join/join-view data)

       :else
       (om/build groops.screens.intro/intro-view data)))))


;; ----------------------------------------

(defn ping []
  (swap! app-state update-in [:count] (fnil inc 0)))

#_(fw/watch-and-reload :websocket-url "ws://localhost:3449/figwheel-ws"
                     :jsload-callback #(do
                                         (println "reloading")
                                         (ping)))


(when-let [target (.getElementById js/document "om")]
  (om/root app-view app-state {:target target
                               :shared {:chan (chan)}}))

