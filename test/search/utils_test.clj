(ns search.utils-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [plumbing.core :refer [defnk fnk]]
            [plumbing.graph :as g]
            [conjure.core :as conjure]
            [plumbing.fnk.pfnk :as pfnk]
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


(utils/defnk-fn test-fn :- s/Int "test" [a] [b] (+ a b))
(deftest defnk-fn-test
  (let [macro-form `(utils/defnk-fn ~'hi :- [s/Int]
                       "description"
                       [~'a :- s/Int]
                       [~'arg0 :- s/Int]
                       [~'a ~'arg0])
        expanded (macroexpand-1 macro-form)
        intended-expanded `(defnk ~'hi :- ~(s/=> [s/Int] s/Int)
                            "description"
                            [~'a :- s/Int]
                            (s/fn ~'hi-inner :- [s/Int]
                              [~'arg0 :- s/Int]
                              [~'a ~'arg0]))]
    (is (= intended-expanded expanded)))
   ; (are [exp orig] (= exp (macroexpand-1 (macroexpand-1 orig))))
    ; `(fnk [] (s/fn [] :value)) '(utils/defnk-fn [] [] :value)
    ; `(fnk [a] (s/fn [b] [a b])) '(utils/defnk-fn [a] [b] [a b])
    ; `(fnk hi [] (s/fn hi-inner [] :value)) '(utils/defnk-fn hi [] [] :value)
    ; `(fnk hi [] (s/fn hi-inner :- return-schema [] :value)) '(utils/defnk-fn hi :- return-schema [] [] :value))
  (is (= 3 ((test-fn {:a 1}) 2))))

(deftest seq->fn-test
  (let [f (utils/seq->fn (cycle [1 2]))]

    (is (= 1 (f)))
    (is (= 2 (f)))
    (is (= 1 (f))))

  (is (= [1 2] (repeatedly 2 (utils/seq->fn [1 2])))))

(deftest repeatedly-set-test
  (is (= #{} (utils/repeatedly-set 0 identity)))
  (is (= #{1 2} (utils/repeatedly-set 2 (utils/seq->fn [1 1 1 2 3 4]))))
  (is (= #{1 2} (utils/repeatedly-set 2 (utils/seq->fn (cycle [1 2 3]))))))

(deftest take-set-test
  (is (= #{} (utils/take-set 0 (repeat 3))))
  (is (= #{1} (utils/take-set 1 (repeat 1))))
  (is (= #{1 2} (utils/take-set 2 (cycle [1 1 2 3])))))



(defn do! [] :something)
(defn do-arg! [a] a)

(deftest do-before-test
  (conjure/instrumenting [do!]
    (let [xs (utils/do-before do! (repeat :hey))]
      (conjure/verify-call-times-for do! 0)

      (is (= :hey (first xs)))
      (conjure/verify-call-times-for do! 1)

      (is (= :hey (second xs)))
      (conjure/verify-call-times-for do! 1))))


(deftest do-during-test
  (conjure/instrumenting [do-arg!]
    (let [xs (utils/do-during do-arg! (cycle [1 2 3]))]
      (conjure/verify-call-times-for do-arg! 0)

      (is (= 1 (first xs)))
      (conjure/verify-called-once-with-args do-arg! 1)

      (is (= 2 (second xs)))
      (conjure/verify-nth-call-args-for 2 do-arg! 2))))

(deftest do-after-test
  (conjure/instrumenting [do!]
    (let [xs (utils/do-after do! [1 2])]
      (conjure/verify-call-times-for do! 0)

      (is (= 1 (first xs)))
      (conjure/verify-call-times-for do! 0)

      (is (= 2 (second xs)))
      (conjure/verify-call-times-for do! 0)

      (is (= [1 2] (doall xs)))
      (conjure/verify-call-times-for do! 1))))

(defnk square :- s/Int
  [i :- s/Int]
  (* i i))

(defmacro is-same-schema
  [intended actual]
  `(is (= (pfnk/input-schema ~intended) (pfnk/input-schema ~actual)))
  `(is (= (pfnk/output-schema ~intended) (pfnk/output-schema ~actual))))

(defmacro is-square-then-incr
  [f]
  `(is-same-schema square ~f)
  `(is (= 5 (~f {:i 2}))))

(deftest wrap-test
  (is-square-then-incr (utils/wrap square (comp inc square))))

(deftest wrap-output-test
  (is-square-then-incr (utils/wrap-output square inc)))

(defmacro is-square
  [f]
  `(is-same-schema square ~f)
  `(is (= 4 (~f {:i 2}))))

(defnk do-square-input! :- s/Keyword
  [i :- s/Int]
  :something)


(deftest wrap-before-test
  (conjure/instrumenting [do-square-input!]
    (is-square (utils/wrap-before square do-square-input!))
    (conjure/verify-called-once-with-args do-square-input! {:i 2})))

(s/defn do-square-output! :- s/Keyword
  [_ :- s/Int]
  :something)


(deftest wrap-after-test
  (conjure/instrumenting [do-square-output!]
    (is-square (utils/wrap-after square do-square-output!))
    (conjure/verify-called-once-with-args do-square-output! 4)))


(deftest map-leaf-fns-test
  (let [orig-f (fnk hello :- s/Str [] "hello")
        g {:int (fnk [i] i)
           :f (fnk [] orig-f)}
        wrapper (fnk [f ks] (utils/wrap-output f #(str % ks)))
        wrapped-g (utils/map-leaf-fns wrapper g)
        ran (g/run wrapped-g {:i 2})]

    ; non functions shouldnt be wrapped
    (is (= 2 (:int ran)))

    ; resaulting fn should be wrapped
    (is-same-schema orig-f (:f ran))
    (is (= "hello[:f]" ((:f ran) {})))))
