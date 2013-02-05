(ns spokes.views
  (:require [clojure.string :as str]
            [environ.core :refer [env]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [spokes.gps :as gps]
            [spokes.util :as u])
  (:use [hiccup.def :only [defhtml]]))

(defn fname [person]
  (first (str/split (:name person) #"\s")))

(defn layout [& body]
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html;charset=UTF-8"}]
    [:title "Spokes: Biking Across America,Summer 2013"]

    (u/font-link ["Lato" [400 700] ["italic"]]
                 ["Signika" [400 600 700]])
    (include-css "/css/style.css")]

   [:body body
    (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"
                (str "//maps.googleapis.com/maps/api/js?key=" 
                     (env :google-maps-key) "&sensor=false")
                "/js/main.js")]))

(defn checkbox-div [name label & [value]]
  [:div
   [:label {:for name} label]
   [:input {:type "checkbox" :checked "checked"
            :name name :id name :value (or value name)}]])

(defn route []
  (layout
   [:div#map]

   [:div#trails
    [:h3 "Trails"]
    [:a.toggle {:href "#"} "Toggle All"]
    (for [trail gps/trail-list]
      (checkbox-div (:abbr trail) (:name trail)))]

   [:div#symbols
    [:h3 "Locations"]
    [:a.toggle {:href "#"} "Toggle All"]
    (for [symbol gps/wp-symbols]
      (let [name (u/hyphenate symbol)]
        (checkbox-div name symbol symbol)))]

   [:div#content
    [:h1 "Our Route"]
    [:p "GPS Data from " 
     [:a {:href "https://www.adventurecycling.org"}
      "Adventure Cycling"] "."]

    [:h2 "Locations within a "
     [:input {:type "text" :maxlength "2" :placeholder "10"
              :id "radius" :name "radius"}]
     " mile radius:"]
    [:div#nearby]]

   [:script#gps-data {:type "text/edn"} gps/edn-data]))


(defn q [question title & body]
  [:div {:id question}
   [:h2 [:em (str/capitalize question)]
    (if title (str " " title "?"))]
   body])

(defn home [team]
  (layout
   [:canvas#canvas]

   [:header
    [:h1 "Spokes"]

    [:ul#questions
     (for [question ["who", "what", "when", "where", "why", "how"]]
       [:li.question
        [:h4 [:a {:href (str "#" question)} question]]])]
    
    [:embed#bike {:src "/img/bike-svg.svg" :type "image/svg+xml"}]]

   [:div#content
    (q "who" "are you"
       [:p "We are " (count team) " undergraduates at MIT who are passionate
          about education:"]

       [:ul#team
        (for [person team]
          (let [pfirst (fname person)]
            [:li [:h5 [:a.pill-link {:href (str "#" pfirst)} pfirst]]]))])

    (q "what" "are you doing"
       [:p "We're biking across the United States."])

    (q "when" nil
       [:p "This summer, from " [:time {:datetime "2013-06-09"} "June 9"]
        " through " [:time {:datetime "2013-08-30"} "August 30."]])

    (q "where" "are you going"
       [:p "We'll be biking from San Francisco on the Western Express 
        trail, then taking the Trans America trail to Washington D.C.
        Time allowing, we'll also try heading up the east coast 
        to get back to Cambridge, Massachusetts in time for the 
        fall semester."]
       [:p "Which means we'll get to explore, at a minimum, the following states:"]

       [:ol
        ;; these are made up
        (for [[state eta] [["California" 0] ["Nevada" 5] ["Utah" 10]
                           ["Colorado" 15] ["Kansas" 20] ["Missouri" 25]
                           ["Ohio" 30] ["Kentucky" 35] ["Virginia" 40]
                           ["Maryland" 45]]]
          [:li state])])

    (q "why" "are you doing this"
       [:p "We're crazy."])

    (q "how" "can I help"
       [:p "We are looking for sponsors. "
        "And suggestions for towns to visit along the way. "
        "You can definitely help by spreading the word! "
        "And joining the conversation. "
        "Follow our journey on the "
        [:a.pill-link {:href "http://blog.spokesamerica.org"} "blog"]]
       )]))