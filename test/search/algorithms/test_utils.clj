(ns search.algorithms.test-utils
  (:require [clojure.test :refer [is]]
            [schema.core :as s]
            [plumbing.fnk.pfnk]
            [plumbing.fnk.schema]))

(defn is-algorithm
  [g inputs]
  ; Right now we are just checking to make sure the resaulting algorithm
  ; requieres the proper keys, instead it would be better to make sure then
  ; types of the keys are correct as well.
  ;
  ; The code below does this, but it fails because of some weirdnesses
  ; on funciton types not being the same, even when they are
  ; (is (= (merge {:run-id s/Str
  ;                s/Keyword s/Any}
  ;                 inputs)
  ;        (pfnk/input-schema g))))
  (is (= (set (conj (keys inputs) :run-id))
         (->
          g
          plumbing.fnk.pfnk/input-schema
          plumbing.fnk.schema/explicit-schema-key-map
          keys
          set))))
