//#include "Motor.h"
#include <WiFi.h>
//Motor motor;
WiFiClient client;

const char* ssid = "小騷包魚有C. 菌";
const char* password =  "sakun921118";
 
const uint16_t port = 9700;
const char * host = "192.168.240.187";
 

int motorNow[4][3] = {
  {0, 1, 1024},
  {0, 0, 1024},
  {0, 0, 1024},
  {0, 0, 1024}
};

void socketConnect(){
  while (!client.connect(host, port)) {
    Serial.println("Connection to host failed");
    delay(1000);
  }
}

void setup() {

 Serial.begin(115200);
 
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
 
  Serial.print("/nWiFi connected with IP: ");
  Serial.println(WiFi.localIP()); 
  
  socketConnect();
}

void loop() {
  
  if(client.connected()){
    client.println("AAA BBB CCC");
    while (client.available()){
      String line = client.readStringUntil('\n'); 
      Serial.print("recieve data => "); 
      Serial.println(line); 
      client.write(line.c_str()); 
    }
  }
  else{
    socketConnect();
  }
  delay(1);
 
    //Serial.println("Disconnecting...");
    //client.stop();
 
    //delay(10000);

   


  
  /*for(int i=-1024; i<1024; i+=5){
    motorNow[0][2] = abs(i);
    if(i == 1){
      motorNow[0][0] = !motorNow[0][0];
      motorNow[0][1] = !motorNow[0][1];
    }
    motor.setMotor(motorNow);
    delay(10);
  }*/
}
