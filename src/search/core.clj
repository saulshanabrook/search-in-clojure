(ns search.core
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.run :as run]
            [search.generation :as generation]))

(s/defn execute
  "Execute one run, based on the configuration, recording all progress with the
  recorder"
  [recorder :- schemas/Recorder
   config :- schemas/Config]

  ((:record-config! recorder) config)

  (def run (run/config->run config))
  ((:record-run! recorder) run)

  (def generations (generation/run->generations run))
  (doseq [generation generations] ((:record-generation! recorder) generation))
  ((:record-run-done! recorder) run))
