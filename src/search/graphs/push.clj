(ns search.graphs.push
  "Adds the ability to use the push interpreter to evaluate your programs."
  (:require [plumbing.graph :as g]
            [plumbing.core :refer [fnk defnk]]
            [push.core :as push]
            [schema.core :as s]
            [push.interpreter.templates.one-with-everything :refer [make-everything-interpreter]]
            [clojure.data.generators]

            [search.utils :refer [defnk-fn] :as utils]
            [search.graphs.seq :as seq]))

(defnk instructions
  "Returns all the instructions for the interpreter"
  [interpreter]
  (push/known-instructions interpreter))

(defnk interpreter
  "Returns the push interpreter we should be using"
  [push-bindings]
  (make-everything-interpreter :bindings (zipmap push-bindings (repeat nil))))

(defnk-fn ->instruction :- s/Any
  "Returns a random push instruction from those defined in the `push-interpreter`
   and the bindings names.`"
  [interpreter instructions]
  []
  (let [bindings (push/binding-names interpreter)]
    (-> {instructions 5
         bindings (if (empty? bindings) 0 1)}
      utils/weighted
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
  "Provides the `:->gene` for `search.graphs.seq/graph` by selecting a random
   push instruction. It also provides `:push-evaluate`."
  (g/graph
    :step-limit (utils/v->fnk 500)
    :interpreter interpreter
    :instructions instructions
    :push-evaluate evaluate
    :->gene ->instruction
    seq/graph))
