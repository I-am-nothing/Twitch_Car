#ifndef Motor_H
#define Motor_H

class Motor
{
  private:
    const int motorPin[4][3] = {
      {16, 17, 18},
      {19, 21, 22},
      {23, 25, 26},
      {27, 32, 33}
    };


  public:
    Motor();
    void setMotor(int motor[4][3]);
    void motorA(int motor[3]);
    void motorB(int motor[3]);
    void motorC(int motor[3]);
    void motorD(int motor[3]);
};

#endif
