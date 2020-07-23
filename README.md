# Javascript V8 Adapter
[![CircleCI](https://circleci.com/gh/kgrid/kgrid-adapter/tree/master.svg?style=shield)](https://circleci.com/gh/kgrid/kgrid-adapter/tree/master)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This javascript adapter uses the GraalVM javascript engine to run object payloads written in javascript locally.
Any simple javascript file containing functions can be easily run using this adapter but more complex objects with 
external dependencies should be bundled into one file. (See the guide on creating a bundled KO.)

Example deployment descriptor:
```yaml
endpoints:
  /welcome:
    artifact:
      - 'src/welcome.js'
    adapter: 'V8'
    function: 'welcome'
```

### Clone
To get started you can simply clone this repository using git:
```
git clone https://github.com/kgrid/javascript-v8-adapter.git
cd javascript-v8-adapter
```
Install the adapters to your local maven repository where they can then be used as dependencies by the activator:
```
mvn clean install
```