(ns search.recorders.core
  (require [schema.core :as s]
           [plumbing.fnk.pfnk :as pfnk]
           [plumbing.graph :as g]

           [search.core :as search]
           [search.utils :as utils]))

(def Recorder
  "responsible for displaying or saving the resaults of an execution."
  {:started! (s/=> nil search/Search search/SearchID)
   :generation! (s/=> nil search/Generation)
   :done! (s/=> nil search/SearchID)})

(s/defn wrap :- search/SearchGraph
  "Modify graph g so that it records the run as it progresses."
  [{:keys [started! generation! done!]} :- Recorder
   {->gens :generations :as g} :- search/SearchGraph]
  (assoc g :generations
    (pfnk/fn->fnk
      (fn [{:keys [search-id search] :as m}]
        (->> (->gens m)
          (utils/do-before #(started! search search-id))
          (utils/do-during generation!)
          (utils/do-after #(done! search-id))))
      [(assoc (pfnk/input-schema ->gens)
        :search-id search/SearchID
        :search search/Search)
       (pfnk/output-schema ->gens)])))
