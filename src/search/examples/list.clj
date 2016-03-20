(ns search.examples.list
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.utils :as utils]
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

(def graph
  (g/graph
    :->gene (fnk [] binary)
    (g/instance seq/graph {:n-genes 100})
    :mutate (g/instance seq/mutate {:p 0.1})
    :genome->traits (fnk [] score)
    :trait-specs (utils/v->fnk {:value {:lowest? false}})
    :select (g/instance select/dominates {:trait-key :value})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :value (= 100))})))
