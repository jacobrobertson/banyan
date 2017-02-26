Release checklist
- New WAR
-- run mvn clean install at root of banyan
-- run rsync-war.sh
- New images
-- run rsync-images.sh
- New slim DB
-- see below for full procedures

Periodic checklist
- Tomcat - is what I have locally truly what I have remotely?

How to deploy new derby DB to NFSN
- Configure a new derby connection to the slim location
- Copy original DB to a slim folder
- Connect to slim connection - double check this!
- run slim.sql on that new slim DB (will take a while)
-- Will remove rows, and drop tables, and compress DB - saves 80% of space
- run rsync-db.sh


Other info
- if the rsync asks for the keystore pass, just hit return