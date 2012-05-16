#!/bin/sh
mkdir -p net/minecraft/server
cp *.class net/minecraft/server
zip -r HangableMaps_v1.2.5_bukkit-fml-mcpc93-r1.zip `find net -name '*.class'`
