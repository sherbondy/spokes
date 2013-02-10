(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [org.httpkit.server :as http]
            [spokes.views :refer [home route]]))

(def team
  [{:name "Bruno Faviero"
    :bio ""
    :school "MIT"
    :grad-year 2015}
   {:name "Claire O'Connell"
    :bio ""
    :school "MIT"
    :grad-year 2013}
   {:name "Ethan Sherbondy"
    :bio ""
    :school "MIT"
    :grad-year 2014}
   {:name "Jeff Prouty"
    :bio ""
    :school "MIT"
    :grad-year 2014}
   {:name "Manny Singh"
    :bio ""
    :school "MIT"
    :grad-year 2016}
   {:name "Natasha Balwit"
    :bio ""
    :school "MIT"
    :grad-year 2016}
   {:name "Nathan Kit Kennedy"
    :bio ""
    :school "UC Berkeley"
    :grad-year 2014}
   {:name "Phillip Daniel"
    :bio ""
    :school "MIT"
    :grad-year 2013}
   {:name "Turner Bohlen"
    :bio ""
    :school "MIT"
    :grad-year 2014}])

(defroutes app-routes
  (GET "/" [] (home team))
  (GET "/route" [] (route))
  (route/resources "/")
  (route/not-found "Not Found"))

;; get rid of wrap-reload in production
(def app
  (-> (handler/site app-routes)))


(defn start [port]
  (http/run-server app {:port (or port 8000)}))

(defn -main
  ([] (-main 8000))
  ([port]
     (let [port (or (env :port) port)]
       (start (cond 
               (string? port) (Integer/parseInt port)
               :else port)))))

;; For interactive development, evaluate these:
(comment
  (do
    (require '[ring.middleware.reload :as reload])
    (def app (-> app (reload/wrap-reload)))
    (defonce server (start 8000)))
)
;; server returns a function that, when evaluated, stops the server:
;; (server)
