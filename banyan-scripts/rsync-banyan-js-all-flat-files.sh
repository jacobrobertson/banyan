rsync --chmod=777 -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress \
	../banyan-js/src/main/webapp pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan \
	--delete

