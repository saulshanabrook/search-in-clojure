(ns search.cli-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [plumbing.core :refer [fnk]]
            [schema-generators.generators :as g]

            [search.schemas :as schemas]
            [search.cli :as cli]))

(use-fixtures :once schema.test/validate-schemas)

(def dep-graph {:n (fnk [] 10)})
(def graph {:generations (fnk [n] (repeatedly n (partial g/generate schemas/Generation)))})

(deftest ^:slow test-main
  (testing "graphs"
    (cli/-main "-g" "[search.cli-test/dep-graph search.cli-test/graph]"))
  (testing "recorders"
    (with-out-str
      (cli/-main "-g" "[search.cli-test/dep-graph search.cli-test/graph]"
                 "-w" "[(partial search.wrappers.recorders/wrap search.wrappers.recorders/timbre)]")))
  (testing "values"
    (cli/-main "-g" "[search.cli-test/dep-graph search.cli-test/graph]"
               "-v" "{:n 1}")))
