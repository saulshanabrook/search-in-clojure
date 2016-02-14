(ns search.algorithms.hill-climb
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.algorithms.base :as base]))


(s/defn algorithm :- schemas/Algorithm
  "[Hill climbing](https://en.wikipedia.org/wiki/Hill_climbing) algorithm."
  [->genome :- (s/=> schemas/Genome)
   mutate :- (s/=> schemas/Genome schemas/Genome)
   evaluate :- (s/=> schemas/Genome s/Num)
   done? :- base/Done]
  (base/step-until-end
    (base/generate-initial ->genome 1)
    (base/evaluate-genome #(hash-map :value (evaluate %)))
    done?
    (base/step-breed 1 (s/fn breed :- schemas/Individual
                        [[parent] :- [(s/one schemas/Individual "i")]]
                        (let [old-genome (:genome parent)
                              new-genome (mutate old-genome)
                              old-score (-> parent :traits :value)
                              new-score (evaluate new-genome)
                              new-is-better? (>= new-score old-score)
                              child-genome (if new-is-better? new-genome old-genome)]
                          (base/->child-individual child-genome [parent]))))))
