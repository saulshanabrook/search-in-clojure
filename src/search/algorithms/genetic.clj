(ns search.algorithms.genetic
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.algorithms.base.core :as base]
            [search.algorithms.base.initial :as initial]
            [search.algorithms.base.step :as step]
            [search.algorithms.base.tweak :as tweak]
            [search.algorithms.base.evaluate :as evaluate]))


(s/defn ->algorithm :- schemas/Algorithm
  "Genetic algorithm"
  [->genome :- (s/=> schemas/Genome)
   population-size :- s/Int
   evaluate :- (s/=> schemas/Traits schemas/Genome)
   done? :- base/Done
   select :- step/Select
   tweak :- step/Tweak]
  (base/->algorithm
    (initial/->genome-> ->genome population-size)
    (evaluate/genome->traits-> evaluate)
    done?
    (step/breed-> (step/select-and-tweak->breed select tweak))))
