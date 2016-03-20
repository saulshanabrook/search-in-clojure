(ns search.algorithms.base.initial-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.algorithms.base.initial :as initial]
            [search.utils :as utils]))
(use-fixtures :once schema.test/validate-schemas)


(deftest ->genome-test
  (let [n 3]
    (with-redefs [utils/id (utils/seq->fn (cycle ["0" "1" "2"]))]
      (is (=
           {:index 0
            :individuals (utils/repeatedly-set
                          n
                          (fn []
                            {:genome :test
                             :id (utils/id)
                             :parents-ids #{}
                             :traits {}}))}
           (initial/->genome-> {:->genome (fn [] :test)
                                :n n}))))))
