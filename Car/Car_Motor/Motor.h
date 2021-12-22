#ifndef Motor_H
#define Motor_H

class Motor
{
  private:
    const int motorPin[4][3] = {
      {17, 18, 16},
      {19, 21, 22},
    };  //設定兩個馬達的腳位


  public:
    Motor();
    void setMotor(int motor[4][3]);
    void motorA(int motor[3]);
    void motorB(int motor[3]);
};  //設定馬達轉速

#endif
