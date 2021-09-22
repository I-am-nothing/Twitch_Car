#include "Motor.h"

Motor motor;

int motorNow[4][3] = {
  {0, 1, 1024},
  {0, 0, 1024},
  {0, 0, 1024},
  {0, 0, 1024}
};

void setup() {
  
}

void loop() {
  for(int i=-1024; i<1024; i+=5){
    motorNow[0][2] = abs(i);
    if(i == 1){
      motorNow[0][0] = !motorNow[0][0];
      motorNow[0][1] = !motorNow[0][1];
    }
    motor.setMotor(motorNow);
    delay(10);
  }
}
