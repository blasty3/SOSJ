For Linux environment:

To run SOSJ global registry application, execute sosjreg.sh script. The SOSJ global registry should be run on a different machine from your SOSJ programs. Some functions were implemented to enable SOSJ global registry to run on the same computer with SOSJ programs, but they weren't completely finished nor tested.

The current implementation requires 5 parameters/arguments:

1st arg - Registry ID, name of the registry (identifier) . 2nd arg - Physical Address of the Registry . 3rd arg - Beacon expiry (in milliseconds), period of which beacon must be refreshed. 4th arg - Default gateway Address of the network. 5th arg - SubnetMask Address of the network
If some of the parameters don't apply to your implementation, then the RTS needs to be modified to suit more generalized cases, but this wasn't part of the scope of the work.


To run SOSJ program, execute sosj.sh script
The implementation requires 2 parameters/arguments:

1st arg - the default gateway address of the network . 2nd arg - the configuration file (containing signal mapping, service description, see examples on how they are now structured and should be written) in xml format.

Some examples are provided, but they are there just to show how some of the APIs are used, they are not immediately executable (some modifications are needed).


For Windows environment: use the .bat scripts instead.


The framework requires some 3rd party libraries, JDOM parser, J2ME-JSON, and ASM. All included in the folder 'lib' inside 'SOSJFiles'. The SOSJ binary file is in the 'bin' folder.
To run SOSJ, put the SOSJ RTS binary and the library files to your Java classpath. Modify the executable scripts if need to be.



The framework was developed in Java 1.7 environment with NetBeans IDE. the framework hasn't been tested on Java 1.8 or later. It is compatible to SJ compiler of 2015 or some older versions (the latest compiler which has been tested for SOSJ is provided in the folder). 
No guarantee it will work on the later version of the compiler since some SJ syntaxes/statements and RTS implementations were modified sometime later after 2015.

Some bugs may also still be present, just an FYI. However unfortunately they weren't documented properly.

Unfortunately no manual was ever made. So contact me if you have any questions.


### IMPORTANT NOTE: Please make sure you have root or administrator log in when using SOSJ in your system, since SOSJ uses some network functions which require elevated access. Also, if there's a firewall or other similar security functions involved, configure them so they won't block the network communications of SOSJ.
If you need to use SOSJ in non-elevated access, then the RTS needs to be modified to change some implementation so SOSJ will not use functions requiring elevated access.


I can be contacted through udayanto.atmojo@gmail.com. Though I'll try to answer questions as best I can, I may not be able to give much technical help since i'm no longer working on this project.