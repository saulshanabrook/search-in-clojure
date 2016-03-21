(ns search.graphs.problems.push-sr-linear
  "Make a push program that performs linear symbolic regression"
  (:require [plumbing.graph :as g]

            [search.graphs.push-sr :as push-sr]))


(def graph
  (g/instance push-sr/graph
    {:xs (range 0 20)
     :->y (partial * 2)
     :output-stack :integer}))
