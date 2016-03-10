#!/bin/bash

source ../env.sh

"$MVN_HOME/mvn" clean package org.codehaus.mojo:appassembler-maven-plugin:1.0:assemble -Dmaven.test.skip=true
rm -rf /cygdrive/D/eclipse-workspaces/git/banyan/banyan-apps
cp -r target /cygdrive/D/eclipse-workspaces/git/banyan/banyan-apps
