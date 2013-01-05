(ns spokes.main
  (:require [clojure.string :as str]))

(defn log [& messages]
  (.log js/console (apply str messages)))

(log "Testing..." "one two three")