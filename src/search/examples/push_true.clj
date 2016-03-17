(ns search.examples.push-true
  "Make a push program that returns the true"
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [clj-fuzzy.metrics :as metrics]

            [push.interpreter.templates.classic :refer [classic-interpreter]]
            [push.types.module.random-scalars :refer [random-scalars-module]]
            [push.interpreter.core :refer [register-modules]]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]
            [search.algorithms.base.done :as done]
            [search.algorithms.base.evaluate :as evaluate]
            [search.algorithms.base.select :as select]
            [search.algorithms.push :as push]))

(defnk-fn genome->traits :- s/Any
  "Whether the returned value is `true`"
  [push-evaluate]
  [genome]
  {:value (if (push-evaluate genome {}) 1 0)})

(def graph
  (g/graph
    (g/instance push/graph {:output-stack :boolean})
    :genome->traits genome->traits
    :select (g/instance select/roulette {:trait-name :value
                                         :lowest? false})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :value (= 1))})))
