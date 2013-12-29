(ns spokes.handler
  (:use watchtower.core
        compojure.core)
  (:require [compojure.route :as route]
            [org.httpkit.server :as http]
            [clojure.tools.namespace.repl :as ns]
            [ring.middleware.reload :as reload]
            [spokes.team :as t]
            [spokes.views :as v]))

(def stop-server-fn (atom nil))

(def all-routes
  [{:url "/index.html"
    :html-fn v/home}
   {:url "/route.html"
    :html-fn v/route}
   {:url "/apply.html"
    :html-fn v/mentor}
   {:url "/channel.html"
    :html-fn v/channel}
   {:url "/error.html"
    :html-fn v/error}])

(defroutes static-routes
  (route/resources "/")
  (route/not-found ((:html-fn (last all-routes)))))

(def app (-> static-routes
             (reload/wrap-reload)))

;; emitting the static version of the site
(def static-home "resources/public")

(defn emit-static-site []
  (println "Emitting static site to: " static-home)
  (println (first t/team))
  (doseq [{:keys [url html-fn]} all-routes]
    (spit (str static-home url)
          (html-fn)))
  (println "done"))

(declare start)
(declare stop)

(defn refresh-and-restart []
  (stop)
  (println "Refreshing clojure source files...")
  (ns/refresh :after 'spokes.handler/start))

(defn watch-for-changes []
  (println "Awaiting changes...")
  (watcher ["src/"]
           (rate 50)
           (file-filter (extensions :clj))
           (on-change refresh-and-restart)))

(defn start []
  (emit-static-site)
  (reset! stop-server-fn
          (http/run-server app {:port 8000}))
  (watch-for-changes))

(defn stop []
  (println "Stopping the server...")
  (@stop-server-fn))

;;(refresh-and-restart)

(defn -main [& args]
  (start))

;; (-main)
