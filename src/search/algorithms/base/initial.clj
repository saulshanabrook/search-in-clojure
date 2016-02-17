(ns search.algorithms.base.initial
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.algorithms.base.core :refer [Initial]]
            [search.utils :as utils]))

(s/defn ->genome-> :- Initial
  "Returns a function that returns the initial population, by making
   populatin it with `n` genomes, created with `->genome`"
  [->genome :- (s/=> schemas/Genome)
   n :- s/Int]
  (s/fn ->genome->-inner :- schemas/Generation
    [run-id :- s/Str]
    {:index 0
     :run-id run-id
     :individuals (repeatedly n (fn [] {:genome (->genome)
                                        :parents-ids []
                                        :id (utils/id)
                                        :traits {}}))}))
