(ns search.graphs.base.evaluate
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.utils :refer [defnk-fn]]))

(defnk-fn genome->traits-> :- schemas/Generation
  "Return an evaluated generation by settings the traits for each individual
   based on its genome"
  [genome->traits :- (s/=> schemas/Traits schemas/Genome)
   map-fn]
  [generation :- schemas/Generation]
  (assoc generation :individuals
    (set
      (map-fn
        (fn [ind]
          (let [traits (-> ind :genome genome->traits)]
            (assoc ind :traits traits)))
        (generation :individuals)))))
