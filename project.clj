(defproject spokes "0.1.0-SNAPSHOT"
  :description "Biking across the country to learn about education."
  :url "http://spokesamerica.org"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.namespace "0.2.4"]

                 [compojure "1.1.5"]
                 [clj-time "0.4.4"]
                 [environ "0.3.0"]
                 [hiccup "1.0.2"]
                 [http-kit "2.0.0-RC4"]
                 [markdown-clj "0.9.19"]
                 [ring "1.2.0-beta1"]
                 [ring/ring-devel "1.1.0"]
                 [watchtower "0.1.1"]

                 ;cljs
                 [prismatic/dommy "0.0.2"]
                 [jayq "2.0.0"]]

  :plugins [[lein-ring "0.8.0"
             :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "0.3.2"]]

  :ring {:handler spokes.handler/app
         :auto-reload? true
         :port 8000}

  :profiles
  {:dev {:dependencies [[com.cemerick/pomegranate "0.0.13"
                         :exclusions [commons-io
                                      commons-codec]]]}}

  :hooks [leiningen.cljsbuild]

  ;; for the cljs macros, add src-cljs
  :source-paths ["src"
                 "src-cljs"]

  :cljsbuild
  {
   :repl-listen-port 9000

   :builds
   [{:source-paths ["src-cljs"],
     :compiler
     {:output-to "resources/public/js/main.js"
      :optimizations :simple
      :pretty-print true}}]}

  :main spokes.handler)
