rsync -avz -e "ssh -o StrictHostKeyChecking=no -o \
UserKnownHostsFile=/dev/null" --chmod=777 --progress \
/cygdrive/D/banyan-images/tiny \
pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan-images