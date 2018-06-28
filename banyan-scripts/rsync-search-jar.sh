cp ../banyan-search/target/banyan-search-*.jar ./banyan-search.jar
rsync -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress banyan-search.jar pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan/
rm ./banyan-search.jar