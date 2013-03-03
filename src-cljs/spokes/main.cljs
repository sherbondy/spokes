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

(defn now []
  (.getTime (js/Date.)))

;; in pixels
(def wheel-radius 90)
;; typical bike wheel has a 0.6m radius 
(def m-to-px (/ 90 0.6))
(def time (atom (now)))
;; cannot draw until we've loaded all resources
(def ready-to-draw (atom false))
;; wheel rotation in rads
(def wheel-rot (atom 0))

;; assume speed is 20 km / hour
;; speed in m/s
(def speed (/ (* 1000 20) (* 60 60)))
(def two-pi (* 2 Math/PI))
(def rad-per-ms (/ (* wheel-radius speed) 
                   (* wheel-radius 1000)))

(defn +clamp [val added max]
  (let [new-val (+ val added)]
    (mod new-val max)))

(add-watch time :wheel-rotation 
  (fn [k r ov nv]
    ;; time delta in ms
    (let [dt   (- nv ov)
          drad (* dt rad-per-ms)]
      (swap! wheel-rot +clamp drad two-pi))))

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

;; normal dist probability function for clouds
(defn pdf [x mean sigma]
  (/ (Math/pow Math/E (/ (* -1 (Math/pow (- x mean) 2))
                         (* 2  (Math/pow sigma 2))))
     (* sigma (Math/sqrt two-pi))))

;; for clouds, define 2 normal distributions
;; with means at x = width/2
;; and y = height
;; sample kandomly within boundaries to draw circles

(defn wh [elem]
  [(.-width elem)
   (.-height elem)])

(defn centered-xy 
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
  (.arc ctx x y r 0 two-pi true))

(defn draw-wheel [ctx r n-spokes]
  (cm/with-path ctx
    (draw-circle ctx 0 0 r)
    (.clearRect ctx (* -1 r) (* -1 r) (* 2 r) (* 2 r))
    (.stroke ctx)

    (cm/with-ctx-props ctx {:line-width 8}
      (dotimes [n n-spokes]
        (let [angle (* 2 n (/ Math/PI n-spokes))]
          (.moveTo ctx 0 0)
          (.lineTo ctx 
                   (* r (Math/sin angle))
                   (* r (Math/cos angle)))
          (.stroke ctx))))))

(defn draw-wheels [ctx w h]
  (let [r        wheel-radius
        x-offset 10
        x1       x-offset
        x2       (+ w (* -1 x-offset))
        wheel-y  (+ h (* -0.5 r))
        rotation @wheel-rot]
    (cm/with-ctx-props ctx {:line-width 20}
      (cm/with-translation ctx x1 wheel-y
        (cm/with-rotation ctx rotation
          (draw-wheel ctx r 8)))
      (cm/with-translation ctx x2 wheel-y
        (cm/with-rotation ctx rotation
          (draw-wheel ctx r 8))))))

(def bike-img (js/Image.))

(defn load-bike-img []
  (set! (.-src bike-img) "/img/bike-frame.png")
  (set! (.-onload bike-img)
        (reset! ready-to-draw true)))

(defn draw-frame [ctx w h]
  (.drawImage ctx bike-img 0 0))

(defn draw-bike [ctx canvas]
  (let [[x y w h] (centered-xy bike-img canvas)
        y (+ y 52)]
    (cm/with-translation ctx x y
      (.clearRect ctx 0 0 w h)
      (draw-wheels ctx w h)
      (draw-frame ctx w h))))

(defn get-ctx [canvas]
  (.getContext canvas "2d"))

(defn draw-scene [canvas]
  (if @ready-to-draw
    (let [ctx (get-ctx canvas)]
      (draw-bike ctx canvas))))

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
              (.fill ctx))))))))

(defn redraw-canvas-fn [$canvas]
  (let [canvas (aget $canvas 0)]
    (fn redraw []
      (js/requestAnimationFrame redraw)
      (reset! time (now))
      (draw-scene canvas))))

(jm/ready
  (when (u/exists? "#canvas")
    (let [$canvas 	 ($ "#canvas")
          $header   ($ "#header")
          canvas 	 (aget $canvas 0)
          redraw-fn (redraw-canvas-fn $canvas)
          resize-fn #(fit $canvas $header)]
      (load-bike-img)
      (redraw-fn)
      (resize-fn)
      (.resize ($ js/window) resize-fn))

 (jq/on ($ "#team") :click "a" toggle-bio)
        
 ;; should NOT have separate canvas for logo
 (let [$logo-canvas ($ "#logo canvas")]
   (draw-cloud $logo-canvas 8)))   

 (when (u/exists? "#map")
   (u/log "Initializing the map..")
   (sm/initialize)))