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

(defn fit-document [$elem]
  (log "resizing canvas")
  (let [$doc ($ js/document)]
    (doseq [attr ["height" "width"]]
      (.attr $elem attr 0) ;; elem could impact document size
      (.attr $elem attr (.attr $doc attr)))))

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

(defn draw-scene [$canvas]
  )

(jm/ready
 ;; comment this out in production

 (comment
   (let [$canvas ($ "#canvas")
         fit-canvas-fn #(do (fit-document $canvas)
                            (draw-scene $canvas))]
     (.resize ($ js/window) fit-canvas-fn)
     (fit-canvas-fn)))

 (when (u/exists? "#map")
   (u/log "Initializing the map..")
   (sm/initialize)))