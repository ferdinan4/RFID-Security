# ﻿Main Idea
Implant a double authentication factor in system which implement RFID technology, using the geolocation of mobile devices associated and a NFC card wallet cipher in our mobile.

First Part of the Project
For the first part of the project, we will implement different elements developed in different technologies, for implement a double authentication factor in RFID.

**1. -Server**: This server contains our APIRest ..

*Resources:*

https://github.com/ferdinan4/RFID-Security/tree/master/server

**2. -Arduino_RFIDReader**: This Arduino code is used to communicate our App that reader  NFC cards with Arduino USB and turn on each leds depends of the 
Authentication is correct/incorrect or authentication is correct but the position of mobile device associate  is not in the allowed range
We add the library used to management the USB.

*Resources*: 

https://github.com/ferdinan4/RFIDSecurity/tree/master/arduino/RFID_Reader

https://github.com/ferdinan4/RFIDSecurity/tree/master/arduino/libraries/USB_Host_Shield_20

**3. -RFID Reader**: This app coding in android (java) allow us to read NFC cards and, using the    APIRest defined in our server, we can check if the “id” 
reader 
from the card, is in the DB of our company, and if is in our DB the second thing that check, is the last location associated to that “id”. So finally, the result of this checks this app are sending to Arduino.

*Resources:*
