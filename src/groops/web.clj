(ns groops.web
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [groops.async :as async]
            [ring.util.response :refer [content-type resource-response]]))


(defroutes app-routes
  (GET "/ws" [] async/ws)
  (GET "/" [] (-> (resource-response "public/home.html")
                  (content-type "text/html")))
  (GET "/js/react.js" [] (resource-response "react/react.min.js"))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (site app-routes))
