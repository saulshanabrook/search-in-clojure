(ns search.utils
  (:require [schema.core :as s]
            [clj-uuid :as uuid]
            [clojure.edn :as edn]
            [plumbing.map]
            [plumbing.graph]
            [clojure.data.generators]
            [clojure.test]
            [slingshot.slingshot :refer [throw+]]))

(defn id [] (uuid/to-string (uuid/v4)))


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

(s/defn symbol-append :- s/Symbol
  "Append the string `after` to the symbol `sym`."
  [sym :- s/Symbol
   after :- s/Str]
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
    (list 'plumbing.core/defnk name ':- inner-fnc-schema
      doc-string
      outer-args
      (list 'schema.core/fn (symbol-append name "-inner") ':- return-schema
        inner-args
        body))))

(defmacro InfSeq
  "Validate an infinite lazy sequence by making sure the first value is `inner`"
  [inner]
  `(s/pred #(s/validate ~inner (first %))))

(s/defn repeatedly-set :- #{s/Any}
  "Call `f` repeatedly until we have `n` unique value and return them as a set."
  [n :- s/Int
   f :- (s/=> s/Any)]
  (loop [s #{}]
    (if (= (count s) n)
      s
      (recur (conj s (f))))))

(s/defn take-set :- #{s/Any}
  "Takes the first `n` unique values from the collection and return as a set."
  [n :- s/Int
   c] ; collection
 (loop [s #{}
        c c]
   (if (= (count s) n)
     s
     (recur (conj s (first c)) (rest c)))))

(s/defn seq->fn :- (s/=> s/Any)
  "Takes a sequence and returns a function that takes no argument and returns
  the first value first, then the second value, and so on.

      (= [1 2 1] (repeatedely 3 (seq->fn (cycle [1 2]))))
  "
  [s]
  (let [a (atom (conj (seq s) nil))]
    (fn []
      (first (swap! a rest)))))

(defn do-before
  "Wraps the lazy sequence to execute the function when the first value is resolved."
  [f xs]
  (concat (lazy-seq (do (f) nil)) xs))

(defn do-during
  "Wraps the lazy sequence to execute the function with each value, as it is resolved."
  [f xs]
  (map (fn [x] (f x) x) xs))


(defn do-after
  "Wraps the lazy sequence to execute the function after the last value is resolved."
  [f xs]
  (concat xs (lazy-seq (do (f) nil))))

; below from https://github.com/plumatic/plumbing/issues/119#issuecomment-197501172
(defn fnk? [x] (try (plumbing.fnk.pfnk/io-schemata x) true (catch Exception e false)))

(s/defschema Fnk (s/pred fnk?))

(s/defschema GraphLike (s/cond-pre {s/Keyword (s/recursive #'GraphLike)} Fnk))

(s/defschema Graph (s/constrained GraphLike plumbing.graph/->graph))

(defn wrap
  "Returns a wrapped version of `orig-f`, where `wrapped-f` should be a wrapped
   version of it, by copying the metadata."
  [orig-f wrapped-f]
  (with-meta wrapped-f (meta orig-f)))

(defn wrap-output
  "Takes a function and wraps its output by calling `orig->new-output` on each
   returned values"
  [orig-f orig->new-output]
  (wrap orig-f (comp orig->new-output orig-f)))

(defn wrap-before
  "Wraps a function by calling function `before!` on the input arguments."
  [orig-f before!]
  (wrap orig-f (fn [& args] (apply before! args) (apply orig-f args))))


(defn wrap-after
  "Wraps a funciton by calling stateful function `after!` when it returns, on the
   return value."
  [orig-f after!]
  (wrap orig-f (fn [& args] (let [res (apply orig-f args)] (after! res) res))))

(s/defn map-leaf-fns :- Graph
  "Wraps all functions returned from the leaves in the graph `g` with `fn-wrapper`.

  `fn-wrapper` should take the old function and the key path and return a new one"
  [fn-wrapper g :- Graph]
  (plumbing.map/map-leaves-and-path
    (fn [ks f]
      (wrap-output f
        (fn [res]
          (if (clojure.test/function? res)
            (fn-wrapper {:f res :ks ks})
            res))))
    g))
