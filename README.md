# IDARE GEO SOFT Reader Plugin

This is a plugin for the Cytoscape IDARE app, which allows you to read GEO SOFT Files and integrate their data into an IDARE imagenode.

##Installation
* Download the Jar from the [University of Luxembourg](http://idare-server.uni.lu/IDAREGEOSoftReader-1.0.jar)
* Build using the sources provided in this repository (see **Building**)
Put the jar file into the apps/installed folder of your Cytoscape configuration folder (commonly %HOME/CytoscapeConfiguration/3/apps/installed).

##Building

To build:
```
git clone https://github.com/sysbiolux/IDAREGEOSoftReader.git IDAREGeoSoft
cd IDAREGeoSoft/GEOSoftReader
mvn install
```
