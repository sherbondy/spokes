(defproject spokes "0.1.0-SNAPSHOT"
  :description "Biking across the country to learn about education."
  :url "http://spokesamerica.org"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.zip "0.1.1"]

                 [compojure "1.1.3"]
                 [environ "0.3.0"]
                 [hiccup "1.0.2"]
                 [http-kit "2.0.0-RC2"]

                 ;cljs
                 [prismatic/dommy "0.0.1"]
                 [jayq "2.0.0"]]

  :plugins [[lein-ring "0.8.0"
             :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "0.2.10"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler spokes.handler/app
         :auto-reload? true
         :port 8000}

  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]
                        [ring/ring-devel "1.1.0"]
                        [com.cemerick/pomegranate "0.0.13"
                         :exclusions [commons-io
                                      commons-codec]]]}}

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds
   [{:source-path "src-cljs"
     :compiler
     {:output-to "resources/public/js/main.js"
      :pretty-print true}}]})
