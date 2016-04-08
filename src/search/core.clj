(ns search.core
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk defnk]]

            [search.schemas :as schemas]
            [search.utils :as utils]))

(defnk ->config :- schemas/Config
  [graph-symbols
   {values {}}
   {wrapper-forms []}]
  {:graph-symbols graph-symbols
   :values values
   :wrapper-forms wrapper-forms})

(def config->run-graph
  "A graph that defines how to take a config and create a graph from it, that,
   when run, will produce the generations."
  (g/graph
   :default-graph (fnk [graph-symbols values wrapper-forms :as s]
                   {:id (fnk [] (utils/id))
                    :config (utils/v->fnk s)
                    :map-fn (utils/v->fnk map)})
   :graphs (fnk [graph-symbols :- [s/Symbol]] (map utils/symbol->value graph-symbols))
   :graph (fnk [graphs :- [utils/Graph]] (apply g/graph graphs))
   :values-graph (fnk [values :- {s/Keyword s/Any}]
                  (->> values
                    (plumbing.core/map-vals utils/eval-load-ns)
                    (plumbing.core/map-vals utils/v->fnk)
                    g/graph))
   :wrappers (fnk [wrapper-forms :- [s/Any]] (map utils/eval-load-ns wrapper-forms))
   :wrapper (fnk [wrappers :- [(s/=> utils/Graph utils/Graph)]] (apply comp (reverse wrappers)))
   :final-graph (fnk [default-graph :- utils/Graph
                      graph :- utils/Graph
                      values-graph :- utils/Graph
                      wrapper :- (s/=> utils/Graph utils/Graph)]
                 (-> default-graph
                  (g/graph graph)
                  (merge values-graph)
                  wrapper))
   :->run (fnk [final-graph :- schemas/RunGraph] (g/compile final-graph))
   :run (fnk [->run] (->run {}))))

(def config->run
  "Generates a `Run` from a `Config`. You can call `:generations` on the `run`
   to get the results."
  (comp :run (g/compile config->run-graph)))
