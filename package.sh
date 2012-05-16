#!/bin/sh
pushd ..
./reobfuscate.sh

pushd reobf/minecraft
zip -r ../../port-hangablemaps/HangableMaps_v1.2.5_client-fml-r1.zip .
popd

pushd reobf/minecraft_server
zip -r ../../port-hangablemaps/HangableMaps_v1.2.5_server-fml-r1.zip .
popd

popd
