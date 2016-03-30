# Contributing

If you want to add a feature create a PR/issue.

If you are adding code, please make sure:

1. It is documented
2. It has tests
3. `lein test-all` passes, which checks some basic code quality


If your test is actually running a search (and so might take a long time)
you should add the `:slow` metadata to the test. Then it will
not be run with normal tests, and only run for `lein test :all` or `lein test :slow`
