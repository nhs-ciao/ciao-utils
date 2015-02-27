#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage getETCD.sh installpath"
    echo "   installpath : Where to install ETCD (note this must be an absolute path e.g. /opt/etcd"
    exit 1
fi

path=$1

# Download the binary
curl -L  https://github.com/coreos/etcd/releases/download/v2.0.3/etcd-v2.0.3-linux-amd64.tar.gz -o etcd-v2.0.3-linux-amd64.tar.gz
tar xzvf etcd-v2.0.3-linux-amd64.tar.gz
rm etcd-v2.0.3-linux-amd64.tar.gz

# Check if path provided has a trailing slash, and if so, remove it
lastChar=${path:(-1)}
echo $lastChar

if [ "$lastChar" == "/" ]
then
  path=${path:0:${#path}-1}
fi

# Move to the correct location
echo "Installing to path: $path"
mv etcd-v2.0.3-linux-amd64 $path

# Add a link in /usr/bin
ln -s $path/etcd /usr/bin/etcd

echo "Install complete, type etcd to start"

