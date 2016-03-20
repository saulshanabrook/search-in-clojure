(ns search.graphs.problems.push-sr-linear
  "Make a push program that performs linear symbolic regression"
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [push.interpreter.templates.one-with-everything :refer [make-everything-interpreter]]


            [search.utils :refer [defnk-fn] :as utils]
            [search.graphs.base.done :as done]
            [search.graphs.base.select :as select]
            [search.graphs.testcases :as testcases]
            [search.graphs.push :as push]))

(def x (range 0 20))
; y = 2x
(def y (map #(* 2 %) x))

(defnk-fn test-fn :- (s/maybe s/Int)
  "Evaluates push with the input set to the `:x` binding"
  [push-evaluate]
  [genome x]
  (push-evaluate genome {:x x}))

(def graph
  (g/graph
    :output-stack (utils/v->fnk :integer)
    :n-genes (utils/v->fnk 10)
    (assoc push/graph
      :interpreter (utils/v->fnk (make-everything-interpreter :bindings {:x nil}))
      :tweak-weights (utils/v->fnk {:two-point-crossover 1 :mutate 1})
      :mutate-p (utils/v->fnk 0.5))
    :test-fn test-fn
    :test-cases (utils/v->fnk (zipmap x y))
    testcases/graph
    :trait-specs (utils/v->fnk (zipmap x (repeat {:lowest? true})))
    :select select/lexicase
    :done? (g/instance done/any-trait {:traits->done? #(->> % vals (every? (partial = 0)))})))
