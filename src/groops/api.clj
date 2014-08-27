(ns groops.api
  (:require [clojure.data.json :as json]
            [compojure.core :refer :all]
            [groops.data :as data]
            [liberator.core :refer [resource]]))

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
                         (println "GET /api/rooms")
                         {:room-count-map (data/get-room-count-map)})))

(def get-messages
  (resource :allowed-methods [:get]
            :available-media-types ["application/json"]
            :handle-ok (fn [room]
                         (println "GET /api/room/messages/" room)
                         {:msg-vect (data/get-messages room)})))

(def post-message
  (resource :allowed-methods [:post]
            :available-media-types ["application/json"]
            :handle-created :created-message
            :post! (fn [ctx]
                     (let [{:keys [room user message]} (get-in ctx [:request :params])]
                       (println "POST /api/room/message room:" room "user:" user)
                       (data/push-message room user message)
                       {:created-message {:room room :user user :message message}}))))

(defroutes api-routes
  (context "/api" []
           (POST "/user" [] post-user)
           (POST "/room" [] post-room)
           (GET "/rooms" [] get-rooms)
           (GET "room/messages/:room")
           (POST "/room/message" [] post-message)))










