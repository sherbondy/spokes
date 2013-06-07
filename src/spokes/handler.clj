(ns spokes.handler
  (:use watchtower.core
        compojure.core)
  (:require [compojure.route :as route]
            [org.httpkit.server :as http]
            [ring.middleware.reload :as reload]
            [spokes.views :refer [channel error home mentor route]]))

(def all-routes
  [{:url "/index.html"
    :html (home)}
   {:url "/route.html"
    :html (route)}
   {:url "/apply.html"
    :html (mentor)}
   {:url "/channel.html"
    :html (channel)}
   {:url "/error.html"
    :html (error)}])

(defroutes static-routes
  (route/resources "/")
  (route/not-found (:html (last all-routes))))

(def app (-> static-routes
             (reload/wrap-reload)))

;; emitting the static version of the site
(def static-home "resources/public")

(defn emit-static-site []
  (println "Emitting static site to: " static-home)
  (doseq [{:keys [url html]} all-routes]
    (spit (str static-home url) html)))

(emit-static-site)

(defn -main [& args]
  (emit-static-site)
  (watcher ["src/"]
           (rate 50)
           (file-filter (extensions :clj))
           (on-change #(emit-static-site)))
  (http/run-server app {:port 8000})
  (println "Awaiting changes..."))

;; (-main)

