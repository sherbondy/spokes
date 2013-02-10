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
(comment
  (do
    (use '[cljs.repl.browser :only [repl-env]])
    (def brepl (repl-env :port 9000))
    (cemerick.piggieback/cljs-repl :repl-env (doto brepl cljs.repl/-setup)))
  )

;; (repl/connect "http://localhost:9000/repl")

;; Tips (for the terminal):
;;> lein cljsbuild auto
;; To compile all cljs files into JavaScript

;;> lein trampoline cljsbuild repl-listen
;; To start up an interactive cljs repl (once the script is compiled).
;; But piggieback (nREPL) works better

(defn fit [$elem $doc]
  (u/log "resizing canvas")
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

(def two-pi (* 2 Math/PI))

;; normal dist probability function for clouds
(defn pdf [x mean sigma]
  (/ (Math/pow Math/E (/ (* -1 (Math/pow (- x mean) 2))
                         (* 2  (Math/pow sigma 2))))
     (* sigma (Math/sqrt two-pi))))

;; for clouds, define 2 normal distributions
;; with means at x = width/2
;; and y = height
;; sample randomly within boundaries to draw circles

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

(defn draw-circle [ctx x y r]
  (.arc ctx x y (* r 2) 0 two-pi true))

(defn draw-wheel [ctx x y r]
  (cm/with-path ctx
    (draw-circle ctx x y r)
    (.stroke ctx)
    (.fill ctx)))

(defn draw-wheels [ctx x y w h]
  (u/log "drawing wheels now")
  (let [r        45
        x-offset 10
        x1       (+ x x-offset)
        x2       (+ x w (* -1 x-offset))
        wheel-y  (+ y h (* -1 r))]
    (cm/with-ctx-props ctx {:line-width 20 :fill-style "rgb(255,255,255)"}
      (draw-wheel ctx x1 wheel-y r)
      (draw-wheel ctx x2 wheel-y r))))

(defn draw-bike-frame [ctx canvas pre-draw-fn]
  (let [bike-img (js/Image.)]
    (set! (.-onload bike-img)
          (fn []
            (let [[x y w h] (center-xy bike-img canvas)
                  y (+ y 52)]
              (pre-draw-fn ctx x y w h)
              (.drawImage ctx bike-img x y)
              (u/log "Drew bike frame"))))
    (set! (.-src bike-img) "/img/bike-frame.png")))

(defn draw-bike [ctx canvas]
  (draw-bike-frame ctx canvas draw-wheels))

(defn get-ctx [canvas]
  (.getContext canvas "2d"))

(defn draw-scene [canvas]
  (let [ctx (get-ctx canvas)]
    (draw-bike ctx canvas)))

;; (draw-scene (aget ($ "#canvas") 0))

(defn toggle-bio [e]
  (this-as this
           (let [$this ($ this)]
             (.preventDefault e)
             (jq/hide ($ "#bios div"))
             (jq/show ($ (jq/attr $this "href")))
             (.removeClass ($ "#team a") "active")
             (.addClass $this "active"))))

(defn draw-cloud [$elem r]
  (let [canvas   (aget $elem 0)
        ctx      (get-ctx canvas)
        w        (.width $elem)
        h        (.height $elem)
        min-val  (* 2 r)
        x-sigma  (* w 0.5)
        x-mean   (+ x-sigma min-val)
        y-sigma  (* h 0.25)
        y-mean   (+ (* h 0.5) min-val)
        cutoff-m 0.15
        x-cutoff (* cutoff-m (/ x-sigma w))
        y-cutoff (* cutoff-m (/ y-sigma h))]
    (cm/with-ctx-props ctx {:fill-style "rgba(255,255,255,0.5)"}
      (doseq [x (range min-val (+ w (* 4 min-val)))
              y (range min-val h)]
        (let [xp    (pdf x x-mean x-sigma)
              yp    (pdf y y-mean y-sigma)
              draw? (and (< (rand x-cutoff) xp)
                         (< (rand y-cutoff) yp))]
          (when draw?
            (cm/with-path ctx
              (draw-circle ctx x y r)
              (.fill ctx))))))
    (u/log "done rendering cloud")))

(jm/ready
 (let [$canvas ($ "#canvas")
       $header ($ "#header")
       canvas  (aget $canvas 0)
       header  (aget $header 0)
       fit-canvas-fn #(do (fit $canvas $header)
                          (draw-scene canvas))]
   (.resize ($ js/window) fit-canvas-fn)
   (fit-canvas-fn))

 (jq/on ($ "#team") :click "a" toggle-bio)

 (let [$logo-canvas ($ "#logo canvas")]
   (draw-cloud $logo-canvas 6))

 (when (u/exists? "#map")
   (u/log "Initializing the map..")
   (sm/initialize)))