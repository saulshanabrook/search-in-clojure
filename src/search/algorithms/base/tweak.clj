(ns search.algorithms.base.tweak
  (:require [schema.core :as s]
            [clojure.data.generators]

            [search.algorithms.base.step :refer [Tweak]]
            [search.schemas :as schemas]
            [search.utils :as utils]))

(s/defn ->child-individual :- schemas/Individual
 [parents :- [schemas/Individual]
  child-genome :- schemas/Genome]
 {:id (utils/id)
  :genome child-genome
  :parents-ids (map :id parents)
  :traits {}})


(def TweakGenome (s/=> [schemas/Genome] [schemas/Genome]))

(s/defn tweak-genome-> :- Tweak
  [fn_ :- TweakGenome
   n-parents :- s/Int]
  (s/fn tweak-genome-inner
    [all-parents];- lazy infinite [schemas/Individual]
    (let [parents (take n-parents all-parents)
          parents-genomes (map :genome parents)
          child-genomes (fn_ parents-genomes)]
      (map (partial ->child-individual parents) child-genomes))))
