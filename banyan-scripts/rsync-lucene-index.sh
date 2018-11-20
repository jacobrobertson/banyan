rsync --chmod=777 -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress \
	/cygdrive/D/home/private/banyan-lucene/ pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/private/banyan-lucene/ \
	--delete

