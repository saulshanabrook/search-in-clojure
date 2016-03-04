(ns search.algorithms.push
  (:require [plumbing.graph :as g]
            [plumbing.core :refer [fnk]]
            [push.core :as push]
            [schema.core :as s]
            [push.interpreter.templates.one-with-everything :refer [make-everything-interpreter]]

            [search.utils :refer [defnk-fn]]
            [search.algorithms.seq :as seq]))

(defnk-fn ->instruction :- s/Any
  "Returns a random push instruction from those defined in the `push-interpreter`
   plus the `input-binding-names`"
  [interpreter input-binding-names :- [s/Keyword]]
  []
  (rand-nth (concat (push/known-instructions interpreter) input-binding-names)))

(defnk-fn evaluate :- s/Any
  "Get the value off the `output-stack` by running the `program` with the bindings
  from `bindings`"
  [interpreter
   output-stack :- s/Keyword
   step-limit :- s/Int]
  [program bindings :- {s/Keyword s/Any}]
  (let [output (push/run interpreter program step-limit {:bindings bindings})]
    (first (push/get-stack output-stack output))))


(def graph
  (g/graph
    :step-limit (fnk [] 10000)
    :interpreter (fnk [] (make-everything-interpreter))
    :push-evaluate evaluate
    :->gene ->instruction
    seq/graph))
