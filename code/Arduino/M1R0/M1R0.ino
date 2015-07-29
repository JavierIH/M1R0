#include <Zowi.h>
#include <Servo.h>
#include <Oscillator.h>
#include "EEPROM.h"

int pinLinear1=2, pinLinear2=3, pinBlack=4, pinYellow=5, pinWhite=6, pinNeedle=7, pinLink1=8, pinLink2=9;
bool newInfoReceived=false;

Zowi zowi;
int pos[] = {0, 180, 0,0};




void setup(){
	Serial.begin(19200);
	Serial.flush();

	zowi.init(pinLink1, pinLink2, 10, 11, 0); //4 pines servos + bool 0 = no carga de eeprom
	zowi.setTrims(0,0,0,0); //ajuste punto 0
	zowi.moveServos(0, pos);
	pinMode(pinLinear1, OUTPUT);
	pinMode(pinLinear2, OUTPUT);
}




String readString(){
	String inString ="";
	char inChar;
	while(Serial.available()>0){
		inChar =(char) Serial.read();
		/*if (inChar=='='){
			inString="";
		}
		else if(inChar!='+'){*/
			inString+=inChar;
		/*}*/
		delay(1);
	}
	newInfoReceived=true;
	return inString;
}




void linearActuator(){
	int ms=20000;
	digitalWrite(pinLinear1, HIGH);
	digitalWrite(pinLinear2, LOW);
	setCoordinates(0,0,2000); //move to side
	delay(ms);
	for(int i=0; i<2; i++){
		int a1=11;
		int a2=40;
		setCoordinates(a1+10,a2+10,2000);//
		setCoordinates(a1+10,a2-10,2000);//
		setCoordinates(a1-10,a2+10,2000);//
		setCoordinates(a1-10,a2-10,2000);//
	}
	setCoordinates(0,0,2000); //move to side
	digitalWrite(pinLinear1, LOW);
	digitalWrite(pinLinear2, HIGH);
	delay(10000);
	setCoordinates(0,180,5000);
}




bool setCoordinates(int a1, int a2, int moveTime){
	pos[0]=a1;
	pos[1]=a2;
	zowi.moveServos(moveTime, pos); //2000
	return true;
}




void useTool(int tool, int state){
	Servo myServo;
	myServo.attach(tool);
	myServo.write(90); //needle down or press bottle
	if(tool!=4){
		delay(150);
		myServo.write(0); //release bottle
	}
	else {
		if (state==1) myServo.write(0); //needle up
	}
}




void think(String msg){
	if (msg=="linearAct")	{
		linearActuator();	
	}
	else if(msg=="up"){
		useTool(pinNeedle,1);
	}
	else if (msg!=""){
		int commaIndex = msg.indexOf(',');
		int commaIndex2 = msg.indexOf(',', commaIndex+1);
		int commaIndex3 = msg.indexOf(',', commaIndex2+1);

		int A1 = msg.substring(0, commaIndex).toInt();
		int A2 = msg.substring(commaIndex+1, commaIndex2).toInt();
		int moveTime = msg.substring(commaIndex2+1, commaIndex3).toInt();
		int scaraOption=msg.substring(commaIndex3).toInt();
		
		int finishedMoving=false;

		finishedMoving=setCoordinates(A1,A2,moveTime);
		if(A1==0 && A2==180) Serial.end(); //end comms with scara in starting position

		int pinTool;
		switch (scaraOption) {
			case 1:
				pinTool=pinBlack;
				break;
			case 2:
				pinTool=pinYellow;
				break;
			case 3:
				pinTool=pinWhite;
				break;
			case 4:
				pinTool=pinNeedle;
				break;
		}
		
		if(finishedMoving==true) useTool(pinTool,0);
	}
}




void loop(){
	String incoming;
	if(Serial.available()) incoming=readString();
	if (newInfoReceived==true){
		think(incoming);
		newInfoReceived=false;
	}
}