(ns search.graphs.problems.push-sr
  "Make a push program that performs symbolic regression"
  (:require [plumbing.graph :as g]

            [search.graphs.push-sr :as push-sr]))


(def double-graph
  (g/instance push-sr/graph
    {:xs (range 0 10)
     :->y (partial * 2)
     :output-stack :integer}))

(def plus-six-graph
  (g/instance push-sr/graph
    {:xs (range 0 10)
     :->y (partial + 6)
     :output-stack :integer}))

(def idea-of-numbers-graph
  (g/instance push-sr/graph
    {:xs (range 0 10)
     :->y (fn [x] (+ 1995 (* 9 x) (* 15 x x)))
     :output-stack :integer}))

(def sin-graph
  (g/instance push-sr/graph
    {:xs (range 0 6 0.2)
     :->y #(Math/sin %)
     :output-stack :float}))
