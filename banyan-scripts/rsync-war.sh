# rename current war
# overwrite remote war
# delete local copied ear

cp ../banyan-web/target/species.war ROOT.war

rsync -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress ROOT.war pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan/apache-tomcat-5.5.33/webapps

#uncomment this if the favicon changes
#rsync -avz -e "ssh -vvvv -i ~/.ssh/id_rsa.pub" --progress ../banyan-web/src/main/webapp/favicon.ico pepblast_jrob@ssh.phx.nearlyfreespeech.net:/home/public/banyan

rm ROOT.war