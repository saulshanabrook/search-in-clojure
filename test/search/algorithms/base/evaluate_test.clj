(ns search.algorithms.base.evaluate-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [com.rpl.specter :as sp]

            [search.core :as search]
            [search.algorithms.base.evaluate :as evaluate]))
(use-fixtures :once schema.test/validate-schemas)

(deftest genome->traits->-test
  (let [genomes [[1, 2, 3],
                 [1, 2, 3, 4]]
        ind-with-genome #(-> search/Individual g/generate (assoc :genome %))
        generation (-> search/Generation g/generate (assoc :individuals (map ind-with-genome genomes)))

        genome->traits #(hash-map :value (count %))
        evaluated-generation ((evaluate/genome->traits-> {:genome->traits genome->traits}) generation)
        values  (sp/select [:individuals sp/ALL :traits :value] evaluated-generation)]
    (is (= [3, 4] values))))
