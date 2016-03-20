(ns search.recorders.core
  (:require [schema.core :as s]
            [plumbing.fnk.pfnk :as pfnk]

            [search.core :as search]
            [search.algorithms.base.select :as select]
            [search.utils :as utils]))

(def Metadata
  "Metadata about the search, including its configuration, id, and the trait specs."
  {:id search/SearchID
   :search search/Search
   :trait-specs select/TraitSpecs})

(def Recorder
  "responsible for displaying or saving the resaults of an execution."
  {:started! (s/=> nil Metadata)
   :generation! (s/=> nil Metadata search/Generation)
   :done! (s/=> nil Metadata)})

(s/defn wrap :- search/SearchGraph
  "Modify graph g so that it records the run as it progresses."
  [{:keys [started! generation! done!]} :- Recorder
   {->gens :generations :as g} :- search/SearchGraph]
  (assoc g :generations
    (pfnk/fn->fnk
      (fn [m]
        (let [metadata (select-keys m (keys Metadata))]
          (->> (->gens m)
            (utils/do-before #(started! metadata))
            (utils/do-during #(generation! metadata %))
            (utils/do-after #(done! metadata)))))
      [(merge (pfnk/input-schema ->gens) Metadata)
       (pfnk/output-schema ->gens)])))
