(ns design-spikes.top-down
  (:require [search.recorder]
            [search.recorder.stdout]
            [search.config]
            [search.run :as run]
            [search.state :as state]
            [search.core :as search]))

; The recorder is responsible for all side affects. The default would be
; one that logs resaults to stdout
; Another option would be one that saved things to Datomic
(def recorder (search.recorders.stdout/make))

; The config is the complete record of your current experiment
; It has things like the algorithm (ga/hill climbing) and all parameters
; for that algorithm, as well as everything we need to know about the problem,
; like it's error function, mtuation function, and initial population function
(def config (search.config/make ...args))
(search.recorder/record-config! recorder config)

; A run is like one execution of the configuration. It simply has a unique ID
; as well as a reference to the config.
(def run (run/config->run config))
(search.recorder/record-run! recorder run)

; A run is transformed into a list of states. This is the step that performs
; the algorithms. Each state is like one generation. So the list of states
; is actually a lazy sequence, so that the execution is only performed
; as needed. This allows us to record each state as it comes in, in order
; to save progress of a run at at the end of each generation.

; A state itself holds some idea of current population and any information
; about each individual in that population. It also holds a reference to the
; run and a an index, of what round it is at.
(def states (state/run->states run))
(doseq [state states] (search.recorder/record-state! recorder state))


;; In total, this process can condenced into one line here:
(search/execute (search.recorders.stdout/make) (search.config/make ...args))