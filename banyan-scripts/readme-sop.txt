Release checklist
* Note that if you are going to run war or db sync, first stop (then start) the daemon on NFSN
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
- Configure a new derby connection to the slim location (note there is no user or pass to connect - this is okay, it's not a server)
  - look in the eclipse "Data Source Explorer"
  - D:\banyan-db\derby-slim\species
- Copy original DB to a slim folder
- Connect to slim connection - double check this!
- run slim.sql on that new slim DB (will take a while) (from the sql view in eclipse)
-- Will remove rows, and drop tables, and compress DB - saves 80% of space
- run rsync-db.sh


Other info
- if the rsync asks for the keystore pass, just hit return