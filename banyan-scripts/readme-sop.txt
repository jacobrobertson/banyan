/cygdrive/D/eclipse-workspaces/git/banyan-parent/banyan-scripts

Release checklist for banyan-js (complete re-build and redeploy for new species findings, etc.)
- run JsWorker (will delete and re-generate all json and rebuild lucene index)
- run rsync-banyan-js-core-webfiles.sh
- run mvn clean package for banyan-search
- run rsync-search-jar.sh
- run rsync-lucene-index.sh
- restart the NFSN daemon
- run aws sync scripts for json (note this costs money)
- run aws sync scripts for images (note this costs money)

Other info
- if the rsync asks for the keystore passphrase, just hit return


MORE NOTES
How to get Banyan up and running locally “from scratch”?
Create these folders under a D drive - map it if you have to
D:\banyan-db\derby
D:\banyan-images
D:\wikispecies-cache
Create the DB
Create the folder "D:\banyan-db\derby”
Run DerbyDataSource main to create the db
Run the crawler
Start out crawling something specific to test that it’s working
Browse the DB to see the data being saved
Derby can only have one connection at a time I think, so wait until the crawler isn’t running
