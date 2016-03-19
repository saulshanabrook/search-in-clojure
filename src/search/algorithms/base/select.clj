(ns search.algorithms.base.select
  (:require [schema.core :as s]
            [clojure.data.generators]
            [plumbing.core :refer [defnk]]

            [search.core :as search]
            [search.utils :refer [defnk-fn] :as utils]))

(s/defn invert-list :- [long]
  "Takes a list of longs and inverts them all and multiplies them by the highest
   number to return a list of longs"
  [xs :- [s/Int]]
  (let [max_ (apply max xs)]
    (map #(long (/ max_ %)) xs)))

(def TraitSpec {:lowest? s/Bool})
(def TraitSpecs {search/TraitKey TraitSpec})

(defnk-fn roulette :- s/Any ; :- (utils/InfSeq search/Individual)
  "Selects parent individuals, proportional to the `trait-name` trait of the
  of individual. A higher value will cause the individual to be selected more
  often (or opposite if `lowest?`).

  This is a single objective selection."
  [trait-specs :- TraitSpecs
   trait-key :- search/TraitKey]
  [inds :- #{search/Individual}]
  (let [weights (->> inds
                  (map #(get-in % [:traits trait-key]))
                  ((if (-> trait-key trait-specs :lowest?) invert-list identity))
                  (map vector inds)
                  (into {}))]
    (repeatedly (partial clojure.data.generators/weighted weights))))

(defnk best-trait :- search/Individual
  [inds :- #{search/Individual}
   trait-key :- search/TraitKey
   trait-spec :- TraitSpec]
  "Returns best individual according the `trait-key` trait."
  (apply
   (partial
     (if (:lowest? trait-spec) min-key max-key)
     (comp trait-key :traits))
   inds))

(defnk-fn dominates :- s/Any ; :- s(utils/InfSeq search/Individual)
  "Selects the parent with the highest (or lowest if `lowest?`) `trait-name`.

   Single opjective selection."
  [trait-specs :- TraitSpecs
   trait-key :- search/TraitKey]
  [inds :- #{search/Individual}]
  (repeat (best-trait {:inds inds
                       :trait-key trait-key
                       :trait-spec (trait-specs trait-key)})))

(defnk-fn lexicase :- s/Any ; :- s(utils/InfSeq search/Individual)
  "Chooses the individuals by selecting the one that does the best on each
   trait, when we order the traits randomly."
  [trait-specs :- TraitSpecs]
  [inds :- #{search/Individual}]
  (map
   (fn [[trait-key trait-spec]]
     (best-trait {:inds inds
                  :trait-key trait-key
                  :trait-spec trait-spec}))
   (-> trait-specs seq clojure.data.generators/shuffle cycle)))
