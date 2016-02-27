(ns search.utils
  (:require [schema.core :as s]
            [clj-uuid :as uuid]
            [clojure.edn :as edn]
            [clojure.data.generators]
            [slingshot.slingshot :refer [throw+]]))

(defn id [] (uuid/to-string (uuid/v1)))


(defn take-until
  "Returns a lazy sequence of successive items from coll until
  (pred item) returns true, including that item. pred must be
  free of side-effects.

  Copied from http://dev.clojure.org/jira/browse/CLJ-1451"
  [pred coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (if (pred (first s))
        (cons (first s) nil)
        (cons (first s) (take-until pred (rest s)))))))

(s/defn symbol->value :- s/Any
  "Takes in a symbol like 'my.project/function and returns
  the value that the symbol refers to."
  [s :- s/Symbol]
  (let [namespace_ (namespace s)]
    (if namespace_
      (-> namespace_ symbol require)
      (throw+ {:type ::no-namespace :symbol s :hint "Couldn't evaluate this symbol, should be like `project/function`"})))
  (eval s))


(defmacro value->symbol
  "Takes any value and return the symbol to it, including it's namespace

  Note: You can't use this in a funciton on one of it's arguments. If
  `(var v)` will fail in your context then `(value->symbol v)` will also fail.
  See http://stackoverflow.com/questions/11856517/how-to-get-name-of-argument-in-clojure"
  [v]
  `(let [m# (meta (var ~v))]
    (symbol
     (name (ns-name (:ns m#)))
     (name (:name m#)))))



(def Probability (s/constrained s/Num #(<= 0 % 1)))

(s/defn rand-true? :- s/Bool
 "Returns `true` with probability `p` and `false` with probability `1-p`"
 [p :- Probability]
 (<= (clojure.data.generators/float) p))

(s/defn exports-correctly? :- s/Bool
 [form :- s/Any]
 (= form (-> form pr-str edn/read-string)))

(s/defn symbol-append :- s/Symbol
  [sym :- s/Symbol after :- s/Str]
  (symbol (str sym after)))

(defmacro defnk-fn
  "Used to define a `plumbing.core/defnk` that returns a `schema/core/fn`

  For example:

    (defnk-fnk name :- s/Int
      \"the `name` function does a lot of great stuff\"
      [outer-input]
      [inner-input]
      (+ outer-input inner-input))

  expands to:

    (defnk name :- (s/=> s/Int s/Any)
      \"the `name` function does a lot of great stuff\"
      [outer-input]
      (fn name-inner :- s/Int
        [inner-input]
        (+ outer-input inner-input)))

  So the arguments are:
    `<name> :- <return-schema> <doc-string> <outer-args> <inner-args> <body>`
  "
  [name _ return-schema doc-string outer-args inner-args body]
  (let [inner-fnc-dummy (eval `(s/fn _ :- ~return-schema ~inner-args nil))
        inner-fnc-schema (s/fn-schema inner-fnc-dummy)]
    `(plumbing.core/defnk ~name :- ~inner-fnc-schema
      ~doc-string
      ~outer-args
      (s/fn ~(symbol-append name "-inner") :- ~return-schema
        ~inner-args
        ~body))))
;
; (defn fnk-rename-args
;   "Takes a fnk and a mapping of old argument names to new argument names.
;   For example, `(fnk-rename-args (fnk [a] a) {:a :b})` would return a fnk that
;   takes an input named `b`"
;   [fnk_ mapping])
;
