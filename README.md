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

##Guidance for Knowledge Object Developers
Thi adapter is for activating Knowledge Objects written in javascript.

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
##Examples
An example KO can be found in our [example collection](https://github.com/kgrid-objects/example-collection/releases/latest) here:
[js/simple/1.0](https://github.com/kgrid-objects/example-collection/releases/latest/download/js-simple-v1.0.zip)

##Important Notes
- Currently, multi-artifact KOs are not supported as the `import` statement is not supported in Graal VM. You can use a javascript compiler such as [babel](https://babeljs.io/) to build your code into a single file that is compatible with this adapter.