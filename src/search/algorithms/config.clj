(ns search.algorithms.config
  (:require [search.config.evaluate :refer [->call ->get-in-config]]))


(def hill-climb-algorithm (->call
                            'search.algorithms.hill-climb/algorithm
                            (->get-in-config :problem :new-genome)
                            (->get-in-config :problem :mutate)
                            (->get-in-config :problem :evaluate)
                            (->get-in-config :problem :done)))
