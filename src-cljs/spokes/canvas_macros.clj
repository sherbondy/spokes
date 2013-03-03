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
  `(let [starting-props# (spokes.canvas/get-ctx-props ~ctx ~prop-map)]
     (spokes.canvas/set-ctx-props! ~ctx ~prop-map)
     (do ~@body)
     (spokes.canvas/set-ctx-props! ~ctx starting-props#)))

(defmacro with-translation [ctx x y & body]
  `(do
     (.save ~ctx)
     (.translate ~ctx ~x ~y)
     (do ~@body)
     (.restore ~ctx)))

(defmacro with-rotation [ctx rot & body]
  `(do
     (.save ~ctx)
     (.rotate ~ctx ~rot)
     (do ~@body)
     (.restore ~ctx)))

(defmacro with-scale [ctx x y & body]
  `(do
     (.save ~ctx)
     (.scale ~ctx ~x ~y)
     (do ~@body)
     (.restore ~ctx)))

(defmacro with-trans-rot-scale
  [ctx [dx dy] angle [scale-x scale-y] & body]
    `(do
      (let [s-cos# (* ~scale-x (spokes.util/cos ~angle))
            s-sin# (* ~scale-y (spokes.util/sin ~angle))]
       (.save ~ctx)
       (.transform ~ctx
                   s-cos#
                   s-sin#
                   (* -1 s-sin#)
                   s-cos#
                   ~dx
                   ~dy)
       (do ~@body)
       (.restore ~ctx))))
