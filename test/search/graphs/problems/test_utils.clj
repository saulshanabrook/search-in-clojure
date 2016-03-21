(ns search.graphs.problems.test-utils
  (:require [clojure.test]
            [search.core :as search]))

(defn finishes
  [graphs]
  (clojure.test/is
    (-> {:graph-symbols graphs}
      search/->config
      search/config->generations
      doall
      seq)))
