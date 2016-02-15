(ns search.algorithms.base.schemas
  (:require [schema.core :as s]

            [search.schemas :as schemas]))

(def Initial (s/=> s/Str schemas/Generation))
(def Evaluate (s/=> schemas/Generation schemas/Generation))
(def Done (s/=> schemas/Generation s/Bool))
(def Step (s/=> schemas/Generation schemas/Generation))
