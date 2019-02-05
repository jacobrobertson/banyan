# warning, this deletes all files that don't exist locally, **EXCEPT** GENERATED JSON - that's safe!
rsync --chmod=777 -avz --exclude 'json' -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress \
	../banyan-js/src/main/webapp/ pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan/ \
	--delete
