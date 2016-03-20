(ns search.wrappers.graph-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [conjure.core :as conjure]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.wrappers.graph :as graph-utils]))

(use-fixtures :once schema.test/validate-schemas)


(deftest log-wrap-test
  (conjure/mocking [graph-utils/println-level]
    (is (= 4 ((-> {:square (fnk [mult] (fn [a] (mult a a)))
                   :mult (fnk [] (fn [a b] (* a b)))}
               graph-utils/log-wrap
               (g/run {})
               :square)
              2)))
    (conjure/verify-nth-call-args-for 1 graph-utils/println-level 0 "Calling" [:square])
    (conjure/verify-nth-call-args-for 2 graph-utils/println-level 1 "Calling" [:mult])
    (conjure/verify-nth-call-args-for 3 graph-utils/println-level 1 "Finished" [:mult])
    (conjure/verify-nth-call-args-for 4 graph-utils/println-level 0 "Finished" [:square])))
