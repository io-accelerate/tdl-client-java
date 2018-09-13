#!/bin/bash

set -e
set -u
set -o pipefail

BROKER_TYPE=${BROKER_TYPE:-"amazonsqs"}
SQS_PORT=28161

startWiremocks() {
    echo "~~~~~~~~~~ Starting Wiremocks on ports 41375 and 8222 ~~~~~~~~~"
    python wiremock/fetch-wiremock-and-run.py start 41375
    python wiremock/fetch-wiremock-and-run.py start 8222
}

startBroker() {
    echo "~~~~~~~~~~ Starting Broker ~~~~~~~~~"
    if [[ "${BROKER_TYPE}" == "activemq" ]]; then
        echo "~~~~~~~~~~ Broker type: ${BROKER_TYPE} ~~~~~~~~~"
        python broker/activemq-wrapper.py start
    else
        IMAGE_NAME=goaws
        echo "~~~~~~~~~~ Broker type: ${BROKER_TYPE} ~~~~~~~~~"
        docker pull pafortin/${IMAGE_NAME}
        docker run -d --name ${IMAGE_NAME} -p ${SQS_PORT}:4100 pafortin/${IMAGE_NAME}

        export AWS_ACCESS_KEY_ID="local_test_access_key"
        export AWS_SECRET_ACCESS_KEY="local_test_secret_key"
        export SQS_REGION="amazonsqs"

cat > ${HOME}/.aws/config <<EOL
[default]
region = amazonsqs
output = json
EOL

        aws --endpoint-url http://localhost:${SQS_PORT} sqs create-queue --queue-name testuser.req
        aws --endpoint-url http://localhost:${SQS_PORT} sqs create-queue --queue-name testuser.resp
    fi
}

startWiremocks
startBroker
