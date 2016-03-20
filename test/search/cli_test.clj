(ns search.cli-test
  (:require [clojure.test :refer :all]
            [schema.test]

            [search.cli :as cli]))

(use-fixtures :once schema.test/validate-schemas)


(deftest test-main
  (testing "list genetic"
    (testing "graphs"
      (cli/-main "-g" "[search.examples.list/graph search.graphs.algorithms.genetic/graph]"))
    (testing "recorders"
      (with-redefs [println identity]
        (cli/-main "-g" "[search.examples.list/graph search.graphs.algorithms.genetic/graph]"
                   "-w" "[(partial search.recorders.core/wrap search.recorders.text/best-traits)]")))
    (testing "values"
      (cli/-main "-g" "[search.examples.list/graph search.graphs.algorithms.genetic/graph]"
                 "-v" "{:max-generations 1}"))))
