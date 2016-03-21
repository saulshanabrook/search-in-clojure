(ns search.graphs.base.step
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.utils :refer [defnk-fn] :as utils]
            [search.core :as search]))

(defnk-fn breed-> :- search/Generation
  "Step to the next generation by taking the first `n` individuals from calling
  `breed` on the current generation."
  [n :- s/Int
   breed :- (s/=> [search/Individual] search/Generation)]
  [generation :- search/Generation]
  {:index       (inc (:index generation))
   :individuals (utils/take-set n (breed generation))})

(def Tweak {:f (s/=> (s/cond-pre [search/Genome] search/Genome) & [search/Genome])
            :n-parents s/Int
            :multiple-children? s/Bool})

(s/defn ->child-individual :- search/Individual
  [parents :- [search/Individual]
   child-genome :- search/Genome]
  {:id (utils/id)
   :genome child-genome
   :parents-ids (set (map :id parents))
   :traits {}})

(defnk-fn select-and-tweak :- (utils/InfSeq search/Individual)
  "Returns an infinite lazy sequence of possible offspring.

  First it calls `select` with the current individuals in the population, to
  get an infinite lazy sequence of parents.

  Then it repeatedly calls the `->tweak` function to get a `Tweak` function. It calls
  each `f` with the required number of parents, and lazily returns the
  children generated. Then it recurses with the rest of the parents."
  [select :- (s/=> (utils/InfSeq search/Individual) #{search/Individual})
   ->tweak :- (s/=> Tweak)]
  [{individuals :individuals} :- search/Generation]
  (let [f (fn select-and-tweak-inner [parents]
            (let [{:keys [f n-parents multiple-children?]} (->tweak)
                  [first_parents rest_parents] (split-at n-parents parents)
                  child-genome_s (apply f (map :genome first_parents))
                  child-genomes (if multiple-children? child-genome_s [child-genome_s])
                  child-inds (map (partial ->child-individual first_parents) child-genomes)]
              (concat
               child-inds
               (lazy-seq (select-and-tweak-inner rest_parents)))))]
    (f (select individuals))))


(defnk-fn weighted-tweaks :- Tweak
  "Returns a random `tweak` from `tweaks`, by selecting based on the weights
   defined in `tweak-weights`"
  [tweaks :- {s/Keyword Tweak}
   tweak-weights :- {s/Keyword s/Int}]
  []
  (do
    (assert (apply clojure.set/subset? (map (comp set keys) [tweak-weights tweaks])))
    (->
      tweak-weights
      clojure.data.generators/weighted
      tweaks)))
