(defproject search "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://github.com/saulshanabrook/search-in-clojure"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :main ^:skip-aot search.cli
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.generators "0.1.2"]
                 [prismatic/schema "1.0.4"]
                 [prismatic/plumbing "0.5.2"]
                 [com.taoensso/timbre "4.2.1"]
                 [org.clojure/test.check "0.9.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [org.clojars.runa/conjure "2.1.3"]
                 [com.rpl/specter "0.9.2"]
                 [slingshot "0.12.2"]
                 [klapaucius "0.1.10-SNAPSHOT"]
                 [clj-fuzzy "0.3.1"]
                 [org.clojure/tools.cli "0.3.3"]
                 [mvxcvi/puget "1.0.0"]]
  :profiles {:perf {:jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"
                               "-Dclojure.compiler.direct-linking=true"]
                    :aot [search.cli]}
             :dev {:dependencies [[slamhound "1.5.5"]]
                   :plugins [[lein-kibit "0.1.2"]
                             [jonase/eastwood "0.2.3"]
                             [lein-ancient "0.6.8"]
                             [lein-cloverage "1.0.6"]
                             [lein-codox "0.9.4"]
                             [lein-exec "0.3.6"]
                             [lein-gorilla "0.3.6"]]}}


  :aliases {"test-all" ["do" ["check"] ["kibit"] ["eastwood"] ["test"]]
            "slamhound" ["run" "-m" "slam.hound"]}
  :eastwood {:linters [:all]
             :exclude-namespaces [search.conjure-utils]
             :exclude-linters [:unused-locals ; warns with defnk-fn macro
                               :keyword-typos ; warns on graph definitions in core
                               :unused-ret-vals ; warns when `(apply <non-pure-function> args)`
                               :constant-test]} ; warns on timbre profiling macro
  :codox {:source-uri "http://github.com/saulshanabrook/search-in-clojure/blob/master/{filepath}#L{line}"
          :output-path "doc"
          :metadata {:doc/format :markdown}}
  :hiera {:cluster-depth 3
          :trim-ns-prefix true
          :path "docs/ns-hierarchy.png"
          :ignore-ns #{search.schemas search.graphs.base.schemas search.config.schemas}})
