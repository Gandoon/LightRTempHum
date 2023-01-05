# LightRTempHum
An Arduino based clock with temperature and humidity readings, and a control and logging interface.

Copyright is held by by Erik Hedlund *(LightR)*, 2021-2023

## EnvironmentMonitor
This repository currently contains the java based control and logging application. 

To build this the following dependencies will be required (later or earlier versions may or may not work out of the box): 

[MiGLayout](https://github.com/mikaelgrev/miglayout)

[JFreeChart 1.5.3](https://github.com/jfree/jfreechart)

[JSerialComm >= 2.6.2 (v2.9.3 confirmed to work on Apples ARM chips)](https://github.com/Fazecast/jSerialComm/releases)

## Licensing
This project is released under Gnu GPL v2 (available in this repository). 
This is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
### Dependency licenses
Licensing of the additional required external libraries is according to their respective original licenses. Parts of the project, such as the communication subsystem using jSerialComm, and some presentation components, are based on code from previously written code released under various licenses. JSerialComm uses Gnu (L)GPLv3 and Apache-2.0 and retains those licenses. The MiG Layout allows for use of either a GPL or BSD license. Finally, the JFreeChart, used for the visual representation of values (dials and charts) is released under LGPLv2.1. 

No code from any of the dependencies is included here. If you want to build this, please source the dependencies in a way that fits you.
