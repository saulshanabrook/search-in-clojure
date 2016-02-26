(ns search.cli
  (:require [schema.core :as s]

            [search.utils :refer [symbol->value]]
            [search.core :as search]))

(def str->value (comp symbol->value symbol))

(defn -main
  "Creates and executes a run. It takes two required positional arguments:
  * `recorder-path`: Path to the recorder (i.e. `search.recorders.text/timbre`)
  * `config-path`: Path to the config var (i.e. `search.examples.list/config`)

  If you set the `VALIDATE_SCHEMA` env variable it will validate the schemas
  as well."
  [recorder-path config-path]
  (when (System/getenv "VALIDATE_SCHEMA")
    (s/set-fn-validation! true))
  (search/execute (str->value recorder-path) (str->value config-path)))
