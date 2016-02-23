(ns search.utils
  (:require [schema.core :as s]
            [clj-uuid :as uuid]
            [clojure.data.generators]))

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
