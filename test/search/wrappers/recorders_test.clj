(ns search.wrappers.recorders-test
  (:require [clojure.test :refer :all]
            [conjure.core :as conjure]
            [schema.test]
            [plumbing.core :refer [fnk]]
            [schema-generators.generators :as g]
            [clojure.test.check.generators :as generators]
            [plumbing.graph]

            [search.wrappers.recorders :as recorders]
            [search.utils :as utils]
            [search.schemas :as schemas]))

(use-fixtures :once schema.test/validate-schemas)

(defn started! [_] nil)
(defn generation! [_ _] nil)
(defn done! [_] nil)

(deftest wrap-test
  (conjure/instrumenting [started! generation! done!]
    (let [gens (repeatedly 10 #(g/generate schemas/Generation))
          metadata (g/generate recorders/Metadata {utils/Probability (generators/choose 0 1)})
          g (assoc (plumbing.core/map-vals utils/v->fnk metadata)
              :generations (fnk [] gens))
          recorder {:started! started!
                    :generation! generation!
                    :done! done!}
          wrapped-gens (-> (recorders/wrap recorder g) (plumbing.graph/run {}) :generations)]

      (conjure/verify-call-times-for started! 0)
      (conjure/verify-call-times-for generation! 0)
      (conjure/verify-call-times-for done! 0)

      (is (= (first wrapped-gens) (first gens)))
      (conjure/verify-called-once-with-args started! metadata)
      (conjure/verify-called-once-with-args generation! metadata (first gens))

      (is (= (last wrapped-gens) (last gens)))
      (conjure/verify-called-once-with-args done! metadata)
      (conjure/verify-nth-call-args-for 10 generation! metadata (last gens)))))
