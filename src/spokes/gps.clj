(ns spokes.gps
  (:use [clojure.data.zip.xml :only [attr seq-test text text= xml->]])
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]))

(def we1 (xml/parse "resources/gps/WE01V008.gpx"))
(def we1z (zip/xml-zip we1))

(def all-waypoints
  (xml-> we1z :wpt))

(defn tag-data [wpt tagname]
  (first (xml-> wpt tagname text)))

(defn useful-info [wpt]
  {:desc (tag-data wpt :desc)
   :sym  (tag-data wpt :sym)
   :lat  (attr wpt :lat)
   :lon  (attr wpt :lon)})

(useful-info (first all-waypoints))

(set (map (comp :sym useful-info) all-waypoints))

(first true-waypoints)