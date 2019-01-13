aws s3 sync /cygdrive/D/banyan-images/detail s3://banyan-files/banyan-images/detail
aws s3 sync /cygdrive/D/banyan-images/preview s3://banyan-files/banyan-images/preview

// do not sync "tiny" - these are all in the json

aws s3 sync --dryrun /cygdrive/D/eclipse-workspaces/git/banyan-parent/banyan-js/src/main/webapp/json s3://banyan-files/banyan-website/json

http://banyan-files.s3-website.us-east-2.amazonaws.com/banyan-images/detail/6e/Acacia%20restiacea.jpg