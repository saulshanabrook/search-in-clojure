(ns search.utils
  (:require [clj-uuid :as uuid]))

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
