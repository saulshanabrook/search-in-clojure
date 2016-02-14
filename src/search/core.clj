(ns search.core
  (:require [schema.core :as s]

            [search.config.core :as config]
            [search.schemas :as schemas]
            [search.utils :as utils]))

(s/defn config->run :- schemas/Run
  "Create a new run from an existing config"
  [config :- schemas/Config]
  {:config config
   :id (utils/id)})

(s/defn run->generations :- [schemas/Generation]
  "Generate the generations, by calling the algorithm specified in the
   config"
  [run :- schemas/Run]
  (let [algorithm (config/config->algorithm (:config run))]
    (algorithm (:id run))))

(s/defn execute
  "Execute one run, based on the configuration, recording all progress with the
  recorder"
  [recorder :- schemas/Recorder
   config :- schemas/Config]

  ((:record-config! recorder) config)

  (let [run (config->run config)]
    ((:record-run! recorder) run)
    (let [generations (run->generations run)]
      (doseq [generation generations] ((:record-generation! recorder) generation))
      ((:record-run-done! recorder) run))))
