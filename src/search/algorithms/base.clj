(ns search.algorithms.base
  (:require [schema.core :as s]

            [search.schemas :as schema]
            [search.utils :as utils]))

(s/defn step-until-end :- schema/Algorithm
  "Creates an algorithm that returns `init-generation` as its first generation
   and then continiously calls `step` on one generation to get the next.
   This will continue until `(end? generation)` returns true, in which case
   that generation will be the last"
  [init-generation :- (s/=> s/Str schema/Generation)
   end? :- (s/=> schema/Generation s/Bool)
   step :- (s/=> schema/Generation schema/Generation)]
  (s/fn step-until-end-inner [run-id :- s/Str]
    (utils/take-until
     end?
     (iterate step (init-generation run-id)))))
