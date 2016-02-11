(ns search.generation
  (:require [schema.core :as s]
            [search.schemas :as schemas]
            [search.config.core :as config]))

(s/defn run->generations :- [schemas/Generation]
  "Generate the generations, by calling the algorithm specified in the
  config"
  [run :- schemas/Run]
  (let [algorithm (config/config->algorithm (:config run))]
    (algorithm (:id run))))
