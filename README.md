[![Java Version](http://img.shields.io/badge/Java-21-blue.svg)](http://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
[![Maven Version](http://img.shields.io/maven-central/v/io.accelerate/tdl-client-java.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.accelerate%22%20AND%20a%3A%22tdl-client-java%22)

### Submodules

Project contains the client spec as mentioned in the `.gitmodules` file:

- src/test/resources/tdl/client (gets cloned into src/test/resources/acceptance)

Use the below command to update the submodules of the project:

```
git submodule update --init
```
### Getting started

JVM client to connect to the central kata server.

# Installing

## Installing dependencies needed by this project

```bash
./gradlew build
```

# Testing

All test require the ActiveMQ broker and Wiremock to be started.

Start ActiveMQ
```shell
export ACTIVEMQ_CONTAINER=apache/activemq-classic:6.1.0
docker run -d -it --rm -p 28161:8161 -p 21616:61616 --name activemq ${ACTIVEMQ_CONTAINER}
```

The ActiveMQ web UI can be accessed at:
http://localhost:28161/admin/
use admin/admin to login

Start two Wiremock servers
```shell
export WIREMOCK_CONTAINER=wiremock/wiremock:3.7.0
docker run -d -it --rm -p 8222:8080 --name challenge-server ${WIREMOCK_CONTAINER}
docker run -d -it --rm -p 41375:8080 --name recording-server ${WIREMOCK_CONTAINER}
```

The Wiremock admin UI can be found at:
http://localhost:8222/__admin/
and docs at
http://localhost:8222/__admin/docs


Then run the tests in RunAllAcceptanceTest.java via the CLI:

```bash
./gradlew test
```

Or via the IDE

### Release

Configure the version inside the "gradle.properties" file

Create publishing bundle into Maven Local
```bash
./gradlew publishToMavenLocal
```

Check Maven Local contains release version:
```
CURRENT_VERSION=$(cat gradle.properties | grep version | cut -d "=" -f2)

ls -l $HOME/.m2/repository/io/accelerate/tdl-client-java/${CURRENT_VERSION}
```


### Publish to Maven Central - the manual way

At this point publishing to Maven Central from Gradle is only possible manually.
Things might have changed, check this page:
https://central.sonatype.org/publish/publish-portal-gradle/

Generate the Maven Central bundle:
```
./generateMavenCentralBundle.sh
```

Upload the bundle to Maven Central by clicking the "Publish Component" button.
https://central.sonatype.com/publishing
