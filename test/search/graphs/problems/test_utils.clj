(ns search.graphs.problems.test-utils
  (:require [clojure.test]
            [search.core :as search]))

(defn finishes
  [graphs]
  (clojure.test/is
    (-> {:graph-symbols graphs}
      search/->search
      search/search->generations
      doall
      seq)))
