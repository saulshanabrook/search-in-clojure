# search

[![Travis branch](https://img.shields.io/travis/saulshanabrook/search-in-clojure/master.svg?style=flat-square)](https://travis-ci.org/saulshanabrook/search-in-clojure) [![API Docs](https://img.shields.io/badge/api%20docs-master-blue.svg?style=flat-square)](http://saulshanabrook.github.io/search-in-clojure/)


A Clojure framework for [metaheauristc search algorithms](https://en.wikipedia.org/wiki/Metaheuristic).

## Why?
This framework is built to help with the research at the
[Hampshire College Institute for Computational Intelligence](http://faculty.hampshire.edu/lspector/ici.html)
into genetic programming using the [Push programming language](http://faculty.hampshire.edu/lspector/push.html).

The current framework ([Clojush](https://github.com/lspector/Clojush)) makes
heavy use of global state and does not clearly separate the Push programming
language from the evolutionary search code. Combined with a lack of unit tests,
this made it very hard to implement large scale additions without an intricate
knowledge of how everything connected.

In contrast, this framework has a small core ([`search.core`](./src/search/core.clj))
and implements all the interesting parts of search as pluggable modules.

## Running searches
![ns graph](/docs/ns-hierarchy.png?raw=true)


*We make heavy use of plumatic's [schema](https://github.com/plumatic/schema/)
and [plumbing](https://github.com/plumatic/plumbing/) libraries. Schema gives
us some nice (optional) runtime type validation as well as making the codebase
more explicit. We use the plumbing library to do all of our computation with
it's graph module.*

Regardless of your algorithm or problem, the result of a search is a
sequence of [`search.core/Generation`](./src/search/core.clj)s. The
`search.core/config->generations` function is really the only core logic in
this library. The command line tool is just a small wrapper around this.

The whole point of the `Config` is to create a graph with a `generations` key.
At a minimum, it contains a list of graphs to merge together. For example,
we can combine a symbolic regression in push problem with a genetic algorithm:

```bash
lein trampoline run -g '[search.graphs.problems.push-sr/plus-six-graph search.graphs.algorithms.genetic/graph]'
```

The `-g` here stands for `graph-symbols`, which are just symbols that point
to graphs. When these two graphs are merged together, then they have all the
pieces needed to be able to be run to get the `generations` key.

This isn't very interesting so far, however, because it is entirely side effect
free (so we don't see any output). Let's begin by printing out the individual
with the smallest sum of errors, during each generation.

```bash
lein trampoline run -g '[search.graphs.problems.push-sr/plus-six-graph search.graphs.algorithms.genetic/graph]' \
                    -w '[(partial search.wrappers.recorders/wrap search.wrappers.recorders/smallest-ind)]'
```

Here the `-w` means `wrapper-forms`. This is a list of forms, that when evaluated,
should each return a function that wraps the whole graph.

At least when I run this, it isn't very interesting because it solves it in
the first generation. Let's limit the population-size so it takes a bit longer.

```bash
lein trampoline run -g '[search.graphs.problems.push-sr/plus-six-graph search.graphs.algorithms.genetic/graph]' \
                    -w '[(partial search.wrappers.recorders/wrap search.wrappers.recorders/smallest-ind)]' \
                    -v '{:population-size 50}'
```

We can also add profiling:

```bash
lein trampoline run -g '[search.graphs.problems.push-sr/plus-six-graph search.graphs.algorithms.genetic/graph]' \
                    -w '[(partial search.wrappers.recorders/wrap search.wrappers.recorders/smallest-ind) search.wrappers.graph/profile-fns-wrap search.wrappers.graph/print-profile-gen-wrap]' \
                    -v '{:population-size 50}'
```

The `perf` profile in lein enables some optimizations and can be enabled
with `lein with-profile +perf trampoline run ...`

Also, if you are getting an error in the run, you can add the `-s` flag to
enable schema validation, which should help you verify and diagnose the
execution. It does have some (untested) performance penalty so it is disabled
by default.

## Contributing
The code is split into two main subdirectory, `graphs` and `wrappers`.
Primarily, the difference is that the `graphs` provides a bunch of, well, graphs
that get merged together. At the top level, these are the `algorithms` and
`problems`. Each of those in tern combines a bunch of graphs and together make
up a full search. This is just how it has worked out so far, however. It is
totally possible to design a whole search without using any of the existing
graphs, you would just end up repeating a lot of things.

The `wrappers` are all function that take a graph as input and return a new
graph. They do fancy things like add a stateful function call as the generations
are evaluated and add profiling.
