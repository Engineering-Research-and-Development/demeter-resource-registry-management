![](https://portal.ogc.org/files/?artifact_id=92076)
# Resource Registry Management

Core DEH (Demeter Enabler Hub) module provides functionalities for managing DEH Resources. It manages creating, validating, editing, deleting, discovery and consumption of DEH Resources.


## Table of contents
* [**Architecture**](#architecture)
* [**Technologies**](#technologies)
* [**Features**](#features)
* [**Production**](#production)
* [**Requirements**](#requirements)
* [**Setup local instance**](#setup-local-instance)
* [**How to use**](#how-to-use)
* [**Endpoints**](#endpoints)
* [**Support team**](#support-team)
* [**Status**](#status)
* [**Release**](#release)
* [**Roadmap**](#roadmap)
* [**Licence**](#licence)


## Architecture

To make the solution more flexible and easier to maintain, all components inside the DEH are developed as separate services and deployed as standalone Docker containers.
_**DEH Dashboard (DEH Dymer sub-component)**_ is developed as an external component outside of the _**DEH Resource Registry Management APIs**_ , which has  next internal modules: _**DEH Resource Registry Management**_, _**Compatibility Checker**_  and _**Discovery Management**_ .
Secured communication among all components is provided by _**ASC**_ , more specifically by the _**Identity Manager**_ , and _**Resource Access Control**_  components.

![](https://raw.githubusercontent.com/Engineering-Research-and-Development/demeter-resource-registry-management/develop/screenshots/deh_arch.jpg)


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
* Storing Resources consumption metrics
* Accessing Resources consumption metrics


## Requirements

* Installed Docker (version >= 18)
* Installed Docker Compose


## Production

Resource Registry Management is deployed and up and running.
All information about API is avaibable as Swagger documentation on: [https://deh.h2020-demeter-cloud.eu/swagger](https://deh.h2020-demeter-cloud.eu/swagger)

You can find a download a Postman collection both for DEH Producer and DEH Consumer with all necessary calls for communication with RRM without using DEH Dashboard available on [https://deh.h2020-demeter-cloud.eu](https://deh.h2020-demeter-cloud.eu)

Necessary steps to do:

1. Obtain an Authentication Token from IDM (x-subject-token)
2. Obtain Capability Token from Capability Manager (x-auth-token)
3. Make a request to RMM with x-subject and x-auth-token in the header


_**Important:**_ _For each call that you make to RRM, a new Capability token should be obtained e.g. Get the list of all resources, save a new resource, etc_

## Setup local instance

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
| **/api/v1/metrics**|  **POST**   | Saving new DEH Resource metrics                               | Readings from DEH Client  | Message about successful metrics storing                    |
| **/api/v1/metrics**|  **GET**   | Get all user related metrics (Owned and consumed)                              | / | List of all user related resources metrics                       |
| **/api/v1/metrics/rrmId/{rrmId}**|  **GET**   | Metrics for specific Resource                           | Resource uid | List of all metrics for specific resource                   |
| **/api/v1/metrics/containerId/{containerId}**|  **GET**   | Metrics for specific container                               | Container id  | List of all metrics for specific container                     |

## Support team

* [Marko Stojanovic (Development)](mailto:marko.stojanovic@eng.it)
* [Marco Bernandino Romano (Development)](mailto:MarcoBernardino.Romano@eng.it)
* [Gianluca Isgro' (Delivery)](mailto:gianluca.isgro@eng.it)
* [Antonio Caruso (Integration)](mailto:Antonio.Caruso@eng.it)



## Release

V1.0

| :dart: [Roadmap](roadmap.md) |
| ------------------------------------------ |


## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
