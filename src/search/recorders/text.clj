(ns search.recorders.text
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]
            [search.schemas :as schemas]))

(s/def timbre :- schemas/Recorder
  {:record-config! (s/fn [config :- schemas/Config] (timbre/info "Config: " config))
   :record-run! (s/fn [run :- schemas/Run] (timbre/info "Run: " run))
   :record-generation! (s/fn [generation :- schemas/Generation] (timbre/info "Generation: " generation))
   :record-run-done! (s/fn [run :- schemas/Run] (timbre/info "Run finished: " run))})
