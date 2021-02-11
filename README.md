![](https://portal.ogc.org/files/?artifact_id=92076)
# Resource Registry Management

Core DEH (Demeter Enabler Hub) module provides functionalities for the resource management process and interfacing with other components such as Compatibility Checker and Discovery Management.


## Table of contents
* [**Technologies**](#technologies)
* [**Features**](#features)
* [**Production**](#production)
* [**Requirements**](#requirements)
* [**Setup**](#setup)
* [**How to use**](#how-to-use)
* [**Endpoints**](#endpoints)
* [**Troubleshoot**](#troubleshoot)
* [**Contributors**](#contributors)
* [**Status**](#status)
* [**Release**](#release)
* [**Roadmap**](#roadmap)
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

* Installed Docker (version >= 18) 
* Installed Docker Compose


## Production


Resource Registry Management Hub is deployed and up an running.
All information about API is avaibable as Swagger documentation on: [https://deh.h2020-demeter-cloud.eu/swagger](https://deh.h2020-demeter-cloud.eu/swagger) 


## Setup

After pulling the source code, go to root folder and follow the next steps:

 

### Run application using docker-compose

* _Run_ `docker-compose up` _to run Docker Compose with server image and MongoDB_
* _If you want to run containers in background run next command_ `docker-compose up -d`
* _All docker variables related to RRM can be changed in_ `.env` _file_


## How to use

After containers are up and running, you can use Resource Registry Management through exposed REST API endpoints.


## Endpoints

Project supports Swagger, so all endpoints after starting an application are available for testing on `http://localhost:9091/swagger-ui.html` 

**List of endpoints**


| URL                            | Type         | Used for                                         | Input                                | Output                                                  |
| :----------------------------- | :----------: | :----------------------------------------------- | :----------------------------------- | :------------------------------------------------------ |
| **/api/v1/resources**          | **GET**      | Get list of all resources                        |                                      | List of all resources                                   |
| **/api/v1/resources**          | **POST**     | Save a new resource                              | Resource with attributes             | Saved resources with all details                        |
| **/api/v1/resources/{uid}**    | **PUT**      | Update whole resource                            | Resource uid, Updated resource       | Updated resource with all details                       |
| **/api/v1/resources/{uid}**    | **DELETE**   | Delete existing Resource                         | Resource uid                         | Resource deleted                                        |
| **/api/v1/resources/{uid}**    | **GET**      | Find resource by uid                             | Resource uid                         | Resource with all details                               |
| **/api/v1/resources/search**   | **GET**      | Advanced searching and filtering resources       | Value of any Resource attribute      | Resources that match the search criteria                |
| **/api/v1/resources/{uid}/rate**|  **POST**   | Rating DEH Resource                              | Resource uid, Resource rating (1-5)  | Updated resource with all details                       |


## Troubleshoot
**TO DO**


## Contributors

* [Marko Stojanovic](https://github.com/marest94) 
* [Marco Bernandino Romano](https://github.com/marco-romano-eng) 
* [Gianluca Isgro'](https://github.com/gianluca-isgro) 


## Release
V1.0

 | :dart: [Roadmap](roadmap.md) |
| ------------------------------------------ |


## License
<!--- If you're not sure which open license to use see https://choosealicense.com/--->
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)