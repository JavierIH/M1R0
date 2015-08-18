#include <Zowi.h>
#include <Servo.h>
#include <Oscillator.h>
#include <EEPROM.h>

int pinLinear1=2, pinLinear2=3, pinBlack=4, pinYellow=5, pinWhite=6,  pinLink1=8, pinLink2=9, pinNeedle=10;
bool newInfoReceived=false;

int memBlack=50, memYellow=60, memWhite=70;

Zowi zowi;
int pos[] = {0, 180, 0,0};




void setup(){
	Serial.begin(19200);
	Serial.flush();

	zowi.init(pinLink1, pinLink2, 12, 11, 0); //4 servos + bool 0 = doesn't read from eeprom
	zowi.setTrims(0,0,0,0); //adjust center for servo
	zowi.moveServos(0, pos);

	pinMode(pinLinear1, OUTPUT);
	pinMode(pinLinear2, OUTPUT);

	//Start with needle up
	Servo myServo;
	myServo.attach(pinNeedle);
	myServo.write(90);
	delay(500);
	myServo.detach();

	}




String readString(){
	String inString ="";
	char inChar;
	while(Serial.available()>0){
		inChar =(char) Serial.read();
		inString+=inChar;
		delay(1);
	}
	newInfoReceived=true;
	return inString;
}


void recharge(int i){
	/*static Servo myServo;
	int pin[3]={pinBlack,pinYellow, pinWhite};
	int mem[3]={memBlack,memYellow, memWhite};
	int posInit=50;

	myServo.attach(pin[i]);
	myServo.write(posInit);
	EEPROM.write(mem[i], posInit);
	myServo.detach();

	Serial.println(i);*/


	//recargar las tres a la vez
	Servo myServo;
	int pin[3]={pinBlack,pinYellow, pinWhite};
	int mem[3]={memBlack,memYellow, memWhite};
	for (int i=0; i<3; i++){
		int posInit=50;
		myServo.attach(pin[i]);
		myServo.write(posInit);
		EEPROM.write(mem[i], posInit);
		delay(1000);
		myServo.detach();
	}

	if (i ==1) digitalWrite(13,HIGH);
	else digitalWrite(13,LOW);
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
		setCoordinates(a1+10,a2+10,2000);
		setCoordinates(a1+10,a2-10,2000);
		setCoordinates(a1-10,a2+10,2000);
		setCoordinates(a1-10,a2-10,2000);
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
	zowi.moveServos(moveTime, pos); 
	return true;
}




void useTool(int tool, int state){
	static Servo myServo;
	myServo.attach(tool);

	//static int posInit=50;
	int posFinal=140;
	int steps=5;	
	int ms=1000;
	
	//static int positions[3]={posInit,posInit,posInit};
	static int positions[3]={EEPROM.read(memBlack),EEPROM.read(memYellow),EEPROM.read(memWhite)};

	if(tool==pinBlack){
		if(positions[0]<posFinal) {
	 		positions[0]=positions[0]+steps; //increment position of syringe
	 		myServo.write(positions[0]);
	 		//Serial.println(positions[0]);
	 		EEPROM.write(memBlack, positions[0]);
	 		delay(ms);
	 		myServo.detach();
		 	}
	}
	else if (tool==pinYellow){
		if(positions[1]<posFinal) {
	 		positions[1]=positions[1]+steps; //increment position of syringe
	 		myServo.write(positions[1]); 
	 		EEPROM.write(memYellow, positions[1]);
	 		delay(ms);
	 		myServo.detach();			
		 	}
	}
	else if (tool==pinWhite){
		if(positions[2]<posFinal) {
	 		positions[2]=positions[2]+steps; //increment position of syringe
	 		myServo.write(positions[2]); 
	 		EEPROM.write(memWhite, positions[2]);
	 		delay(ms);
	 		myServo.detach();			
		 	}
	}
	else if (tool==pinNeedle){
		ms=500;
		if (state==0 ){
			myServo.write(5); //needle down
			delay(ms);
			myServo.detach();
		}
		else{
			myServo.write(90); //needle down
			delay(ms);
			myServo.detach();
		}
	}
	
}




void think(String msg){
	if (msg=="linearAct")	{
		linearActuator();	
	}
	else if(msg=="up"){
		useTool(pinNeedle,1);
	}
	else if(msg.substring(0,8)=="recharge"){
		recharge( msg.substring(8).toInt() );
	}
	else if (msg!=""){
		int commaIndex = msg.indexOf(',');
		int commaIndex2 = msg.indexOf(',', commaIndex+1);
		int commaIndex3 = msg.indexOf(',', commaIndex2+1);

		int A1 = msg.substring(0, commaIndex).toInt();
		int A2 = msg.substring(commaIndex+1, commaIndex2).toInt();
		int moveTime = msg.substring(commaIndex2+1, commaIndex3).toInt();
		int scaraOption=msg.substring(commaIndex3+1).toInt();
		
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