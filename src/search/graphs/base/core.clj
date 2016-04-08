(ns search.graphs.base.core
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.schemas :as schemas]
            [search.utils :as utils]))

(defnk generations ; :- (possibly infinite) lazy [schemas/Generation]
  "Basic high level algorithm that will cover most use cases.

  1. Gets the initial generation from `initial`.
  2. Call `evaluate` on the current generation, which should return
     a generation whose `individuals` have updated `traits`
  3. Check current generation is `done?`:
    1. If yes then return the current generation
    2. If no, then recur with a new generation, from `step`,
       to step 2."
  [initial :- schemas/Generation
   evaluate :- (s/=> schemas/Generation schemas/Generation)
   done? :- (s/=> s/Bool schemas/Generation)
   step :- (s/=> schemas/Generation schemas/Generation)]
  (utils/take-until
    done?
    (iterate (comp evaluate step) (evaluate initial))))
