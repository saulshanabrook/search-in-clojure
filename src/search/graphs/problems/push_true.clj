(ns search.graphs.problems.push-true
  "Make a push program that returns the true"
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.utils :refer [defnk-fn] :as utils]
            [search.graphs.base.done :as done]
            [search.graphs.base.select :as select]
            [search.graphs.push :as push]))

(defnk-fn genome->traits :- s/Any
  "Whether the returned value is `true`"
  [push-evaluate]
  [genome]
  {:value (if (push-evaluate genome {}) 1 0)})

(def graph
  (g/graph

    (g/instance push/graph {:output-stack :boolean :n-genes 50 :push-bindings []})
    :genome->traits genome->traits
    :trait-specs (utils/v->fnk {:value {:lowest? false}})
    :select (g/instance select/roulette {:trait-key :value})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :value (= 1))})))
