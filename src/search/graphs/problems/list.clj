(ns search.graphs.problems.list
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.utils :as utils]
            [search.graphs.base.select :as select]
            [search.graphs.seq :as seq]
            [search.graphs.base.done :as done]))

(def Gene s/Int)
(def Genome [Gene])

(s/defn score :- {:value s/Int}
  "count of the number of ones in the list"
  [ind :- Genome]
  {:count-ones (apply + ind)})

(s/defn binary :- Gene
  "random int, either 1 or 0"
  []
  (rand-int 2))

(def graph
  (g/graph
    :->gene (utils/v->fnk binary)
    :n-genes (utils/v->fnk 100)
    seq/graph
    :mutate (g/instance seq/mutate {:p 0.01})
    :genome->traits (utils/v->fnk score)
    :trait-specs (utils/v->fnk {:count-ones {:lowest? false}})
    :select (g/instance select/dominates {:trait-key :count-ones})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :count-ones (= 100))})))
