(ns search.algorithms.base.evaluate
  (:require [schema.core :as s]
            [com.rpl.specter :as sp]

            [search.schemas :as schemas]
            [search.algorithms.base.core :refer [Evaluate]]))

(s/defn genome->traits-> :- Evaluate
  "Returns an evaluate function that generates the traits for each individual
   based on its genome"
  [genome->traits]
  (let [path (sp/comp-paths :individuals sp/ALL (sp/collect-one :genome) :traits)]
    (s/fn ->genome->-inner :- schemas/Generation
      [generation :- schemas/Generation]
      (sp/compiled-transform
        path
        (fn [genome _] (genome->traits genome))
        generation))))
