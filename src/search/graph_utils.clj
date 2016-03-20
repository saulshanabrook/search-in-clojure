(ns search.graph-utils
  (:require [schema.core :as s]
            [plumbing.core :refer [fnk]]
            [taoensso.timbre.profiling :as profiling]

            [search.utils :as utils]
            [search.core :as search]))

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

(def profile-fns-wrap
  "Add timbre profile to each function"
  (partial utils/map-leaf-fns
    (fnk profile-wrap-inner [f ks]
      (utils/wrap f (fn [& args] (profiling/p (str ks) (apply f args)))))))

(s/defn print-profile-gen-wrap :- search/SearchGraph
  "Prints the total profiling after each generation"
  [{->gens :generations :as g} :- search/SearchGraph]
  (assoc g :generations
    (utils/wrap ->gens
      (fn [m]
        (let [f (fn profile-fns-inner [gens]
                  (let [first-g (profiling/profile :info :Generation (first gens))]
                    (if (nil? first-g)
                      nil
                      (lazy-seq (cons first-g
                                      (profile-fns-inner (rest gens)))))))]
          (f (->gens m)))))))
