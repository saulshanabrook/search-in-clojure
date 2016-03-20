(ns search.recorders.text
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]
            [puget.printer :as puget]

            [search.core :as search]
            [search.algorithms.base.select :as select]
            [search.recorders.core :refer [Recorder Metadata]]))

(s/def timbre :- Recorder
  {:started! (s/fn [md :- Metadata] (timbre/info "Created " md))
   :generation! (s/fn [md :- Metadata generation :- search/Generation] (timbre/info "Generation " generation))
   :done! (s/fn [md :- Metadata] (timbre/info "Finished " md))})

(s/defn get-best-traits :- search/Traits
  "Returns a map of each trait value with its best value out of all the inds"
  [trait-specs :- select/TraitSpecs
   inds :- #{search/Individual}]
  (->> trait-specs
   seq
   (mapcat
     (fn [[trait-key trait-spec]]
       (->> {:inds inds
             :trait-key trait-key
             :trait-spec trait-spec}
        select/best-trait
        :traits
        (#(% trait-key))
        (list trait-key))))
   (apply hash-map)))

(s/def best-traits :- Recorder
  "Prints the best value for each trait, by selecting the best individual
   for each trait."
  {:started! (fn [_] nil)
   :generation! (s/fn [{trait-specs :trait-specs} :- Metadata
                       {individuals :individuals} :- search/Generation]
                  (println (get-best-traits trait-specs individuals)))
   :done! (fn [_] nil)})
