(ns search.conjure
  "Monkey patch conjure so that it uses thread safe redefs so it is compatible
   with https://github.com/jakepearson/quickie")

; ; from https://gist.github.com/gfredericks/7143494
; (defn with-local-redefs-fn
;   [a-var its-new-value func]
;   (cast clojure.lang.IFn @a-var)
;   (alter-meta! a-var
;                (fn [m]
;                  (if (::scope-count m)
;                    (update-in m [::scope-count] inc)
;                    (assoc m
;                      ::scope-count 1
;                      ::thread-local-var (doto (clojure.lang.Var/create @a-var)
;                                           (.setDynamic true))))))
;   (let [thread-local-var (-> a-var meta ::thread-local-var)]
;     (with-redefs-fn {a-var (fn [& args]
;                              (apply (var-get thread-local-var) args))}
;       (fn []
;         (push-thread-bindings {thread-local-var its-new-value})
;         (try (func)
;              (finally
;                (pop-thread-bindings)
;                (alter-meta! a-var
;                             (fn [m]
;                               (if (= 1 (::scope-count m))
;                                 (dissoc m ::scope-count ::thread-local-var)
;                                 (update-in m [::scope-count] dec))))))))))
;
;
; (defmacro with-local-redefs
;   "Like with-redefs, but changes a var's value thread-locally. Unlike binding,
;   it does not require the var to be dynamic, but it does require it to be a
;   function.
;
;   Does not compose with with-redefs, as it uses with-redefs internally."
;   [bindings & body]
;   (if-let [[v val & more] (seq bindings)]
;     `(with-local-redefs-fn (var ~v) ~val
;        (fn [] (with-local-redefs ~more ~@body)))
;     (cons 'do body)))
