(ns search.run
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.utils :as utils]))

(s/defn config->run :- schemas/Run
  "Create a new run from an existing config"
  [config :- schemas/Config]
  {:config config
   :id (utils/id)})
