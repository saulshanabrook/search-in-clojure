(ns search.cli
  (:require [schema.core :as s]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string]

            [search.utils :refer [symbol->value]]
            [search.core :as search])
  (:gen-class))

(def str->value (comp symbol->value symbol))


(def cli-options
  [["-s" "--validate-schema" "Turn on schema validation. Will likely be slower."]
   ["-g" "--graphs [path.graph/1 path.graph/2]" "Paths to the graphs to use"
    :default []
    :id :graph-symbols
    :parse-fn read-string]
   ["-v" "--values {:value-1}" "Graph overrides"
    :default {}
    :id :values
    :parse-fn read-string]
   ["-w" "--wrappers [[path.to/wrapper \"wrapper-arg\"] [path.to/other]]"
    "Wrap the graph with these functions, from left to right."
    :default []
    :id :wrapper-symbols
    :parse-fn read-string]
   ["-h" "--help"]])


(defn -main
  "Creates and executes a run. It takes two required positional arguments:
  * `recorder-path`: Path to the recorder (i.e. `search.recorders.text/timbre`)
  * `search-path`: Path to the search var (i.e. `search.examples.list/hill-climb-search`)

  If you set the `VALIDATE_SCHEMA` env variable it will validate the schemas
  as well."
  [recorder-path search-path])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn usage [options-summary]
  (->> ["Creates and executes a search run."
        ""
        "Usage: lein run [options]"
        ""
        "Options:"
        options-summary
        ""
        "Each option is parsed using `read-string` to create the search."]
       (clojure.string/join \newline)))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (when (:validate-schema options) (s/set-fn-validation! true))
    (-> options
      (select-keys (keys search/Search))
      search/search->generations
      doall)))
