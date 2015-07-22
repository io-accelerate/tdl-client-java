[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Maven Version](http://img.shields.io/maven-central/v/ro.ghionoiu/tdl-client-java.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22ro.ghionoiu%22%20AND%20a%3A%22tdl-client-java%22)
[![Codeship Status for julianghionoiu/tdl-client-java](https://img.shields.io/codeship/da7ca170-097e-0133-70b1-36ea30c979a9.svg)](https://codeship.com/projects/90604)
[![Coverage Status](https://coveralls.io/repos/julianghionoiu/tdl-client-java/badge.svg?branch=master&service=github)](https://coveralls.io/github/julianghionoiu/tdl-client-java?branch=master)

# 1. Description
Clients to connect to the central kata server.


# 2. How to get the library


# 3. Usage

You start by configuring a `competition.client.Client`.

```java
String brokerURL = "tcp://<IP>:<PORT>"; 
String username =  "your_username";
Client client = new Client(brokerURL, username);

client.trialRunWith(params -> {
    return null;
});
```

If the configuration is correct then you should be able to see these lines in the logs:

```text
INFO  c.c.transport.CentralQueueConnection - Starting client
INFO  competition.client.Client - Stopping client.
INFO  c.c.transport.CentralQueueConnection - Stopping client
```


The client has two methods:
 - `trialRunWith(UserImplementation userImplementation)`
 - `goLiveWith(UserImplementation userImplementation)`
 
## a. Trial run

The trial run allows you to see the first item and the result provided by your implementation.
Nothing is submitted to the server.

```java
//Example: Adding two numbers
client.trialRunWith(params -> {
    Integer x = Integer.parseInt(params[0]);
    Integer y = Integer.parseInt(params[1]);
    return x + y;
});
```

## b. Go live

Once your implementation is sound and you are confident you can server user requests you just replace: `trialRunWith` with `goLiveWith`

```java
//Example: Adding two numbers
client.goLiveWith(params -> {
    Integer x = Integer.parseInt(params[0]);
    Integer y = Integer.parseInt(params[1]);
    return x + y;
});
```

The method will stop processing requests if the implementation returns null or throws an exception.
