------------------------------------------------------------------------
This is the project README file. Here, you should describe your project.
Tell the reader (someone who does not know anything about this project)
all he/she needs to know. The comments should usually include at least:
------------------------------------------------------------------------

PROJECT TITLE: phys.p.realtime
PURPOSE OF PROJECT: simulate 2d particle physics in real time using multithreading
VERSION or DATE:
HOW TO START THIS PROJECT:
To enable ridiculous numbers of threads to be made, use the option -Xss
The minimum value possible (that allows java to run still) seems to be 1k.
That seems to work fine, but it would be much safer to use -Xss128k
use somewhere between 1k and 256k. (Default is 1m)
java -Xss64k Execute

Also, if you provide an argument, it will be used as debugging. e.g.
java -Xss64k Execute 0
AUTHORS: Jonathan Black
USER INSTRUCTIONS:
