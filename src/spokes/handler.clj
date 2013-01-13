(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.reload :as reload]
            [spokes.views :refer [home]]))

(def team
  [{:name "Natasha Balwit"}
   {:name "Turner Bohlen"}
   {:name "Phillip Daniel"} 
   {:name "Bruno Faviero"}
   {:name "Sophie Geoghan"}
   {:name "Nathan Kit Kennedy"}
   {:name "Claire O'Connell"}
   {:name "Jeff Prouty"}
   {:name "Ethan Sherbondy"}
   {:name "Manny Singh"}
   {:name "Cathie Yun"}])

(sort-by :name team)

(defroutes app-routes
  (GET "/" [] (home team))
  (route/resources "/")
  (route/not-found "Not Found"))

;; get rid of wrap-reload in production
(def app
  (-> (handler/site app-routes)
      (reload/wrap-reload)))

;; For interactive development, evaluate these:
;; (use 'ring.adapter.jetty)
;; (defonce server (run-jetty #'app {:port 8080 :join? false}))

;; To stop the server, just do:
;; (.stop server)
;; (.start server)

;; NOTE: ;; #'app is just sugar for (var app)

