(ns spokes.views
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]])
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

(defn layout [& body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Spokes: Biking Across America,Summer 2013"]

    (font-link ["Lato" [400 700] ["italic"]]
               ["Signika" [400 600 700]])
    (include-css "/css/style.css")
    (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"
                "/js/main.js")
   [:body
    body]]))

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
       [:p "We are " (count team) " undergraduates at MIT who are passionate "
        "about education."]

       (comment [:ul
                 (for [person team]
                   [:li [:h5 (:name person)]])]))

    (q "what" "are you doing"
       [:p "We're biking across the United States."])

    (q "when" nil
       [:p "This summer, from " [:time {:datetime "2013-06-09"} "June 9"]
        " through " [:time {:datetime "2013-08-30"} "August 30."]])

    (q "where" "are you going"
       [:p "Here's a map outlining our planned route:"]
       ;; replace with the real, interactive map
       [:img.map {:src "/img/map.jpg"}]

       [:p "Which means we'll get to explore the following states:"]
       [:ol
        ;; these are made up
        (for [[state eta] [["California" 0] ["Nevada" 5] ["Utah" 10]
                           ["Colorado" 15] ["Kansas" 20] ["Missouri" 25]
                           ["Ohio" 30] ["Kentucky" 35] ["Virginia" 40]]]
          [:li state])])

    (q "why" "are you doing this"
       [:p "We're crazy."])

    (q "how" "can I help"
       [:p "We are looking for sponsors. "
        "And suggestions for towns to visit along the way. "
        "You can definitely help by spreading the word! "
        "And joining the conversation. "
        "Follow our journey on the "
        [:a.blog {:href "http://spokesmit.tumblr.com"} "blog"]]
       )]))