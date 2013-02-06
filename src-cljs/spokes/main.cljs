(ns spokes.main
  (:require [clojure.string :as str]
            [clojure.browser.repl :as repl]
            [jayq.core :as jq :refer [$ css]]
            [spokes.map :as sm]
            [spokes.util :as u])
  (:require-macros [jayq.macros :as jm]
                   [spokes.canvas-macros :as cm]))

;; Eval these (in clj land) for interactive development!

;; for rhino (server-side js, mainly for testing):
;; (cemerick.piggieback/cljs-repl)

;; for browser repl:

;;  (use '[cljs.repl.browser :only [repl-env]])
;;  (def brepl (repl-env :port 9000))
;;  (cemerick.piggieback/cljs-repl :repl-env (doto brepl cljs.repl/-setup))

(repl/connect "http://localhost:9000/repl")

(defn log [& messages]
  (.log js/console (apply str messages)))


;; Tips (for the terminal):
;;> lein cljsbuild auto
;; To compile all cljs files into JavaScript

;;> lein trampoline cljsbuild repl-listen
;; To start up an interactive cljs repl (once the script is compiled).
;; But piggieback (nREPL) works better

(defn fit [$elem $doc]
  (log "resizing canvas")
  (.attr $elem "width"  (.width $doc))
  (.attr $elem "height" (.height $doc)))

(defn bounding-box [$elem]
  (let [offset (.offset $elem)
        height (.height $elem)
        width  (.width $elem)
        top    (.-top offset)
        left   (.-left offset)
        right  (+ left width)
        bottom (+ top height)]
    [[left top]     [right top]
     [right bottom] [left bottom]]))

(defn tlbl [$elem]
  (let [[tl _ _ bl] (bounding-box $elem)]
    [tl bl]))

(defn x-shift [n [x y]]
  [(+ x n) y])

(defn camel-name
  "Convert :fill-style to \"fillStyle\""
  [kw]
  (let [nom (name kw)
        split-nom (str/split nom #"\-")]
    (apply str (cons (first split-nom) 
                     (map str/capitalize (rest split-nom))))))

(defn set-ctx-props! [ctx prop-map]
  (doseq [[attr value] prop-map]
    (aset ctx (camel-name attr) value)))

(defn get-ctx-props [ctx props]
  (cond
   (map? props)     (get-ctx-props ctx (keys props))
   (keyword? props) (get-ctx-props ctx [props])
   :else (into {} (map (fn [prop] 
                         [prop (aget ctx (camel-name prop))])
                       props))))

(defn wh [elem]
  [(.-width elem)
   (.-height elem)])

(defn center-xy 
  "Return (top left) x, y, width, and height of child centered inside parent.
   Does not actually relocate child."
  [child parent]
  (let [[cw ch] (wh child)
        [pw ph] (wh parent)]
    [(/ (- pw cw) 2)
     (/ (- ph cw) 2)
     cw
     ch]))

(defn draw-bike-frame [ctx canvas next-draw-fn]
  (let [bike-img (js/Image.)]
    (set! (.-onload bike-img)
          (fn []
            (let [[x y w h] (center-xy bike-img canvas)]
              (next-draw-fn ctx x y w h)
              (.drawImage ctx bike-img x y)
              (log "Drew bike frame"))))
    (set! (.-src bike-img) "/img/bike-frame.png")))

(defn draw-wheel [ctx x y d]
  (let [r (/ d 2)]
    (cm/with-path ctx
      (.arc ctx x y d 0 (* 2 Math/PI) true)
      (.stroke ctx)
      (.fill ctx))))

(defn draw-wheels [ctx x y w h]
  (log "drawing wheels now")
  (let [d  90
        x1 (+ x 10)
        x2 (+ x 350)
        wheel-y (+ y h (* -1 (/ d 2)))]
    (cm/with-ctx-props ctx {:line-width 20 :fill-style "rgb(255,255,255)"}
      (draw-wheel ctx x1 wheel-y d)
      (draw-wheel ctx x2 wheel-y d))))

(defn draw-bike [ctx canvas]
  (draw-bike-frame ctx canvas draw-wheels))

(defn get-ctx [canvas]
  (.getContext canvas "2d"))

(defn draw-scene [canvas]
  (let [ctx (get-ctx canvas)]
    (draw-bike ctx canvas)))

;; (draw-scene (aget ($ "#canvas") 0))

(jm/ready
 ;; comment this out in production

 (comment)
 (let [$canvas ($ "#canvas")
       $header ($ "#header")
       canvas  (aget $canvas 0)
       header  (aget $header 0)
       fit-canvas-fn #(do (fit $canvas $header)
                          (draw-scene canvas))]
   (.resize ($ js/window) fit-canvas-fn)
   (fit-canvas-fn))

 (when (u/exists? "#map")
   (u/log "Initializing the map..")
   (sm/initialize)))