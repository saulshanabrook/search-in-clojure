(ns search.algorithms.base.select
  (:require [schema.core :as s]
            [clojure.data.generators]
            [plumbing.graph :as g]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]))

(defnk-fn roulette :- search/Individual
  "Select a parent individual, proportional to the `trait-name` trait of the
  of individual. A higher value will cause the individual to be selected more
  often.

  This is a single objective selection."
  [trait-name :- s/Keyword]
  [inds :- [search/Individual]]
  (->>
    inds
    (map #(vector %1 (get-in %1 [:traits trait-name])))
    (into {})
    clojure.data.generators/weighted))
