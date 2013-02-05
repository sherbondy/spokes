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