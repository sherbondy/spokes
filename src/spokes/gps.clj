(ns spokes.gps
  (:use [clojure.data.zip.xml :only [attr seq-test text text= xml->]])
  (:require [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure.zip :as zip]))

;; [trail count] pairs
(def trails {:we 4 :ta 12 :ac 7})

(defn pad-zero [n]
  (str (if (< n 10) "0") n))

(defn all-z-waypoints [z]
  (xml-> z :wpt))

(defn tag-data [wpt tagname]
  (first (xml-> wpt tagname text)))

;; precompute latitude and longitude in radians
(defn useful-info [trail wpt]
  (let [lat (Float/parseFloat (attr wpt :lat))
        lon (Float/parseFloat (attr wpt :lon))]
    {(tag-data wpt :name)
     {:desc  (tag-data wpt :desc)
      :sym   (tag-data wpt :sym)
      :trail trail
      :lat   lat
      :lon   lon
      :lat-r (Math/toRadians lat)
      :lon-r (Math/toRadians lon)}}))

(defn route-fname [trail i]
  (str "resources/gps/" trail (pad-zero i) ".gpx"))

(defn get-route [trail route-file]
  (let [route-xml (xml/parse route-file)
        route-z   (zip/xml-zip route-xml)]
    (apply merge (map (partial useful-info trail)
                      (all-z-waypoints route-z)))))

(defn get-routes [trail file-count]
  (apply merge
         (for [i (range 1 (inc file-count))]
           (let [route-file (route-fname trail i)]
             (get-route trail route-file)))))

(defn kw-routes [kw]
  (get-routes (str/upper-case (name kw))
              (kw trails)))

;; should probably maintain a sorted order by longitude
(def we (kw-routes :we))
(def ta (kw-routes :ta))
(def ac (kw-routes :ac))

(def all-locations 
  (merge we ta ac))
;; (map? all-locations)
;; (count all-locations)

(def wp-symbols (set (map :sym (vals all-locations))))
;; #{"Waypoint" "Movie Theater" "Bike Trail" "Campground" "Park" "Information" "Summit" "Shopping Center" "Gas Station" "Restroom" "Museum" "City (Small)" "Triangle, Blue" "Convenience Store" "Scenic Area" "Library" "Lodging" "Post Office" "Restaurant"}

(def edn-data
  {:symbols wp-symbols
   :trails [{:name "Western Express" :abbr "WE"}
            {:name "TransAmerica"    :abbr "TA"}
            {:name "Atlantic Coast"  :abbr "AC"}]
   :data all-locations})

;; in mi
(def r-earth (float 3959))

(defn law-of-cos [^Float lat1 ^Float lon1 ^Float lat2 ^Float lon2]
  (* r-earth
     (Math/acos (+ (* (Math/sin lat1) (Math/sin lat2))
                   (* (Math/cos lat1) (Math/cos lat2)
                      (Math/cos (- lon2 lon1)))))))

;; sym = the type of resource, eg :Bathroom
(defn nearby-resource [sym lat lon mi-radius]
  (filter #(and (= sym (:sym %))
                (> mi-radius
                   (law-of-cos lat lon (:lat-r %) (:lon-r %))))
          (vals all-locations)))

;; (count (nearby-resource "Lodging" (Math/toRadians 37.775) (Math/toRadians -122.418) 10))

(defn filter-sym [sym] 
  (filter #(= sym (:sym %)) (vals all-locations)))

;; (def bathrooms (filter-sym "Restroom"))
;; (count bathrooms)
;; (def campgrounds (filter-sym "Campground"))
;; (count campgrounds)