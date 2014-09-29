(ns groops.screens.intro
  (:require [ajax.core :as ajax]
            [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [component]]))



(defn post-user [params handler]
                (ajax/POST "/api/user"
                           {:params params
                            :format (ajax/json-format {:keywords? true})
                            :handler handler
                            :error-handler (fn [response]
                                             (println "ERROR!" response))}))
(defn intro-view [data owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (letfn [(simple-field [fkey]
                (do-> (set-attr "value" (fkey state))
                      (listen :on-change #(om/set-state! owner fkey
                                                         (.. % -target -value)))))

              (login-user [e data]
                (.preventDefault e)
                (let [{:keys [name email twitter]} state]
                  (if (and (not (empty? name))
                           (not (empty? email)))
                    (post-user {:name name :email email :twitter twitter}
                               (fn [user]
                                 (om/update! data :user user)))
                    (js/alert "Please complete the name and email fields"))))]


        (component
         "templates/intro.html"
         {[:#name]    (simple-field :name)
          [:#email]   (simple-field :email)
          [:#twitter]  (simple-field :twitter)
          [:#submit-btn] (listen :onClick #(login-user % data))})))))











