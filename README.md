# Javascript V8 Adapter
[![CircleCI](https://circleci.com/gh/kgrid/kgrid-adapter/tree/master.svg?style=shield)](https://circleci.com/gh/kgrid/kgrid-adapter/tree/master)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This javascript adapter uses the GraalVM javascript engine to run object payloads written in javascript locally.
Any simple javascript file containing functions can be easily run using this adapter but more complex objects with 
external dependencies should be bundled into one file. (See the guide on creating a bundled KO.)

## Installation

This is an embedded runtime, already pulled in by the activator
as a dependency. If you'd like to include it in your maven project,
do so by adding the following dependency to your pom.xml file:
```
<dependency>
  <groupId>org.kgrid</groupId>
  <artifactId>javascript-v8-adapter</artifactId>
</dependency>
```

## Configuration
There are currently no configurable settings for this adapter.

## Start the runtime
As an embedded adapter, this will automatically be enabled when the activator starts.

## Examples
An example KO can be found in our [example collection](https://github.com/kgrid-objects/example-collection/) here:
[js/simple/1.0](https://github.com/kgrid-objects/example-collection/releases/latest/download/js-simple-v1.0.zip)

There is also a KO that uses ECMAscript 5 modules: [js/simple/1.0](https://github.com/kgrid-objects/example-collection/releases/latest/download/js-modules-v1.0.zip)

## Guidance for Knowledge Object Developers
This adapter is for activating Knowledge Objects written in javascript.

An example KO with naan of `hello`, a name of `neighbor`, api version of `1.0`, and endpoint `welcome`,
a Deployment Specification might look like this:

```yaml
/welcome:
  post:
    artifact:
      - "src/hello.js"
    engine: "javacript"
    function: "main"
```
Where `function` is the name of the main javascript entry function.

You would then execute this endpoint to see the code work: 

`POST <activator url>/<naan>/<name>/<api version>/<endpoint>`

In this example: `POST <activator url>/hello/neighbor/1.0/welcome`

The Service Specification for this object would likewise then be

```yaml
openapi: 3.0.2
info:
  version: '1.0'
  title: 'Hello neighbor'
  description: An example of simple Knowledge Object
  license:
    name: GNU General Public License v3 (GPL-3)
    url: >-
      https://tldrlegal.com/license/gnu-general-public-license-v3-(gpl-3)#fulltext
  contact:
    name: KGrid Team
    email: kgrid-developers@umich.edu
    url: 'http://kgrid.org'
servers:
  - url: /js/neighbor
    description: Hello world
tags:
  - name: KO Endpoints
    description: Hello world Endpoints
paths:
  /welcome:
    post:
      ... 

```

In the Service Specification the servers.url must match the naan and name of the object (`/js/neighbor`) and the path must match the path in Deployment Specification (`/welcome`).
The service spec conforms to the swagger [OpenAPI spec.](https://swagger.io/specification/)

You can call other activated object endpoints in the activator from your javascript using `let childEndpoint = context.getExecutor("naan/name/api-version/endpoint");` to get a reference to the main function specified by the Deployment Specification for that endpoint. Then you can invoke that function by passing it an argument and the content type (as a string) of the argument:  `let result = childEndpoint.execute("inputs", "text/plain");`. Note that due to a [restriction in the Graal JS engine](https://github.com/oracle/graal/issues/631) you can only pass primitives as inputs, not objects or arrays. If you want to pass an object you must use `JSON.stringify` and then pass it in with the content type `"application/json"` which will ensure that it is parsed correctly.

The context object also allows you to access environment variables using `context.getProperty(property.name)`.


## Using ECMAscript 5 modules
This adapter supports using standard [ES5 modules](https://v8.dev/features/modules). However, note that every source file in the object must be an ES5 module.
This means that your js source files must end in the `.mjs` extension. See the example js-modules object for a sample of how this works.
Your entry function must be exported and all your source files must be listed in the deployment.yaml.

deployment.yaml example:
```yaml
/welcome:
  post:
    artifact:
      - src/index.mjs
      - src/math.mjs
    engine: javascript
    function: welcome
    entry: src/index.mjs
```

## Important Notes
- The v8 engine cannot return native javascript arrays. You can work around this problem by using javascript objects instead.
  Or use the graal polyglot methods for creating a java array in javascript:
    ```javascript
    let intArray = Java.type('int[]');
    let iarr = new intArray(3);
    return iarr;
    ```
  
- See the [GraalVM documentation](https://www.graalvm.org/reference-manual/js/JavaScriptCompatibility/) for more information on the capabilities and limitations of the GraalVM javascript implementation.
