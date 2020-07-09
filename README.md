![](https://portal.ogc.org/files/?artifact_id=92076)
# Resource Registry Management

Core DEH (Demeter Enabler Hub) module provides functionalities for the resource management process and interfacing with other components such as Compatibility Checker and Discovery Management.


## Table of contents
* [**Technologies**](#technologies)
* [**Features**](#features)
* [**Requirements**](#requirements)
* [**Setup**](#setup)
* [**How to use**](#how-to-use)
* [**Endpoints**](#endpoints)
* [**Troubleshoot**](#troubleshoot)
* [**Contributors**](#contributors)
* [**Status**](#status)
* [**Licence**](#licence)


## Technologies

| Description                                     | Language | Version          |
| :---------------------------------------------- | :------: | :--------------: |
| [Java SE Development Kit 8][1]                  | Java     | 1.8.0_251        |
| [Spring Boot][2]                                | Java     | 2.3.1            |
| [Apache Maven 3][3]                             |          | 3.6.3            |
| [Apache Tomcat 9][4]                            |          | 9.0.36           |
| [MongoDB][5]                                    |          | 4.2.8            |
| [Docker][6]                                     |          | 19.03.12         |


[1]: https://www.oracle.com/it/java/technologies/javase/javase-jdk8-downloads.html
[2]: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/
[3]: http://maven.apache.org/ 
[4]: https://tomcat.apache.org/tomcat-9.0-doc/changelog.html
[5]: https://www.mongodb.com/try/download/community
[6]: https://docs.docker.com/get-docker/


## Features

* Searching Resources
* Accessing Resources
* Preparing Resource for storing
* Storing Resources
* Editing Resources
* Deleting Resources


## Requirements
**_Current setup includes build Spring Boot image localy, since image is not avaiable on Docker Hub, so next two requirements are currently mandatory_**

* Installed Java JDK (Version > 8) 
* Installed Apache Maven

**_After having Docker image available on Docker hub, only those requirements will be mandatory_**
* Installed Docker
* Installed Docker Compose


## Setup

**_Current setup includes build Spring Boot image localy, since image is not avaiable on Docker Hub_**

After pulling the source code, open terminal and go to root folder and follow next steps:

* _Run_ `mvn install` _to build jar locally_. 
* _After operation is complete, run_ `docker build -t resource-registry-management .` _in order to build the docker image_
* _After image is built, run_ `docker-compose up` _to run Docker Compose with server image and MongoDB_
* _If you want to run containers in background run next command_ `docker-compose up -d`



## How to use

After containers are up and running, you can use Resoruce Registry Management through exposed REST API endpoints.


## Endpoints
**TO DO**

| URL                            | Type     | Used for                                         | Input                                | Output                                 |
| :----------------------------- | :------: | :----------------------------------------------- | :----------------------------------- | :------------------------------------- |


## Troubleshoot
**TO DO**


## Contributors

* [Marko Stojanovic](https://github.com/marest94) 


## Status
Project is: _in progress_


## License
