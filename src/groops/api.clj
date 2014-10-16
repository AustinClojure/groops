(ns groops.api
  (:require [clojure.data.json :as json]
            [compojure.core :refer :all]
            [groops.data :as data]
            [liberator.core :refer [resource defresource]]
            [ring.util.codec :refer [url-encode]]))

(def post-user
  (resource :allowed-methods [:post]
            :available-media-types ["application/json"]
            :handle-created :created-user
            :post! (fn [ctx]
                     (let [{:keys [name email twitter]} (get-in ctx [:request :params])]
                       (println "POST /api/user" name email twitter)
                       (data/register-user name email twitter)
                       {:created-user {:name name :email email :twitter twitter}}))))

(def post-room
  (resource :allowed-methods [:post]
            :available-media-types ["application/json"]
            :handle-created :created-room
            :post! (fn [ctx]
                     (let [{:keys [room-name]} (get-in ctx [:request :params])]
                       (println "POST /api/room" room-name)
                       (data/create-room room-name)
                       {:created-room {:room-name room-name}}))))

(def get-rooms
  (resource :allowed-methods [:get]
            :available-media-types ["application/json"]
            :handle-ok (fn [_]
                         (let [room-names (data/get-rooms-list)
                               encoded-room-names (map url-encode room-names)
                               counts (map data/get-room-user-count room-names)]
                             (println "GET /api/rooms")
                             (reduce conj (sorted-map) 
                                     (zipmap encoded-room-names counts))))))

(defresource get-messages [room]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [_]
               (println "get-messages response: " {:msg-vect (data/get-messages room)})
               {:msg-vect (data/get-messages room)}))

(def post-message
  (resource :allowed-methods [:post]
            :available-media-types ["application/json"]
            :handle-created :created-message
            :post! (fn [ctx]
                     (let [{:keys [room user message gravatar-uri]}
                           (get-in ctx [:request :params])]
                       (println "POST /api/room/message room:" room "user:" user "gravatar-uri" gravatar-uri)
                       (data/push-message room user message gravatar-uri)
                       {:created-message {:room room 
                                          :user user
                                          :message message 
                                          :gravatar-uri gravatar-uri}}))))

(defroutes api-routes
  (context "/api" []
           (POST "/user" [] post-user)
           (POST "/room" [] post-room)
           (GET "/rooms" [] get-rooms)
           (GET "/room/messages/:room" [room] (get-messages room))
           (POST "/room/message" [] post-message)))
