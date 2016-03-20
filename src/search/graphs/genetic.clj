(ns search.graphs.genetic
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.graphs.base.core :as base]
            [search.graphs.base.initial :as initial]
            [search.graphs.base.step :as step]
            [search.graphs.base.evaluate :as evaluate]))


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
    :population-size (fnk [] 1000)
    :initial (g/instance initial/->genome-> [population-size] {:n population-size})
    :evaluate evaluate/genome->traits->
    :breed step/select-and-tweak
    :step (g/instance step/breed-> [population-size] {:n population-size})
    :generations base/generations))
