(ns search.graphs.algorithms.hill-climb
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.graphs.base.core :as base]
            [search.graphs.base.initial :as initial]
            [search.graphs.base.step :as step]
            [search.graphs.base.evaluate :as evaluate]
            [search.utils :refer [defnk-fn] :as utils]))


(defnk-fn breed :- [search/Individual]
  "Mutates the parent individual, scores it with `genome->traits` and then
  selects
   ."
  [select :- (s/=> (utils/InfSeq search/Individual) #{search/Individual})
   genome->traits :-  (s/=> search/Traits search/Genome)
   mutate :- (s/=> search/Genome search/Genome)]
  [{[& [parent]] :individuals :- search/Generation}]
  (let [old-genome (:genome parent)
        new-genome (mutate old-genome)
        new-ind (assoc
                 (step/->child-individual {:parent-ids #{(:id parent)}
                                           :genome new-genome})
                 :traits
                 (genome->traits new-genome))]
    (take 1 (select #{parent new-ind}))))

(def graph
  "[Hill climbing](https://en.wikipedia.org/wiki/Hill_climbing) algorithm
  adapted for multiple objectives."
  (g/graph
    :initial (g/instance initial/->genome-> {:n 1})
    :evaluate evaluate/genome->traits->
    (g/instance step/graph {:population-size 1})
    :generations (g/instance base/generations {:n 1})))
