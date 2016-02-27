(ns search.algorithms.base.evaluate
  (:require [schema.core :as s]
            [com.rpl.specter :as sp]

            [search.core :as search]
            [search.utils :refer [defnk-fn]]))

(defnk-fn genome->traits-> :- search/Generation
  "Return an evaluated generation by settings the traits for each individual
   based on its genome"
  [genome->traits :- (s/=> search/Traits search/Genome)]
  [generation :- search/Generation]
  (sp/compiled-transform
    (sp/comp-paths :individuals sp/ALL (sp/collect-one :genome) :traits)
    (fn [genome _] (genome->traits genome))
    generation))
