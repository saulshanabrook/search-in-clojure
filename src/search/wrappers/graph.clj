(ns search.wrappers.graph
  (:require [schema.core :as s]
            [plumbing.core :refer [fnk]]
            [taoensso.timbre.profiling :as profiling]

            [search.utils :as utils]
            [search.schemas :as schemas]))

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
  it is called.

  This is useful when debugging where your application is spending time."
  [g :- utils/Graph]
  (let [indent (atom 0)]
    (utils/map-leaf-fns
      (fnk log-wrap-inner [f ks]
        (-> f
          (utils/wrap-before (fn [& _] (println-level @indent "Calling" ks) (swap! indent inc)))
          (utils/wrap-after (fn [_] (swap! indent dec) (println-level @indent "Finished" ks)))))
      g)))


(s/defn profile-wrap :- utils/Graph
  "Print timbre profiling after each graph key is generated"
  [g :- utils/Graph]
  (plumbing.map/map-leaves-and-path
    (fn [ks f]
      (utils/wrap
        f
        (fn [& args]
          (profiling/profile :info :Graph
            (profiling/p (str ks) (apply f args))))))
    g))

(defn wrap-fn-profile
  [label f]
  (utils/wrap f (fn [& args] (profiling/p label (apply f args)))))

(def profile-fns-wrap
  "Add timbre profile to each function"
  (partial utils/map-leaf-fns
    (fnk profile-wrap-inner [ks f]
      (wrap-fn-profile (str ks :inner) f))))

(s/defn print-profile-gen-wrap :- schemas/SearchGraph
  "Prints the total profiling after each generation"
  [{->gens :generations :as g} :- schemas/SearchGraph]
  (assoc g :generations
    (utils/wrap ->gens
      (fn [m]
        (let [f (fn profile-fns-inner [gens]
                  (let [first-g (profiling/profile :info :Generation (first gens))]
                    (when-not (nil? first-g)
                      (lazy-seq (cons first-g
                                      (profile-fns-inner (rest gens)))))))]
          (f (->gens m)))))))
