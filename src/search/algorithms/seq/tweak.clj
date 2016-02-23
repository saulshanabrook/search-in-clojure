(ns search.algorithms.seq.tweak
  (:require [schema.core :as s]
            [clojure.data.generators]

            [search.algorithms.base.step :refer [Tweak]]
            [search.algorithms.base.tweak :refer [TweakGenome tweak-genome->]]
            [search.algorithms.seq.schemas :refer [SeqGene SeqGenome]]
            [search.utils :as utils]))

(s/defn mutate :- TweakGenome
  "Replaces each gene in the genome with probability `prob`, creating a new
  gene with `->gene` if needed."
  [prob :- utils/Probability
   ->gene :- (s/=> SeqGene)]
  (s/fn seq-mutate-inner :- [SeqGenome]
    [[genome] :- [SeqGenome]]
    [(map #(if (utils/rand-true? prob) (->gene) %) genome)]))

(defn mutate-tweak
  "Returns a tweak, passing all args to the `mutate` function."
  [& args]
  (tweak-genome-> (apply mutate args) 1))

(s/defn length-within :- s/Int
  "Given a number of sequences, returns a random length that is less than all
   their lengths and chosen randomly uniform from the smallest sequences."
  [seqs :- [[s/Any]]]
  (let [max-length (apply min (map count seqs))]
    (if (= 1 max-length)
      1
      (clojure.data.generators/uniform 1 max-length))))

(s/defn one-point-crossover :- [SeqGenome]
  "Creates two new children from `first_` and `second_` by chosing a point and
   swapping all elements before and after that point."
  [genomes :- [(s/one SeqGenome "first") (s/one SeqGenome "second")]]
  (let [split-length (length-within genomes)
        [first-split second-split] (map (partial split-at split-length) genomes)]
    [(concat (first first-split) (second second-split))
     (concat (first second-split) (second first-split))]))

(defn one-point-crossover-tweak
 "Returns a tweak, passing all args to the `one-point-crossover` function."
 [& args]
 (tweak-genome-> (apply one-point-crossover args) 2))

; (s/defn alternation
;   "Merges two sequences, taking sequential items from one then crossing over and
;   taking items from the other. After each item there is  a `prob`
;   chance of switching to the other sequence.
;
;   It will stop when it tries to select an index passed the end of either sequence."
;   [prob :- utils/Probability
;    [first_ second_] :- [(s/one s/SeqGenome "first") (s/one s/SeqGenome "second")]]
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
