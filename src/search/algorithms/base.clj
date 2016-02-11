(ns search.algorithms.base)
;
; (defn individuals->generation [ind->traits step run genomes]
;   (map #(map->Individual {:traits (ind->traits %1) :genome %1} ) genomes))
;
; (s/defn base-algorithm [init :- ind->traits end? step]
;   (s/schematize-fn (fn algorithm [run]) Algorithm)
;   (defn inner
;     ([] (inner))
;     ([n] (lazy-seq (cons)))
;    ≥nm?))
