(ns search.conjure-utils
  (:require [conjure.core :refer :all]
            [clojure.test :refer [do-report]]))

; all copied from https://github.com/amitrathore/conjure/issues/12#issuecomment-124854191
(defprotocol IArgMatcher
  (-matches-arg? [this x]))

(extend-type clojure.lang.IFn
  IArgMatcher
  (-matches-arg? [this x]
    (true? (this x))))

(extend-type java.util.regex.Pattern
  IArgMatcher
  (-matches-arg? [this x]
    (some? (re-matches this (str x)))))

(extend-type Object
  IArgMatcher
  (-matches-arg? [this x]
    (= this x)))

(defmacro verify-first-call-args-for-p
  "Asserts that the faked function was called at least once, and the first call
   was passed the args matching the specified predicates/values"
  [fn-name & args]
  `(do
     (assert-in-fake-context "verify-first-call-args-for-p")
     (assert-conjurified-fn "verify-first-call-args-for-p" ~fn-name)
     (when-not (pos? (count (get @call-times ~fn-name)))
       (do-report {:type     :fail, :message "Expected function not invoked"
                   :expected '(~fn-name ~@args)}))
     (let [actual-args# (first (get @conjure.core/call-times ~fn-name))
           arg-preds-match# (map -matches-arg? ~(vec args) actual-args#)]
       (when-not (every? true? arg-preds-match#)
         (do-report {:type     :fail, :message "Actual arguments do not match expectations"
                     :expected '(~fn-name ~@args), :actual (apply list '~fn-name actual-args#)})))))

(def __ (constantly true))
