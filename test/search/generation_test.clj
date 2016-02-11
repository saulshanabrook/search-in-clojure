(ns search.generation-test
  (:require [clojure.test :refer :all]
            [schema.test :as st]
            [schema.experimental.generators :as g]

            [search.schemas :as schemas]
            [search.generation :as generation]
            [search.run :as run]
            [search.config.core :as config]
            [search.config.evaluate :refer [->require]]))

(def sample-generation (g/generate schemas/Generation))
(def sample-algorithm (fn [run_id] [(assoc sample-generation :run-id run_id)]))
(st/deftest run->generations-test
  (let [config_ (config/->config {:algorithm
                                  (->require 'search.generation-test/sample-algorithm)})
        run_ (run/config->run config_)
        generations_ (generation/run->generations run_)]
    (is (= [(assoc sample-generation :run-id (:id run_))] generations_))))
