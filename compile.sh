#!/bin/sh
MCP=../
cp -r client/* $MCP/src/minecraft/net/minecraft/src/
cp -r server/* $MCP/src/minecraft_server/net/minecraft/src/
pushd $MCP
./recompile.sh
popd
