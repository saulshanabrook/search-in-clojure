(ns search.algorithms.base.initial-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [conjure.core :as conjure]

            [search.algorithms.base.initial :as initial]
            [search.utils :as utils]))
(use-fixtures :once schema.test/validate-schemas)


(deftest ->genome-test
  (let [n 10
        run-id "_"
        id_ "_"]
    (conjure/stubbing [utils/id id_]
      (is (=
           {:index 0
            :run-id run-id
            :individuals (repeat n {:genome :test
                                    :id id_
                                    :parents-ids []
                                    :traits {}})}
           (initial/->genome {:->genome (fn [] :test)
                              :n n
                              :run-id run-id}))))))
