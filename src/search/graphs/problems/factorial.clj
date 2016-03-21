(ns search.graphs.problems.factorial
  "Make a push program that performs symbolic regression on the factorial function"
  (:require [plumbing.graph :as g]
            [schema.core :as s]
            [clojure.math.numeric-tower :as math]

            [search.utils :refer [v->fnk]]
            [search.graphs.push-sr :as push-sr]
            [search.graphs.algorithms.genetic :as genetic]))

(s/defn factorial :- s/Int
  [n :- s/Int]
  (reduce * (range 1 (inc n))))

(def graph
  (g/instance push-sr/graph
    {:xs (range 1 11)
     :->y factorial
     :output-stack :integer}))

(def clojush-graph
  "Meant to replicate https://github.com/lspector/Clojush/blob/master/src/clojush/problems/:integer-regression/factorial.clj
   as closely as possible."
  (g/graph
   (assoc (g/graph graph genetic/graph)
    :test-output->trait-value
      (v->fnk
        (s/fn [exp :- s/Num
               calculated :- (s/maybe s/Num)]
          (if (nil? calculated)
            1000000000
            (math/abs (- exp calculated)))))
    :population-size (v->fnk 1000)
    :step-limit (v->fnk 1000)
    :alternation-p (v->fnk 0.05)
    :mutate-p (v->fnk 0.05)
    :tweak-weights (v->fnk {:mutate 1 :alternation 1})
    :instructions
      (v->fnk
       [0
        1
        :boolean-and
        :boolean-dup
        :boolean-equal?
        :integer->boolean
        :boolean-not
        :boolean-or
        :boolean-pop
        :boolean-rotate
        :boolean-swap
        :exec-dup
        :exec-equal?
        :exec-if
        :exec-k
        :exec-noop
        :exec-pop
        :exec-rotate
        :exec-s
        :exec-swap
        :exec-when
        :exec-y
        :integer-add
        :integer-divide
        :integer-dup
        :integer-equal?
        :boolean->integer
        :integer>?
        :integer<?
        :integer-mod
        :integer-multiply
        :integer-pop
        :integer-rotate
        :integer-subtract
        :integer-swap]))))
