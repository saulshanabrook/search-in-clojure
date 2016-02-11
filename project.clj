(defproject search "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://github.com/saulshanabrook/search-in-clojure"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.generators "0.1.2"]
                 [prismatic/schema "1.0.4"]
                 [com.taoensso/timbre "4.2.1"]
                 [org.clojure/test.check "0.9.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [org.clojars.runa/conjure "2.1.3"]]

  :profiles {:dev {:dependencies [[slamhound "1.5.5"]]
                   :plugins [[lein-kibit "0.1.2"]
                             [jonase/eastwood "0.2.3"]
                             [lein-ancient "0.6.8"]
                             [lein-cloverage "1.0.6"]]}}
  :aliases {"test-all" ["do" ["check"] ["kibit"] ["eastwood"] ["test"]]
            "slamhound" ["run" "-m" "slam.hound"]
            "deps" ["ancient"]})
