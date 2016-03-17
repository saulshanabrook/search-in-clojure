(ns search.core
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk defnk]]
            [plumbing.fnk.schema]

            [search.utils :as utils]))

(def Genome s/Any)

(def TraitKey s/Any)
(def TraitValue s/Int)

(def Traits
  "Traits are any information we want to know about an indivual. For single
  objective search we commonly use a `:value` trait."
  {TraitKey TraitValue})

(def Individual
  {:genome Genome
   :id s/Str
   :traits Traits
   :parents-ids #{s/Str}})

(def Generation
  "Holds the whole state for a current generation of individuals."
  {:search-id s/Str
   :index s/Int
   :individuals #{Individual}})

(def Wrapper [(s/one s/Symbol "fn") s/Any])

(def Search
  "A search configuration is represented as a map. It contains all the data
   neccesary to run the search in a serializiable form, so that it can be
   preserved in a text form.

   The most important are the `graph-symbols`. These are a list of symbols
   that should point to partial graphs which are all merged.

   Then `values` are used to override keys with values.

   The `wrapper-symbols` represent a sequence of functions that take in a graphs
   and return an udpated graph. They are represented as `[fn-symbol & optional-args]`
   and called like `((apply partial fn optional-args) graph)`. If any of the
   args is a symbol, they are resolved into the value at that symbol. The leftmost
   one is called first, like the thread macro.
   "
  {:graph-symbols [s/Symbol]
   :values {s/Keyword s/Any}
   :wrapper-symbols [Wrapper]})

(def SearchID s/Str)
(def SearchGraph (s/constrained utils/Graph #(contains? % :generations)))

(defnk ->search :- Search
  [{graph-symbols []}
   {values {}}
   {wrapper-symbols []}]
  {:graph-symbols graph-symbols
   :values values
   :wrapper-symbols wrapper-symbols})

(defn val->fnk
  "Returns a fnk that just returns the passed in value

  We have to make sure the output has the right schema to deal with
  https://github.com/plumatic/plumbing/issues/117"
  [v]
  (s/schematize-fn
    (fn [_] v)
    (s/=>
      (eval (plumbing.fnk.schema/guess-expr-output-schema v))
      {s/Keyword s/Any})))

(def compute-search-graph
  (g/graph
   :default-graph (fnk [] (hash-map :search-id (fnk [] (utils/id))))
   :graphs (fnk [graph-symbols :- [s/Symbol]] (map utils/symbol->value graph-symbols))
   :graph (fnk [graphs :- [utils/Graph]] (apply g/graph graphs))
   :values-graph (fnk [values :- {s/Keyword s/Any}]
                  (->> values
                    (plumbing.core/map-vals val->fnk)
                    g/graph))
   :wrappers (fnk [wrapper-symbols :- [Wrapper]]
              (let [opt-symbol->value #(if (symbol? %1) (utils/symbol->value %1) %1)]
                (map (partial map opt-symbol->value) wrapper-symbols)))
   :wrapper-fns (fnk [wrappers :- [s/Any]] (map #(apply partial (first %1) (rest %1)) wrappers))
   :wrapper-fn (fnk [wrapper-fns :- [s/Any]] (apply comp (reverse wrapper-fns)))
   :final-graph (fnk [default-graph :- utils/Graph
                      graph :- utils/Graph
                      values-graph :- utils/Graph
                      wrapper-fn]
                 (-> default-graph
                  (g/graph graph)
                  (merge values-graph)
                  wrapper-fn))
   :computed (fnk [final-graph :- SearchGraph search :- Search]
              (g/run final-graph {:search search}))))

(def compute-search (g/compile compute-search-graph))

(s/defn search->generations ; infinite sequence of generations
  "Computes the generations for the search. Returns a (possibly infinite)
  lazy sequence of generations. The computation happens when you resolve them."
  [search :- Search]
  (-> search
    (assoc :search search)
    compute-search
    :computed
    :generations))
