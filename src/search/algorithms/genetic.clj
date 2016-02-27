(ns search.algorithms.genetic
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.algorithms.base.core :as base]
            [search.algorithms.base.initial :as initial]
            [search.algorithms.base.step :as step]
            [search.algorithms.base.tweak :as tweak]
            [search.algorithms.base.evaluate :as evaluate]))


(def graph
  "Genetic algorithm"
  (g/graph
    :n (fnk [population-size :- s/Int] population-size)
    :initial initial/->genome
    :evaluate evaluate/genome->traits
    :breed step/select-and-tweak
    :step step/breed
    :generations base/generations))
