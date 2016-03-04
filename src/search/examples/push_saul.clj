(ns search.examples.push-saul
  "Make a push program that returns the string `saul`"
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [clj-fuzzy.metrics :as metrics]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]
            [search.algorithms.base.done :as done]
            [search.algorithms.base.evaluate :as evaluate]
            [search.algorithms.push :as push]))

(defnk-fn genome->traits :- s/Any
  "Computes the levenshtein distance between the "
  [push-evaluate]
  [genome]
  {:distance (metrics/levenshtein "saul" (push-evaluate genome {}))})

(def problem-graph
  (g/graph
    (g/instance push/graph {:output-stack :string :input-binding-names []})
    :genome->traits genome->traits
    :done? (g/instance done/any-trait {:traits->done? #(-> % :distance (= 0))})))


(def genetic-config
  (search/->config ['search.algorithms.genetic/graph
                    'search.examples.push-saul/problem-graph]))
