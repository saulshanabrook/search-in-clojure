(ns search.graphs.base.initial
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.core :as search]
            [search.utils :as utils]))

(defnk ->genome-> :- search/Generation
  "Create an initial generation by making `n` genomes from `->genome`"
  [->genome :- (s/=> search/Genome)
   n :- s/Int]
  {:index 0
   :individuals (utils/repeatedly-set n (fn []
                                          {:genome (->genome)
                                           :parents-ids #{}
                                           :id (utils/id)
                                           :traits {}}))})
