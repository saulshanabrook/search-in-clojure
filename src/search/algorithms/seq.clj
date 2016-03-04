(ns search.algorithms.seq
  (:require [schema.core :as s]
            [clojure.data.generators]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.utils :refer [defnk-fn] :as utils]
            [search.algorithms.base.core :as base]
            [search.algorithms.base.step :as step]
            [search.algorithms.base.initial :as initial]))


(def Gene s/Any)
(def Genome [Gene])

(defnk-fn ->genome :- Genome
  "Creates an initial genome by creating `n` genes, from `->gene`"
  [->gene :- (s/=> Gene)
   n :- s/Int]
  []
  (repeatedly n ->gene))


(defnk-fn mutate :- Genome
  "Replaces each gene in the genome with probability `prob`, creating a new
  gene with `->gene` if needed."
  [p :- utils/Probability
   ->gene :- (s/=> Gene)]
  [genome :- Genome]
  (map #(if (utils/rand-true? p) (->gene) %) genome))

(s/defn length-within :- s/Int
  "Given a number of sequences, returns a random length that is less than all
   their lengths and chosen randomly uniform from the smallest sequences."
  [seqs :- [[s/Any]]]
  (let [max-length (apply min (map count seqs))]
    (if (= 1 max-length)
      1
      (clojure.data.generators/uniform 1 max-length))))

(s/defn one-point-crossover :- [Genome]
  "Creates two new children from `first_` and `second_` by chosing a point and
   swapping all elements before and after that point."
  [first_ :- Genome
   second_ :- Genome]
  (let [split-length (length-within [first_ second_])
        [first-split second-split] (map (partial split-at split-length) [first_ second_])]
    [(concat (first first-split) (second second-split))
     (concat (first second-split) (second first-split))]))

(def tweak-weights {:mutate 1
                    :one-point-crossover 99})

(def tweaks-graph
 (g/graph
   :mutate-p (fnk [] 0.01)
   :tweaks
    {:mutate {:f (g/instance mutate [mutate-p] {:p mutate-p})
              :n-parents (fnk [] 1)
              :multiple-children? (fnk [] false)}
     :one-point-crossover {:f (fnk [] one-point-crossover)
                           :n-parents (fnk [] 2)
                           :multiple-children? (fnk [] true)}}))

(def graph
 (g/graph
   :->genome ->genome
   tweaks-graph
   :tweak-weights (fnk _ :- {s/Keyword s/Int} [] tweak-weights)
   :->tweak step/weighted-tweaks
   :initial initial/->genome->))

; (s/defn alternation
;   "Merges two sequences, taking sequential items from one then crossing over and
;   taking items from the other. After each item there is  a `prob`
;   chance of switching to the other sequence.
;
;   It will stop when it tries to select an index passed the end of either sequence."
;   [prob :- utils/Probability
;    [first_ second_] :- [(s/one s/Genome "first") (s/one s/Genome "second")]]
;   (let [switch? (partial weighted-bool prob)]
;     (loop [index 0
;            first_? True
;            merged (list)]
;       (let [current (if first_? first_ second_)]
;         (if (>= index (count current))
;           merged
;           (recur
;             (inc index)
;             (if (switch?) (not first_) second_)
;             (conj merged (nth current index))))))))
