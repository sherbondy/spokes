(ns spokes.util
  (:use [jayq.core :only [$]])

  (:require [clojure.string :as str]))

(defn redirect! [url]
  (set! (.-location js/window) url))

(def location-hash (.-hash js/location))

(defn wait [ms func]
  (js/setTimeout func ms))

(defn log [& v]
  (.log js/console (apply str v)))

(defn exists? [$sel]
  (not= (.-length ($ $sel)) 0))

(defn form-to-map 
  "Converts a form's inputs into a hashmap.
  Takes a jQuery selector as input."
  [$form]
  (into {} (for [field (.serializeArray $form)]
    { (keyword (.-name field)) (str/trim (.-value field))})))

(defn hash-mapify-vector
  "Converts a vector of strings into a hashmap,
   assuming that the strings alternate between k and v.
   Auto-keywordizes the keys."
  [v]
  (apply hash-map
    (map-indexed 
      (fn [idx val] 
        (if (even? idx) 
            (keyword val) 
            val)) v)))

(defn mapify-hash 
  "convert location.hash into a clojure map"
  []
  (let [hash (.slice location-hash 1)
        split-hash (.split hash #"[=&]")]
    (hash-mapify-vector split-hash)))


(defn camel-name
  "Convert :fill-style to \"fillStyle\""
  [kw]
  (let [nom (name kw)
        split-nom (str/split nom #"\-")]
    (apply str (cons (first split-nom) 
                     (map str/capitalize (rest split-nom))))))

(defn now 
  "return the current time in miliseconds"
  []
  (.getTime (js/Date.)))


(def Tau (* 2 Math/PI))
(def e Math/E)

(defn +clamp
  "Increment val by added, but clamp to
   fall in the range [0-max]"
  [val added max]
  (let [new-val (+ val added)]
    (mod new-val max)))

(def cos Math/cos)
(def sin Math/sin)