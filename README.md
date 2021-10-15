Ballerina Azure Service Bus Connector
===================

[![Build](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/workflows/CI/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions?query=workflow%3ACI)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-azure-service-bus.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/commits/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The [Azure Service Bus](https://azure.microsoft.com/en-us/services/service-bus/) is a reliable cloud Messaging as a
Service (Maas). It is an enterprise message broker with message queues and publish-subscribe topics. The service bus is
used to decouple applications and services from each other. Data is transferred between different applications and
services using messages. You can find more
information [here](https://docs.microsoft.com/en-us/azure/service-bus-messaging/).

The Azure Service Bus [Ballerina](https://ballerina.io/) connector allows you to connect to the Azure Service Bus to
send and receive messages. You can perform actions such as send to queue, send to topic, receive from queue, receive
from subscription, etc.

For more information, go to the module(s).

- [asb](asb-ballerina/Module.md)

## Building from the source

### Setting up the prerequisites

1. Download and install Java SE Development Kit (JDK) version 11. You can install
   either [OpenJDK](https://adoptopenjdk.net/)
   or [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

   > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2. Download and install [Ballerina](https://ballerina.io/).

### Building the source

Execute the commands below to build from the source.

1. To Build & Package the Native Java Wrapper:
   Change the current directory to the root directory and execute the following command.

```shell script
    ./gradlew build
```

2. To build the Ballerina package:
   Change the current directory to the asb-ballerina home directory and execute the following command.

```shell script
    bal build -c
```

3. To build the Ballerina package without running the tests:
   Change the current directory to the asb-ballerina home directory and execute the following command.

```shell script
    bal build -c --skip-tests
```

## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community.

For more information, go to
the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

* Discuss code changes of the Ballerina project
  in [ballerina-dev@googlegroups.com](mailto:ballerina-dev@googlegroups.com).
* Chat live with us via our [Slack channel](https://ballerina.io/community/slack/).
* Post all technical questions on Stack Overflow with
  the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
