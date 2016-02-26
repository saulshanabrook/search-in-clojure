(ns search.examples.list
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.core :as search]
            [search.algorithms.base.done :as done]))

(def Gene s/Int)
(def Genome [Gene])

(s/defn score :- s/Int
  "count of the number of ones in the list"
  [ind :- Genome]
  (->>
    ind
    (filter (partial = 1))
    count))

(s/defn binary :- Gene
  "random int, either 1 or 0"
  []
  (rand-int 2))

(s/defn individual :- Genome
  "a new random list of 1s and 0s"
  []
  (into [] (repeatedly 10 binary)))

(s/defn mutate :- Genome
  "changes a random index of the list to a 0|1"
  [ind :- Genome]
  (assoc ind (rand-int 10) (binary)))

(def problem-graph
  (g/graph
    :->genome (fnk [] individual)
    :mutate (fnk [] mutate)
    :genome->value (fnk [] score)
    :done? (g/instance done/max-trait {:name :value :max_ 10})))

(def hill-climb-config
  (search/->config ['search.algorithms.hill-climb/graph
                    'search.examples.list/problem-graph]))
