(ns search.graphs.base.done
  "Partial graphs that provide the `done` function. "
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.utils :refer [defnk-fn]]))


(defnk-fn max-generations :- s/Bool
  "Returns true we have should stop generating new generations, because we have
  already generated the `max_`, by looking at the `:index` of the current
  generation"
  [max_ :- s/Int]
  [{index :index} :- schemas/Generation]
  (>= (inc index) max_))

(defnk-fn any-trait :- s/Bool
  "Returns true if `traits->done?` returns true for any of the individuals' traits."
  [traits->done? :- (s/=> s/Bool schemas/Traits)]
  [generation :- schemas/Generation]
  (->> generation
    :individuals
    (map :traits)
    (some traits->done?)
    boolean))
