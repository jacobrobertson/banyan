

# TINY ONLY
rsync --chmod=777 -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress /cygdrive/D/banyan-images/tiny pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan-images

# PREVIEW ONLY
rsync --chmod=777 -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress /cygdrive/D/banyan-images/preview pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan-images

# NOT doing DETAIL

#rsync -avz -e "ssh -vvv -i ~/.ssh/id_rsa.pub -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" --chmod=777 --progress /cygdrive/D/banyan-images/tiny pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan-images

#ssh -vvvv -i ~/.ssh/id_rsa.pub pepblast_jrob@ssh.phx.nearlyfreespeech.net


#rsync -avz --progress /cygdrive/D/banyan-images/tiny pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan-images