(ns search.config.core
  (:require [schema.core :as s]

            [search.schemas :as schemas]
            [search.config.evaluate :as evaluate]
            [search.utils :as utils]))

(s/defn config->algorithm :- schemas/Algorithm
  [config :- schemas/Config]
  (evaluate/recursively-evaluate config (:algorithm config)))

(s/defn ->config :- schemas/Config
  ([] (->config {}))
  ([m] (merge
        {:algorithm (evaluate/->require 'search.algorithms.default/algorithm)
         :id (utils/id)}
        m)))
