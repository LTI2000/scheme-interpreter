#!/bin/bash

GROUP_ID="com.google.googlejavaformat"
MVN_PATH=${GROUP_ID//.//}
ARTIFACT_ID="google-java-format"
VERSION="1.34.1"
CLASSIFIER="all-deps"

# Download JAR from Maven repo
mvn dependency:get -q \
                   -DgroupId=${GROUP_ID} \
                   -DartifactId=${ARTIFACT_ID} \
                   -Dversion=${VERSION} \
                   -Dclassifier=${CLASSIFIER} \
                   -Dtransitive=false

# Resolve local repository path
MVN_REPO=$(mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)

# Execute JAR from the local repository
java -jar ${MVN_REPO}/${MVN_PATH}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${VERSION}-${CLASSIFIER}.jar ${@}
