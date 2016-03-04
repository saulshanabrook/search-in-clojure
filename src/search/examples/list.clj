(ns search.examples.list
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.algorithms.base.select :as select]
            [search.algorithms.seq :as seq]
            [search.algorithms.base.done :as done]))

(def Gene s/Int)
(def Genome [Gene])

(s/defn score :- {:value s/Int}
  "count of the number of ones in the list"
  [ind :- Genome]
  {:value (->>
           ind
           (filter (partial = 1))
           count)})

(s/defn binary :- Gene
  "random int, either 1 or 0"
  []
  (rand-int 2))

(def problem-graph
  (g/graph
    :->gene (fnk [] binary)
    (g/instance seq/graph {:n 100})
    :mutate (g/instance seq/mutate {:p 0.1})
    :genome->traits (fnk [] score)
    :select (g/instance select/dominates {:trait-name :value :lowest? false})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :value (= 100))})))

(def hill-climb-config
  (search/->config ['search.examples.list/problem-graph
                    'search.algorithms.hill-climb/graph]))

(def genetic-config
  (search/->config ['search.examples.list/problem-graph
                    'search.algorithms.genetic/graph]))
