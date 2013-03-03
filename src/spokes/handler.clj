(ns spokes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [org.httpkit.server :as http]
            [spokes.views :refer [home route]]))

(defroutes app-routes
  (GET "/" [] (home))
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
    (defonce server (start 8000))))
  
;; server returns a function that, when evaluated, stops the server:
;; (server)
