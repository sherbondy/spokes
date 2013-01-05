(ns spokes.views
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn layout [& body]
  (html5
   [:head
    (include-css "css/style.css")
    (include-js "/js/main.js")]
   [:body
    body]))

(defn q [question title & body]
  [:div {:id question}
   [:h2 [:em (str/capitalize question)]
    (if title (str " " title "?"))]
   body])

(defn home [team]
  (layout
   [:header
    [:h1 "Spokes"]

    [:ul#questions
     (for [question ["who", "what", "when", "where", "why", "how"]]
       [:li.question
        [:a {:href (str "#" question)} question]])]]

   (q "who" "are you"
      [:p "We are " (count team) " undergraduates at MIT who are passionate"
       "about education:"]

      [:ul
       (for [person team]
         [:li [:h3 (:name person)]])])

   (q "what" "are you doing"
      [:p "We're biking across the United States."])

   (q "when" nil
      [:p "This summer, from " [:time {:datetime "2013-06-09"} "June 9"]
       " through " [:time {:datetime "2013-08-30"} "August 30."]])

   (q "where" "are you going"
      [:p "Here's a map outlining our planned route:"]
      ;; replace with the real, interactive map
      [:img {:src "/img/map.jpg" :width "50%"}]

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
       "Follow our journey on the blog."]
      )))


(defn show-cljs [file]
  (let [contents (slurp (str "src-cljs/spokes/" file))]
    (layout contents)))