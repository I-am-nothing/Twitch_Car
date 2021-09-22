#include "Motor.h"
#include <Arduino.h>

Motor::Motor(){
  for(int i=0; i<4; i++){
    for(int j=0; j<3; j++){
      pinMode(motorPin[i][j], OUTPUT);
    }
    ledcSetup(i, 5000, 10);
    ledcAttachPin(motorPin[i][2], i);
  }
}

void Motor::setMotor(int motor[4][3]){
  motorA(motor[0]);
  motorB(motor[1]);
  motorC(motor[2]);
  motorD(motor[3]);
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

void Motor::motorC(int motor[3]){
  digitalWrite(motorPin[2][0], motor[0]);
  digitalWrite(motorPin[2][1], motor[1]);
  ledcWrite(2, motor[2]);
}

void Motor::motorD(int motor[3]){
  digitalWrite(motorPin[3][0], motor[0]);
  digitalWrite(motorPin[3][1], motor[1]);
  ledcWrite(3, motor[2]);
}
