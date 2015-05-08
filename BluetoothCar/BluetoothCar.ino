#include <NewPing.h>
#include <TimedAction.h>
#include <SoftwareSerial.h>
#define TRIGGER_PIN  6  // Arduino pin tied to trigger pin on the ultrasonic sensor.
#define ECHO_PIN     7 // Arduino pin tied to echo pin on the ultrasonic sensor.
#define MAX_DISTANCE 400 // Maximum distance we want to ping for (in centimeters). Maximum sensor distance is rated at 400-500cm.
#define RX_PIN 0
#define TX_PIN 1

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // NewPing setup of pins and maximum distance.
TimedAction timedAction = TimedAction(250,blinked);

SoftwareSerial BT = SoftwareSerial(RX_PIN, TX_PIN);

const int ledPinLeft = 13;
const int ledPinRight = 2;
const int dataPin = 10;
const int latchPin = 11;
const int clockPin = 12;
const int PWMLeftWheel = 3;
const int PWMRightWheel = 5;
const int echoPin =7;
const int trigPin = 6;

boolean turning = false;
boolean LEDstateLeft = false;
boolean LEDstateRight = false;
boolean isLeftTurn = false;
boolean isRightTurn = false;

long elapsedTime = 0;
int enableControl = 0;
byte binaryNumber = 0;
float distance;
float duration;
float avgDistance;

void setup()
{
  Serial.begin(9600);
  
  pinMode(ledPinRight, OUTPUT);
  pinMode(ledPinLeft, OUTPUT);
  pinMode(dataPin, OUTPUT);
  pinMode(latchPin, OUTPUT);
  pinMode(clockPin, OUTPUT);
  pinMode(PWMRightWheel, OUTPUT);
  pinMode(PWMLeftWheel, OUTPUT);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);


  BT.begin(9600);

  digitalWrite(ledPinLeft, LEDstateLeft);
  digitalWrite(ledPinRight, LEDstateRight);
 
  

}

void loop()
{
  while(BT.available() >0)
  {
   BTControl();
  }
  while(BT.available()==0)
  {
    automatic();
  }
}

void halt()
{ 
    callShiftRegister(0);
     WheelSignal(0);  
}
void sensor()
{  
  duration = sonar.ping_median(5);
  avgDistance = sonar.convert_cm(duration);
  //Serial.println(avgDistance); 
}

/*
function to move forward normally
*/
void moveForward() 
{
    callShiftRegister(5);  
     WheelSignal(220); 
    LEDstateLeft = false;
    LEDstateRight = false;
}

void slowDown()
{
    callShiftRegister(85);
     WheelSignal(120);
   
    LEDstateLeft = false;
    LEDstateRight = false;
}

/*
  check distance of nearest object in front
*/
void checkDistance()  
{
  if (avgDistance >= 75)  {
    moveForward(); 
  }
  else if (avgDistance >= 50 && avgDistance < 75) {
    slowDown();
  }
  else if (avgDistance < 50)
  {
    halt();
    delay(100);
   (random(0,2)==1)? leftTurn():rightTurn(); 
  }
}

void leftTurn()
{
    turning = true;
    isLeftTurn = true;
    elapsedTime = millis(); 
   callShiftRegister(6);
    WheelSignal(140);
}

void rightTurn()
{
    turning = true;
    isRightTurn = true;
    elapsedTime = millis(); 
    callShiftRegister(9);
     WheelSignal(140);
}
boolean isDoneTurning()
{
  long currentTime = millis();
  if (currentTime - elapsedTime > 1400)  {
    elapsedTime = 0;   
    LEDstateLeft = false;
    LEDstateRight = false;
    return true; 
  }
  return false;
}
void blinked()
{
  (LEDstateLeft==true) ? LEDstateLeft=false : LEDstateLeft=true;
  (LEDstateRight==true) ? LEDstateRight=false : LEDstateRight=true;
  if (isLeftTurn)
  {digitalWrite(ledPinLeft,LEDstateLeft);}
  else if (isRightTurn)
  {digitalWrite(ledPinRight,LEDstateRight);}
}

void turnOffEverything()
{
 isLeftTurn = false;
 isRightTurn = false;
 LEDstateLeft = false;
 LEDstateRight = false;
 turning = false;
 halt();
 
}
void moveBackward()
{
  callShiftRegister(10);
   WheelSignal(180);

  
}
void BTLeftTurn()
{
    LEDstateRight = false;
   // timedActionBT.check();
    callShiftRegister(6);
     WheelSignal(140);
   
}
void BTRightTurn()
{
  LEDstateLeft=false;
  //timedActionBT.check();
  callShiftRegister(9);
  WheelSignal(140);
  
}
void automatic()
{
     delay(10);
      if(enableControl)  {
       if (!turning)  {
         sensor(); 
         checkDistance(); 
         LEDstateLeft = false;
         LEDstateRight = false;
       }
       else if (turning)  {
         if (isDoneTurning())  {
            turnOffEverything();
            delay(100); 
       }
       else 
       {
         turning = true;
         timedAction.check();
       }
     } 
    }  
  else if(!enableControl)  {  
      halt();
      LEDstateLeft = false;
      LEDstateRight = false;
   }
}

void BTControl()
{
  
  LEDstateLeft = false;
  LEDstateRight = false;
  char letter = '1';

    Serial.println(letter);
  if(enableControl)
{
  automatic();
}  
    
  while (letter != '0')
  {
  LEDstateLeft = false;
  LEDstateRight = false;
    BT.listen();
    letter = BT.read();
    if (letter == '8')
      moveForward();
    else if(letter == '2')
      moveBackward();
    else if(letter == '4')
      BTLeftTurn();
    else if(letter == '6')
      BTRightTurn();
    else if (letter == 'C')
    {
       Serial.println("yes");
       enableControl = 1;
       automatic(); 
       //moveForward();
    }
    else if (letter == 'D')
    {
      enableControl = 0;
      halt();
    }
  } 
  
  if (letter == '0')
  {
   letter = '1'; 
  }
}
void callShiftRegister(byte inBin)
{
  binaryNumber = inBin;
  //ground latchPin and hold low for as long as you are transmitting
    digitalWrite(latchPin, LOW);
    shiftOut(dataPin, clockPin, MSBFIRST, binaryNumber);   
    //return the latch pin high to signal chip that it 
    //no longer needs to listen for information
    digitalWrite(latchPin, HIGH); 
    delay(10);
    digitalWrite(latchPin, LOW);
    delay(50);
 
}
void WheelSignal(int inPWM)
{
  analogWrite(PWMLeftWheel, inPWM);
  analogWrite(PWMRightWheel, inPWM); 
}
