#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $(cd $DIR/src && javac -d ../build MyBot.java)
echo $(cd $DIR && ./halite -i logs -q -r 'java -cp "lib/*":build MyBot' 'java -cp "lib/*":build MyBot')
echo $(cd $DIR && mv *.log logs)
