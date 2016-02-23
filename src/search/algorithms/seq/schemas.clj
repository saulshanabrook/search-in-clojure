(ns search.algorithms.seq.schemas
  (:require [schema.core :as s]))

(def SeqGene s/Any)
(def SeqGenome [SeqGene])
