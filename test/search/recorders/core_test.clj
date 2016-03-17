(ns search.recorders.core-test
  (:require [clojure.test :refer :all]
            [conjure.core :as conjure]
            [search.conjure-utils :refer [verify-first-call-args-for-p]]
            [schema.test]
            [plumbing.core :refer [fnk]]
            [schema.experimental.generators :as g]
            [plumbing.graph]

            [search.recorders.core :as recorders]
            [search.core :as search]))

(use-fixtures :once schema.test/validate-schemas)

(defn started! [_ _] nil)
(defn generation! [_] nil)
(defn done! [_] nil)

(deftest wrap-test
  (conjure/instrumenting [started! generation! done!]
    (let [search (g/generate search/Search)
          search-id (g/generate search/SearchID)
          gens (repeatedly 10 #(g/generate search/Generation))
          g {:search-id (fnk [] search-id)
             :generations (fnk [] gens)}
          recorder {:started! started!
                    :generation! generation!
                    :done! done!}
          wrapped-g (recorders/wrap recorder g)
          wrapped-gens (-> wrapped-g (plumbing.graph/run {:search search}) :generations)]

      (conjure/verify-call-times-for started! 0)
      (conjure/verify-call-times-for generation! 0)
      (conjure/verify-call-times-for done! 0)

      (is (= (first wrapped-gens) (first gens)))
      (conjure/verify-called-once-with-args started! search search-id)
      (conjure/verify-called-once-with-args generation! (first gens))

      (is (= (last wrapped-gens) (last gens)))
      (conjure/verify-called-once-with-args done! search-id)
      (conjure/verify-nth-call-args-for 10 generation! (last gens)))))
