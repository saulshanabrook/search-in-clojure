(ns search.graphs.testcases
  "Graph parts to facilitate using multiple test cases to evaluate your genomes.

  For example, if you are trying to evolve a function, like y=x^2, to evaluate
  it you might test a bunch `[x, y]` pairs."
  (:require [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [schema.core :as s]

            [search.schemas :as schemas]
            [search.utils :refer [defnk-fn]]))

(def TestInput s/Any)
(def TestOutput s/Any)
(def TestCases {TestInput TestOutput})
(def TestFn
  "Uses the genome to evaluate the test-input, returning the test-output"
  (s/=> TestOutput schemas/Genome TestInput))

(def TestOutput->TraitValue
  "Takes the intended test case output and the computed test case output
   and returns the 'value' for this pair. For example, this could take the
   squared difference between the two, or just the absolute value of the difference."
  (s/=> schemas/TraitValue TestOutput TestOutput))

(defnk-fn testcases->traits :- schemas/Traits
  "Evaluates the `test-fn` on each of the inputs in `test-cases`. The traits
  it returns have keys equal to the test cases inputs and the outputs
  computed by calling `test-output->trait-value` on each actual value, to computed
  value."
  [test-cases :- TestCases
   test-output->trait-value :- TestOutput->TraitValue
   test-fn :- TestFn]
  [genome :- schemas/Genome]
  (into {}
    (map
      (fn [[t-in t-out]]
        [t-in
         (test-output->trait-value t-out (test-fn genome t-in))])
      test-cases)))

(def graph
  (g/graph
    :genome->traits testcases->traits))
