(ns search.examples.push-saul
  "Make a push program that returns the string `saul`"
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [clj-fuzzy.metrics :as metrics]

            [push.interpreter.templates.classic :refer [classic-interpreter]]
            [push.types.module.random-scalars :refer [random-scalars-module]]
            [push.interpreter.core :refer [register-modules]]

            [search.utils :refer [defnk-fn]]
            [search.algorithms.base.done :as done]
            [search.algorithms.base.select :as select]
            [search.algorithms.push :as push]))

(defnk-fn genome->traits :- s/Any
  "Computes the levenshtein distance between the genome and 'saul'"
  [push-evaluate]
  [genome]
  {:distance (metrics/levenshtein "saul" (push-evaluate genome {}))})

(def graph
  (g/graph
    (assoc (g/instance push/graph {:output-stack :string})
      :interpreter (fnk [] (register-modules (classic-interpreter) [random-scalars-module])))
    :genome->traits genome->traits
    :select (g/instance select/roulette {:trait-name :distance :lowest? true})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :distance (= 0))})))
