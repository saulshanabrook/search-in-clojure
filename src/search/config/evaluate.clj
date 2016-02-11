(ns search.config.evaluate
  (:require [schema.core :as s]
            [search.config.schemas :refer [Config]]))

(s/defschema ShouldEvaluate {:should-evaluate-type s/Keyword
                             :input s/Any})


(s/defn ->should-evaluate :- ShouldEvaluate
  [type :- s/Keyword input]
  {:should-evaluate-type type
   :input input})

(defmulti evaluate (fn [config m] (:should-evaluate-type m)))

(def should-evaluate-checker (s/checker ShouldEvaluate))
(defn should-evaluate? [form] (nil? (should-evaluate-checker form)))
(s/defn recursively-evaluate :- s/Any
  "Evaluates some form from within a configuration and evaluates it. It replaces
   all ShouldEvaluate types with their evaluation."
  [config :- Config
   form :- s/Any]
  (clojure.walk/postwalk
    (fn [x]
      (if (should-evaluate? x)
        (recursively-evaluate config (evaluate config x))
        x))
    form))


(s/defn symbol->value :- s/Any
  "Takes in a symbol like 'my.project/function and returns
  the value that the symbol refers to."
  [s :- s/Symbol]
  (-> s namespace symbol require)
  (eval s))


(s/defmethod evaluate :call
  [config :- Config
   {[f-symbol & f-args] :input} :- (assoc ShouldEvaluate :input [(s/one s/Symbol "symbol") s/Any])]
  (let [f (symbol->value f-symbol)
        f-args-evaled (recursively-evaluate config f-args)]
    (apply f f-args-evaled)))

(def ->call (partial ->should-evaluate :call))

(s/defmethod evaluate :require
  [_ {symbol :input} :- (assoc ShouldEvaluate :input s/Symbol)]
  (symbol->value symbol))
(def ->require (partial ->should-evaluate :require))

(s/defmethod evaluate :get-in-config
  [config :- Config
   {ks :input} :- (assoc ShouldEvaluate :input [s/Keyword])]
  (recursively-evaluate config (get-in config ks)))
(def ->get-in-config (partial ->should-evaluate :get-in-config))
