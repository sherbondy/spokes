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

(def default-options
  (clj->js {:center (google.maps.LatLng. 40 -95)
            :zoom 4
            :mapTypeId google.maps.MapTypeId.ROADMAP
            :scrollwheel false}))


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

(defn gps-data []
  (:data @gps-response))

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

;; auto add/remove markers from the map when the atom changes
; k r o n = key, reference, old state, new state
(defn data-watcher [k r o n]
  (doseq [key n]
    (if-not (contains? o key)
      (let [marker (get @markers key)]
        (.setMap marker @gmap))))

  ;; never delete markers, just take them off the map
  (doseq [key (difference o n)]
    (.setMap (get @markers key) nil)))


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


(defn init-data [route-data]
  (reset! gps-response route-data)

  ;; initialize the symbol and trail sets
  (add-watch filtered-data :data data-watcher)
  (doseq [[kw atm] {:symbols symbols :trails trails
                    :radius  radius  :dists  distances}]
    (add-watch atm kw filter-watcher))

  (reset! markers (apply merge (for [[k _] (gps-data)]
                                 {k (make-spokes-marker k)})))

  (reset! filtered-data (set (keys (gps-data)))))

(defn init-map [$elem & [opts]]
  (reset! gmap (google.maps.Map. (aget $elem 0)
                                 (or opts default-options))))

(defn initialize []
  (init-map ($ "#map"))

  (let [route-data (read-string (.text ($ "#gps-data")))]
    (init-data route-data))

  (u/log "done initializing"))