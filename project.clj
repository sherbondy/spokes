(defproject spokes "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.2"]
                 [jayq "0.2.0"]]
  :plugins [[lein-ring "0.7.5"]
            [lein-cljsbuild "0.2.10"]]
  :ring {:handler spokes.handler/app
         :auto-reload? true}

  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]
                        [ring-serve "0.1.2"]]}}

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds
   [{:source-path "src-cljs"
     :compiler
     {:output-to "resources/public/js/main.js"
      :pretty-print true}}]})
