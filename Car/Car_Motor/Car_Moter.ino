   
#include "Motor.h"
#include <WiFi.h>
Motor motor;
WiFiClient client;

const char* ssid = "小騷包魚有C. 菌";
const char* password =  "sakun921118";
 
const uint16_t port = 9700;
const char * host = "192.168.240.187";
 

int motorNow[2][3] = {
  {0, 1, 1024},
  {1, 0, 1024},
};

void socketConnect(){
  while (!client.connect(host, port)) {
    Serial.println("Connection to host failed");
    delay(1000);
  }
}

void setup() {

 Serial.begin(115200);
 
  /*WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
 
  Serial.print("/nWiFi connected with IP: ");
  Serial.println(WiFi.localIP()); 
  
  socketConnect();*/
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


    void runAsyncSocketClient(){
  if(aClient)//client already exists
    return;

  aClient = new AsyncClient();
  if(!aClient)//could not allocate client
    return;

  aClient->onError([](void * arg, AsyncClient * client, int error){
    Serial.println("Connect Error");
    aClient = NULL;
    delete client;
  }, NULL);

  aClient->onConnect([](void * arg, AsyncClient * client){
    Serial.println("Connected");
    aClient->onError(NULL, NULL);

    client->onDisconnect([](void * arg, AsyncClient * c){
      Serial.println("Disconnected");
      aClient = NULL;
      delete c;
    }, NULL);

    client->onData([](void * arg, AsyncClient * c, void * data, size_t len){
      //Serial.print("\r\nData: ");
      //Serial.println(len);
      //uint8_t * d = (uint8_t*)data;
      for(size_t i=0; i<len;i++){
       ((char*) data)[i];
      }
      //Serial.println((char*) data);
      //c->write((char*) data, len);
      //c->write("\n");
    }, NULL);

    //send the request
    //client->write("Hello\n");
  }, NULL);

  aClient->setAckTimeout(5000);

  if(!aClient->connect(WiFi.gatewayIP(), port)){
    Serial.println("Connect Failed");
    AsyncClient * client = aClient;
    aClient = NULL;
    delete client;
  }
}


   


  
  for(int i=-1024; i<1024; i+=5){
    motorNow[0][2] = abs(i);
    motorNow[1][2] = abs(i);
    if(i == 1){
      motorNow[0][0] = !motorNow[0][0];
      motorNow[0][1] = !motorNow[0][1];
      motorNow[1][0] = !motorNow[1][0];
      motorNow[1][1] = !motorNow[1][1];
    }
    motor.setMotor(motorNow); 
    delay(100);
  }
}
}   
