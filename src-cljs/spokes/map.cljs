(ns spokes.map
  (:require [cljs.reader :refer [read-string]]
            [clojure.string :as str]
            [jayq.core :as jq]
            [jayq.core :refer [$]]
            [jayq.util :as util]
            [spokes.util :as u])

  (:require-macros [jayq.macros :as jm]))

(defn make-marker [options]
  (google.maps.Marker. (clj->js options)))

(defn map-marker [map coords title & [opts]]
  (make-marker (merge opts
                      {:position coords 
                       :map map
                       :title title})))

(defn lat-lng [lat lng]
  (google.maps.LatLng. (js/parseFloat lat) (js/parseFloat lng)))

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
            :mapTypeId google.maps.MapTypeId.ROADMAP}))



(def ^:dynamic *gmap* nil)
(def ^:dynamic *gps-response* nil)
;; a hash-map keyed by location id
(def markers (atom {}))
;; Waypoint, Campground, etc, toggle-able
(def symbols (atom #{}))
;; Trails, toggle-able
(def trails (atom #{}))
;; a set of location ids
(def filtered-data (atom #{}))
;; the user's geolocation
(def user-geo (atom {:lat nil :lng nil}))

(defn gps-data [] (:data *gps-response*))

(defn all-locations []
  (vals (gps-data)))

(defn gps-item [key]
  (get (gps-data) key))

(defn icon-for-trail [trail]
  (str "/img/" (condp = trail
                 "WE" "blue_MarkerW"
                 "TA" "orange_MarkerT"
                 "AC" "darkgreen_MarkerA") ".png"))

(defn marker-val [loc-id]
  (let [loc-map (gps-item loc-id)
        coords  (lat-lng (:lat loc-map) (:lon loc-map))
        marker  (map-marker *gmap* coords (:desc loc-map)
                            {:icon (icon-for-trail (:trail loc-map))})]
    marker))

;; auto add/remove markers from the map when the atom changes
; k r o n = key, reference, old state, new state
(defn data-watcher [k r o n]
  (doseq [key n]
    (if-not (contains? o key)
      (swap! markers assoc key (marker-val key))))

  (doseq [key o]
    (when-not (contains? n key)
      (.setMap (@markers key) nil)
      (swap! markers dissoc key))))

(defn filter-fn [[k v]]
  (let [trail-set  @trails
        symbol-set @symbols]
    (and (contains? trail-set  (:trail v))
         (contains? symbol-set (:sym v)))))

(defn filter-watcher [k r o n]
  (reset! filtered-data
          (set (map first (filter filter-fn (gps-data))))))

(defn geo-watcher [k r o n])

(defn checked-set
  "A set of checked input element values"
  [inputs]
  (set (map jq/val (vec (.filter inputs ":checked")))))

(defn trail-inputs [] ($ "#trails input"))
(defn toggle-trails []
  (reset! trails (checked-set (trail-inputs))))

(defn symbol-inputs [] ($ "#symbols input"))
(defn toggle-symbols []
  (reset! symbols (checked-set (symbol-inputs))))

(defn init-map [$elem & [opts]]
  (set! *gmap* (google.maps.Map. (aget $elem 0)
                                 (or opts default-options)))
  (set! *gps-response* (read-string (.text ($ "#gps-data"))))

  (reset! symbols (checked-set (symbol-inputs)))
  (reset! trails (checked-set (trail-inputs)))

  (u/log "adding the watchers...")
  (add-watch filtered-data :data-watcher data-watcher)
  (add-watch symbols :symbols-watcher filter-watcher)
  (add-watch trails :trails-watcher filter-watcher)
  (add-watch user-geo :geo-watcher geo-watcher)

  (reset! filtered-data (set (keys (gps-data)))))

(def geolocation (.-geolocation js/navigator))

(defn get-user-location []
  (if geolocation
    (.getCurrentPosition geolocation
      (fn [pos]
        (let [coords (.-coords pos)]
          (u/log coords)
          (reset! user-geo {:lat (.-longitude coords)
                            :lon (.-latitude coords)}))))))

(defn initialize []
  (init-map ($ "#map"))

  (get-user-location)

  (.change (trail-inputs) toggle-trails)
  (.change (symbol-inputs) toggle-symbols))


;; in mi
(def r-earth 3959.0)

(defn law-of-cos [lat1 lon1 lat2 lon2]
  (* r-earth
     (Math/acos (+ (* (Math/sin lat1) (Math/sin lat2))
                   (* (Math/cos lat1) (Math/cos lat2)
                      (Math/cos (- lon2 lon1)))))))

;; sym = the type of resource, eg :Bathroom
(defn nearby-resource [sym lat lon mi-radius]
  (filter #(and (= sym (:sym %))
                (> mi-radius
                   (law-of-cos lat lon (:lat-r %) (:lon-r %))))
          all-locations))

;; (count (nearby-resource "Lodging" (Math/toRadians 37.775) (Math/toRadians -122.418) 10))

(defn filter-sym [sym] 
  (filter #(= sym (:sym %)) all-locations))

;; (def bathrooms (filter-sym "Restroom"))
;; (count bathrooms)
;; (def campgrounds (filter-sym "Campground"))
;; (count campgrounds)