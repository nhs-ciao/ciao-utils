#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage getTKW.sh type installpath username"
    echo "   type : this specified which TKW version you want to install - it must either be ITK or Spine"
    echo "   installpath : Where to install TKW (note this must be an absolute path e.g. /opt/TKW"
    echo "   username : This is the username which will be added to the configuration files (see user manual for details"
    exit 1
fi

tkwtype=$1
path=$2
username=$3

# Download and extract TKW

if [ "$tkwtype" == "ITK" ]
then
  jarName="tkwinstaller.jar"
elif [ "$tkwtype" == "Spine" ]
then
  jarName="spinetkwinstal.jar"
else
  echo "The type parameter must be either ITK or Spine"
fi

wget -O tkwinstaller.jar http://systems.hscic.gov.uk/sa/tools/$jarName
unzip -qq tkwinstaller.jar tkwinstaller/TKW.zip
unzip -qq tkwinstaller/TKW.zip
rm -Rf tkwinstaller
rm -f tkwinstaller.jar

# Check if path provided has a trailing slash, and if so, remove it
lastChar=${path:(-1)}
echo $lastChar

if [ "$lastChar" == "/" ]
then
  path=${path:0:${#path}-1}
fi

# Move to the correct location
echo "Installing to path: $path"

mv TKW $path

# Fix paths and username in config files (replicates what the gui installer does)
echo "Fixing config files"

escapedPath=${path//\//\\/}
find $path/config -name "*.properties" | while read f
do
  perl -pi -e "s/__USER_NAME_AND_ORGANISATION_NOT_SET__/$username/g" $f
  perl -pi -e "s/TKW_ROOT/$escapedPath/g" $f
done

find $path/config -name "*.txt" | while read f
do
  perl -pi -e "s/__USER_NAME_AND_ORGANISATION_NOT_SET__/$username/g" $f
  perl -pi -e "s/TKW_ROOT/$escapedPath/g" $f
done

find $path/config -name "*.conf" | while read f
do
  perl -pi -e "s/__USER_NAME_AND_ORGANISATION_NOT_SET__/$username/g" $f
  perl -pi -e "s/TKW_ROOT/$escapedPath/g" $f
done

echo "Installation complete"
