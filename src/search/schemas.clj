(ns search.schemas
  (:require [schema.core :as s]

            [search.config.edn :as edn]
            [search.config.schemas]))

(def Config (s/constrained
             search.config.schemas/Config,
             edn/exports-correctly?
             "Exports and imports as EDN correctly"))

(def Run {:config Config
          :id s/Str})

(def Genome s/Any)
(def Traits
  "Traits are any information we want to know about an indivual. For single
  objective search we commonly use a `:value` trait."
  {s/Keyword s/Int})

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

(def Algorithm
  "Takes in a run id and returns a sequence of generations. This is where
  all the interesting things happen."
  (s/=> [Generation] s/Str))

; A Recorder is responsible for displaying or saving the resaults of an
; execution.
(def Recorder
  "responsible for displaying or saving the resaults of an execution."
  {:record-config! (s/=> Config [])
   :record-run! (s/=> Run [])
   :record-generation! (s/=> Generation [])
   :record-run-done! (s/=> Run [])})
