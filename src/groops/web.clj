(ns groops.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [groops.brepl :refer [brepl brepl-injection]]
            [groops.api :as api]
            [groops.async :as async]
            [groops.middleware :refer [basic-site]]
            [net.cgrand.enlive-html :as html]))

(html/deftemplate landing-page "templates/home.html"
  [req]
  [:body] (brepl-injection))

(defroutes app-routes
  (GET "/" [] landing-page)
  (GET "/ws/chat" [] async/chat-ws)
  api/api-routes
  (route/resources "/")
  (route/resources "/js" {:root "react"})
  (route/not-found "Not Found"))

(def app (basic-site #'app-routes))
