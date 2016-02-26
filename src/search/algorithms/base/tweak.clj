(ns search.algorithms.base.tweak
  (:require [schema.core :as s]
            [clojure.data.generators]
            [plumbing.graph :as g]

            [search.core :as search]
            [search.utils :as utils :refer [defnk-fn]]))

(s/defn ->child-individual :- search/Individual
 [parents :- [search/Individual]
  child-genome :- search/Genome]
 {:id (utils/id)
  :genome child-genome
  :parents-ids (map :id parents)
  :traits {}})


(defnk-fn tweak-genome :- [search/Individual]
  "Calls `f` with the first `n-parents` genomes from `all-parents`
  and creates children individuals from the genomes returned by `f`"
  [f :- (s/=> [search/Genome] & [search/Genome])
   n-parents :- s/Int]
  [all-parents] ;- lazy infinite [search/Individual]
  (let [parents (take n-parents all-parents)
        parents-genomes (map :genome parents)
        child-genomes (apply f parents-genomes)]
    (map (partial ->child-individual parents) child-genomes)))
