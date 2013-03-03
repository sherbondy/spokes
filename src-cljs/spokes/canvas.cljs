(ns spokes.canvas
  (:use [spokes.util :only [Tau]])
  (:require [spokes.util :as u]))

(defn fit [$elem $container]
  (.attr $elem "width"  (.width $container))
  (.attr $elem "height" (.height $container)))

(defn wh [elem]
  [(.-width elem)
   (.-height elem)])

(defn bounding-box [$elem]
  (let [elem     (aget $elem 0)
        offset   (.offset $elem)
        [width 
         height] (wh elem)
        top      (.-top offset)
        left     (.-left offset)
        right    (+ left width)
        bottom   (+ top height)]
    [[left top]     [right top]
     [right bottom] [left bottom]]))

(defn tlbl [$elem]
  (let [[tl _ _ bl] (bounding-box $elem)]
    [tl bl]))

(defn x-shift [n [x y]]
  [(+ x n) y])

(defn get-ctx [canvas]
  (.getContext canvas "2d"))

(defn set-ctx-props! [ctx prop-map]
  (doseq [[attr value] prop-map]
    (aset ctx (u/camel-name attr) value)))

(defn get-ctx-props [ctx props]
  (cond
   (map? props)     (get-ctx-props ctx (keys props))
   (keyword? props) (get-ctx-props ctx [props])
   :else (into {} (map (fn [prop] 
                         [prop (aget ctx (u/camel-name prop))])
                       props))))

(defn calc-center
  "Return (top left) x, y, width, and height of child centered inside parent.
   Does not actually relocate child."
  ([cw ch pw ph]
    [(/ (- pw cw) 2)
     (/ (- ph cw) 2)
     cw
     ch])
  
  ([child parent]
    (let [[cw ch] (wh child)
          [pw ph] (wh parent)]
      (calc-center cw ch pw ph))))
  

;; (calc-center 10 20 100 300)

(defn draw-circle [ctx x y r]
  (.arc ctx x y r 0 Tau true))
