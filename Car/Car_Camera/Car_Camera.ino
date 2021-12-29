#include "esp_camera.h"
#include "AsyncTCP.h"
#include <WiFi.h>

#define CAMERA_MODEL_AI_THINKER

#include "camera_pins.h"

const char* ssid = "小騷包魚有C. 菌";
const char* password =  "sakun921118";

const uint16_t port = 9700;

TaskHandle_t Task1;
AsyncClient* aClient = NULL;

bool flashStatus = false;
bool cameraStatus = false;

void setUpPin();
void sendStream();
void hintLed(int, int);
void runAsyncSocketClient();

void Task1_senddata(void * pvParameters ) {
  while(true) {
    if(flashStatus){
      digitalWrite(4, HIGH);
      delay(50);
      digitalWrite(4, LOW);
    }
    delay(50);
  }
}

void setup() {
  // Serial initial
  Serial.begin(115200);

  // Camera initial
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 19000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size = FRAMESIZE_UXGA;
  config.jpeg_quality = 20;
  config.fb_count = 1;

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }
  
  sensor_t* s = esp_camera_sensor_get();
  s -> set_framesize(s, FRAMESIZE_VGA);
  s -> set_hmirror(s, 1); 
  s -> set_vflip(s, 1); 

  // Pin initial
  pinMode(4, OUTPUT);
  pinMode(33, OUTPUT);

  digitalWrite(33, 1);

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

  xTaskCreatePinnedToCore(Task1_senddata, "FLASH", 1000, NULL, 1, &Task1, 1);   
}

void loop() {
  runAsyncSocketClient();
  sendStream();
}

void hintLed(int times, int flashDelay){
  for(int i=0; i<times; i++){
      delay(100);
      digitalWrite(33, 0);
      delay(50);
      digitalWrite(33, 1);
    }
    delay(flashDelay);
}

void sendStream(){
  if(!aClient || !cameraStatus){
    return;
  }

  if(!aClient->canSend()){
    Serial.print("WAIT ");
    Serial.println(aClient->space());
    return;
  }

  aClient->write("IMAGE DONE\n");

  camera_fb_t* fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("Frame buffer could not be acquired");
    return;
  }
  
  char imageCommand[16];
  sprintf(imageCommand, "IMAGE$%06d$", fb->len);
    
  aClient->write(imageCommand);
  aClient->write("\n");
  
  int fbIndex = 0;

  while(fbIndex < fb->len){
    if(aClient->canSend()){
      int len = min(aClient->space(), (fb->len - fbIndex)); 
      aClient->write(((char*)fb->buf) + fbIndex, len);
      fbIndex += len;
    }
  }
  
  //aClient->write((const char*) fb->buf, fb->len);
  
  esp_camera_fb_return(fb);
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
        c->write("WHO$CAMERA$\n");
      }
      else{
        index$ = String(input).indexOf('$', subStartPos);
        
        if(String(input).substring(subStartPos, index$) == "FLASHLIGHT"){
          Serial.println("input");
          subStartPos = index$ + 1;
          index$ = String(input).indexOf('$', subStartPos);

          if(String(input).substring(subStartPos, index$) == "START"){
            flashStatus = true;
          }
          else{
            flashStatus = false;
          }
        }
        else if(String(input).substring(subStartPos, index$) == "CAMERA"){
          Serial.println("input");
          subStartPos = index$ + 1;
          index$ = String(input).indexOf('$', subStartPos);

          if(String(input).substring(subStartPos, index$) == "START"){
            cameraStatus = true;
          }
          else{
            cameraStatus = false;
          }
        }
      }
    }, NULL);
  }, NULL);

  aClient->setAckTimeout(5000);

  if(!aClient->connect(WiFi.gatewayIP(), port)){
    Serial.println("Connect Failed");
    AsyncClient * client = aClient;
    aClient = NULL;
    delete client;
  }
}
