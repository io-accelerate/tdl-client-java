#!/bin/bash

SCRIPT_FOLDER="$(cd -P "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION_FILE="${SCRIPT_FOLDER}/gradle.properties"
SPEC_FOLDER="${SCRIPT_FOLDER}/src/test/resources/tdl/client/"
TMP_VERSION_FILE="${SCRIPT_FOLDER}/build/versions.txt"


function gradle_property() {
    local property=$1
    cat ${TMP_VERSION_FILE} | grep ${property} | cut -d "=" -f2 | tr -d " "
}

echo "Reading gradle properties. This might take some time."

# Previous
${SCRIPT_FOLDER}/gradlew -q printVersionInformation | tee ${TMP_VERSION_FILE}
PREVIOUS_VERSION=`gradle_property PREVIOUS_VERSION`
CURRENT_VERSION=`gradle_property CURRENT_VERSION`

# Prompt for version confirmation
read -p "Going to release version ${CURRENT_VERSION} (previous ${PREVIOUS_VERSION}). Proceed ? [y/n] "
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "Aborting."
    exit
fi

# Release current version
git tag -a "v${CURRENT_VERSION}" -m "Release ${CURRENT_VERSION}"
git push origin "v${CURRENT_VERSION}"
echo "Pushed tag to Git origin. It will now trigger the deployment pipeline."

cat > "${VERSION_FILE}" <<-EOF
previousVersion=$CURRENT_VERSION
# the current MAJOR.MINOR.PATCH version is dynamically computed from the version of the Spec and previous patch
EOF