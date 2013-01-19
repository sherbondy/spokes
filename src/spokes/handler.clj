(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as ring]
            [spokes.views :refer [home route]]))

(def team
  [{:name "Natasha Balwit"}
   {:name "Turner Bohlen"}
   {:name "Phillip Daniel"} 
   {:name "Bruno Faviero"}
   {:name "Nathan Kit Kennedy"}
   {:name "Claire O'Connell"}
   {:name "Jeff Prouty"}
   {:name "Ethan Sherbondy"}
   {:name "Manny Singh"}
   {:name "Cathie Yun"}])

(defroutes app-routes
  (GET "/" [] (home team))
  (GET "/route" [] (route))
  (route/resources "/")
  (route/not-found "Not Found"))

;; get rid of wrap-reload in production
(def app
  (-> (handler/site app-routes)))


(defn start [port]
  (ring/run-jetty (var app)
                  {:port (or port 8000) :join? false}))

(defn -main
  ([] (-main 8000))
  ([port]
     (let [port (or (env :port) port)]
       (start (cond 
               (string? port) (Integer/parseInt port)
               :else port)))))

;; For interactive development, evaluate these:
;; (require '[ring.middleware.reload :as reload])
;; (def app (-> app (reload/wrap-reload)))
;; (defonce server (start 8000))

;; To stop the server, just do:
;; (.stop server)
;; (.start server)
