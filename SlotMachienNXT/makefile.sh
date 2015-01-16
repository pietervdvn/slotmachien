nxjc -d bin src/io/*.java src/observable/*.java src/slotmachien/*.java src/slotmachien/actions/*.java src/time/*.java
cd bin
nxj -o test.nxj slotmachien.NXTMain
