Readme of project 2 NU 

There following classes and packages are necessery for the server part (which should be on the raspberry pi) ; 
- Package Timer
- Package Server 
- Class Fileclass
- Class MakePacket
- Class Receiving 
- Class sending 

The following classes and packages are necessary for the client part (on the laptop): 
- Package Timer
- Package Client
- Package Exceptions
- Class Fileclass
- Class MakePacket
- Class Receiving 
- Class sending 



To get the files to the raspberry pi you have to type : 
```
./gradlew deploy
```
This will deploy the the folder nu-module2-mod2.2023 to the raspberry pi, which contains all the necessary files. 


If you start the raspberry pi you the server side should automatically start you can test this with : 
```
systemctl status num2
```

To stop the server you should type : 
```
sudo shutdown -h now
```

To start a client you should run the class clientTUI. Make sure the server is running. 
When starting the client the it asks for the Inetaddress and port number : 
The Inetadresss : 172.16.1.1 
The port number:  62830 (is as shown when you type systemctl status num2) 


If you want to run the test classes read the instructions (if any)  carefully. 
The ResentTest will not stop running but gives an example of how the timer resend a packet. 
