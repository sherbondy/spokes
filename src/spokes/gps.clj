(ns spokes.gps
  (:use [clojure.data.zip.xml :only [attr seq-test text text= xml->]])
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure.zip :as zip]))

;; Obviously, I shouldn't be doing this xml processing
;; for every request. Do it once, then grab pre-processed
;; version.

;; maybe determine the curvature of the map and determine
;; the subset whose tangents are representative of the whole

;; [trail count] pairs
(def trails {:we 4 :ta 12 :ac 7})

(def trail-list [{:name "Western Express" :abbr "WE"}
                 {:name "Trans America"    :abbr "TA"}
                 {:name "Atlantic Coast"  :abbr "AC"}])

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

(defn get-route [trail route-file filter-fn]
  (let [route-xml (xml/parse route-file)
        route-z   (zip/xml-zip route-xml)]
    (into {}
          (filter filter-fn
                  (map (partial useful-info trail)
                       (all-z-waypoints route-z))))))

(defn get-routes [trail file-count filter-fn]
  (apply merge
         (for [i (range 1 (inc file-count))]
           (let [route-file (route-fname trail i)]
             (get-route trail route-file filter-fn)))))

(defn kw-routes [kw & [filter-fn]]
  (get-routes (str/upper-case (name kw))
              (kw trails)
              (or filter-fn identity)))

;; should probably maintain a sorted order by longitude
(def fval (comp first vals))

(def we (kw-routes :we (fn [m] (< (:lon (fval m)) -104))))

(def ta (kw-routes :ta (fn [m] (and (> (:lon (fval m)) -105)
                                    (< (:lon (fval m)) -77.6)))))

(def ac (kw-routes :ac (fn [m] (and (> (:lat (fval m)) 38)
                                    (< (:lat (fval m)) 38.9)))))

(defn all-locations []
  (merge we ta ac))

(first (all-locations))

;; filter to only the waypoints
(defn waypoints []
  (into {}
        (filter (fn [[k v]] (= (:sym v) "Waypoint"))
          (all-locations))))

;; (count (all-locations))

(def wp-symbols (set (map :sym (vals (all-locations)))))
;; #{"Waypoint" "Movie Theater" "Bike Trail" "Campground" "Park" "Information" "Summit" "Shopping Center" "Gas Station" "Restroom" "Museum" "City (Small)" "Triangle, Blue" "Convenience Store" "Scenic Area" "Library" "Lodging" "Post Office" "Restaurant"}

(def edn-data
  {:symbols wp-symbols
   :trails  trail-list
   :data    (waypoints)})

(defn data-file [fmt]
  (str "resources/public/route-data." fmt))

(defn spit-edn-data []
  (spit (data-file "edn") edn-data))

(defn spit-json-data []
  (spit (data-file "json") (json/write-str edn-data)))

(spit-edn-data)
;; (spit-json-data)

(defn slurp-edn-data []
  (slurp data-file))
