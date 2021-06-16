### Performance tests

For now, we only performance test the Javascript V8 embedded runtime using a couple of objects/endpoints from the [example-collection](https://github.com/kgrid-objects/example-collection).

* Download [the k6 load testing tool](https://k6.io/)
* Download the [example-collection](https://github.com/kgrid-objects/example-collection) (or check it out locally)
   
   ```
   kgrid download -e -m https://github.com/kgrid-objects/example-collection/releases/download/4.1.1/manifest.json
   ```
* Download an Activator (or use a local build) and start it pointed to the shelf created from the example collection you downloaded:
   
   ```
   java -jar target/kgrid-activator-1.5.4-SNAPSHOT.jar --kgrid.shelf.cdostore.url=filesystem:file:<path-to-shelf>
   ```

* The test script is in the `performanceTest` directory. We will start by running the tests against a simple Javascript **Hello, World** KO endpoint (js/simple/1.0/welcome)
   * First, do a warmup run (of 30s with 10 users) using `k6`:

      ```
      k6 run -e URL=http://localhost:8080/js/simple/1.0/welcome performanceTest/k6post.js -d 30s -u 10
      ```
   * Then run a test pass using the same script and KO endpoint (5s, 10 users):

      ```
      k6 run -e URL=http://localhost:8080/js/simple/1.0/welcome performanceTest/k6post.js -d 5s -u 10
      ```

Results are reported on every run. We are mostly interested in the test pass, not the warmup, and the values for`http_req_duration` (average time) and `http_reqs` (throughput in reqs/sec)

* Repeat the warmup and test pass using the Javascript executive object default endpoint (node/executive/1.0/process). We are looking for the same duration and throughput numbers from the test pass.

   ```
   k6 run -e URL=http://localhost:8080/js/executive/1.0/execute performanceTest/k6post.js -d 30s -u 10
  k6 run -e URL=http://localhost:8080/js/executive/1.0/execute performanceTest/k6post.js -d 5s -u 10
   ```

If we establish a new baseline (e.g. after changes to the Javascript V8 adapter) add the date and results in the table below. Remember to run the "before" tests on your own machine to reset the baseline (duplicating the last line then updating, running tests after changes, recording results)

| date | ko/endpoint | `http_req_duration` | `http_reqs` |
| ----- | ----- | --- | --- |
| 6/16/2021| js/simple | 1.87ms | 4992.758701/s |
| 6/16/2021| js/executive | 2.56ms | 3667.741663/s |
| After upgrading the graal-v8 engine to 21.1.0| | | |
| 6/16/2021| js/simple | 2.21ms | 4201.915839/s |
| 6/16/2021| js/executive | 2.7ms | 3472.974475/s |
