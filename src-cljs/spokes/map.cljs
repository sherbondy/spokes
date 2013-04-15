(ns spokes.map
  (:require [cljs.reader :refer [read-string]]
            [clojure.set :refer [difference]]
            [clojure.string :as str]
            [dommy.template :as template]
            [jayq.core :as jq]
            [jayq.core :refer [$]]
            [jayq.util :as util]
            [spokes.util :as u])

  (:require-macros [jayq.macros :as jm]
                   [spokes.util-macros :refer [click-fn]]))

(def nav-geo (.-geolocation js/navigator))

(defn deg-to-rad [deg]
  (* deg Math/PI (/ 1 180)))

(defn lat-lng [lat lng]
  (google.maps.LatLng. (js/parseFloat lat) (js/parseFloat lng)))

(defn make-marker [options]
  (google.maps.Marker. (clj->js options)))

(defn map-marker [map coords title & [opts]]
  (make-marker (merge opts
                      {:position coords 
                       :map map
                       :title title})))

(def geocoder (google.maps.Geocoder.))

(defn geocode [location callback]
  (let [request (clj->js {:address location})]
    (.geocode geocoder request callback)))

(defn grab-coords
  "Extracts the coordinates from a geocode response and passes
   them to success-fn on success."
  [success-fn]
  (fn [result status]
    (if (= status google.maps.GeocoderStatus.OK)
      (let [coords (.-location (.-geometry (nth result 0)))]
        (success-fn coords)))))

(def default-options 
  (clj->js {:center (google.maps.LatLng. 40 -95)
            :zoom 4
            :mapTypeId google.maps.MapTypeId.ROADMAP
            :scrollwheel false}))


;; in mi
(def r-earth 3959.0)

(defn law-of-cos [lat1 lon1 lat2 lon2]
  (* r-earth
     (Math/acos (+ (* (Math/sin lat1) (Math/sin lat2))
                   (* (Math/cos lat1) (Math/cos lat2)
                      (Math/cos (- lon2 lon1)))))))

(defn nearby? 
  "Expects lat and lon in RADIANS, not degrees"
  [lat lon mi-radius loc]
  (> mi-radius
     (law-of-cos lat lon (:lat-r loc) (:lon-r loc))))


(def gmap (atom nil))
(def gps-response (atom {}))
(def markers (atom {})) ;; a map of {location-id marker}

