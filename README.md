# LockFactoryServer

[![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)

_Project information_        
[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?)](https://opensource.org/licenses/MIT)
![Top languaje](https://img.shields.io/github/languages/top/oscar-besga-panel/LockFactoryServer)
[![Wiki](https://badgen.net/badge/icon/wiki?icon=wiki&label)](https://github.com/oscar-besga-panel/LockFactoryServer/wiki)
[![OpenHub](https://badgen.net/badge/%20/openhub/purple?icon=awesome)](https://openhub.net/p/LockFactoryServer)

_Current Build_  
[![Build Status](https://app.travis-ci.com/oscar-besga-panel/LockFactoryServer.svg?branch=main)](https://app.travis-ci.com/oscar-besga-panel/LockFactoryServer)
![Issues](https://img.shields.io/github/issues/oscar-besga-panel/LockFactoryServer)  

_todo codecov_

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

## How to build

This project uses JDK11 and Gradle (provided gradlew 7.4)  

If you need a integration test for REST connection, you can use curl or postman

