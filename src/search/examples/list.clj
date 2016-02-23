(ns search.examples.list
  (:require [search.config.core :as config]
            [search.config.evaluate :refer [->require ->call]]))


(defn score
  "count of the number of ones in the list"
  [ind]
  (->>
    ind
    (filter (partial = 1))
    count))

(defn binary
  "random int, either 1 or 0"
  []
  (rand-int 2))

(defn individual
  "a new random list of 1s and 0s"
  []
  (repeatedly 10 binary))

(defn mutate
  "changes a random index of the list to a 0|1"
  [ind]
  (assoc ind (rand-int 10) (binary)))

(def config (config/->config
             {:algorithm (->require 'search.algorithms.config/hill-climb-algorithm)
              :problem {:new-genome (->require 'search.examples.list/individual)
                        :mutate (->require 'search.examples.list/mutate)
                        :evaluate (->require 'search.examples.list/score)
                        :done (->call 'search.algorithms.base/done?-max-trait :value 10)}}))
