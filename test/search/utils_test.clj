(ns search.utils-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [schema.test]

            [search.utils :as utils]))

(use-fixtures :once schema.test/validate-schemas)

(def sample-value 1)

(deftest value->symbol-test
  (is (= 'clojure.core/get-in (utils/value->symbol get-in)))
  (is (= 'search.utils-test/sample-value (utils/value->symbol sample-value))))

(deftest rand-true?-test
  (is (utils/rand-true? 1))
  (is (not (utils/rand-true? 0))))

(def require-sample :test-val)

(deftest symbol->value-test
  (is (= :test-val (utils/symbol->value 'search.utils-test/require-sample))))

(deftest defnk-fn-test
  (let [macro-form `(utils/defnk-fn ~'hi :- [s/Int]
                       "description"
                       [~'a :- s/Int]
                       [~'b :- s/Int]
                       [~'a ~'b])
        expanded (macroexpand-1 macro-form)
        intended-expanded `(defnk ~'hi
                            "description"
                            [~'a :- s/Int]
                            (s/fn ~'hi-inner :- [s/Int]
                              [~'b :- s/Int]
                              [~'a ~'b]))]
    (is (= intended-expanded expanded)))
   ; (are [exp orig] (= exp (macroexpand-1 (macroexpand-1 orig))))
    ; `(fnk [] (s/fn [] :value)) '(utils/defnk-fn [] [] :value)
    ; `(fnk [a] (s/fn [b] [a b])) '(utils/defnk-fn [a] [b] [a b])
    ; `(fnk hi [] (s/fn hi-inner [] :value)) '(utils/defnk-fn hi [] [] :value)
    ; `(fnk hi [] (s/fn hi-inner :- return-schema [] :value)) '(utils/defnk-fn hi :- return-schema [] [] :value))
  (utils/defnk-fn test-fn :- s/Int "test" [a] [b] (+ a b))
  (is (= 3 ((test-fn {:a 1}) 2))))
