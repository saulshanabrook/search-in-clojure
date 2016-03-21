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
  {:value (apply + ind)})

(s/defn binary :- Gene
  "random int, either 1 or 0"
  []
  (rand-int 2))

(def graph
  (g/graph
    :->gene (utils/v->fnk binary)
    (assoc (g/instance seq/graph {:n-genes 100})
      :tweak-weights (utils/v->fnk {:mutate 1}))
    :mutate (g/instance seq/mutate {:p 0.01})
    :genome->traits (fnk [] score)
    :trait-specs (utils/v->fnk {:value {:lowest? false}})
    :select (g/instance select/dominates {:trait-key :value})
    :done? (g/instance done/any-trait {:traits->done? #(-> % :value (= 100))})))
