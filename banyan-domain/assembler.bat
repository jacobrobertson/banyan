call mvn clean package org.codehaus.mojo:appassembler-maven-plugin:1.0:assemble -Dmaven.test.skip=true
call rmdir /S /Q c:\species-target
call xcopy target C:\species-target /S /I
