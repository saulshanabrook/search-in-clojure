(ns search.core-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.utils :as utils]))

(use-fixtures :once schema.test/validate-schemas)

(defn gens-with-search-id
  [search-id]
  (repeatedly #(->
                search/Generation
                g/generate
                (assoc :search-id search-id))))

(def search-id-graph
  {:generations (fnk [search-id] (gens-with-search-id search-id))})

(def base-graph
  {:something (fnk [] "some value")})

(def depends-on-base-graph
  {:generations (fnk [something] (gens-with-search-id something))})

(def takes-map-graph
  {:generations (fnk [some-map :- {:search-id search/SearchID}] (gens-with-search-id (:search-id some-map)))})

(defn change-search-id
  [new-id g]
  (assoc g :search-id (fnk [] new-id)))

(defn is-first-search-id
  [search-id search-args]
  (is (= search-id (-> search-args search/->search search/search->generations first :search-id))))

(deftest search->generations-test
  (testing "search-id"
    (with-redefs [utils/id (fn [] "test-id")]
      (is-first-search-id "test-id" {:graph-symbols [`search-id-graph]})))

  (testing "multiple graphs"
    (is-first-search-id "some value" {:graph-symbols [`base-graph
                                                      `depends-on-base-graph]}))

  (testing "values override"
    (is-first-search-id
      "some other value"
      {:graph-symbols [`base-graph
                       `depends-on-base-graph]
       :values {:something "some other value"}}))

  (testing "values override map"
    (is-first-search-id
      "yeah"
      {:graph-symbols [`takes-map-graph]
       :values {:some-map {:search-id "yeah"}}}))

  (testing "multiple wrappers"
    (is-first-search-id
      "second"
      {:graph-symbols [`search-id-graph]
       :wrapper-forms `[(partial change-search-id "first")
                        (partial change-search-id "second")
                        identity]})))
