(ns spokes.main
	(:use [spokes.util :only [Tau e]])
  (:require [clojure.string :as str]
            [clojure.browser.repl :as repl]
            [jayq.core :as jq :refer [$ css]]
            [spokes.canvas :as c]
            [spokes.map :as sm]
            [spokes.util :as u])
  (:require-macros [jayq.macros :as jm]
                   [spokes.canvas-macros :as cm]))

;; Tips (for the terminal):
;;> lein cljsbuild auto
;; To compile all cljs files into JavaScript

;; in pixels
(def wheel-radius 90)
;; typical bike wheel has a 0.6m radius 
(def m-to-px (/ 90 0.6))
(def time (atom (u/now)))
;; cannot draw until we've loaded all resources
(def ready-to-draw (atom false))
;; wheel rotation in rads
(def wheel-rot (atom 0))

;; assume speed is 20 km / hour
;; speed in m/s
(def bike-speed (/ (* 1000 20) (* 60 60)))
(def rad-per-ms (/ (* wheel-radius bike-speed) 
                   (* wheel-radius 1000)))

(add-watch time :wheel-rotation 
  (fn [k r ov nv]
    ;; time delta in ms
    (let [dt   (- nv ov)
          drad (* dt rad-per-ms)]
      (swap! wheel-rot u/+clamp drad Tau))))


;; normal dist probability function for clouds
(defn pdf [x mean sigma]
  (/ (Math/pow e (/ (* -1 (Math/pow (- x mean) 2))
                    (* 2  (Math/pow sigma 2))))
     (* sigma (Math/sqrt Tau))))

;; for clouds, define 2 normal distributions
;; with means at x = width/2
;; and y = height
;; sample kandomly within boundaries to draw circles


(defn draw-wheel [ctx r n-spokes]
  (cm/with-path ctx
    (c/draw-circle ctx 0 0 r)
    (.stroke ctx)

    (cm/with-ctx-props ctx {:line-width 8}
      (dotimes [n n-spokes]
        (let [angle (* n (/ Tau n-spokes))]
          (.moveTo ctx 0 0)
          (cm/with-rotation ctx angle
            (.lineTo ctx 0 r))
          (.stroke ctx))))))

(defn draw-wheels [ctx w h]
  (let [r        wheel-radius
        n-spokes 8
        x-offset 10
        x1       x-offset
        x2       (- w x-offset)
        y        (- h (* 0.5 r))
        rotation @wheel-rot]
    (cm/with-ctx-props ctx {:line-width 20}
      (cm/with-trans-rot-scale ctx [x1 y] rotation [1 1]
        (draw-wheel ctx r n-spokes))
      (cm/with-trans-rot-scale ctx [x2 y] rotation [1 1]
        (draw-wheel ctx r n-spokes)))))

(def bike-img (js/Image.))

(defn load-bike-img []
  (set! (.-src bike-img) "/img/bike-frame.png")
  (set! (.-onload bike-img)
        (reset! ready-to-draw true)))

(defn draw-frame [ctx w h]
  (.drawImage ctx bike-img 0 0))

(defn draw-pedal [ctx pedal-w pedal-h]
  (let [half-w (* pedal-w 0.5)]
    (cm/with-ctx-props ctx {:line-width pedal-h}
      (.moveTo ctx (* -1 half-w) 0)
      (.lineTo ctx half-w 0))))

(defn draw-crank [ctx crank-w crank-r x y]
  (let [pedal-w 40
        pedal-h crank-w
        rot     @wheel-rot
        neg-rot (* -1 rot)]
    (cm/with-path ctx
      (cm/with-ctx-props ctx {:line-width crank-w}
        (cm/with-trans-rot-scale ctx [x y] rot [1 1]
                                 
          (cm/with-trans-rot-scale ctx 
            [0 crank-r] neg-rot [1 1]
            (draw-pedal ctx pedal-w pedal-h))
                                 
          (cm/with-trans-rot-scale ctx
            [0 (* -1 crank-r)] neg-rot [1 1]
            (draw-pedal ctx pedal-w pedal-h))
                                 
          (.moveTo ctx 0 (* -1 crank-r))
          (.lineTo ctx 0 crank-r)
          (.stroke ctx))))))

(defn draw-pedals [ctx w h]
  (let [crank-w 20
        crank-r 40
        x       (- (/ w 2) (* crank-w 0.75))
        y       (- h (* 0.5 crank-r))]
    (draw-crank ctx crank-w crank-r x y)))

(defn draw-bike [ctx canvas w h]
  (let [y (rand-int 2)]
    (cm/with-translation ctx 0 y
      (draw-wheels ctx w h)
      (draw-frame ctx w h)
      (draw-pedals ctx w h))))

(defn draw-scene [canvas]
  (if @ready-to-draw
    (let [ctx         (c/get-ctx canvas)
          [w h]       (c/wh canvas)
          [bw bh]     (c/wh bike-img)
          w-frac      (/ bw w)
          max-w-frac  0.4
          scale       (min (/ max-w-frac w-frac) 1)
          scaled-bw   (* scale bw)
          scaled-bh   (* scale bh)
          [tx ty _ _] (c/calc-center scaled-bw scaled-bh w h)
          ty          (+ ty 50)]
      (.clearRect ctx 0 0 w h)
      (cm/with-translation ctx tx ty
        (cm/with-scale ctx scale scale
          (draw-bike ctx canvas bw bh))))))

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
        ctx      (c/get-ctx canvas)
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
              (c/draw-circle ctx x y r)
              (.fill ctx))))))))

(defn redraw-canvas-fn [$canvas]
  (let [canvas (aget $canvas 0)]
    (fn redraw []
      (js/requestAnimationFrame redraw)
      (reset! time (u/now))
      (draw-scene canvas))))

(jm/ready
  (when (u/exists? "#canvas")
    (let [$canvas 	 ($ "#canvas")
          $header   ($ "#header")
          canvas 	 (aget $canvas 0)
          redraw-fn (redraw-canvas-fn $canvas)
          resize-fn #(c/fit $canvas $header)]
      (load-bike-img)
      (redraw-fn)
      (resize-fn)
      (.resize ($ js/window) resize-fn))

 (jq/on ($ "#team") :click "a" toggle-bio)
        
 ;; should NOT have separate canvas for logo
 (let [$logo-canvas ($ "#logo canvas")]
   (draw-cloud $logo-canvas 7)))

 (when (u/exists? "#map")
   (u/log "Initializing the map..")
   (sm/initialize)))