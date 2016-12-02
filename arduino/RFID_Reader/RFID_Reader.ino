#include <adk.h>

#define GRANTED_LED 49
#define FORBIDDEN_LED 48
#define DENIED_LED 13

USB Usb;
// Manufacturer, Model, Description, Version, URL, Serial Number
ADK adk(&Usb, "FuriiDuino", "ArduinoADK", "Reading NFC Cards", "1.0", "http://rfid.furiios.es/RFID-Security.apk", "20161202");

uint32_t timer;
bool connected;

// Started USB connection with Usb.init and we defined the behave of these pines and set the data rate in bauds
void setup() {
  Usb.Init();
  Serial.begin(115200);  
  pinMode(GRANTED_LED, OUTPUT);
  pinMode(FORBIDDEN_LED, OUTPUT);
  pinMode(DENIED_LED, OUTPUT);
}

//We used Usb.Task for update status of devices connected by usb, adk.isReady() returns True when the device connected is ready to communicate
void loop() {
  Usb.Task();
  if (adk.isReady()) {
    if (!connected) {
      connected = true;
    }

//We define the variable of 1 byte and afther that we use his pointer to used in adk.RcvData()
    uint8_t msg[1];
    uint16_t len = sizeof(msg);
    uint8_t rcode = adk.RcvData(&len, msg);

//When we recieve a data we check if there are 1,2 or 3 and we determined which led should be turn on
    if (len > 0) {
      digitalWrite(GRANTED_LED, msg[0] == 1 ? HIGH : LOW);
      digitalWrite(FORBIDDEN_LED, msg[0] == 2 ? HIGH : LOW);
      digitalWrite(DENIED_LED, msg[0] == 3 ? HIGH : LOW);
    }
//This "else" is used to turn off the leds in the case of the conection go down
  } else {
    if (connected) {
      connected = false;
      digitalWrite(GRANTED_LED, LOW);
      digitalWrite(FORBIDDEN_LED, LOW);
      digitalWrite(DENIED_LED, LOW);
    }
  }
}
