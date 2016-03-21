(ns search.graphs.seq
  (:require [schema.core :as s]
            [clojure.data.generators]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.utils :refer [defnk-fn v->fnk] :as utils]
            [search.graphs.base.step :as step]))


(def Gene s/Any)
(def Genome [Gene])

(defnk-fn ->genome :- Genome
  "Creates an initial genome by creating `n-genes` genes, from `->gene`"
  [->gene :- (s/=> Gene)
   n-genes :- s/Int]
  []
  (repeatedly n-genes ->gene))


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
      (clojure.data.generators/uniform 0 (inc max-length)))))

(s/defn one-point-crossover :- [Genome]
  "Creates two new children from `first_` and `second_` by chosing a point and
   swapping all elements before and after that point."
  [first_ :- Genome
   second_ :- Genome]
  (let [split-length (length-within [first_ second_])
        [first-split second-split] (map (partial split-at split-length) [first_ second_])]
    [(concat (first first-split) (second second-split))
     (concat (first second-split) (second first-split))]))

(s/defn two-point-crossover :- [Genome]
  "Creates two new children from `a` and `b` by chosing two points and
  swapping all elements between those points."
  [a :- Genome
   b :- Genome]
  ; both are split into three chunks [l c r] and then those are exchanged
  (let [both [a b]
        [l-len lc-len] (sort (repeatedly 2 (partial length-within both)))
        c-len (- lc-len l-len)
        [[a-l a-c a-r] [b-l b-c b-r]]
        (map
         #(let [[l cr] (split-at l-len %)
                [c r] (split-at c-len cr)]
           [l c r])
         both)]
    [(concat a-l b-c a-r)
     (concat b-l a-c b-r)]))


(defnk-fn alternation :- Genome
 "Merges two sequences, taking sequential items from one then crossing over and
  taking items from the other. After each item there is a `p`
  chance of switching to the other sequence."
 [p :- utils/Probability]
 [a :- Genome
  b :- Genome]
 (let [switch? (partial utils/rand-true? p)]
   (loop [index 0
          currents (cycle [a b])
          merged []]
     (let [current (first currents)]
       (if (>= index (count current))
         merged
         (recur
           (inc index)
           (if (switch?) (rest currents) currents)
           (conj merged (nth current index))))))))



(def graph
 (g/graph
   :->genome ->genome
   :mutate-p (v->fnk 0.01)
   :alternation-p (v->fnk 0.05)
   :tweaks
    {:mutate {:f (g/instance mutate [mutate-p] {:p mutate-p})
              :n-parents (v->fnk 1)
              :multiple-children? (v->fnk false)}
     :one-point-crossover (v->fnk
                           {:f one-point-crossover
                            :n-parents 2
                            :multiple-children? true})
     :two-point-crossover (v->fnk
                           {:f two-point-crossover
                            :n-parents 2
                            :multiple-children? true})
     :alternation {:f (g/instance alternation [alternation-p] {:p alternation-p})
                   :n-parents (v->fnk 2)
                   :multiple-children? (v->fnk false)}}
   :tweak-weights (v->fnk {:mutate 2
                           :one-point-crossover 0
                           :two-point-crossover 2
                           :alternation 2})
   :->tweak step/weighted-tweaks))
