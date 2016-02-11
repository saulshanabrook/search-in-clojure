(ns search.examples.really-dumb
  (:require [schema.core :as s]

            [search.core]
            [search.config.core :as config]
            [search.recorders.text]))

(def config (config/->config))
(def recorder search.recorders.text/timbre)

(defn -main []
  (search.core/execute recorder config))
