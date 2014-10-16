(ns groops.screens.join
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [>!]]
            [clojure.string :as string]
            [groops.gravatar :as gravatar]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [kioo.om :refer [defsnippet component]]))


(defn twitter-url [username]
  (when (not-empty username)
    (str "https://twitter.com/"
         (-> username
             (string/trim)
             (string/replace "@" "")))))


(defsnippet room-item-snippet "templates/join.html" [:.row-item]
  [data owner room-vect]
  {[:td.room-name]
   (content (js/decodeURIComponent (name (first room-vect))))

   [:td.user-count]
   (content (second room-vect))

   [:button.join-btn]
   (listen :onClick
           #(let [room-name (name (first room-vect))
                  user (:user @data)]
              (println "JOINZ")
              (when-let [ch (om/get-shared owner :chan)]
                (println "!!! ch" ch)
                (go (>! ch {:topic "join"
                            :data {:user (:name user)
                                   :room room-name}}))

                (om/update! data :selected-room room-name))))})


(defprotocol IRefreshState
  (refresh-state [_]))

(defn join-view [data owner]
  (reify
    IRefreshState
    (refresh-state [_]
      (ajax/GET "/api/rooms"
                {:format (ajax/json-format {:keywords? true})
                 :error-handler (fn [response]
                                  (println "get-rooms ERROR!" response))
                 :handler (fn [response]
                            (om/update! data :room-list (:room-count-map response)))}))

    om/IWillMount
    (will-mount [this]
      (refresh-state this))

    om/IRenderState
    (render-state [this state]
      (let [user
            (:user data)

            post-room
            (fn [room-name]
              (ajax/POST "/api/room"
                         {:params {:room-name room-name}
                          :format (ajax/json-format {:keywords? true})
                          :handler  (fn [resp]
                                      (refresh-state this))
                          :error-handler (fn [response]
                                           (println "post-room ERROR!" response))}))

            create-room
            (fn [e]
              (.preventDefault e)
              (let [room-name (js/encodeURIComponent (:room-name state))] 
                (when (not-empty room-name)
                  (println "room-name: " room-name)
                  (om/set-state! owner :room-name "")
                  (post-room room-name))))]

        (component
         "templates/join.html"
         {[:span.username]
          (content (:name user))

          [:span.email]
          (content (:email user))

          [:a#twitter]
          (do-> (content (:twitter user))
                (set-attr :href (twitter-url (:twitter data))))

          [:img#gravatar]
          (set-attr :src (gravatar/gravatar-url (:email user)))

          [:#room-name]
          (do-> (set-attr "value" (:room-name state))
                (listen :on-change #(om/set-state! owner :room-name
                                                   (.. % -target -value))))
          [:#create-room-btn]
          (listen :onClick create-room)

          [:tbody.room-table]
          (content (map #(room-item-snippet data owner %) (:room-list data)))})))))













