#!/bin/bash

set -e
set -u
set -o pipefail

BROKER_TYPE=${BROKER_TYPE:-"activemq"}
SQS_PORT=28161

stopProcessAtPort() {
    PORT=$1
    PID=$(netstat -tulpn | grep :${PORT} | awk '{print $7}' | tr -d "/java" || true)
    if [[ -z "${PID}" ]]; then
        echo "~~~~~~~~~~ Process on port ${PORT} stopped ~~~~~~~~~"
    else
        kill -9 ${PID}
        echo "~~~~~~~~~~ Process on port ${PORT} killed ~~~~~~~~~"
    fi
}

stopWiremocks() {
    echo "~~~~~~~~~~ Stopping Wiremocks listening on ports 41375 and 8222 ~~~~~~~~~"
    python wiremock/fetch-wiremock-and-run.py stop || true

    stopProcessAtPort 41375
    stopProcessAtPort 8222
}

stopBroker() {
    echo "~~~~~~~~~~ Stopping Broker ~~~~~~~~~"
    if [[ "${BROKER_TYPE}" == "activemq" ]]; then
        python broker/activemq-wrapper.py stop
    elif [[ "${BROKER_TYPE}" == "elasticmq" ]]; then
        python ../tdl-local-sqs/elasticmq-wrapper.py stop
        stopProcessAtPort 9324
    elif [[ "${BROKER_TYPE}" == "amazonsqs" ]]; then
        CONTAINER_NAME=goaws
        CONTAINER_ID=$(docker ps --filter="name=${CONTAINER_NAME}" -q)
        echo ${CONTAINER_ID} | xargs -r docker stop || true
        sleep 2
        CONTAINER_ID=$(docker ps --filter="name=${CONTAINER_NAME}" -q || true)
        if [[ -z "${CONTAINER_ID}" ]]; then
            echo "~~~~~~~~~~ Broker Stopped ~~~~~~~~~"
        else
           echo "~~~~~~~~~~ Killing Broker (was still running as Container ID ${CONTAINER_ID}) ~~~~~~~~~"
           echo ${CONTAINER_ID} | xargs -r docker kill || true
        fi

        echo "~~~~~~~~~~ Removing container ${CONTAINER_ID} ~~~~~~~~~"
        CONTAINER_ID=$(docker ps --filter="name=${CONTAINER_NAME}" -q -a)
        echo ${CONTAINER_ID} | xargs -r docker rm || true
    fi
}

stopWiremocks
stopBroker
