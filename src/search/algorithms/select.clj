(ns search.algorithms.select
  (:require [schema.core :as s]

            [search.schemas :as schema]
            [search.algorithms.base :as base]))

(s/defn max-trait :- base/Select
  "Select the individual with the highest of one traits"
  [trait :- s/Keyword]
  (s/fn max-trait-inner :- schema/Individual
    [individuals :- [schema/Individual]]
    (apply max-key (comp trait :traits) individuals)))
