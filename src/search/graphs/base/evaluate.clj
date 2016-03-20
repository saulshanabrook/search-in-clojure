(ns search.graphs.base.evaluate
  (:require [schema.core :as s]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]))

(defnk-fn genome->traits-> :- search/Generation
  "Return an evaluated generation by settings the traits for each individual
   based on its genome"
  [genome->traits :- (s/=> search/Traits search/Genome)]
  [generation :- search/Generation]
  (assoc generation :individuals
    (set
      (pmap
        (fn [ind]
          (let [traits (-> ind :genome genome->traits)]
            (assoc ind :traits traits)))
        (generation :individuals)))))
