#!/bin/bash

set -e
set -u
set -o pipefail


stopWiremocks() {
    echo "~~~~~~~~~~ Stoping Wiremocks listening on ports 41375 and 8222 ~~~~~~~~~"
    python wiremock/fetch-wiremock-and-run.py stop || true
}

stopBroker() {
    echo "~~~~~~~~~~ Stoping Broker ~~~~~~~~~"
    python broker/activemq-wrapper.py stop || true
}

stopWiremocks
stopBroker