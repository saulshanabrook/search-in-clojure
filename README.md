# search

[![Travis branch](https://img.shields.io/travis/saulshanabrook/search-in-clojure/master.svg?style=flat-square)](https://travis-ci.org/saulshanabrook/search-in-clojure) [![API Docs](https://img.shields.io/badge/api%20docs-master-blue.svg?style=flat-square)](http://saulshanabrook.github.io/search-in-clojure/) 


A Clojure framework for [metaheauristc search algorithms](https://en.wikipedia.org/wiki/Metaheuristic).


## Terms

* Goals:

* Idiomatic Clojure
  * Pure functions as much as possible
  * Use built in data types, don't create custom ones
* Flexible reporting for analysis
  * Support logging progress as the run goes to stdout
  * Collecting of information about runs for later analysis in database
  * Need to be able to collect many different metrics:
    * best score of each generation, to track progress over time
    * custom metrics per individual, like whether it is reproductively competent
* Easy integration with push
* Define problem once, test with GP, hill climbing, and others

## Thoughts

The hard part to get right is not the actual algorithms. Understand and writing
a hill climbing or genetic programming algorithm can get complicated, but
these challenged scale with how complicated your techniques are.

The harder part, I think, to get write is the infrastructure that surrounds
the algorithm, like:

* logging
  * For debug
  * multiple metrics per individual
    * resource consumption (time/memory/cpu)
    * might have multiple traits and scores
    * metrics not used to guide reproduction but just for information
      (reproductive fitness)
  * To see how well it does
    * comparison between runs
    * storage of performance over time in dedicated location
  * Understand progress through more in depth analysis
    * graphs of individual/parent relations over time
    * Visualizing search space in 2d or 3d and showing progress over time
    * Attempt to understand how good solutions come about, by looking at their
      individual evolution
* Parallelization (multi core and multi machine)

Instead of starting to focus on what generalizations can be made between
search algorithms, I think if we first decide how these two components should
work and fit together, this will provide the proper constraints to design the
rest of the system around

### Pure Functions
Maybe logging is more complicated because it is usually stateful.

Maybe instead I should focus on building the purely functional primitives and ignore logging.

Once those are implemented and can compose together, we can assess the best way to add in the inpurity.
