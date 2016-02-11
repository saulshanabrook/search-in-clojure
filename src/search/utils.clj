(ns search.utils
  (:require [clj-uuid :as uuid]))

(defn id [] (uuid/to-string (uuid/v1)))
