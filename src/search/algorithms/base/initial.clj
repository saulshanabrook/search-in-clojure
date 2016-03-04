(ns search.algorithms.base.initial
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.core :as search]
            [search.utils :as utils]))

(defnk ->genome-> :- search/Generation
  "Create an initial generation by making `n` genomes from `->genome`
   and initializing with the `run-id`"
  [->genome :- (s/=> search/Genome)
   n :- s/Int
   run-id :- s/Str]
  {:index 0
   :run-id run-id
   :individuals (repeatedly n (fn [] {:genome (->genome)
                                      :parents-ids #{}
                                      :id (utils/id)
                                      :traits {}}))})
