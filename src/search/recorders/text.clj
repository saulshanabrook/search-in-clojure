(ns search.recorders.text
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]
            [search.core :as search]))

(s/def timbre :- search/Recorder
  {:record-config! (s/fn [config :- search/Config] (timbre/info "Config: " config))
   :record-run! (s/fn [run :- search/Run] (timbre/info "Run: " run))
   :record-generation! (s/fn [generation :- search/Generation] (timbre/info "Generation: " generation))
   :record-run-done! (s/fn [run :- search/Run] (timbre/info "Run finished: " run))})
