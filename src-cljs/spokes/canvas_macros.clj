(ns spokes.canvas-macros)

(defmacro with-path [ctx & body]
  `(let [ctx# ~ctx]
     (.beginPath ctx#)
     (do ~@body)
     (.closePath ctx#)))

(defmacro with-ctx-props
  "Make canvas more functional by encouraging
   temporary pushing of properties followed
   by reverting to previous state."
  [ctx prop-map & body]
  `(let [starting-props# (spokes.main/get-ctx-props ~ctx ~prop-map)]
     (spokes.main/set-ctx-props! ~ctx ~prop-map)
     (do ~@body)
     (spokes.main/set-ctx-props! ~ctx starting-props#)))