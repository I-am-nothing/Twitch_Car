#ifndef Motor_H
#define Motor_H

class Motor
{
  private:
    const int motorPin[4][3] = {
      {12, 13, 14},
      {15, 16, 17},
      {18, 19, 21},
      {22, 23, 24}
  };

  public:
    Motor();
    void SetMotorPin();
};

#endif
