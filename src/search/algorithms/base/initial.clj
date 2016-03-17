(ns search.algorithms.base.initial
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.core :as search]
            [search.utils :as utils]))

(defnk ->genome-> :- search/Generation
  "Create an initial generation by making `n` genomes from `->genome`
   and initializing with the `search-id`"
  [->genome :- (s/=> search/Genome)
   n :- s/Int
   search-id :- s/Str]
  {:index 0
   :search-id search-id
   :individuals (utils/repeatedly-set n (fn []
                                          {:genome (->genome)
                                           :parents-ids #{}
                                           :id (utils/id)
                                           :traits {}}))})
