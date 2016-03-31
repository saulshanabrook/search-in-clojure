(ns search.schemas
  (:require [schema.core :as s]

            [search.utils :refer [Graph]]))

(s/defschema Genome s/Any)

(s/defschema TraitKey s/Any)
(s/defschema TraitValue (s/maybe s/Num))

(s/defschema Traits
  "Traits are any information we want to know about an indivual. For single
  objective search we commonly use a `:value` trait.

  They are a superset testcases, because they could also include things Like
  performance characteristcs. Currently everything 'about' the individual is
  stored in this one flat map called `traits`"
  {TraitKey TraitValue})

(s/defschema Individual
  {:genome Genome
   :id s/Str
   :traits Traits
   :parent-ids #{s/Str}})

(s/defschema Generation
  "Holds the whole state for a current generation of individuals."
  {:index s/Int
   :individuals #{Individual}})

(s/defschema Wrapper s/Any)

(s/defschema Config
  "A search configuration is represented as a map. It contains all the data
   neccesary to run the search in a serializiable form, so that it can be
   preserved in a text form.

   The most important are the `graph-symbols`. These are a list of symbols
   that should point to partial graphs which are all merged.

   Then `values` are used to override keys with values.

   The `wrapper-forms` represent a sequence of functions that take in a graph
   and return an updated graph. Each one, when `eval`ed, should return a
   function. All needed namespaces will be imported.

   For example, if search.wrappers/log is function that takes a key and a graph
   and returned an updated graph, the `wrapper-forms` could be
   `[(partial search.wrappers/log :some-key)]`."
  {:graph-symbols [s/Symbol]
   :values {s/Keyword s/Any}
   :wrapper-forms [Wrapper]})

(s/defschema SearchGraph
  "A graph that retuns a [Generation] from it's `:generations` key"
  (s/constrained Graph #(contains? % :generations)))
