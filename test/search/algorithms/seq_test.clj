(ns search.algorithms.seq-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]

            [search.algorithms.seq :as seq]))
(use-fixtures :once schema.test/validate-schemas)


(deftest ->genome-test
  (let [val (atom 0)
        ->genome (seq/->genome {:n-genes 3
                                :->gene #(swap! val inc)})]
    (is (= [1 2 3] (->genome)))))


(deftest mutate-test
  (let [mutate #(seq/mutate {:p %
                             :->gene (fn [] :new)})]
    (is (= [:old :old] ((mutate 0) [:old :old])))
    (is (= [:new :new] ((mutate 1) [:old :old])))))

(s/defn same-seqs? :- s/Bool
  "Tests if `x` has the same (unordered) sequences inside any of the sequences in `ys`.
  So if x was [1 3 2] and `ys` was [[4 3 2] [1 2 3]] this would return True
  because [1 3 2] is the same unordered sequences as [1 2 3]."
  [ys :- [[[s/Any]]]
   x :- [[s/Any]]]
  (= true (some #(= (set (map seq %)) (set (map seq x))) ys)))

(deftest one-point-crossover-test
  (are [x1 x2 outs] (let [actual-out (seq/one-point-crossover x1 x2)]
                      (some #(= % (set actual-out)) outs))
    [1] [2] #{#{[1] [2]}}
    [1 1] [2 2] #{#{[1 2] [2 1]}}
    [1 2 3] [4 5 6] #{#{[1 5 6] [4 2 3]}
                      #{[1 2 6] [4 5 3]}}))
