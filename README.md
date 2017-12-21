[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Maven Version](http://img.shields.io/maven-central/v/ro.ghionoiu/tdl-client-java.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22ro.ghionoiu%22%20AND%20a%3A%22tdl-client-java%22)
[![Codeship Status for julianghionoiu/tdl-client-java](https://img.shields.io/codeship/da7ca170-097e-0133-70b1-36ea30c979a9.svg)](https://codeship.com/projects/90604)
[![Coverage Status](https://coveralls.io/repos/julianghionoiu/tdl-client-java/badge.svg?branch=master&service=github)](https://coveralls.io/github/julianghionoiu/tdl-client-java?branch=master)


Java client to connect to the central kata server.

To run the acceptance tests, start the WireMock servers:
```
python wiremock-wrapper/fetch-wiremock-and-run.py 41375
python wiremock-wrapper/fetch-wiremock-and-run.py 8222
```

And the broker, with:
```
python broker/activemq-wrapper.py start
```

Then run the tests in RunAllAcceptanceTest.java


How to release a new version:
```bash
./release.sh
```

After Codeship build finishes, go to bintray.com and publish the new version then sync to Maven Central.