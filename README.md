# Slerp 

Slerp is a maven plugin to generate jpa code for spring project with postgresql within simple input to make you faster than before

## Introduction

Slerp can write code for entity and repository, function, generator, api and also unit testing for jpa

## Prerequisites

You can build the slerp project with simple maven install after clone this repository
Slerp only work for java 8 code because we use lamda expression to create this application

## Before generate code you need to install
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - The Java Virtual Machine
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
## Example

Update your pom.xml in the spring boot project and add slerp-core, slerp-base, inside dependencies like this
```
<dependency>
  <groupId>org.slerp</groupId>
  <artifactId>slerp-core</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.slerp</groupId>
  <artifactId>slerp-base</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

and than inside the slerp-maven-plugin inside build plugins
```
<plugin>
  <groupId>org.slerp.plugin</groupId>
  <artifactId>slerp-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
    <configuration>
      <properties>${project.basedir}/src/main/resources/application.properties</properties>
      <apiDir>${project.basedir}/src/main/java</apiDir>
      <srcDir>/home/kiditz/apps/framework/slerp-ecommerce-service/src/main/java</srcDir>
		</configuration>
</plugin>
```
