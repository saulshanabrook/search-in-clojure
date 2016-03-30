(ns search.wrappers.recorders
  "Recorders are used to somehow show the information about the run.

   This could be logging to stdout or sending information to a database."
  (:require [schema.core :as s]
            [plumbing.fnk.pfnk :as pfnk]
            [taoensso.timbre :as timbre]
            [puget.printer :as puget]

            [search.core :as search]
            [search.graphs.base.select :as select]
            [search.utils :as utils]))

(def Metadata
  "Metadata about the search, including its configuration, id, and the trait specs."
  {:id s/Str
   :config search/Config
   (s/optional-key :trait-specs) select/TraitSpecs
   (s/optional-key :population-size) s/Int
   (s/optional-key :n-genes) s/Int
   (s/optional-key :tweak-label-weights) {s/Keyword s/Int}
   (s/optional-key :mutate-p) utils/Probability
   (s/optional-key :alternation-p) utils/Probability})


(def Recorder
  "responsible for displaying or saving the resaults of an execution."
  {:started! (s/=> nil Metadata)
   :generation! (s/=> nil Metadata search/Generation)
   :done! (s/=> nil Metadata)})


(s/defn wrap :- search/SearchGraph
  "Modify graph g so that it records the run as it progresses"
  [{:keys [started! generation! done!]} :- Recorder
   {->gens :generations :as g} :- search/SearchGraph]
  (assoc g :generations
    (pfnk/fn->fnk
      (fn [m]
        (let [metadata-keys (map #(if (s/optional-key? %1) (:k %1) %1)
                                 (keys Metadata))
              metadata (select-keys m metadata-keys)]
          (->> (->gens m)
            (utils/do-before #(started! metadata))
            (utils/do-during #(generation! metadata %))
            (utils/do-after #(done! metadata)))))
      [(merge (pfnk/input-schema ->gens) Metadata)
       (pfnk/output-schema ->gens)])))

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

(s/def smallest-ind
  "Prints the individual with the traits that are the closest to 0, by summing
   them"
   {:started! (fn [md] (puget/cprint md))
    :generation! (s/fn [_ {inds :individuals} :- search/Generation]
                   (puget/cprint
                     (apply select/min-key-null
                       (fn [i] (some->> i
                                :traits
                                vals
                                ((fn [vs] (when (not-any? nil? vs) vs)))
                                (apply +)))
                       inds)))
    :done! (fn [_] nil)})
