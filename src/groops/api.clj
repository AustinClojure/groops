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

(defroutes api-routes
  (context "/api" []
           (POST "/user" [] post-user)))










