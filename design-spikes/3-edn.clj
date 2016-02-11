(defn load-symbol
  "Returns the value of the symbol"
  [sym]
  (-> sym namespace symbol require)
  (eval sym))
