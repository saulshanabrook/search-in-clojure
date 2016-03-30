(ns search.graphs.base.initial
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.schemas :as schemas]
            [search.utils :as utils]))

(defnk ->genome-> :- schemas/Generation
  "Create an initial generation by making `n` genomes from `->genome`"
  [->genome :- (s/=> schemas/Genome)
   n :- s/Int]
  {:index 0
   :individuals (utils/repeatedly-set n (fn []
                                          {:genome (->genome)
                                           :parent-ids #{}
                                           :id (utils/id)
                                           :traits {}}))})