;; Waypoint, Campground, etc, toggle-able
(def symbols (atom #{}))
;; Trails, toggle-able
(def trails (atom #{}))
;; a set of location-ids
(def filtered-data (atom #{}))
;; the user's geolocation
(def user-geo (atom {:lat nil :lng nil}))
;; the radius of nearby locations to search (in mi)
(def radius (atom nil))
;; a map of {location-id distance} pairs
(def distances (atom {}))

(defn gps-data [] (:data @gps-response))

(defn all-locations []
  (vals (gps-data)))

(defn gps-item [key]
  (get (gps-data) key))

(defn icon-for-trail [trail]
  (str "/img/" (condp = trail
                 "WE" "blue_MarkerW"
                 "TA" "orange_MarkerT"
                 "AC" "darkgreen_MarkerA") ".png"))

(defn marker-descr [loc-info coord-v]
  (str (:desc loc-info) 
       " " coord-v
       " #" (:sym loc-info)))

(defn make-spokes-marker [loc-id]
  (let [loc-info (gps-item loc-id)
        coord-v  (map #(% loc-info) [:lat :lon])
        coords   (apply lat-lng coord-v)
        marker   (map-marker @gmap coords
                             (marker-descr loc-info coord-v)
                             {:icon (icon-for-trail (:trail loc-info))})]
    marker))

(defn float-str [num places]
  (let [[l r] (str/split (str num) ".")]
    (str l "." (subs r 0 places))))

(defn nearby-template [results]
  [:ol
   (for [val results]
     [:li 
      [:h4 (str (:desc val) " (" 
                (float-str (:distance val) 2) " mi)")]
      [:div [:strong "Category:"] " " (:sym val)]
      [:div [:strong "Geolocation:"] " " 
       (str [(:lat val) (:lon val)])]])])

(defn render-nearby [keys]
  (let [$nearby   ($ "#nearby")
        key-count (count keys)]
    (if (< key-count 100)
      (let [results (map #(assoc (get (gps-data) %)
                            :distance (get @distances %)) keys)]
        (.html $nearby
               (template/node (nearby-template 
                               (sort-by :distance results)))))
      (.html $nearby (str "Too many (" key-count ") results to display. "
                          "Please try a smaller radius.")))))

;; auto add/remove markers from the map when the atom changes
; k r o n = key, reference, old state, new state
(defn data-watcher [k r o n]
  (doseq [key n]
    (if-not (contains? o key)
      (let [marker (get @markers key)]
        (.setMap marker @gmap))))

  ;; never delete markers, just take them off the map
  (doseq [key (difference o n)]
    (.setMap (get @markers key) nil))
  
  (render-nearby n))

(defn filter-fn [[k v]]
  (let [trail-set  @trails
        symbol-set @symbols
        mi-radius  @radius
        dists      @distances
        no-loc?    (some true? (map nil? [mi-radius (first dists)]))]
    (and (contains? trail-set  (:trail v))
         (contains? symbol-set (:sym v))
         (if no-loc?
           true
           (> mi-radius (k dists))))))

(defn filter-watcher [k r o n]
  (reset! filtered-data 
          (set (map first (filter filter-fn (gps-data))))))

(defn update-distances [k r o n]
  (let [lat (:lat n)
        lon (:lon n)]
    (reset! distances
            (apply merge
                   (for [[k v] (gps-data)]
                     {k (law-of-cos lat lon (:lat-r v) (:lon-r v))})))))

(defn checked-set
  "A set of checked input element values"
  [inputs]
  (set (map jq/val (vec (.filter inputs ":checked")))))

(defn symbol-inputs [] ($ "#symbols input"))
(defn toggle-symbols []
  (reset! symbols (checked-set (symbol-inputs))))

(defn trail-inputs [] ($ "#trails input"))
(defn toggle-trails []
  (reset! trails (checked-set (trail-inputs))))

(defn init-data []
  (reset! gps-response (read-string (.text ($ "#gps-data"))))

  ;; initialize the symbol and trail sets
  (toggle-symbols)
  (toggle-trails)

  (add-watch filtered-data :data data-watcher)
  (add-watch user-geo :geo update-distances)
  (doseq [[kw atm] {:symbols symbols :trails trails 
                    :radius  radius  :dists  distances}]
    (add-watch atm kw filter-watcher))

  (reset! markers (apply merge (for [[k _] (gps-data)] 
                                 {k (make-spokes-marker k)})))

  (reset! filtered-data (set (keys (gps-data)))))

(defn init-map [$elem & [opts]]
  (reset! gmap (google.maps.Map. (aget $elem 0)
                                 (or opts default-options)))
  (init-data))

(defn get-user-location []
  (u/log "getting location")
  (if nav-geo
    (.getCurrentPosition nav-geo
      (fn [pos]
        (let [coords (.-coords pos)
              lat    (.-latitude coords)
              lon    (.-longitude coords)]
          (reset! user-geo
                  {:lat (deg-to-rad lat)
                   :lon (deg-to-rad lon)})

          (map-marker @gmap (lat-lng lat lon)
                      "Your Current Location"))))
    (u/log "geolocation not supported")))

(defn update-radius [e]
  (let [val   (.val ($ "#radius"))
        new-r (js/parseInt val)]
    (if (and (number? new-r) (> new-r 0))
      (if (not= new-r @radius) (reset! radius new-r))
      (reset! radius nil))))

(defn on-toggle-checkboxes [$parent $checkboxes]
  (jq/on $parent :click ".toggle"
         (click-fn [e]
                   (.prop $checkboxes "checked"
                          (fn [i val] (not val)))
                   ;; only trigger one change event
                   (.trigger ($ (first $checkboxes)) "change"))))

(defn initialize []
  (init-map ($ "#map"))

  (get-user-location)

  (.change (trail-inputs) toggle-trails)
  (.change (symbol-inputs) toggle-symbols)

  (jq/on ($ "#radius") :keyup update-radius)

  (on-toggle-checkboxes ($ "#trails") (trail-inputs))
  (on-toggle-checkboxes ($ "#symbols") (symbol-inputs))
  (u/log "done initializing"))