(ns search.config.evaluate-test
  (:require [schema.core :as s]
            [schema.test :as st]
            [clojure.test :refer :all]

            [search.config.evaluate :as evaluate]
            [search.config.core :refer [->config]]))



(def require-sample (evaluate/->require 'search.config.evaluate-test/some-test-var))

(st/deftest symbol->value-test
  (is (= :test-val (evaluate/symbol->value 'search.config.evaluate-test/some-test-var))))

(def some-test-var :test-val)
(st/deftest require-test
  (let [_require (evaluate/->require 'search.config.evaluate-test/some-test-var)]
    (is (= some-test-var (evaluate/evaluate (->config) _require)))
    (testing "recursively-evaluate"
      (is (= some-test-var (evaluate/recursively-evaluate (->config) _require))))))

(defn some-test-fn [arg] arg)
(st/deftest call-test
  (let [_call (evaluate/->call 'search.config.evaluate-test/some-test-fn :dog)]
    (is (= :dog (evaluate/evaluate (->config) _call)))
    (testing "recursively-evaluate"
      (is (= :dog (evaluate/recursively-evaluate (->config) _call))))))

(st/deftest get-in-config-test
  (let [_config (->config {:first {:second :final-value}})
        _get-in-config (evaluate/->get-in-config :first :second)]
    (is (= :final-value (evaluate/evaluate _config _get-in-config)))
    (testing "recursively-evaluate"
      (is (= :final-value
             (evaluate/recursively-evaluate _config _get-in-config)))
      (is (= :test-val
             (evaluate/recursively-evaluate
               (->config {:first {:second require-sample}})
               _get-in-config))))))

(st/deftest recursively-evaluate-test
  (is (= :test-key (evaluate/recursively-evaluate (->config) :test-key))))
