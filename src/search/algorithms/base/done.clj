(ns search.algorithms.base.done
  "Partial graphs that provide the `done` function. "
  (:require [schema.core :as s]
            [com.rpl.specter :as sp]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]))


(defnk-fn max-generations :- s/Bool
  "Returns true we have should stop generating new generations, because we have
  already generated the `max_`, by looking at the `:index` of the current
  generation"
  [max_ :- s/Int]
  [{index :index} :- search/Generation]
  (>= (inc index) max_))

(defnk-fn max-trait :- s/Bool
  "Returns true after any individual in the current generation has trait `name`
   greater than `max`."
  [name :- s/Keyword
   max_ :- s/Int]
  [generation :- search/Generation]
  (<= max_ (apply max (sp/select [:individuals sp/ALL :traits name] generation))))
