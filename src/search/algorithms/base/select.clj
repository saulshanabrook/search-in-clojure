(ns search.algorithms.base.select
  (:require [schema.core :as s]
            [clojure.data.generators]

            [search.core :as search]
            [search.utils :refer [defnk-fn] :as utils]))

(s/defn invert-list :- [long]
  "Takes a list of longs and inverts them all and multiplies them by the highest
   number to return a list of longs"
  [xs :- [s/Int]]
  (let [max_ (apply max xs)]
    (map #(long (/ max_ %)) xs)))

(defnk-fn roulette :- s/Any ; :- (utils/InfSeq search/Individual)
  "Selects parent individuals, proportional to the `trait-name` trait of the
  of individual. A higher value will cause the individual to be selected more
  often (or opposite if `lowest?`).

  This is a single objective selection."
  [trait-name :- s/Keyword
   lowest? :- s/Bool]
  [inds :- #{search/Individual}]
  (let [weights (->> inds
                  (map #(get-in % [:traits trait-name]))
                  ((if lowest? invert-list identity))
                  (map vector inds)
                  (into {}))]
    (repeatedly (partial clojure.data.generators/weighted weights))))

(defnk-fn dominates :- s/Any ; :- search/Individual
  "Selects the parents with the highest (or lowest if `lowest?`) `trait-name`.

   Single opjective selection."
  [trait-name :- s/Keyword
   lowest? :- s/Bool]
  [inds :- #{search/Individual}]
  (repeat (apply
           (partial (if lowest? min-key max-key) (comp trait-name :traits))
           inds)))
