(ns search.config.edn
  (:require [schema.core :as s]
            [clojure.edn :as edn]

            [search.config.evaluate :as evaluate]))

(def opts {:readers {'search/call evaluate/->call
                     'search/require evaluate/->require
                     'search/get-in-config evaluate/->get-in-config}})

(defn read-string [str] (edn/read-string opts str))
(defn read [stream] (edn/read opts stream))

(s/defn exports-correctly? :- s/Bool
  [form :- s/Any]
  (= form (-> form pr-str read-string)))
