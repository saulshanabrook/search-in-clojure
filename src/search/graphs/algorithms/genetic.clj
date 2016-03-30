(ns search.graphs.algorithms.genetic
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
        :->genome (s/=> schemas/Genome)
        :tweak (s/=> [schemas/Individual] [schemas/Individual])
        :genome->traits (s/=> schemas/Traits schemas/Genome)})))
        :select (s/=> schemas/Individual [schemas/Individual])
        :done? (s/=> s/Bool schemas/Generation)
      }
  "
  (g/graph
    :population-size (fnk [] 1000)
    :initial (g/instance initial/->genome-> [population-size] {:n population-size})
    :evaluate evaluate/genome->traits->
    step/graph
    :generations base/generations))
