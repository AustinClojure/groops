(ns groops.web
  (:require [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [groops.async :as async]
            [clojure.java.io :as io]))

(defroutes app-routes
  (GET "/ws" [] async/ws)
  (GET "/" [] (io/resource "public/home.html"))
  (GET "/js/react.js" [] (io/resource "react/react.min.js"))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (site app-routes))
