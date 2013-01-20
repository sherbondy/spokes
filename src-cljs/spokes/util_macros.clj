(ns spokes.util-macros)

;; handle the boilerplate of preventing the
;; default event action
(defmacro click-fn [[e] & body]
  `(fn [~e]
     (.preventDefault ~e)
     ~@body))
