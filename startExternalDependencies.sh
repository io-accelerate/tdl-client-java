#!/bin/bash

set -e
set -u
set -o pipefail

startWiremocks() {
    echo "~~~~~~~~~~ Starting Wiremocks on ports 41375 and 8222 ~~~~~~~~~"
    python wiremock/fetch-wiremock-and-run.py start 41375
    python wiremock/fetch-wiremock-and-run.py start 8222
}

startBroker() {
    echo "~~~~~~~~~~ Starting Broker ~~~~~~~~~"
    python broker/activemq-wrapper.py start
}

startWiremocks
startBroker
