(ns search.algorithms.seq.tweak
  (:require [schema.core :as s]
            [clojure.data.generators]

            [search.utils :refer [defnk-fn] :as utils]))


(def SeqGene s/Any)
(def SeqGenome [SeqGene])

(defnk-fn mutate :- [SeqGenome]
  "Replaces each gene in the genome with probability `prob`, creating a new
  gene with `->gene` if needed."
  [p :- utils/Probability
   ->gene :- (s/=> SeqGene)]
  [genome :- SeqGenome]
  [(map #(if (utils/rand-true? p) (->gene) %) genome)])

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
  [first_ :- SeqGenome
   second_ :- SeqGenome]
  (let [split-length (length-within [first_ second_])
        [first-split second-split] (map (partial split-at split-length) [first_ second_])]
    [(concat (first first-split) (second second-split))
     (concat (first second-split) (second first-split))]))

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