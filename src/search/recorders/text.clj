(ns search.recorders.text
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]

            [search.core :as search]
            [search.algorithms.base.select :as select]
            [search.recorders.core :refer [Recorder Metadata]]))

(s/def timbre :- Recorder
  {:started! (s/fn [md :- Metadata] (timbre/info "Created " md))
   :generation! (s/fn [md :- Metadata generation :- search/Generation] (timbre/info "Generation " generation))
   :done! (s/fn [md :- Metadata] (timbre/info "Finished " md))})

(s/def best-traits :- Recorder
  "Prints the best for each trait, using the best ind."
  {:started! (fn [_] nil)
   :generation! (s/fn [{trait-specs :trait-specs} :- Metadata
                       {individuals :individuals} :- search/Generation]
                 (->> trait-specs
                  seq
                  (mapcat
                    (fn [[trait-key trait-spec]]
                      (->> {:inds individuals
                            :trait-key trait-key
                            :trait-spec trait-spec}
                       select/best-trait
                       :traits
                       trait-key
                       (#(list trait-key %)))))
                  (apply hash-map)
                  println))
   :done! (fn [_] nil)})
