(ns search.algorithms.select
  (:require [schema.core :as s]

            [search.schemas :as schema]
            [search.algorithms.base.step :refer [Select]]))

(s/defn trait->select :- Select
  "Select the individual with the highest of one traits"
  [trait :- s/Keyword]
  (s/fn trait->select-inner :- schema/Individual
    [individuals :- [schema/Individual]]
    (apply max-key (comp trait :traits) individuals)))
