(ns search.recorders.text
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]
            [search.core :as search]))

(s/def timbre :- search/Recorder
  {:record-config! (s/fn [config :- search/Config] (timbre/info "Config: " config))
   :record-run! (s/fn [run :- search/Run] (timbre/info "Run: " run))
   :record-generation! (s/fn [generation :- search/Generation] (timbre/info "Generation: " generation))
   :record-run-done! (s/fn [run :- search/Run] (timbre/info "Run finished: " run))})

(s/def max-value :- search/Recorder
  "Prints the max `:value` trait each generation on a newline."
  {:record-config! (fn [_] nil)
   :record-run! (fn [_] nil)
   :record-generation! #(->> % :individuals (map (comp :value :traits)) (apply max) println)
   :record-run-done! (fn [_] nil)})

(s/def min-distance :- search/Recorder
 "Prints the min `:distance` trait each generation on a newline."
 {:record-config! (fn [_] nil)
  :record-run! (fn [_] nil)
  :record-generation! #(->> % :individuals (map (comp :distance :traits)) (apply min) println)
  :record-run-done! (fn [_] nil)})
