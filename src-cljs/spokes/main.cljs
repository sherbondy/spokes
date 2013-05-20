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

(defn to-days [ms]
  (js/Math.floor (/ ms (* 1000 60 60 24))))

;; javascript's month is 0-indexed :(
(def start-date (.getTime (js/Date. 2013 5 10)))
(def days-left (to-days (- start-date (u/now))))


(defn toggle-bio [e]
  (this-as this
    (let [$this ($ this)]
      (.preventDefault e)
      (.addClass ($ "#bios div") "hidden")
      (.removeClass ($ (jq/attr $this "href")) "hidden")
      (.removeClass ($ "#team a") "active")
      (.addClass $this "active"))))

(jm/ready

 (jq/on ($ "#team") :click "a" toggle-bio)

 (jq/on ($ "#play") :click
        (fn [e]
          (.preventDefault e)
          (js/alert "The video will be up in a few days!")))

 (when (u/exists? "#video")
   (let [$window ($ js/window)]
     (.scroll $window
       (fn []
         (let [scroll-y (.scrollTop $window)
               vid-y    (.-top (.offset ($ "#video")))
               $fixed   ($ "#fixed")]
           (if (> scroll-y vid-y)
             (.addClass $fixed "visible")
             (.removeClass $fixed "visible")))))))

 (when (u/exists? "#days-left")
   (jq/text ($ "#days-left") days-left))

 (when (u/exists? "#map")
   (sm/initialize)))