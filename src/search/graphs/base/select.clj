(ns search.graphs.base.select
  (:require [schema.core :as s]
            [clojure.data.generators]
            [plumbing.core :refer [defnk]]

            [search.core :as search]
            [search.utils :refer [defnk-fn] :as utils]))

(s/defn invert-list :- [long]
  "Takes a list of integers and inverts them all and multiplies them by the highest
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

(def max-num Float/MAX_VALUE)
(def min-num Float/MIN_VALUE)

(defn min-key-null
  "Same as, min-key, but handles nil values"
  [k & args]
  (apply min-key (comp #(if (nil? %1) max-num %1) k) args))

(defn max-key-null
  "Same as, max-key, but handles nil values"
  [k & args]
  (apply max-key (comp #(if (nil? %1) min-num %1) k) args))

(defnk best-trait :- search/Individual
  [inds :- #{search/Individual}
   trait-key :- search/TraitKey
   trait-spec :- TraitSpec]
  "Returns best individual according the `trait-key` trait."
  (apply
   (partial
     (if (:lowest? trait-spec) min-key-null max-key-null)
     #(get-in % [:traits trait-key]))
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
  (cycle
    (map
     (fn [[trait-key trait-spec]]
       (best-trait {:inds inds
                    :trait-key trait-key
                    :trait-spec trait-spec}))
     (-> trait-specs seq clojure.data.generators/shuffle))))

(s/defn sum-of-squares :- (s/maybe s/Num)
  "Returns the sum of the squared values or `nil` if any are `nil`."
  [xs :- [(s/maybe s/Num)]]
  (try
    (reduce + (map #(* %1 %1) xs))
    (catch NullPointerException _ nil)))

(s/defn less-than-null
  "Like `<`, but always returns false when the first is nil
   and true if the second is nil"
  [a b]
  (cond
    (nil? a) false
    (nil? b) true
    :else (< a b)))

(s/defn least-sum-squares :- s/Any
  "Orders the individuals by the sum of their square traits and chooses the least"
  [inds :- #{search/Individual}]
  (cycle (sort-by #(->> % :traits vals sum-of-squares) (comparator less-than-null) inds)))
