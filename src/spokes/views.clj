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

;; the base template for all of the other pages
(defn layout [& body]
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html;charset=UTF-8"}]
    [:meta {:name "viewport" 
            :content "width=device-width,
                      initial-scale=1.0,
                      maximum-scale=1.0"}]
    [:title "Spokes: Biking Across America,Summer 2013"]
    (lt-script 36099)

    ;; be weary of the relative paths!
    (u/font-link ["Lato" [400 700] ["italic"]]
                 ["Signika" [400 600 700]])
    (include-css "/css/style.css" "/css/live.css"
                 "/css/bootstrap-responsive.min.css")]

   [:body 
    [:header#header
     [:div#logo.cloud
      [:h1 "Spokes"]
      [:canvas]]
     [:h4#slogan.span3 "Inspiring students to learn what they love"]]

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

;; main page section boilerplate 
;; (generated for each question section)
(defn q [question title & body]
  [:div {:id question}
   [:div.content
     [:h3 [:em (str/capitalize question)]
      (if title (str " " title "?"))]
     body]])

;; trip calendar markup
(def start-date    (t/date-time 2013 6 9))
(def end-date      (t/date-time 2013 8 21))
(def cal-interval  (t/interval (t/date-time 2013 6)
                               (t/date-time 2013 9)))
(def trip-interval (t/interval start-date (t/plus end-date (t/days 1))))
(def month-fmt     (tf/formatter "MMMM"))
(def p-fmt         (tf/formatter "MMMM d"))
(def time-fmt      (tf/formatter "y-MM-dd"))
(def weekdays ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"])

(defn weekday-offset
  "Return values range 0-6 instead of 1-7"
  [date]
  (- (t/day-of-week date) 1))

(defhtml calendar []
  [:div.row-fluid
   (for [i (range (t/in-months cal-interval))]
     (let [month-start   (t/plus (t/start cal-interval) (t/months i))
           next-month    (t/plus month-start (t/months 1))
           month-str     (tf/unparse month-fmt month-start)
           day-offset    (weekday-offset month-start)
           days-in-month (t/in-days (t/interval month-start next-month))]
       [:div.month.pull-left.span4
        [:h4 month-str]
        [:table.calendar
         [:thead
          [:tr
           (for [wd (range 7)]
             [:td (nth weekdays wd)])]]
         [:tbody
          (for [w (range 6)]
            [:tr
             (for [d (range 7)]
               (let [box-no     (+ d (* w 7))
                     day        (- box-no day-offset)
                     box-date   (t/plus month-start (t/days day))
                     valid-day? (and (>= day 0) (< day days-in-month))
                     trip-day?  (and valid-day? 
                                     (t/within? trip-interval box-date))]
                 [:td {:class (if trip-day? "trip")}
                  (if valid-day?
                    (inc day))]))])]]]))])

(defhtml time-elem [date]
  [:time {:datetime (tf/unparse time-fmt date)}
   (tf/unparse p-fmt date)])

(defn home []
  (layout
   [:canvas#canvas]
   
   [:a.navbar-toggle
    {:data-toggle "collapse" :data-target "#navigation"}
    (for [i (range 3)]
      [:span.icon-bar])]
     
     [:div#navigation.nav-collapse.collapse
      [:ul.nav
        (for [question ["who", "what", "when", "where", "why", "how"]]
          [:li
           [:a {:href (str "#" question)} (str/capitalize question)]])
        [:li
         [:a {:href "http://blog.spokesamerica.org"} "Blog!"]]]]

   [:div#content.row-fluid
     (q "who" "are you"
        [:div
         [:p "We are " (count team) " college students from MIT and 
             UC Berkeley who are passionate about education:"]]
        
        [:ul#team
         (map-indexed
          (fn [idx person]
            (let [pfirst    (fname person)
                  lc-pfirst (str/lower-case pfirst)
                  p-img     (str "/img/team/" lc-pfirst ".jpg")]
              [:li
               [:a {:href (str "#" lc-pfirst)}
                [:img {:alt (:name person)
                       :src p-img}]
                [:h5 pfirst]]]))
          team)]

       [:div#bios
        [:div
         [:h4 "Click on a face for a brief bio."]]
        (for [person team]
          (let [lc-pfirst (str/lower-case (fname person))]
            [:div.hidden {:id lc-pfirst}
             [:h4 (:name person)
              [:small.pull-right 
               (:school person) " Class of " (:grad-year person)]]
              (md/md-to-html-string (:bio person))]))])

     (q "what" "are you doing"
        [:div
          [:p "We're biking across the United States in partnership
               with "
           [:a {:href "http://teachforamerica.org"} "Teach for America"]
           " as part of an effort to rethink
            public education. As we go, we'll be stopping
            at public schools throughout the country to hold "
           [:em "learning festivals"]
            " geared towards middle and high-school students.
             Each of us will be teaching a hands-on,
             project-oriented class based on one of our passions."]
          [:p "Here are the courses we'll be offering:"]]

        (carousel "courses" courses))

     (q "when" "is it"
        [:div
          [:p "This summer, from " (time-elem start-date)
           " through " (time-elem end-date) "."]
          (calendar)
          [:h4 
           [:strong#days-left (t/in-days (t/interval (t/now) start-date))]
           " more days until we get going."]])

     (q "where" "are you going"
        [:div#map]
        [:div.row-fluid
          [:div.span4.box
            [:p "We'll be biking from San Francisco to Washington D.C."]
            [:p "We're taking the Western Express trail, then
                 Trans America."]]])

     (q "why" "are you doing this"
        [:div
          [:p "We are dedicated to revealing the exploratory, 
           self-directed, and boundless nature of learning to students 
           across the US. Our mission stems from the simple idea 
           that most of the learning we do over the course of our 
           lifetimes happens outside of a classroom as the
           result of semi-random explorations into topics 
           that genuinely interest us."]
          [:p "We want to show this to high school students and give 
           them an opportunity to feel inspired and find something they love."]])

     (q "how" "can I help"
        [:div
          [:p "We are currently looking for sponsors. "
           "If you're interested in getting in touch, please " 
           [:a {:href "mailto:spokes@mit.edu"} "email us"] "."]
  
          [:p
           "You can definitely help by spreading the word! "
           "Follow our journey on the "
           [:a {:href "http://blog.spokesamerica.org"} "blog"] "."]])
    
    ;; map data
    [:script#gps-data {:type "text/edn"} gps/edn-data]]))
  

(defn error []
  (layout
   [:div#content.row-fluid
     [:div#who
      [:h1 "Page not Found"]
      [:p "Sorry, we couldn't find the page you were looking for."]
      [:p "Try visiting "
       [:a {:href "/"} "the home page"] "."]]]))

(defn mentor []
  (layout
    [:iframe#app {:src "https://docs.google.com/forms/d/1wr8j-tCvf4RuJnw16GR3GEj1v6E-LgpGB2d2Tz_f4N8/viewform?embedded=true"}
     "Loading..." ]))

