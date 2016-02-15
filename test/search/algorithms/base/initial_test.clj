(ns search.algorithms.base.initial-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [conjure.core :as conjure]

            [search.algorithms.base.initial :as initial]
            [search.utils :as utils]))
(use-fixtures :once schema.test/validate-schemas)


(deftest ->genome->-test
  (let [->genome (fn [] :test)
        n 10
        ->initial (initial/->genome-> ->genome n)
        id_ "_"
        run-id "_"]
    (conjure/stubbing [utils/id id_]
      (is (=
           {:index 0
            :run-id run-id
            :individuals (repeat n {:genome :test
                                    :id id_
                                    :parents-ids []
                                    :traits {}})}
           (->initial run-id))))))
