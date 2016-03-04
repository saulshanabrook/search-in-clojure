(ns search.algorithms.genetic
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.algorithms.base.core :as base]
            [search.algorithms.base.initial :as initial]
            [search.algorithms.base.step :as step]
            [search.algorithms.base.evaluate :as evaluate]))


(def graph
  "Genetic algorithm.

  It requires nodes:

      {
        :->genome (s/=> search/Genome)
        :tweak (s/=> [search/Individual] [search/Individual])
        :genome->traits (s/=> search/Traits search/Genome)})))
        :select (s/=> search/Individual [search/Individual])
        :done? (s/=> s/Bool search/Generation)
      }
  "
  (g/graph
    :population-size (fnk [] 2)
    :initial (g/instance initial/->genome-> [population-size] {:n population-size})
    :evaluate evaluate/genome->traits->
    :breed step/select-and-tweak
    :step (g/instance step/breed-> [population-size] {:n population-size})
    :generations base/generations))