(ns search.algorithms.base.select
  (:require [schema.core :as s]
            [clojure.data.generators]

            [search.schemas :as schemas]
            [search.algorithms.base.step :refer [Select]]))

(s/defn roulette :- Select
  "Select individuals proportional to their fitness. If an individual has a
  higher fitness it is selected more often. `fitness-trait` is the name of the
  trait to use as the fitness.

  This is a single objective selection."
  [fitness-trait :- s/Keyword]
  (s/fn roulette-inner :- schemas/Individual
    [inds :- [schemas/Individual]]
    (->>
      inds
      (map #(vector %1 (get-in %1 [:traits fitness-trait])))
      (into {})
      clojure.data.generators/weighted)))
