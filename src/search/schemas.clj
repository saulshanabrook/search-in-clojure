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
(def Objectives {s/Keyword s/Num})

(def Individual {:genome Genome
                 :id s/Str
                 :objectives Objectives
                 :parents-ids [s/Str]})

(def Generation {:run-id s/Str
                 :index s/Int
                 :individuals [Individual]})

(def Algorithm (s/=> [Generation] s/Str))

; A Recorder is responsible for displaying or saving the resaults of an
; execution.
(def Recorder {:record-config! (s/=> Config [])
               :record-run! (s/=> Run [])
               :record-generation! (s/=> Generation [])
               :record-run-done! (s/=> Run [])})
