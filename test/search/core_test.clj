(ns search.core-test
  (:require [clojure.test :refer :all]
            [conjure.core :as conjure]
            [search.conjure-utils :refer [verify-first-call-args-for-p]]
            [schema.test :as st]
            [schema.experimental.generators :as g]

            [search.core :as search]
            [search.schemas :as schemas]
            [search.config.core :as config]
            [search.config.evaluate :refer [->require]]))


(defn record-config! [_])
(defn record-run! [_])
(defn record-generation! [_])
(defn record-run-done! [_])

(def sample-generation (g/generate schemas/Generation))
(defn sample-algorithm
  [run_id]
  [(assoc sample-generation :run-id run_id)])

(st/deftest execute-test
  (conjure/instrumenting [record-config! record-run! record-generation! record-run-done!]
    (let [recorder_ {:record-config! record-config!
                     :record-run! record-run!
                     :record-generation! record-generation!
                     :record-run-done! record-run-done!}
          config_ (config/->config {:algorithm
                                    (->require 'search.core-test/sample-algorithm)})]
      (search/execute recorder_ config_)
      (testing "record-config!"
        (conjure/verify-called-once-with-args record-config! config_))
      (testing "record-run!"
        (conjure/verify-call-times-for record-run! 1)
        (verify-first-call-args-for-p record-run! #(= config_ (:config %)))
        (verify-first-call-args-for-p record-run! #(not (clojure.string/blank? (:id %)))))
      (testing "record-generation!"
        (conjure/verify-call-times-for record-generation! 1)
        (verify-first-call-args-for-p record-generation! #(not (clojure.string/blank? (:run-id %))))
        (verify-first-call-args-for-p record-generation! #(=
                                                           (dissoc sample-generation :run-id)
                                                           (dissoc % :run-id))))
      (testing "record-run-done!"
        (conjure/verify-call-times-for record-run-done! 1)
        (verify-first-call-args-for-p record-run-done! #(= config_ (:config %)))
        (verify-first-call-args-for-p record-run-done! #(not (clojure.string/blank? (:id %))))))))
