(ns search.core
  (:require [schema.core :as s]
            [plumbing.graph :as g]

            [search.utils :as utils]))


(def Config {:graphs [s/Symbol]
             :id s/Str})

(def Run {:config Config
          :id s/Str})

(def Genome s/Any)

(def TraitKey s/Keyword)
(def TraitValue s/Int)

(def Traits
  "Traits are any information we want to know about an indivual. For single
  objective search we commonly use a `:value` trait."
  {TraitKey TraitValue})

(def Individual
  {:genome Genome
   :id s/Str
   :traits Traits
   :parents-ids [s/Str]})

(def Generation
  "Holds the whole state for a current generation of individuals."
  {:run-id s/Str
   :index s/Int
   :individuals [Individual]})

; A Recorder is responsible for displaying or saving the resaults of an
; execution.
(def Recorder
  "responsible for displaying or saving the resaults of an execution."
  {:record-config! (s/=> Config [])
   :record-run! (s/=> Run [])
   :record-generation! (s/=> Generation [])
   :record-run-done! (s/=> Run [])})

(s/defn ->config :- Config
  "Creates a new configuration. Each item in `graphs` should be a symbol
   pointing to a partial graph."
  [graphs :- [s/Symbol]]
  {:id (utils/id) :graphs graphs})


(s/defn config->run :- Run
  "Create a new run from an existing config"
  [config :- Config]
  {:config config
   :id (utils/id)})

(s/defn config->graph
  [{:keys [graphs]} :- Config]
  (apply merge (map utils/symbol->value graphs)))

(s/defn run->generations :- [Generation]
  "Return all the generations, by accessing the `:generations` key on the map
   from the config."
  [run :- Run]
  (let [graph (config->graph (:config run))]
    (:generations (g/run graph {:run-id (:id run)}))))

(s/defn execute
  "Execute one run, based on the configuration, recording all progress with the
  recorder"
  [recorder :- Recorder
   config :- Config]

  ((:record-config! recorder) config)

  (let [run (config->run config)]
    ((:record-run! recorder) run)
    (let [generations (run->generations run)]
      (doseq [generation generations] ((:record-generation! recorder) generation))
      ((:record-run-done! recorder) run))))
