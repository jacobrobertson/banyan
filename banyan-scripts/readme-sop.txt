Release checklist for banyan-js (complete re-build and redeploy for new species findings, etc.)
- run JsWorker (will delete and re-generate all json and rebuild lucene index)
- run rsync-banyan-js-all-flat-files.sh
- run rsync-images.sh
- run mvn clean package for banyan-search
- run rsync-search-jar.sh
- run rsync-lucene-index.sh
- restart the NFSN daemon

Other info
- if the rsync asks for the keystore passphrase, just hit return