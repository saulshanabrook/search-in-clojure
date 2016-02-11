(ns search.examples.list)
;
; (defn score [ind]
;   "count of the number of ones in the list"
;   (->>
;     ind
;     (filter (partial = 1))
;     count))
;
; (defn binary []
;   "random int, either 1 or 0"
;   (rand-int 2))
;
;
; (defn individual []
;   "a new random list of 1s and 0s"
;   (->>
;     (range)
;     (take 100)
;     (map (fn [_] (binary)))))
;
;
; (defn mutate [ind]
;   "changes a random index of the list to a 0|1"
;   (assoc ind (rand-int 100) (binary)))
;
;
; ; (search/hill-climbing
; ;   :score score
; ;   :mutate mutate
; ;   :individual individual
; ;   :stop (fn [score] (= score 100)))
;
; (+ 1 1)
