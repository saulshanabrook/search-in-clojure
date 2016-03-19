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

(def Wrapper s/Any)

(def Search
  "A search configuration is represented as a map. It contains all the data
   neccesary to run the search in a serializiable form, so that it can be
   preserved in a text form.

   The most important are the `graph-symbols`. These are a list of symbols
   that should point to partial graphs which are all merged.

   Then `values` are used to override keys with values.

   The `wrapper-forms` represent a sequence of functions that take in a graph
   and return an updated graph. Each one, when `eval`ed, should return a
   function. All needed namespaces will be imported.

   For example, if search.wrappers/log is function that takes a key and a graph
   and returned an updated graph, the `wrapper-forms` could be
   `[(partial search.wrappers/log :some-key)]`."
  {:graph-symbols [s/Symbol]
   :values {s/Keyword s/Any}
   :wrapper-forms [Wrapper]})

(def SearchID s/Str)
(def SearchGraph (s/constrained utils/Graph #(contains? % :generations)))

(defnk ->search :- Search
  [graph-symbols
   {values {}}
   {wrapper-forms []}]
  {:graph-symbols graph-symbols
   :values values
   :wrapper-forms wrapper-forms})

(def compute-search-graph
  (g/graph
   :default-graph (fnk [] (hash-map :search-id (fnk [] (utils/id))))
   :graphs (fnk [graph-symbols :- [s/Symbol]] (map utils/symbol->value graph-symbols))
   :graph (fnk [graphs :- [utils/Graph]] (apply g/graph graphs))
   :values-graph (fnk [values :- {s/Keyword s/Any}]
                  (->> values
                    (plumbing.core/map-vals utils/v->fnk)
                    g/graph))
   :wrappers (fnk [wrapper-forms :- [Wrapper]] (map utils/eval-load-ns wrapper-forms))
   :wrapper (fnk [wrappers :- [(s/=> utils/Graph utils/Graph)]] (apply comp (reverse wrappers)))
   :final-graph (fnk [default-graph :- utils/Graph
                      graph :- utils/Graph
                      values-graph :- utils/Graph
                      wrapper :- (s/=> utils/Graph utils/Graph)]
                 (-> default-graph
                  (g/graph graph)
                  (merge values-graph)
                  wrapper))
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
