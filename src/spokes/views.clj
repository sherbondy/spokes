(ns spokes.views
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [markdown.core :as md]
            [spokes.gps :as gps]
            [spokes.util :as u])
  (:use [hiccup.def :only [defhtml]]
        [spokes.team :only [team]]
        [spokes.courses :only [courses]]))

(defn fname [person]
  (first (str/split (:name person) #"\s")))

(defhtml lt-script [port]
  [:script {:type "text/javascript" :id "lt_ws" 
            :src (str "http://localhost:"
                      port
                      "/socket.io/lighttable/ws.js")}])

(def start-date    (t/date-time 2013 6 9))
(def end-date      (t/date-time 2013 8 21))
(def cal-interval  (t/interval (t/date-time 2013 6)
                               (t/date-time 2013 9)))
(def trip-interval (t/interval start-date (t/plus end-date (t/days 1))))
(def month-fmt     (tf/formatter "MMMM"))
(def p-fmt         (tf/formatter "MMMM d"))
(def time-fmt      (tf/formatter "y-MM-dd"))

(defhtml time-elem [date]
  [:time {:datetime (tf/unparse time-fmt date)}
   (tf/unparse p-fmt date)])

;; the base template for all of the other pages
(defn layout [& body]
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html;charset=UTF-8"}]
    (comment
      [:meta {:name "viewport" 
              :content "width=device-width,
                        initial-scale=1.0,
                        maximum-scale=1.0"}])
    [:title "Spokes: Biking Across America,Summer 2013"]
    ;;(lt-script 36099)
    
    [:link {:rel "shortcut icon" :href "img/spokes_logo_green.png"}]

    ;; be weary of the relative paths!
    (include-css "/css/style.css" "/css/live.css"
                 "/css/bootstrap-responsive.min.css")]

   [:body
    [:div#title
      [:header#header
       [:img#logo {:src "/img/spokes_logo_white.png"}]
       [:div.center
        [:h1 "Spokes"]
        [:h2#slogan "Inspiring students to learn what they love"]]]]
      
    [:div#fixed
      [:ul#social
       [:li.facebook [:a {:href "#"}]]
       ;; use blog instead of rss
       [:li.blog [:a {:href "http://blog.spokesamerica.org"}]]
       [:li.twitter [:a {:href "http://twitter.com/spokesamerica"}]]
       [:li.vimeo [:a {:href "http://vimeo.com/spokesamerica"}]]]
      
      [:h2#fixed-title 
       [:img.logo {:src "/img/spokes_logo_white.png" 
                   :width "64px" :height "64px"}]
       "Spokes"]]

    body

    (include-js 
     "//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"
     (str "//maps.googleapis.com/maps/api/js?key=" 
          (env :google-maps-key) "&sensor=false")
     "/js/anim.js"
     "/js/bootstrap.min.js"
     "/js/hashgrid.js"
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

   [:div#content.row-fluid
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

;; bootstrap carousel markup
(defn carousel [id items]
  (let [hash-id (str "#" id)]
    [:div.carousel.slide {:id id}
      [:ol.carousel-indicators
       (for [i (range (count items))]
        [:li {:data-target hash-id :data-slide-to i
              :class (when (= i 0) "active")}])]
     
      [:div.carousel-inner
       (for [i (range (count items))]
         (let [item (nth items i)]
          [:div {:class (u/cond-class "item" [(= i 0) "active"])}
           [:img {:src (:image item)}]
           [:div.carousel-caption
            [:h4 (:title item)]
            [:div (md/md-to-html-string (:description item))]]]))]
     
      ;; nav
      [:a.carousel-control.left 
       {:href "#courses" :data-slide "prev"} "&lsaquo;"]
      [:a.carousel-control.right
       {:href "#courses" :data-slide "next"} "&rsaquo;"]]))


(defn home []
  (layout
  
   (comment
     [:a.navbar-toggle
      {:data-toggle "collapse" :data-target "#navigation"}
      (for [i (range 3)]
        [:span.icon-bar])])
     
   [:div#what
    [:div.center
      [:p "We're biking across the United States in collaboration
           with "
       [:a {:href "http://teachforamerica.org"} "Teach for America"]
       " as part of an effort to rethink
        education. As we go, we'll be stopping
        at public schools throughout the country to hold "
       [:em "learning festivals"]
        " geared towards middle and high-school students.
         Each of us will be teaching a hands-on,
         project-oriented class based on one of our passions."]]]
   
   [:div#video
    [:a#play.center {:href "#"}]
    [:div.explanation
     [:p "Watch our Indiegogo video to learn more about "
      [:strong "Spokes"] "."]]]
    
   [:div#classes
    [:div.righty
      [:h2 "Our Classes"]
      (carousel "courses" courses)]]
   
  [:div#why
    [:div.righty
      [:h2 "Our Motivation"]
      [:p "We are dedicated to revealing the exploratory, 
       self-directed, and boundless nature of learning to students 
       across the US. Our mission stems from the simple idea 
       that most of the learning we do over the course of our 
       lifetimes happens outside of a classroom as the
       result of semi-random explorations into topics 
       that genuinely interest us."]
      [:p "We want to show this to high school students and give 
       them an opportunity to feel inspired and find something they love."]]]
   
  [:div#where
    [:div#map]
   
    [:div#countdown
     [:h2#days-left (t/in-days (t/interval (t/now) start-date))]
     [:h4 "more days"]]
   
    [:div.row-fluid
      [:div.span4.box
        [:h4 "From " (time-elem start-date)
             " through " (time-elem end-date)]
        [:p "We'll be biking from San Francisco to Washington D.C."]]]]
  
  [:div#who
    [:div.righty
     [:h2 "Meet Our Team"]
      [:ul#team
       (map-indexed
        (fn [idx person]
          (let [pfirst    (fname person)
                lc-pfirst (str/lower-case pfirst)
                p-img     (str "/img/team/" lc-pfirst ".jpg")]
            [:li
             [:a {:href (str "#" lc-pfirst)}
              [:img {:alt (:name person)
                     :src p-img}]]]))
        team)]

       [:div#bios
        [:div
         [:h3 "Click on a face for a brief bio"]
         [:p "We are " (count team) " college students from MIT and 
             UC Berkeley who are passionate about education."]]
        (for [person team]
          (let [lc-pfirst (str/lower-case (fname person))]
            [:div.hidden {:id lc-pfirst}
             [:h3 (:name person)
              [:small.pull-right
               (:school person) " Class of " (:grad-year person)]]
             
              (md/md-to-html-string (:bio person))]))]]]

     [:div#help
      [:div#explain-social
       [:p "Follow the trip through social media."]]
      
      [:img#logo-bottom {:src "/img/spokes_logo_green.png" 
                         :width "300px" :height "300px"}]
      
      [:div.righty
       [:h2 "Help " [:em "Spokes!"]]
       [:p "We are currently looking for sponsors. "
        "If you're interested in getting in touch, please " 
        [:a {:href "mailto:spokes@mit.edu"} "email us"] "."]
  
       [:p
        "You can definitely help by spreading the word! "
        "Follow our journey on the "
        [:a {:href "http://blog.spokesamerica.org"} "blog"] "."]]]
    
    ;; map data
    [:script#gps-data {:type "text/edn"} gps/edn-data]))
  

(defn error []
  (layout
   [:div#who.row-fluid
     [:div.righty
      [:h1 "Page not Found"]
      [:p "Sorry, we couldn't find the page you were looking for."]
      [:p "Try visiting "
       [:a {:href "/"} "the home page"] "."]]]))

(defn mentor []
  (layout
    [:iframe#app {:src "https://docs.google.com/forms/d/1wr8j-tCvf4RuJnw16GR3GEj1v6E-LgpGB2d2Tz_f4N8/viewform?embedded=true"}
     "Loading..." ]))

