(ns search.cli
  (:require [schema.core :as s]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string]

            [search.core :as search]
            [search.schemas :as schemas])
  (:gen-class))

(def cli-options
  [["-s" "--validate-schema" "Turn on schema validation. Will likely be slower."]
   ["-g" "--graphs [path.graph/1 path.graph/2]" "Paths to the graphs to use"
    :id :graph-symbols
    :parse-fn read-string]
   ["-v" "--values {:value-1}" "Graph overrides"
    :default {}
    :id :values
    :parse-fn read-string]
   ["-w" "--wrappers [(partial path.to/wrapper \"wrapper-arg\"] path.to/other]"
    "Wrap the graph with these functions, from left to right."
    :default []
    :id :wrapper-forms
    :parse-fn read-string]
   ["-h" "--help"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn usage [options-summary]
  (clojure.string/join
    \newline
    ["Creates a config and executes run with it."
     ""
     "Usage: lein run [options]"
     ""
     "Options:"
     options-summary
     ""
     "Each option is parsed using `read-string` to create the search."]))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (when (:validate-schema options) (s/set-fn-validation! true))
    (-> options
      (select-keys (keys schemas/Config))
      search/config->run
      :generations
      dorun)))
