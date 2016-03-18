(ns search.algorithms.base.done
  "Partial graphs that provide the `done` function. "
  (:require [schema.core :as s]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]))


(defnk-fn max-generations :- s/Bool
  "Returns true we have should stop generating new generations, because we have
  already generated the `max_`, by looking at the `:index` of the current
  generation"
  [max_ :- s/Int]
  [{index :index} :- search/Generation]
  (>= (inc index) max_))

(defnk-fn any-trait :- s/Bool
  "Returns true if `traits->done?` returns true for any of the individuals' traits."
  [traits->done? :- (s/=> s/Bool search/Traits)]
  [generation :- search/Generation]
  (->> generation
    :individuals
    (map :traits)
    (some traits->done?)
    boolean))
