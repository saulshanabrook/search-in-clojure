(ns search.graphs.problems.factorial
  "Make a push program that performs symbolic regression on the factorial function"
  (:require [plumbing.graph :as g]
            [schema.core :as s]
            [clojure.math.numeric-tower :as math]
            [clojure.data.generators]

            [search.utils :refer [v->fnk defnk-fn] :as utils]
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

(defn gaussian-noise-factor
  "Returns gaussian noise of mean 0, std dev 1.

  lifted from https://github.com/lspector/Clojush/blob/e704c850f0811b75e19ac945b9d0787ee3a3e9ec/src/clojush/pushgp/genetic_operators.clj#L18"
  []
  (* (Math/sqrt (* -2.0 (Math/log (clojure.data.generators/long))))
     (Math/cos (* 2.0 Math/PI (clojure.data.generators/long)))))

(defnk-fn alternation-clojush :- [s/Any]
  "Uniformly alternates between the two parents using a similar method to that
  used in ULTRA.

  Lifted from
  [`clojush.pushgp.genetic-operators/alternation`](https://github.com/lspector/Clojush/blob/e704c850f0811b75e19ac945b9d0787ee3a3e9ec/src/clojush/pushgp/genetic_operators.clj#L159)"
  [alternation-rate :- utils/Probability
   alignment-deviation :- s/Num
   max-points :- s/Int]
  [a :- [s/Any]
   b :- [s/Any]]
  (loop [i 0
         use-a (utils/rand-true? 0.5)
         result-genome []]
    (if (or (>= i (count (if use-a a b))) ;; finished current program
            (> (count result-genome) max-points)) ;; runaway growth
      result-genome
      (if (utils/rand-true? alternation-rate)
        (recur (max 0 (+ i (Math/round (* alignment-deviation (gaussian-noise-factor)))))
               (not use-a)
               result-genome)
        (recur (inc i)
               use-a
               (conj result-genome (nth (if use-a a b) i)))))))


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
    :mutate-p (v->fnk 0.05)
    :tweak-labels
    {:alternation {:f (g/instance alternation-clojush {:alternation-rate 0.05
                                                       :alignment-deviation 10
                                                       :max-points 1000})
                   :n-parents (v->fnk 2)
                   :multiple-children? (v->fnk false)}
     :mutate (-> (g/graph graph genetic/graph) :tweak-labels :mutate)}
    :tweak-label-weights (v->fnk {[:mutate :alternation] 1})
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
