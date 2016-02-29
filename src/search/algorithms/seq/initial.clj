(ns search.algorithms.seq.initial
  (:require [schema.core :as s]

            [search.algorithms.seq.core :refer [Gene Genome]]
            [search.utils :refer [defnk-fn]]))

(defnk-fn ->gene->genome :- Genome
  "Creates an initial genome by creating `n` genes, from `->gene`"
  [->gene :- (s/=> Gene)
   n :- s/Int]
  []
  (repeatedly n ->gene))
