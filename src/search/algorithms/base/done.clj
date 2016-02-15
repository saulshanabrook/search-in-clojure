(ns search.algorithms.base.done
  (:require [schema.core :as s]
            [com.rpl.specter :as sp]

            [search.schemas :as schemas]
            [search.algorithms.base.schemas :refer [Done]]))


(s/defn max-generations-> :- Done
  "Returns a Done function based on if we are at the maximum number of generations"
  [max :- s/Int]
  (s/fn max-generations->-inner :- s/Bool
    [generation :- schemas/Generation]
    (>= (inc (:index generation)) max)))

(s/defn max-trait-> :- Done
  "Returns a Done function based on if we have reached an acceptable trait value"
  [trait :- s/Keyword
   max_ :- s/Int]
  (s/fn max-trait->-inner :- s/Bool
    [generation :- schemas/Generation]
    (<= max_ (apply max (sp/select [:individuals sp/ALL :traits trait] generation)))))
