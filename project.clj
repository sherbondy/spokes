(defproject spokes "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.zip "0.1.1"]

                 [compojure "1.1.3"
                  :exclusions [ring/ring-core]]
                 [environ "0.3.0"]
                 [hiccup "1.0.2"]

                 [com.cemerick/pomegranate "0.0.13"]

                 ;cljs
                 [jayq "2.0.0"]]

  :plugins [[lein-ring "0.8.0"
             :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "0.2.10"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler spokes.handler/app
         :auto-reload? true}

  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}}

;;  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds
   [{:source-path "src-cljs"
     :compiler
     {:output-to "resources/public/js/main.js"
      :pretty-print true}}]})
