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

(defn first-search-id
  [search]
  (-> search search/search->generations first :search-id))

(deftest search->generations-test
  (testing "search-id"
    (with-redefs [utils/id (fn [] "test-id")]
      (let [search_ {:graph-symbols [`search-id-graph]
                     :values {}
                     :wrapper-symbols []}]
        (is (= "test-id" (first-search-id search_))))))

  (testing "multiple graphs"
    (let [search_ {:graph-symbols [`base-graph
                                   `depends-on-base-graph]
                   :values {}
                   :wrapper-symbols []}]
      (is (= "some value" (first-search-id search_)))))

  (testing "values override"
    (let [search_ {:graph-symbols [`base-graph
                                   `depends-on-base-graph]
                   :values {:something "some other value"}
                   :wrapper-symbols []}]
      (is (= "some other value" (first-search-id search_)))))

  (testing "values override map"
    (let [search_ {:graph-symbols [`takes-map-graph]
                   :values {:some-map {:search-id "yeah"}}
                   :wrapper-symbols []}]
      (is (= "yeah" (first-search-id search_)))))

  (testing "multiple wrappers"
    (let [search_ {:graph-symbols [`search-id-graph]
                   :values {}
                   :wrapper-symbols [[`change-search-id "first"]
                                     [`change-search-id "second"]]}]
      (is (= "second" (first-search-id search_))))))
