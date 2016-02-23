(ns search.cli
  (:require [search.config.evaluate :refer [symbol->value]]
            [search.core :as search]))

(def str->value (comp symbol->value symbol))

(defn -main
  "Creates and executes a run. It takes two required positional arguments:
  * `recorder-path`: Path to the recorder (i.e. `search.recorders.text/timbre`)
  * `config-path`: Path to the config var (i.e. `search.examples.list/config`)"
  [recorder-path config-path]
  (search/execute (str->value recorder-path) (str->value config-path)))
