# LockFactoryServer

[![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)

_Project information_        
[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?)](https://opensource.org/licenses/MIT)
![Top languaje](https://img.shields.io/github/languages/top/oscar-besga-panel/LockFactoryServer)
[![Wiki](https://badgen.net/badge/icon/wiki?icon=wiki&label)](https://github.com/oscar-besga-panel/LockFactoryServer/wiki)
[![Github Web page](https://badgen.net/badge/github/website?icon=github)](https://oscar-besga-panel.github.io/LockFactoryServer/)
[![OpenHub](https://badgen.net/badge/%20/openhub/purple?icon=awesome)](https://openhub.net/p/LockFactoryServer)

_Current Build_  
[![Build Status](https://app.travis-ci.com/oscar-besga-panel/LockFactoryServer.svg?branch=main)](https://app.travis-ci.com/oscar-besga-panel/LockFactoryServer)
![Issues](https://img.shields.io/github/issues/oscar-besga-panel/LockFactoryServer)
[![codecov](https://codecov.io/gh/oscar-besga-panel/LockFactoryServer/branch/main/graph/badge.svg?token=BUFDK9DQ3Q)](https://codecov.io/gh/oscar-besga-panel/LockFactoryServer)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/oscar-besga-panel/LockFactoryServer.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/oscar-besga-panel/LockFactoryServer/context:java)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/95b46a3667b143ba80848c2bd3889890)](https://www.codacy.com/gh/oscar-besga-panel/LockFactoryServer/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=oscar-besga-panel/LockFactoryServer&amp;utm_campaign=Badge_Grade)

## Introduction


A synchroniztation primitives  (lock / semaphore / countdownlach) server with various connection interfaces (like gRPC, Java RMI, rest),
developed in Java and ready to use.  
So various processes/applications/threads can acccess the same primitive by using any connection method
(the connections share the primitives caches ).
A synchronization primitive will be automatically erased from the cache unless it is in use (particular for every type).

You can activate or deactivate each type of primitive and also each type of connection.
But once something is activated, is avalible to be used/accesed by every other active type (no fine grained control)




In really, really alpha for now...  

See the [wiki](https://github.com/oscar-besga-panel/LockFactoryServer/wiki) for more documentation

## Structure

The project has 4 sub-modules
* **core**: it has the main proto and interface files. It also servers as documentation for the service and their methods and operations.
All other modules depend on this one 
* **server**: This is the server implementation, with the RMI, REST and gRPC connections that offer the core-defined services.  
The server can run standalone or be embeeded into other services or applications.
* **client**: A Java client implementation for each service and connection type; it offers a simplified way to use the services and a simple coding example
* **integration**: this module depends on the server and client, and obviously the core. It has no production code, but only test code.  
It executes integration tests, where a local server is started and local clients execute complex operations against it. It servers to ensure the good performance and correctness of the system.  
There are more than 50 integration tests to ensure the correctness of the project.

There are also more than 235 unit test to ensure a working project and resilence againts changes.

## How to build

This project uses JDK11 and Gradle (provided gradlew 7.4)  

If you need a integration test for REST connection, you can use curl or postman


## Rationale

I made this project because I wanted to have a 100% Java implementation of a server which provides synchronization primitives as services.  
I know you can do this with mongo, redis (as see [here](https://github.com/oscar-besga-panel/InterruptingJedisLocks)) or other servers; but I wanted a stand-alone, slimmed, only-for-this-purpose, do-one-thing-and-do-it-well implementation.  
This is why I don't want to provide with memory-related apis, nor messaging or other utilites; as redis, rabbitmq and other are fully available out there.


