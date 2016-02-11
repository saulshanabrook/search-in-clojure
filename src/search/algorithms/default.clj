(ns search.algorithms.default
  (:require [search.utils :as utils]))

(defn algorithm
  "Algorithm that continiously returns a single individual, in
  every generation, that has a fitness score of 0,
  for ten generations"
  [run-id]
  (map
   (fn [i] {:run-id run-id
            :index i
            :individuals [{:genome 1
                           :parent-ids []
                           :id (utils/id)
                           :objectives {}}]})
   (range 10)))
