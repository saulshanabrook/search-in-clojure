(ns search.graphs.problems.odd
  "The 'odd' problem: take a positive integer input and push a Boolean indicating
  whether or not the input is an odd number. There are many ways to compute this
  and PushGP sometimes finds unusual methods.

  Copied from [`clojush.problems.demos.odd`](https://github.com/lspector/Clojush/blob/e704c850f0811b75e19ac945b9d0787ee3a3e9ec/src/clojush/problems/demos/odd.clj)"
  (:require [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [schema.core :as s]

            [search.graphs.push :as push]
            [search.graphs.base.select :as select]
            [search.graphs.base.done :as done]
            [search.graphs.testcases :as testcases]))


(def graph
  (g/graph
    (g/instance push/graph {:push-bindings [:x]
                            :output-stack :boolean
                            :n-genes 20})
    :xs (fnk [] (range 0 10))
    :ys (fnk [xs] (map odd? xs))
    :test-cases (fnk test-cases :- {s/Num s/Num}
                  [xs ys]
                  (zipmap xs ys))
    :test-fn (fnk [push-evaluate] (fn [genome x] (push-evaluate genome {:x x})))
    :test-output->trait-value (fnk [] (fn [a b] (if (= a b) 0 1)))
    testcases/graph
    :trait-specs (fnk trait-specs :- select/TraitSpecs
                  [xs]
                  (zipmap xs (repeat {:lowest? true})))
    :select select/lexicase
    :traits->done? (fnk [] (comp (partial every? zero?) vals))
    :done? done/any-trait))
