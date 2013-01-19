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
(def ^:dynamic *gps-data* nil)
;; a hash-map keyed by location id
(def markers (atom {}))
;; a set of location ids
(def filtered-data (atom #{}))

(defn gps-item [key]
  (get (:data *gps-data*) key))

(defn marker-hash [loc-id]
  (let [loc-map (gps-item loc-id)
        coords  (lat-lng (:lat loc-map) (:lon loc-map))
        marker  (map-marker *gmap* coords (:desc loc-map))]
    {loc-id marker}))

;; auto add/remove markers from the map when the atom changes
; k r o n = key, reference, old state, new state
(defn data-watcher [k r o n]
  (doseq [key n]
    (if-not (contains? o key)
      (swap! markers assoc key (marker-hash key))))

  (doseq [key o]
    (when-not (contains? n key)
      (.setMap (@markers o) nil)
      (swap! markers dissoc key))))

(defn init-map [$elem & [opts]]
  (set! *gmap* (google.maps.Map. (aget $elem 0)
                                 (or opts default-options)))
  (set! *gps-data* (read-string (.text ($ "#gps-data"))))

  (u/log "adding the watcher..")
  (add-watch filtered-data :data-watcher data-watcher)
  (reset! filtered-data (set (keys (:data *gps-data*)))))

(defn initialize []
  (u/log "initializing")
  (init-map ($ "#map")))