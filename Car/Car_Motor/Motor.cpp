#include "Motor.h"
#include <Arduino.h>

Motor::Motor(){
  for(int i=0; i<2; i++){
    for(int j=0; j<3; j++){
      pinMode(motorPin[i][j], OUTPUT);
    }
    ledcSetup(i, 5000, 10);
    ledcAttachPin(motorPin[i][2], i);
  }
}

void Motor::setMotor(int motor[2][3]){
  motorA(motor[0]);
  motorB(motor[1]);
}

void Motor::motorA(int motor[3]){
  digitalWrite(motorPin[0][0], motor[0]);
  digitalWrite(motorPin[0][1], motor[1]);
  ledcWrite(0, motor[2]);
}

void Motor::motorB(int motor[3]){
  digitalWrite(motorPin[1][0], motor[0]);
  digitalWrite(motorPin[1][1], motor[1]);
  ledcWrite(1, motor[2]);
}
