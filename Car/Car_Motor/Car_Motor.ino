#include "Motor.h"
#include <WiFi.h>
#include "AsyncTCP.h"


const char* ssid = "小騷包魚有C. 菌";
const char* password =  "sakun921118";
 
const uint16_t port = 9700; 

Motor motor;
AsyncClient* aClient = NULL;

void runAsyncSocketClient();
void hintLed(int, int);

void setup() {

  Serial.begin(115200);

  pinMode(2, OUTPUT);
  digitalWrite(2, HIGH);

  // Wifi initial
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    hintLed(2, 700);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("WiFi connected");
  Serial.println("IP: " + WiFi.localIP().toString());
  Serial.println("HOST: " + WiFi.gatewayIP().toString());
}

void loop() {
  
  runAsyncSocketClient();
  delay(500);
  
}

void hintLed(int times, int flashDelay){
  for(int i=0; i<times; i++){
      delay(100);
      digitalWrite(2, 0);
      delay(50);
      digitalWrite(2, 1);
    }
    delay(flashDelay);
}

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

      int motorNow[2][3] = {
            {0, 0, 0},
            {0, 0, 0},
      };
      motor.setMotor(motorNow);
      
      aClient = NULL;
      delete c;
    }, NULL);

    client->onData([](void * arg, AsyncClient * c, void * data, size_t len){
      char input[len];
      strncpy(input, (char*) data, len);
      Serial.println(len);
      Serial.println(input);

      int subStartPos = 0;
      int index$ = 0;

      if(String(input) == "WHO ?"){
        c->write("WHO$MOTOR$\n");
      }
      else{
        index$ = String(input).indexOf('$', subStartPos);
        
        if(String(input).substring(subStartPos, index$) == "MOTOR"){
          
          subStartPos = index$ + 1;
          index$ = String(input).indexOf('$', subStartPos);
          
          int r = String(input).substring(subStartPos, index$).toInt();
          
          subStartPos = index$ + 1;
          index$ = String(input).indexOf('$', subStartPos);

          int angle = String(input).substring(subStartPos, index$).toInt();
          Serial.println(String(r) + " " + String(angle));

          double theta = angle / 180.0 * double(PI);
          double cosTheta = cos(theta);
          double cos2Theta = cos(theta * 2);

          Serial.println(String(cosTheta) + " " + String(cos2Theta));

          int motorSpeed = r * cos2Theta;

          int motorNow[2][3] = {
                {0, 0, 0},
                {0, 0, 0},
          };
            
          if(r != 0){
            if(cosTheta < 0){
              if(angle > 180){
                if(motorSpeed < 0){
                  motorNow[1][0] = 1;
                }
                else{
                  motorNow[1][1] = 1;
                }
                motorNow[1][2] = abs(motorSpeed);
                motorNow[0][2] = r;
                motorNow[0][0] = 1;
              }
              else{
                if(motorSpeed < 0){
                  motorNow[0][1] = 1;
                }
                else{
                  motorNow[0][0] = 1;
                }
                motorNow[0][2] = abs(motorSpeed);
                motorNow[1][2] = r;
                motorNow[1][1] = 1;
              }
            }
            else{
              if(angle > 180){
                if(motorSpeed < 0){
                  motorNow[0][0] = 1;
                }
                else{
                  motorNow[0][1] = 1;
                }
                motorNow[0][2] = abs(motorSpeed);
                motorNow[1][2] = r;
                motorNow[1][0] = 1;
              }
              else{
                if(motorSpeed < 0){
                  motorNow[1][1] = 1;
                }
                else{
                  motorNow[1][0] = 1;
                }
                motorNow[1][2] = abs(motorSpeed);
                motorNow[0][2] = r;
                motorNow[0][1] = 1;
              }
            }
          }
          
          motor.setMotor(motorNow);
          c->write("MOTOR OK\n");
        }
      }
    }, NULL);
  }, NULL);

  aClient->setAckTimeout(5000);

  if(!aClient->connect(WiFi.gatewayIP(), port)){
    Serial.println("Connect Failed");
    hintLed(1, 850);
    AsyncClient * client = aClient;
    aClient = NULL;
    delete client;
  }
}
