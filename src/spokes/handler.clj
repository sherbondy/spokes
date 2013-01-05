(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.reload :as reload]
            [spokes.views :refer [home]]))

(def team
  [{:name "Daesun Yim"},
   {:name "Jeff Prouty"},
   {:name "Alisha Lussiez"},
   {:name "Bruno Faviero"},
   {:name "Phillip Daniel"},
   {:name "Cathie Yun"},
   {:name "Ethan Sherbondy"},
   {:name "Turner Bohlen"},
   {:name "Nathan Kit Kennedy"},
   {:name "Claire O'Connell"},
   {:name "Chase Lambert"},
   {:name "Natasha Balwit"},
   {:name "Sophie Geoghan"},
   {:name "Manny Singh"}])

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

