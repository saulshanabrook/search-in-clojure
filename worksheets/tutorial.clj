;; gorilla-repl.fileformat = 1

;; **
;;; # Search
;;; ## High Level Types
;; **

;; @@
(ns search.worksheets.tutorial)

(require '[search.schemas :as schemas])
;; @@

;; @@
(println "Individual" schemas/Individual)
(println "Generation" schemas/Generation)
;; @@

;; @@
(println schemas/Config)
;; @@

;; @@
(require '[search.core :as search])
(clojure.repl/doc search/config->generations)
;; @@

;; @@
(clojure.repl/doc search/->config)
;; @@

;; **
;;; ## Basic Example
;; **

;; @@
(require '[plumbing.core :refer [fnk]])

(def silly-graph
  {
    :->individual (fnk []
                    (fn []
                      {:genome (rand)
                       :traits {}
                       :id "there"}))
    :initial-generation (fnk [->individual population-size]
                          {:id "hey"
                           :index 0
                           :individuals (repeatedly population-size ->individual)})
    :generation->generation (fnk [] #(update-in % [:index] inc))
    :generations (fnk [num-generations initial-generation generation->generation]
                   (take num-generations (iterate generation->generation initial-generation)))
   })

(def silly-config (search/->config {:graph-symbols [`silly-graph] :values {:num-generations 99999999999 :population-size 20}}))
silly-config

;; @@

;; @@
(take 2 (search/config->generations silly-config))
;; @@

;; **
;;; ## Fun Example
;; **

;; @@
(require '[search.graphs.push-sr :as push-sr]
         '[search.graphs.algorithms.genetic :as genetic]
		 '[plumbing.fnk.pfnk :as pfnk]
         '[plumbing.graph :as g])
;; @@

;; @@
(keys genetic/graph)
;; @@

;; @@
(keys push-sr/graph)
;; @@

;; @@
(pfnk/input-schema-keys (g/graph push-sr/graph genetic/graph))
;; @@

;; @@
(def fun-graph
  (g/instance push-sr/graph
    {:output-stack :integer
     :xs (range 0 10)
     :->y (fn [x] (* x 4))}))

(keys fun-graph)
;; @@

;; @@
(def fun-config
  (search/->config
    {:graph-symbols `[fun-graph genetic/graph]
     :values {:map-fn '(partial com.climate.claypoole/pmap :builtin)}
     		  :population-size 500}))
(def fun-generations (search/config->generations fun-config))
;; @@

;; **
;;; ### Resaults
;; **

;; @@
(-> fun-generations first :individuals first)
;; @@

;; @@
(defn average [coll]
  (if (some nil? coll)
    nil
  	(/ (reduce + coll) (count coll))))
(def avg-errors
  (map
    (fn [gen]
      (map
        (fn [ind]
          (average (vals (:traits ind))))
        (:individuals gen)))
    fun-generations))
(second avg-errors)

;; @@

;; @@
(require '[incanter.charts :refer [scatter-plot]]
		 '[incanter-gorilla.render :refer [chart-view]])
(defn scatter-plot-per-gen
  [vals-per-gen]
  (let [index-error	(mapcat (fn [index errs] (map #(vector index %) errs)) (range) vals-per-gen)]
    (scatter-plot (map first index-error) (map second index-error))))
;; @@

;; @@
(chart-view (scatter-plot-per-gen (take 10 avg-errors)))
;; @@

;; @@
(require '[search.wrappers.recorders :refer [->smallest-ind]])

(def smallest-errors (map (comp vals :traits ->smallest-ind) fun-generations))
(first smallest-errors)

;; @@

;; @@
(chart-view (scatter-plot-per-gen (take 10 smallest-errors)))
;; @@

;; @@
(chart-view (scatter-plot-per-gen (take 10 (drop 5 smallest-errors))))
;; @@

;; @@
(def best-genome (-> fun-generations last ->smallest-ind :genome))

best-genome
;; @@

;; **
;;; ### Testing Model
;; **

;; @@
(require '[clojure.repl :refer [source]])

(source search/config->generations)
;; @@

;; @@
(def fun-compute (:computed (search/compute-search fun-config)))
(keys fun-compute)
;; @@

;; @@
(def hopefully-times-four (partial (:test-fn fun-compute) best-genome))
(hopefully-times-four -10)
;; @@

;; @@

;; @@
