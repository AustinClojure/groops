(ns groops.web
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [groops.async :as async]
            [groops.data :refer [register-user 
                                 create-room 
                                 add-user-to-room 
                                 remove-user-from-room
                                 push-message
                                 get-users-in-room
                                 registry-set
                                 room-set]]
            [ring.util.response :refer [content-type resource-response redirect-after-post]]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.nested-params :refer (wrap-nested-params)]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [ring.middleware.session :refer (wrap-session)]
            [ring.middleware.session.store :refer (read-session)]
            [ring.middleware.reload :refer (wrap-reload)]))

;;; Logging/Debugging
(defn log-request [req]
  (println ">>>>" req)) 

(defn wrap-verbose [h]
  (fn [req]
    (log-request req)
    (h req)))

(defn join-page [req]
  (-> (resource-response )))

(defn register [name email twitter]
  (-> (redirect-after-post "/join")
      (assoc :session (register-user name email twitter))))

(defroutes app-routes
  (GET "/ws" [] async/ws)
  (GET "/test" [] (-> (resource-response "public/home.html")
                      (content-type "text/html")))
  (GET "/" [] (-> (resource-response "public/home.html")
                  (content-type "text/html")))
  (GET "/intro" [] (-> (resource-response "public/home.html")
                       (content-type "text/html")))
  (GET "/join" [:as req] (-> (resource-response "public/home.html")
                             (content-type "text/html")))
  (GET "/room" [] (-> (resource-response "public/home.html")
                      (content-type "text/html")))
  (POST "/register" [name email twitter] (register name email twitter))  
  (route/resources "/")
  (route/resources "/"   {:root "generated"})
  (route/resources "/js" {:root "react"})
  (route/not-found "Not Found"))

(def app
  (-> (site #'app-routes)
      ;;(wrap-verbose)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-params)
      (wrap-session)
      (wrap-reload)))
