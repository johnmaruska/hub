# This file is basically a .bash/history of what to do to run the hub in a linux
# VM. Stop-gap measure until I sort out getting a better approach like a
# Clojure/Java Dockerfile that just runs a pre-built JAR. I'd like this up and
# running sooner than later though.

# curl should exist
# bash should exist

# java should exist
sudo apt-get install default-jre
sudo apt-get install default-jdk

# leiningen should exist
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > lein
chmod a+x lein
# add leiningen to path
export PATH=$PATH:$(pwd)

# git should exist
sudo apt-get install git

# Clone the repository
git clone https://github.com/johnmaruska/hub.git
cd hub

# run the thing
# git pull
lein run
