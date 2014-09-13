(ns groops.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [groops.brepl :refer (brepl brepl-injection)]
            [groops.api :as api]
            [groops.async :as async]
            [groops.middleware :refer [basic-site]]
            [ring.util.response :refer [content-type resource-response]]))

#_(defn register [name email twitter]
  (-> (redirect-after-post "/join")
      (assoc :session (register-user name email twitter))))

(html/deftemplate landing-page "public/home.html"
  [req]
  [:body] (brepl-injection))

(defroutes app-routes
  (GET "/ws" [] async/ws)
  (GET "/chat-ws" [] async/chat-ws)
  api/api-routes
  (GET "/" [req] (landing-page req))
  (GET "/" [] (-> (resource-response "public/home.html")
                  (content-type "text/html")))
  (route/resources "/")
  (route/resources "/"   {:root "generated"})
  (route/resources "/js" {:root "react"})
  (route/not-found "Not Found"))

(def app (basic-site #'app-routes))
