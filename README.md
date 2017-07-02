# Slerp 

Slerp is a maven plugin to generate jpa code for spring project in postgresql connection within simple input to make you faster than before

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
#### Create Connection for postgresql
add the application.properties in the src/main/resources and include 
```
slerp.jdbc.driver=org.postgresql.Driver
slerp.jdbc.url=jdbc:postgresql://localhost:5432/ecomerce
slerp.jdbc.username=postgres
slerp.jdbc.password=rioters7
```
## Now you can use the slerp plugin to generate code for you

## Help
To check the slerp feature
```
mvn slerp:help
```
And the output is
```
----------------------------------------------------------------------
Slerp Usage
----------------------------------------------------------------------
1. Entity And Repository		mvn slerp:entity
2. Transaction 				mvn slerp:transaction
3. Function 				mvn slerp:function
4. Unit Test 				mvn slerp:test
5. Restfull 				mvn slerp:rest
----------------------------------------------------------------------
```
## Entity And Repository

Create simple table in postgesql in this case, i just create table with name user_principal

```
CREATE TABLE user_principal
(
  user_id bigint NOT NULL,
  username character varying(30) NOT NULL,
  hashed_password bytea NOT NULL,
  account_non_expired boolean NOT NULL,
  account_non_locked boolean NOT NULL,
  credentials_non_expired boolean NOT NULL,
  enabled boolean NOT NULL,
  CONSTRAINT user_principal_pkey PRIMARY KEY (user_id),
  CONSTRAINT uq_username UNIQUE (username)
)
```
And than you can create entity with mvn slerp:entity
```
----------------------------------------------------------------------
Entity Generator
----------------------------------------------------------------------
Entity Package (org.slerp.auth.entity): org.slerp.auth.entity //Input for Entity Package
Repository Package (org.slerp.auth.repository): org.slerp.auth.repository //Input for Repository Package
Product Name	: PostgreSQL
Product Version	: 9.4.12
Driver Name	: PostgreSQL Native Driver
Login User	: postgres

[ "category", "product", "user_authority", "user_principal" ]
Tables : user_principal //
Will Generate : [user_principal]
Generated Successfully Created : org.slerp.auth.entity.UserPrincipal.java
Generated Successfully Created : org.slerp.auth.repository.UserPrincipalRepository.java
...
```
The output shoud be on src/main/java/org/slerp/auth/entity/UserPrincipal.java and src/main/java/org/slerp/auth/repository/UserPrincipalRepository.java

with your favorite ide or text editor. The output should be like this

```
package org.slerp.auth.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Basic;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "user_principal")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@XmlAccessorType(XmlAccessType.NONE)
public class UserPrincipal implements Serializable {

	@Id
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "username")
	@Basic(optional = false)
	@NotNull(message = "org.slerp.auth.entity.UserPrincipal.username")
	@Size(min = 1, max = 30)
	private String username;
	@Column(name = "hashed_password")
	@Basic(optional = false)
	@NotNull(message = "org.slerp.auth.entity.UserPrincipal.hashedPassword")
	private byte[] hashedPassword;
	@Column(name = "account_non_expired")
	@Basic(optional = false)
	@NotNull(message = "org.slerp.auth.entity.UserPrincipal.accountNonExpired")
	private Boolean accountNonExpired;
	@Column(name = "account_non_locked")
	@Basic(optional = false)
	@NotNull(message = "org.slerp.auth.entity.UserPrincipal.accountNonLocked")
	private Boolean accountNonLocked;
	@Column(name = "credentials_non_expired")
	@Basic(optional = false)
	@NotNull(message = "org.slerp.auth.entity.UserPrincipal.credentialsNonExpired")
	private Boolean credentialsNonExpired;
	@Column(name = "enabled")
	@Basic(optional = false)
	@NotNull(message = "org.slerp.auth.entity.UserPrincipal.enabled")
	private Boolean enabled;
	/*Getter And Setter ...*/
}
```
