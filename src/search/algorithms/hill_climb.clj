(ns search.algorithms.hill-climb
  (:require [schema.core :as s]

            [search.schemas :as schema]
            [search.algorithms.base :as base]))

(s/defn algorithm :- schema/Algorithm
  "[Hill climbing](https://en.wikipedia.org/wiki/Hill_climbing) algorithm."
  [->genome :- (s/=> schema/Genome)
   mutate :- (s/=> schema/Genome schema/Genome)
   evaluate :- (s/=> schema/Genome s/Num)
   done? :- base/Done]
  (base/step-until-end
    (base/generate-initial ->genome 1)
    (base/evaluate-genome #(hash-map :value (evaluate %)))
    done?
    (base/step-breed 1 (fn [[parent]] (let [old-genome (:genome parent)
                                            new-genome (mutate old-genome)
                                            new-score (evaluate new-genome)
                                            old-score (-> parent :traits :value)
                                            new-is-better (>= new-score old-score)
                                            child-genome (if new-is-better new-genome old-genome)]
                                        (base/->child-individual child-genome [parent]))))))
