(ns search.algorithms.base.core
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.core :as search]
            [search.utils :as utils]))

(defnk generations ; :- (possibly infinite) lazy [search/Generation]
  "Basic high level algorithm that will cover most use cases.

  1. Gets the initial generation from `initial`.
  2. Call `evaluate` on the current generation, which should return
     a generation whose `:individuals` have updated `traits`
  3. Check current generation is `done?`:
    1. If yes then return the current generation
    2. If no, then recur with a new generation, from `step`,
       to step 2."
  [initial :- search/Generation
   evaluate :- (s/=> search/Generation search/Generation)
   done? :- (s/=> s/Bool)
   step :- (s/=> search/Generation search/Generation)]
  (utils/take-until
    done?
    (iterate (comp evaluate step) (evaluate initial))))
