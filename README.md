# Slerp 

Slerp is a maven plugin to generate jpa code for spring project within simple input to make you faster than before

## Introduction

Slerp can write code for entity and repository, function, generator, api and also unit testing for jpa

## Prerequisites

You can build the slerp project with simple maven install after clone this repository
Slerp only work for java 8 code because we use lamda expression to create this application

## Before generate code you need to install
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
### Slerp Core
```
cd slerp/slerp-core
mvn clean install
```
### Slerp Base
```
cd slerp/slerp-base
mvn clean install
```
### Slerp Generator
```
cd slerp/slerp-generator
mvn clean install
```
### Slerp Maven Plugin
```
cd slerp/slerp-maven-plugin
mvn clean install
```

