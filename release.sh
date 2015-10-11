#!/bin/bash

SCRIPT_FOLDER="$(cd -P "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION_FILE="${SCRIPT_FOLDER}/gradle.properties"
SPEC_FOLDER="${SCRIPT_FOLDER}/src/test/resources/tdl/client/"


function get_version_from_tag() {
    local target_folder=$1
    git --git-dir ${target_folder}/.git describe --all | cut -d "/" -f 2 | tr -d "v"
}

function get_property() {
    local property=$1
    cat ${VERSION_FILE} | grep "${property}" | cut -d "=" -f2 | tr -d " "
}

# Current
CURRENT_MAJOR_MINOR_VERSION=`get_version_from_tag ${SPEC_FOLDER}`
CURRENT_PATCH_VERSION=`get_property "currentPatchVersion"`
CURRENT_VERSION="${CURRENT_MAJOR_MINOR_VERSION}.${CURRENT_PATCH_VERSION}"

# Previous
PREVIOUS_MAJOR_MINOR_VERSION=`get_property "previousVersion" | cut -d "." -f 1,2`
PREVIOUS_PATCH_VERSION=`get_property "previousVersion" | cut -d "." -f 2`
PREVIOUS_VERSION=`get_property "previousVersion"`

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

# Increment version
next_patch_version() {
    if [ "$CURRENT_MAJOR_MINOR_VERSION" == "$PREVIOUS_MAJOR_MINOR_VERSION" ]; then
        patch=$((patch+1))
    else
        patch="0"
    fi
    echo ${patch}
}

# Switch to next version
NEXT_PATCH_VERSION=`next_patch_version`
echo "Next version is: $NEXT_PATCH_VERSION"

cat > "${VERSION_FILE}" <<-EOF
previousVersion=$CURRENT_VERSION
# the current MAJOR.MINOR version is dynamically computed from the version of the Spec
currentPatchVersion=1
EOF