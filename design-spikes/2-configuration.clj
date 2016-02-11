(ns design-spikes.2-configuration
  (:require [search.core]
            [search.edn]
            [schema.core :as s]
            [search.recorders.stdout]))

(defn dumb-algorithm
  "Algorithm that continiously returns a single individual, in
  every generation, that has a fitness score of 0,
  for ten generations"
  [run]
  (map
    #(map->Generation {
                       :run run
                       :index %
                       :individuals [
                                     :genome 1
                                     :traits {:score 0}]})
    (range 10)))

; all configuration files are EDN maps
; It has one required key, that should resolve to a function. 
; This function should take the current run as an argument
; and return an sequence of Generation's
(def config {:algorithm (search.edn/Require. 'design-spikes.2-configuration/dumb-algorithm})

(def recorder (search.recorders.stdout/make))
(search.core/execute recorder config)


; Most algorithms share similar components, however. 
; They start with an initial list of individuals, score those individuals,
; check if they they should stop, and if not, then step to the next generation,
; and repeat

