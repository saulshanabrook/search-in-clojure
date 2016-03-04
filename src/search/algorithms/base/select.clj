(ns search.algorithms.base.select
  (:require [schema.core :as s]
            [clojure.data.generators]

            [search.core :as search]
            [search.utils :refer [defnk-fn] :as utils]))

(defnk-fn roulette :- s/Any ; :- (utils/InfSeq search/Individual)
  "Selects parent individuals, proportional to the `trait-name` trait of the
  of individual. A higher value will cause the individual to be selected more
  often.

  This is a single objective selection."
  [trait-name :- s/Keyword]
  [inds :- [search/Individual]]
  (let [ind-to-trait (for [ind inds] [ind (get-in ind [:traits trait-name])])]
    (repeatedly (partial clojure.data.generators/weighted (into {} ind-to-trait)))))

(defnk-fn dominates :- s/Any ; :- search/Individual
  "Selects the parents with the highest (or lowest if `lowest?`) `trait-name`.

   Single opjective selection."
  [trait-name :- s/Keyword
   lowest? :- s/Bool]
  [inds :- [search/Individual]]
  (repeat (apply
           (partial (if lowest? min-key max-key) (comp trait-name :traits))
           inds)))
