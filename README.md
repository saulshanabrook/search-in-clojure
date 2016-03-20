# search

[![Travis branch](https://img.shields.io/travis/saulshanabrook/search-in-clojure/master.svg?style=flat-square)](https://travis-ci.org/saulshanabrook/search-in-clojure) [![API Docs](https://img.shields.io/badge/api%20docs-master-blue.svg?style=flat-square)](http://saulshanabrook.github.io/search-in-clojure/)


A Clojure framework for [metaheauristc search algorithms](https://en.wikipedia.org/wiki/Metaheuristic).

![ns graph](/docs/ns-hierarchy.png?raw=true)

This README is not complete yet and the bottom is out of date.

## Quickstart

```bash
lein run search.recorders.text/timbre search.problems.list/hill-climb-config

lein trampoline run search.recorders.text/min-distance search.problems.push-saul/genetic-config
```


## Design
It is easiest to understand the design decisions of this project in the context
of the initial goals/requirements.

### Goals
* Flexible reporting for analysis
  * Don't wait till end of run to log/save progress
  * Collect information in a structure way so that many different types of runs
    could be compared, in terms of performance.
  * Need to be able to collect many different metrics:
    * best score of each generation, to track progress over time
    * multiple metrics per individual
      * resource consumption (time/memory/cpu)
      * might have multiple traits and scores
      * metrics not used to guide reproduction but just for information
        (reproductive fitness)
    * Understand progress through more in depth analysis
      * graphs of individual/parent relations over time
      * Visualizing search space in 2d or 3d and showing progress over time
      * Attempt to understand how good solutions come about, by looking at their
        individual evolution
* Define problem once, test with GP, hill climbing, and others
  * Should be able to separate algorithm from problem specific behavior
* Idiomatic Clojure
  * Pure functions as much as possible
  * Use built in data types, don't create custom ones
* Easy integration with push

### Core Structure
These goals basically come down to flexible structured logging with flexible
configuration. I started by figuring out what the structure of the data would
be like, if we wanted to save all information about runs in a database.

It is basically structured like this (accept `State` has been renamed to `Generation`):

![core structure](/docs/core-structure.jpg?raw=true)

So one `Config` can have many individual `Run`s, which each have sequence of
`Generation`s. The top level execution model (which is in
[`search.core/execute`](./src/search/core.clj)) just consists of:

1. Create a new `Config` that describes what we want to do and record it.
2. Create a new `Run` for that config (that just has a reference to it and a
   unique id) and record it.
3. Generate a (lazy) sequence of `Generation`s for that `Run` (based on the `Config`
   that it points to) and record each one as it is produced.
4. Record that we have finished this `Run`.

This let's use separate our stateful operations (all the recording) from the
stateless execution of our algorithm. This means we can switch out the recorder
easily and maintain the same algorithm or vice versa.

You can look in the [`search.schemas`](./src/search/schemas.clj) namespace to
see what makes up these different structures.

I am relatively happy with this core functionality, so while it might change
a little around the edges (if we need more information in a `Generation` for example)
I think it will work pretty well as is and seems relatively sound.

### Config
The `Config` on the other hand is not quite as mature. Currently, all `Config`
items are basically maps with one required key `:algorithm` that points to
a function that takes in a run ID and returns a sequence of generations.

This makes the configuration extremely flexible. There are core paths for things
like `mutate` or `fitness`. Instead, you can use a bunch of building blocks
(in [`search.graphs.base`](./src/search/algorithms/base)) to create it.

However, I wanted configurations to be serializable (in a human readable format)
so that if you did a run and it was recorded in a central data store, I could
later go back and inspect exactly how you created that run and re-run it myself
if I wanted to.

I also wanted it to be possible to easily switch between say hill climbing and
genetic algorithms, while keeping the same problem specification of
mutate, new individual, and evaluate.

So instead of creating an algorithm function directly, you should use
the functions in `search.config.evaluate` to build up a nested map structure
that is serializable but can compile down to a single `Algorithm` function (by
`search.config.core/config->algorithm`).

So instead of actually calling some function that will return the right algorithms
you would do `(search.config.evaluate/->call 'my.namespace.core/make-algorithm arg1)`
which gives you back a map that you can use for `:algorithm`.

It also gives you a `->get-in-config` function that can be used to access
a path somewhere else in the config file. This facilitates creating multiple
configuration pieces that each use different algorithms but expect the `mutate`
and `->genome` functions to be in the same place in your configuration.

For a simple example, look at `search.graphs.config/hill-climb-algorithm`
that is used in `search.problems.list/config`.


#### Criticism of config

This is a bit more complicated than I would like. The originally intention
was to define all configuration in `.edn` files and use custom readers
to allow you to write things like `search/require path-to-function` in your
`.edn` file and have it parsed into that map which is then later turned into
the `Algorithm` function after it is recorded. This functionality is actually
working (thanks to `search.config.edn`) but I am not sure if this is easier
than just creating your config in a Clojure file directly.

As I think about it more, maybe it isn't necessary to create a human readable
serialization format, which might remove this two step generation process
if we used something like [Nippy](https://github.com/ptaoussanis/nippy) instead
of EDN.

A larger concern is the disorderly nature of creating the algorithm function.
I have tried to use function type annotations in order to make it clearer what
is needed. I have made a bunch of higher level functions that, say, take in a
population size and a new individual function and return an `Initial` function
that returns the whole first generation.

I have made a generalization of most algorithms in the
`search.graphs.base.core/->algorithm` function. One possibility is just to
run with this option and move it into core, forcing all algorithms to behave
like this.

The basic problem I am coming up against is a lack of structure. I feel like
I am implementing some sort of strongly typed polymorphism, but with untyped
functions.

I have just started looking into the [Graph](http://plumatic.github.io/prismatics-graph-at-strange-loop/)
library, which seems to address exactly this problem:

>  When the feed builder was written as a single monolithic function, we struggled to cleanly represent this polymorphism using case statements, multimethods, or protocols. But once we expressed the core composition structure using Graph, the polymorphism became trivial – each feed just provides a set of nodes that are combined into a shared default Graph using ‘merge’.

So I was just thinking maybe the whole `Algorithm` could use this library?
So that the dependency injection is more structured and better type checked?

I will have to try this out and see how it looks.

### Recorders

Right now I just have a single recorder that logs to stdout. I hope to figure
out a good graph database and look at how we could put genome and individual
information in there, to create ancestry graphs.

### Speed

Adding a `pmap` in a few places could speed things up tremendously. I also would
like to support multi-machine execution, but need to look into the library space
for that in Clojure again.
