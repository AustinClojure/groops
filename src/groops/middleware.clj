(ns groops.middleware
  (:require [compojure.handler :refer (site)]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [ring.middleware.nested-params :refer (wrap-nested-params)]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.session :refer (wrap-session)]))

;;; Logging/Debugging
(defn log-request [req]
  (println ">>>>" req))

(defn wrap-verbose [h]
  (fn [req]
    (log-request req)
    (h req)))

(defn basic-site [app-routes]
  (-> (site app-routes)
      ;; (wrap-verbose)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-params)
      (wrap-json-params)
      (wrap-session)))
