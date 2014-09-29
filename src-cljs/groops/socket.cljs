(ns groops.socket
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

(def ws-url (str "ws://" (.-host js/location) "/ws/chat"))

(defn make-socket [handler]
  (let [socket (js/WebSocket. ws-url)]
    (set! (.-onopen socket)
          (fn [event]
            (println "WebSocket connected. Destination: " ws-url)))
    (set! (.-onmessage socket) handler)
    socket))




