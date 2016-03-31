(ns search.graphs.base.step
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk map-keys fnk]]
            [plumbing.graph :as g]

            [search.utils :refer [defnk-fn] :as utils]
            [search.schemas :as schemas]))

(defnk-fn breed-> :- schemas/Generation
  "Step to the next generation by taking the first `n` individuals from calling
  `breed` on the current generation."
  [n :- s/Int
   breed :- (s/=> (utils/InfSeq schemas/Individual) schemas/Generation)]
  [generation :- schemas/Generation]
  {:index       (inc (:index generation))
   :individuals (utils/take-set n (breed generation))})

(s/defschema Select (s/=> schemas/Individual #{schemas/Individual}))
(s/defschema Tweak
  "Take in some number of parent genomes and return a set of child genomes.

   mutate and crossover are two common examples"
  {:f (s/=> (s/cond-pre [schemas/Genome] schemas/Genome) & [schemas/Genome])
   :n-parents s/Int
   :multiple-children? s/Bool})

(s/defschema TweakLabel s/Keyword)

(defn ->seq-if
  "If `?` wrap val in a sequence, else return it directly"
  [? val]
  (if ? [val] val))

(defnk-fn weighted-tweaks->children :- s/Any ;- (utils/InfSeq schemas/Individual)
  "Returns an infinite lazy sequence of possible offspring.

  Chooses children from each pipeline of tweaks using the `tweak-label-weights`.
  Each key in `tweak-label-weights` can be either a single tweak label
  or a sequence of labels."
  [tweak-labels :- {TweakLabel Tweak}
   tweak-label-weights :- {(s/cond-pre TweakLabel [TweakLabel]) s/Int}
   tweaks->children :- (s/=> (utils/InfSeq schemas/Individual) [Tweak] schemas/Generation)]
  [generation :- schemas/Generation]
  (->> tweak-label-weights
     (map-keys #(->> %1
                  (->seq-if (keyword? %1))
                  (map tweak-labels)
                  (tweaks->children generation)))
     utils/interleave-weighted))

(defnk ->child-individual :- schemas/Individual
  [parent-ids :- #{s/Str}
   genome :- schemas/Genome]
  {:id (utils/id)
   :genome genome
   :parent-ids parent-ids
   :traits {}})

(s/defn tweak-intermediate
  "Takes in a tweak and some intermediate individuals and calls the tweak on
    them "
  [children :- s/Any ; [{:genome s/Any :parent-ids #{s/Str}}]
   {:keys [f n-parents multiple-children?]} :- Tweak]
  (->> children
    ; split the intermediate children up into groups, so that the current
    ; tweak can be applied to each group
    (partition n-parents)
    (mapcat
      ; apply the tweak to this group of parents
      (s/fn tweak-intermediate-inner :- [{:genome s/Any :parent-ids #{s/Str}}]
        [parents :- [{:genome s/Any :parent-ids #{s/Str}}]]
        (let [parent-ids (apply clojure.set/union (map :parent-ids parents))]
          (->> parents
            (map :genome)
            (apply f)
            (->seq-if (not multiple-children?))
            (map #(hash-map :genome %
                            :parent-ids parent-ids))))))))


(defnk-fn tweaks->children_ :- s/Any ;- (utils/InfSeq schemas/Individual)
  "Takes a pipeline of tweaks and a way to get a parent, and returns a sequence
  of children with that tweaks applied to the parents in that order"
  [select :- Select]
  [{individuals :individuals} :- schemas/Generation
   tweaks :- [Tweak]]
  (let [->parent (partial select individuals)]
    (->> tweaks
      (reduce
        tweak-intermediate
        (map
          (fn [{:keys [genome id]}]
            {:genome genome :parent-ids #{id}})
          (repeatedly ->parent)))
      (map ->child-individual))))

(def graph
  (g/graph
    :tweaks->children tweaks->children_
    :breed weighted-tweaks->children
    :step (g/instance breed-> [population-size] {:n population-size})))
