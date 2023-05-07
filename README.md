[![Java Version](http://img.shields.io/badge/Java-17-blue.svg)](http://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
[![Maven Version](http://img.shields.io/maven-central/v/ro.ghionoiu/tdl-client-java.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22ro.ghionoiu%22%20AND%20a%3A%22tdl-client-java%22)

### Submodules

Project contains submodules as mentioned in the `.gitmodules` file:

- broker
- src/test/resources/tdl/client (gets cloned into src/test/resources/acceptance)
- wiremock 

Use the below command to update the submodules of the project:

```
git submodule update --init
```

### Getting started

Java client to connect to the central kata server.

#### Manual 
To run the acceptance tests, start the WireMock servers:
```
python3 wiremock/wiremock-wrapper.py start 41375
python3 wiremock/wiremock-wrapper.py start 8222
```

And the broker, with:
```
python3 broker/activemq-wrapper.py start
```

Stopping the above services would be the same, using the `stop` command instead of the `start` command.

#### Automatic (via script)

Start and stop the wiremocks and broker services with the below:
 
```bash
./startExternalDependencies.sh
``` 

```bash
./stopExternalDependencies.sh
``` 

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
ls -l /Users/julianghionoiu/.m2/repository/ro/ghionoiu/tdl-client-java/$(cat gradle.properties | grep version | cut -d "=" -f2)
```

Publish to Maven Central Staging repo

### Publish to Maven Central

Publish to Maven Central Staging repo
```bash
./gradlew publish
```

A Staging repository is created automatically:
https://oss.sonatype.org/#stagingRepositories

To promote to the Live repo, do the following:
- "Close" the Staging repo
- "Refresh" the Staging repos
