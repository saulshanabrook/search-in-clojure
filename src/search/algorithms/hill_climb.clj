(ns search.algorithms.hill-climb
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.algorithms.base.core :as base]
            [search.algorithms.base.initial :as initial]
            [search.algorithms.base.step :as step]
            [search.algorithms.base.evaluate :as evaluate]
            [search.utils :refer [defnk-fn]]))


(defnk-fn breed :- [search/Individual]
  "Mutates the parent individual, scores it with `genome->traits` and then
  selects
   ."
  [select :- (s/=> search/Individual [search/Individual])
   genome->traits :-  (s/=> search/Traits search/Genome)
   mutate :- (s/=> search/Genome search/Genome)]
  [{[parent] :individuals} :- search/Generation]
  (let [old-genome (:genome parent)
        new-genome (mutate old-genome)
        new-ind (assoc
                 (step/->child-individual [parent] new-genome)
                 :traits
                 (genome->traits new-genome))]
    [(select [parent new-ind])]))

(def graph
  "[Hill climbing](https://en.wikipedia.org/wiki/Hill_climbing) algorithm
  adapted for multiple objectives.

  It requires nodes:

      {
        :->genome (s/=> search/Genome)
        :mutate (s/=> search/Genome search/Genome)
        :genome->traits (s/=> search/Traits search/Genome)
        :select (s/=> search/Individual [search/Individual])
        :done? (s/=> s/Bool search/Generation)
      }

   "
  (g/graph
    :initial (g/instance initial/->genome-> {:n 1})
    :evaluate evaluate/genome->traits->
    :breed breed
    :step  (g/instance step/breed-> {:n 1})
    :generations (g/instance base/generations {:n 1})))
