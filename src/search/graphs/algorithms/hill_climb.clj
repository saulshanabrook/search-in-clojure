(ns search.graphs.algorithms.hill-climb
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.schemas :as schemas]
            [search.graphs.base.core :as base]
            [search.graphs.base.initial :as initial]
            [search.graphs.base.step :as step]
            [search.graphs.base.evaluate :as evaluate]
            [search.utils :refer [defnk-fn] :as utils]))


(defnk-fn breed :- [schemas/Individual]
  "Mutates the parent individual, scores it with `genome->traits` and then
  uses the `select` function to return the 'better' individual to keep."
  [select :- (s/=> schemas/Individual #{schemas/Individual})
   genome->traits :-  (s/=> schemas/Traits schemas/Genome)
   mutate :- (s/=> schemas/Genome schemas/Genome)]
  [prev-generation :- schemas/Generation]
  (let [parent (first (:individuals prev-generation))
        new-genome (mutate (:genome parent))
        new-ind {:id (utils/id)
                 :parent-ids #{(:id parent)}
                 :genome new-genome
                 :traits (genome->traits new-genome)}]
    [(select #{parent new-ind})]))

(def graph
  "[Hill climbing](https://en.wikipedia.org/wiki/Hill_climbing) algorithm
   adapted for multiple objectives."
  (g/graph
    :initial (g/instance initial/->genome-> {:n 1})
    :evaluate evaluate/genome->traits->
    :breed breed
    :step (g/instance step/breed-> {:n 1})
    :generations base/generations))
