(ns search.graph-utils
  (:require [schema.core :as s]
            [plumbing.core :refer [fnk]]

            [search.utils :as utils]))

(defn println-level
  "Like println but indents by `indent` first."
  [indent & messages]
  (apply
    println
    (concat (repeat (* 2 indent) "")
            messages)))

(s/defn log-wrap :- utils/Graph
  "Modify graph spec g, producing a new graph spec.

  If any node produces a function, it wraps that function to log when
  it is called."
  [g :- utils/Graph]
  (let [indent (atom 0)]
    (utils/map-leaf-fns
      (fnk log-wrap-inner [f ks]
        (-> f
          (utils/wrap-before (fn [& _] (println-level @indent "Calling" ks) (swap! indent inc)))
          (utils/wrap-after (fn [_] (swap! indent dec) (println-level @indent "Finished" ks)))))
      g)))
