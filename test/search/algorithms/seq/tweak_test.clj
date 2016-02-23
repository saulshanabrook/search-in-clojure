(ns search.algorithms.seq.tweak-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.schemas :as schemas]
            [search.algorithms.seq.tweak :as tweak]))
(use-fixtures :once schema.test/validate-schemas)

(deftest mutate-test
  (let [->gene (fn [] :new)
        genome [:old :old]
        mutate #((tweak/mutate % ->gene) [genome])]
    (is (= [[:old :old]] (mutate 0)))
    (is (= [[:new :new]] (mutate 1)))))

(s/defn same-seqs? :- s/Bool
  "Tests if `x` has the same (unordered) sequences inside any of the sequences in `ys`.
  So if x was [1 3 2] and `ys` was [[4 3 2] [1 2 3]] this would return True
  because [1 3 2] is the same unordered sequences as [1 2 3]."
  [ys :- [[[s/Any]]]
   x :- [[s/Any]]]
  (= true (some #(= (set (map seq %)) (set (map seq x))) ys)))

(deftest one-point-crossover-test
  (are [in outs] (let [actual-out (tweak/one-point-crossover in)]
                   (some #(= % (set actual-out)) outs))
    [[1] [2]] #{#{[1] [2]}}
    [[1 1] [2 2]] #{#{[1 2] [2 1]}}
    [[1 2 3] [4 5 6]] #{#{[1 5 6] [4 2 3]}
                        #{[1 2 6] [4 5 3]}}))
