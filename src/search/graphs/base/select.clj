(ns search.graphs.base.select
  (:require [schema.core :as s]
            [plumbing.core :refer [defnk]]

            [search.schemas :as schemas]
            [search.utils :refer [defnk-fn] :as utils]))

(s/defn invert-list :- [long]
  "Takes a list of integers and inverts them all and multiplies them by the highest
   number to return a list of longs"
  [xs :- [s/Int]]
  (let [max_ (apply max xs)]
    (map #(long (/ max_ %)) xs)))

(s/defschema TraitSpec {:lowest? s/Bool})
(s/defschema TraitSpecs {schemas/TraitKey TraitSpec})

(defnk-fn roulette :- schemas/Individual
  "Selects parent individuals, proportional to the `trait-name` trait of the
  of individual. A higher value will cause the individual to be selected more
  often (or opposite if `lowest?`).

  This is a single objective selection."
  [trait-specs :- TraitSpecs
   trait-key :- schemas/TraitKey]
  [inds :- #{schemas/Individual}]
  (let [weights (->> inds
                  (map #(get-in % [:traits trait-key]))
                  ((if (-> trait-key trait-specs :lowest?) invert-list identity))
                  (map vector inds)
                  (into {}))]
    (utils/weighted weights)))

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

(defnk best-trait :- schemas/Individual
  [inds :- #{schemas/Individual}
   trait-key :- schemas/TraitKey
   trait-spec :- TraitSpec]
  "Returns best individual according the `trait-key` trait."
  (apply
   (partial
     (if (:lowest? trait-spec) min-key-null max-key-null)
     #(get-in % [:traits trait-key]))
   inds))

(defnk all-best-trait :- #{schemas/Individual}
  "Returns the best individuals according to the `trait-key` trait"
  [inds :- #{schemas/Individual}
   trait-key :- schemas/TraitKey
   trait-spec :- TraitSpec]
  (utils/all-nil-comp-key
    #(get-in % [:traits trait-key])
    (if (:lowest? trait-spec) < >)
    inds))

(defnk-fn dominates :- schemas/Individual
  "Selects the parent with the highest (or lowest if `lowest?`) `trait-name`.

   Single opjective selection."
  [trait-specs :- TraitSpecs
   trait-key :- schemas/TraitKey]
  [inds :- #{schemas/Individual}]
  (best-trait {:inds inds
               :trait-key trait-key
               :trait-spec (trait-specs trait-key)}))

(defnk-fn lexicase :- schemas/Individual
  "Lexicase selection as defined in
   https://push-language.hampshire.edu/t/lexicase-selection/90."
  [trait-specs :- TraitSpecs]
  [inds :- #{schemas/Individual}]
  (loop [candidates inds
         cases (-> trait-specs seq clojure.data.generators/shuffle)]
    (if (or (= 1 (count candidates)) (empty? cases))
      (clojure.data.generators/rand-nth (seq candidates))
      (let [[[trait-key trait-spec] & r_cases] cases]
         (recur
           (all-best-trait {:inds candidates :trait-key trait-key :trait-spec trait-spec})
           r_cases)))))

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
  [inds :- #{schemas/Individual}]
  (cycle (sort-by #(->> % :traits vals sum-of-squares) (comparator less-than-null) inds)))
