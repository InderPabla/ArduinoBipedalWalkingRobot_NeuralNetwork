#include <Servo.h>

Servo servos[4];  
byte numberOfServos = 4;
float angles[4];
byte dataCount = 0;
float anglesFix[4] = {0,0,0,-3};
float maxRange = 45;
void setup() {

    for(byte counter = 0; counter < numberOfServos; counter++ ){
        servos[counter].attach(counter+2);  
        angles[counter] = 90+anglesFix[counter];
        servos[counter].write(angles[counter]);  
    }
    
    Serial.begin(9600);
    //Serial.print("dafdafdafaf\n");
    //Serial.write("dafdafdafaf\n");
}

void loop() {

    while (Serial.available()){
        char readChar = (char)Serial.read();
        float affect = 0;
        switch(readChar){
            case '1': affect=15;
            break;  

            case '2': affect=5;
            break; 

            case '3': affect=-15;
            break; 

            case '4': affect=-5;
            break; 

            case '5': affect=100;
            break; 

          
            default: break;
        } 

        if(affect == 100){
              angles[dataCount] = 90+anglesFix[dataCount];
        }
        else {
            if(angles[dataCount]+affect>90+maxRange){
                angles[dataCount] = 90+maxRange;
            }
            else if(angles[dataCount]+affect<90-maxRange){
                angles[dataCount] = 90-maxRange;   
            }  
            else{
                angles[dataCount] += affect;  
            }
        } 

        servos[dataCount].write(angles[dataCount]);
         
        dataCount = dataCount+1;
        
        if(dataCount == 4){
          dataCount = 0;

          if(affect >= 100 ){  
             
              delay(500);  
          }
          else{
              delay(15);
          }
          
          /*
          String s = String(angles[0],1);
          s+=" "+String(angles[1],1)+" "+String(angles[2],1)+" "+String(angles[3],1);
          //Serial.write(s);
          for (int i = 0; i < s.length(); i++)
          {
            Serial.write(s[i]);   // Push each char 1 by 1 on each loop pass
          }
          Serial.write("\n");
          */
          
          Serial.print("ok\n");
        }
    }
    
   
}
