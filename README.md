# ﻿Main Idea
Implant a double authentication factor in system which implement RFID technology, using the geolocation of mobile devices associated.

**Parts of the Project**
In this project, we will develop in different technologies...

**1. -Server**: This server contains our APIRest,

*Resources:*

https://github.com/ferdinan4/RFID-Security/tree/master/server

**2.-RFID Reader**: This app coding in android (java) allow us to read NFC cards and, using the    APIRest defined in our server, we can check if the “id” 
reader from the card, is in the DB of our company, and if is in our DB the second thing that check, is the last location associated to that “id”. So finally, the 
result of this checks this app are sending to Arduino.

*Resources:*

https://github.com/ferdinan4/RFID-Security/tree/master/android/NFCReader

**3. -GP2FA**; This app coding in android (java), we authenticate agains our server, and we save user password(cipher) and the session token, this token will be sending with our geoposition to the server that will use this data for improving the authentication method in a RFID system checking if the position is in the allowed radio 
next to the entrance of the company.

*Resources:*

https://github.com/ferdinan4/RFID-Security/tree/master/android/GPS2FA

**--This part is develop for improving our demo in the presentation of 04-12-2016 in Hackaton of Incibe.**

**Ext-1. -Arduino_RFIDReader**: This arduino code is used in our demo to recieve information that send our Android App "RFID Reader", the information trasmited, will be used for Arduino to simulate a system turning on each leds(Red, Yellow adn Green) depends on the 
authentication is correct/incorrect(green/red) or authentication is correct but the position of mobile device associate  is not in the allowed range(yellow).
We add the library used to management the USB.

*Resources*: 

https://github.com/ferdinan4/RFID-Security/tree/master/arduino/RFID_Reader

https://github.com/ferdinan4/RFID-Security/tree/master/arduino/libraries/USB_Host_Shield_20

#Dependecies

**1. -Server**:

-Use NGINX as a Web Server

-Configurate under RFIDSecurity/Server/nginx.conf

-Add the "server" folder to the Server's root 

**2. -Android**:

-Is necessary add the library JSON_SIMPLE(1.1.1)

-Import this library in Android Studio

-Build

**3. -Arduino**:

-Is necessary add the library USB_HOST_SHIELD to the folder "libraries" in your IDE

-Import the proyect RFID_READER

-Build


