(ns spokes.util
  (:require [clojure.string :as str])
  (:use [hiccup.def :only [defhtml]]))

(defn font
  ([name] (font name nil))
  ([name weights] (font name weights []))
  ([name weights styles]
     (let [styled-weights 
           (for [weight weights style (conj styles "")]
             (str weight style))]
       (str (str/replace name " " "+") 
            (if weights
              (str ":" (str/join "," styled-weights)))))))

(defn fonts [& args]
  (map #(apply font %) args))

(defhtml font-link [& faces]
  [:link {:rel "stylesheet" :type "text/css"
          :href (str "http://fonts.googleapis.com/css?family="
                     (str/join "|" (apply fonts faces)))}])

(defn hyphenate [name]
  (str/lower-case (-> name 
                      (str/replace #"\s+" "-")
                      (str/replace #"[\(\)]" ""))))

(defn cond-class 
  "Returns a string of class names separated by spaces.
   default is returned regardless of the conditions.
   Conditions is a sequence of [condition value] pairs,
   for which the class 'value' is returned if the condition is met.
   Eg: (cond-class \"pizza\" [true \"bagels\"] [false \"spinach\"])
   would return: \"pizza bagels\""
  [default & conditions]
  (apply str default
    (for [[condition value] conditions]
      (if condition (str " " value)))))
