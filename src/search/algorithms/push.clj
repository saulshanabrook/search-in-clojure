(ns search.algorithms.push
  (:require [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [push.core :as push]
            [schema.core :as s]
            [push.interpreter.templates.one-with-everything :refer [make-everything-interpreter]]
            [clojure.data.generators]

            [search.utils :refer [defnk-fn]]
            [search.algorithms.seq :as seq]))

(defnk-fn ->instruction :- s/Any
  "Returns a random push instruction from those defined in the `push-interpreter`
   and the bindings names. Choose a push instruction half the time and a bindings
   half the time, if any exist`"
  [interpreter]
  []
  (let [bindings (push/binding-names interpreter)]
    (-> {(push/known-instructions interpreter) 1
         bindings (if (empty? bindings) 0 1)}
      clojure.data.generators/weighted
      clojure.data.generators/rand-nth)))

(defnk-fn evaluate :- s/Any
  "Get the value off the `output-stack` by running the `program` with the bindings
  from `bindings`"
  [interpreter
   output-stack :- s/Keyword
   step-limit :- s/Int]
  [program bindings :- {s/Keyword s/Any}]
  (let [output (push/run interpreter program step-limit :bindings bindings)]
    (first (push/get-stack output output-stack))))


(def graph
  (g/graph
    :step-limit (fnk [] 500)
    :interpreter (fnk [] (make-everything-interpreter))
    :push-evaluate evaluate
    :->gene ->instruction
    seq/graph))
