(ns search.algorithms.base.core
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.algorithms.base.schemas :refer [Initial Evaluate Done Step]]
            [search.utils :as utils]))

(s/defn ->algorithm :- schemas/Algorithm
  "Basic high level algorithm that will cover most use cases.

  1. Call `initial` with the run ID to get the intial generation.
  2. Call `evaluate` on the current generation, which should return
     a generation whose `:individuals` have updated `traits`
  3. Check current generation is `done?`:
    1. If yes then return the currnent generation
    2. If no, then recur with a new generation, from `step`,
       to step 2."
  [initial :- Initial
   evaluate :- Evaluate
   done? :- Done
   step :- Step]
  (s/fn step-until-end-inner [run-id :- s/Str]
    (utils/take-until
     done?
     (iterate (comp evaluate step) (-> run-id initial evaluate)))))
