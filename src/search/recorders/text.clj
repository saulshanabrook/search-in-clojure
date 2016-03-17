(ns search.recorders.text
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]

            [search.core :as search]
            [search.recorders.core :refer [Recorder]]))

(s/def timbre :- Recorder
  {:started! (s/fn [search :- search/Search id :- search/SearchID] (timbre/info "Created " search "with ID" id))
   :generation! (s/fn [generation :- search/Generation] (timbre/info "Generation: " generation))
   :done! (s/fn [id :- search/SearchID] (timbre/info "Finished " id))})

(s/def max-value :- Recorder
  "Prints the max `:value` trait each generation on a newline."
  {:started! (fn [_ _] nil)
   :generation! #(->> % :individuals (map (comp :value :traits)) (apply max) println)
   :done! (fn [_] nil)})

(s/def min-distance :- Recorder
 "Prints the min `:distance` trait each generation on a newline."
 {:started! (fn [_ _] nil)
  :generation! #(->> % :individuals (apply min-key (comp :distance :traits)) println)
  :done! (fn [_] nil)})
