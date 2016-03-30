(ns search.graphs.base.evaluate-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [com.rpl.specter :as sp]

            [search.schemas :as schemas]
            [search.graphs.base.evaluate :as evaluate]))
(use-fixtures :once schema.test/validate-schemas)

(deftest genome->traits->-test
  (let [genomes [[1, 2, 3],
                 [1, 2, 3, 4]]
        ind-with-genome #(-> schemas/Individual g/generate (assoc :genome %))
        generation (-> schemas/Generation g/generate (assoc :individuals (into #{} (map ind-with-genome genomes))))

        genome->traits #(hash-map :value (count %))
        evaluated-generation ((evaluate/genome->traits-> {:genome->traits genome->traits}) generation)
        values  (sp/select [:individuals sp/ALL :traits :value] evaluated-generation)]
    (is (= #{3, 4} (into #{} values)))))
