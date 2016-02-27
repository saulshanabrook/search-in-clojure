(ns search.algorithms.hill-climb
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.algorithms.base.core :as base]
            [search.algorithms.base.initial :as initial]
            [search.algorithms.base.step :as step]
            [search.algorithms.base.tweak :as tweak]
            [search.algorithms.base.evaluate :as evaluate]
            [search.utils :refer [defnk-fn]]))


(defnk-fn breed :- [search/Individual]
  "Mutates the last individuals genome and stores the new copy if it has
   a higher value, and keeps the old if it is higher."
  [genome->value :- (s/=> search/TraitValue search/Genome)
   mutate :- (s/=> search/Genome search/Genome)]
  [{[parent] :individuals} :- search/Generation]
  (let [old-genome (:genome parent)
        new-genome (mutate old-genome)
        old-score (-> parent :traits :value)
        new-score (genome->value new-genome)
        new-is-better? (>= new-score old-score)
        child-genome (if new-is-better? new-genome old-genome)]
    [(tweak/->child-individual [parent] child-genome)]))

(def graph
  "[Hill climbing](https://en.wikipedia.org/wiki/Hill_climbing) algorithm."
  (g/graph
    :initial (g/instance initial/->genome-> {:n 1})
    :genome->traits (fnk [genome->value] #(hash-map :value (genome->value %)))
    :evaluate evaluate/genome->traits->
    :breed breed
    :step  (g/instance step/breed-> {:n 1})
    :generations (g/instance base/generations {:n 1})))
