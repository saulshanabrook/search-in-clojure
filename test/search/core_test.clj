(ns search.core-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [schema.test]
            [schema-generators.generators :as g]
            [plumbing.core :refer [fnk]]
            [clojure.test.check.generators :as generators]

            [search.core :as search]
            [search.schemas :as schemas]
            [search.utils :as utils]))

(use-fixtures :once schema.test/validate-schemas)

(def ->generation
  (partial g/generate schemas/Generation {s/Any (generators/choose 0 1)}))

(def generation (->generation))

(def simple-graph
  {:generations (fnk [] (repeat generation))})

(def base-graph
  {:something (fnk [] generation)})

(def depends-on-base-graph
  {:generations (fnk [something] (repeat something))})

(def takes-map-graph
  {:generations (fnk [some-map :- {:generation schemas/Generation}]
                 (repeat (some-map :generation)))})

(defn change-something
  [generation g]
  (assoc g :something (utils/v->fnk generation)))

(defn is-first-generation
  [generation config-args]
  (is (= generation (-> config-args search/->config search/config->generations first))))

(deftest config->generations-test
  (testing "one graph"
    (is-first-generation
      generation
      {:graph-symbols [`simple-graph]}))

  (testing "multiple graphs"
    (is-first-generation
      generation
      {:graph-symbols [`base-graph
                       `depends-on-base-graph]}))

  (testing "values override"
    (let [other-gen (->generation)]
      (is-first-generation
        other-gen
        {:graph-symbols [`base-graph
                         `depends-on-base-graph]
         :values {:something other-gen}})))

  (testing "values override map"
    (is-first-generation
      generation
      {:graph-symbols [`takes-map-graph]
       :values {:some-map {:generation generation}}}))

  (testing "multiple wrappers"
    (let [other-gen (->generation)]
      (is-first-generation
        other-gen
        {:graph-symbols [`depends-on-base-graph]
         :wrapper-forms `[(partial change-something '~(->generation))
                          (partial change-something '~other-gen)
                          identity]}))))
