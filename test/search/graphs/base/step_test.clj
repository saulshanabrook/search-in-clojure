(ns search.graphs.base.step-test
  (:require [clojure.test :refer :all]
            [schema.test]
            [schema.experimental.generators :as g]
            [conjure.core :refer :all]

            [search.core :as search]
            [search.graphs.base.step :as step]
            [search.utils :as utils]))
(use-fixtures :once schema.test/validate-schemas)

(deftest breed->-test
  (let [n 10
        ->ind #(g/generate search/Individual)
        inds (utils/repeatedly-set n ->ind)
        generation (->
                    search/Generation
                    g/generate
                    (assoc :index 0
                           :individuals inds))]
    ; breed function that does nothing
    (let [step (step/breed-> {:n n :breed #(-> % :individuals)})
          next-gen (step generation)]
      (is (= {:index 1
              :individuals inds}
             (select-keys next-gen [:individuals :index]))))
    ; breed function that makes new random inds-ids
    (let [new-inds (utils/repeatedly-set n ->ind)
          step (step/breed-> {:n n :breed (fn [_] new-inds)})
          next-gen (step generation)]
      (is (= {:index 1
              :individuals new-inds}
             (select-keys next-gen [:individuals :index]))))))

(defn t->c-dummy [_ _] nil)
(deftest weighted-tweaks->children-test
  (let [->tweak (fn [] {:f (fn [_] nil) :n-parents 1 :multiple-children? true})
        tweak-1 (->tweak)
        tweak-2 (->tweak)
        ind (g/generate search/Individual)
        gen (g/generate search/Generation)]
    (stubbing [t->c-dummy (repeat ind)]
      (let [breed (step/weighted-tweaks->children
                     {:tweak-labels {:1 tweak-1 :2 tweak-2}
                      :tweak-label-weights {:1 0
                                            [:1 :2] 1}
                      :tweaks->children t->c-dummy})]
        (is (= [ind ind] (take 2 (breed gen))))
        (verify-nth-call-args-for 1 t->c-dummy gen [tweak-1])
        (verify-nth-call-args-for 2 t->c-dummy gen [tweak-1 tweak-2])
        (verify-call-times-for t->c-dummy 2)))))

(deftest tweaks->children_-test
  (let [->ind #(-> search/Individual
                g/generate
                (assoc :genome %))
        ind-1 (->ind 1)
        ind-2 (->ind 2)
        select-fn (utils/seq->fn (cycle [ind-1 ind-2]))
        t->c (step/tweaks->children_ {:select (fn [_] (select-fn))})
        gen (-> search/Generation
             g/generate
             (assoc :individuals #{ind-1 ind-2}))
        tweak-add-and-mult {:f (fn [a b] [(+ a b) (* a b)])
                            :multiple-children? true
                            :n-parents 2}
        tweak-inc {:f inc
                   :multiple-children? false
                   :n-parents 1}
        children (t->c gen [tweak-inc tweak-add-and-mult])
        children-some (map #(select-keys % [:genome :parent-ids]) children)
        parent-ids #{(:id ind-1) (:id ind-2)}]
    ; increment 1 and 2 to get 2 and 3, then add them
    (is (=
          {:genome 5
           :parent-ids parent-ids}
          (first children-some)))
    (is (=
          {:genome 6
           :parent-ids parent-ids}
          (second children-some)))))
