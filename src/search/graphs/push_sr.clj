(ns search.graphs.push-sr
  (:require [schema.core :as s]
            [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]

            [search.schemas :as schemas]
            [search.graphs.push :as push]
            [search.graphs.testcases :as testcases]
            [search.graphs.base.select :as select]
            [search.graphs.base.done :as done]
            [search.utils :as utils]))


(s/defn about-equal :- (s/maybe s/Bool)
  "True if x is within `wiggle-room` of y"
  [wiggle-room :- s/Num
   x :- (s/maybe s/Num)
   y :- (s/maybe s/Num)]
  (when-not (or (nil? x) (nil? y))
    (<= (- wiggle-room) (- x y) wiggle-room)))

(utils/defnk-fn traits->done? :- s/Bool
  "We are done if every trait is within `wiggle-room` of 0."
  [wiggle-room :- s/Num]
  [traits :- schemas/Traits]
  (->> traits
    vals
    (every? (partial about-equal wiggle-room 0))))


(s/defn difference-squared :- (s/maybe s/Num)
  [a :- (s/maybe s/Num)
   b :- (s/maybe s/Num)]
  (try
    (let [diff (- a b)]
      (* diff diff))
    (catch NullPointerException _ nil)))


(def graph
  (g/graph
    :ys (fnk [->y xs] (map ->y xs))
    :test-cases (fnk test-cases :- {s/Num s/Num} [xs ys] (zipmap xs ys))
    :n-genes (utils/v->fnk 20)
    (g/instance push/graph {:push-bindings [:x]})
    :test-fn (fnk [push-evaluate] (fn [genome x] (push-evaluate genome {:x x})))
    :test-output->trait-value (fnk [] difference-squared)
    testcases/graph

    :trait-specs (fnk trait-specs :- select/TraitSpecs
                  [xs]
                  (zipmap xs (repeat {:lowest? true})))
    :select select/lexicase

    :wiggle-room (fnk [output-stack :- (s/constrained s/Keyword #{:integer :float})]
                  (if (= output-stack :integer)
                    0
                    0.001))
    :traits->done? traits->done?
    :done? done/any-trait))
