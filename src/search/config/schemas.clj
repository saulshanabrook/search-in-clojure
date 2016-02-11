(ns search.config.schemas
  (:require [schema.core :as s]))

(def Config {:algorithm s/Any
             :id s/Str
             s/Keyword s/Any})
