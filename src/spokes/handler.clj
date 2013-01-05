(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [spokes.views :refer [home show-cljs]]))

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
  (GET "/cljs/:file" [file] (show-cljs file))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
