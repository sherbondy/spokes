(ns spokes.main
  (:require [clojure.string :as str]
            [clojure.browser.repl :as repl]
            [jayq.core :as jq :refer [$ css]])
  (:require-macros [jayq.macros :as jm]))

(defn log [& messages]
  (.log js/console (apply str messages)))

;; Tips:
;;> lein cljsbuild auto
;; To compile all cljs files into JavaScript

;;> lein trampoline cljsbuild repl-listen
;; To start up an interactive cljs repl.
;; (once the application is already compiled).
;; Then, from Emacs: M-x nrepl, and specify port 9000.

(defn resize-canvas []
  (let [$canvas ($ "#canvas")
        $doc ($ js/document)]
    (doseq [attr ["height" "width"]]
      (.attr $canvas attr (.attr $doc attr)))))

(defn tlbl [$elem]
  (let [offset (.offset $elem)
        height (.height $elem)
        top (.-top offset)
        left (.-left offset)]
    [[left top]
     [left (+ top height)]]))

(defn x-shift [n [x y]]
  [(+ x n) y])

(jm/ready 
 ;; comment this out in production
 (repl/connect "http://localhost:9000/repl")

 (.resize ($ js/window) resize-canvas)
 (resize-canvas)

 (let [q-pts      (mapcat #(tlbl ($ (str "#" % " h2")))
                          ["who" "what" "when" "where" "why" "how"])
       road-right (map (partial x-shift -32) q-pts)
       road-left  (reverse (map (partial x-shift -112) road-right))
       road-pts   (concat road-right road-left)]
   (log road-pts)

   (let [ctx (.getContext (first ($ "#canvas")) "2d")]
     (set! (.-fillStyle ctx) "rgb(20,20,20)")
     (set! (.-strokeWidth ctx) 4)

     (.beginPath ctx)
     (doseq [[x y] road-pts]
       (.lineTo ctx x y))
     (.closePath ctx)
     (.fill ctx)

     (set! (.-fillStyle ctx) "rgb(255,0,0)")
     (doseq [[x y] road-pts]
       (.beginPath ctx)
       (.arc ctx x y 5 0 (* Math.PI 2))
       (.fill ctx))
     ))
 
 (log "Testing..." "one, two, three"))