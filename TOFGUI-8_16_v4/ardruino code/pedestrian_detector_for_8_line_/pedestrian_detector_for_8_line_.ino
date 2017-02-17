// 8 line sensor version
// for Arduino Pro Micro (Leonardo)
// 
// Pedestrian Detector 1.0-a1 (2016-10-25)
#include <EEPROM.h>
#define SEND_RECEIVE_PIN 8
#define OUTPUT_PIN 15

int _min[8];
int _max[8];
int _distance[16];
boolean _detected = false;
boolean _valid = false;

byte device = 4;
byte majorversion = 1;
byte minorversion = 0;


  
void setup() 
{  
  pinMode(SEND_RECEIVE_PIN, OUTPUT);
  
  pinMode(OUTPUT_PIN, OUTPUT);
  digitalWrite(OUTPUT_PIN, LOW);
  
  Serial1.begin(115200);  // UART (ToF Sensor)   
  Serial.begin(115200);   // USB
  for(int i = 0; i < 8; i++){
    _min[i] = (int)((EEPROM.read(2*i) << 8 ) & 0xff) + (int)(EEPROM.read(2*i + 1) & 0xff);
    _max[i] = (int)((EEPROM.read(2*i + 16) <<8 ) & 0xff) + (int)(EEPROM.read(2*i + 17) & 0xff);
  }
  
}

int send_receive = HIGH;
byte buf[80];

bool tick_tock = false;

void loop() 
{
  // ----------- Sensor Interface (Serial1) -------------
  
  // Send
  digitalWrite(SEND_RECEIVE_PIN, HIGH); // Send    
  {    
    buf[0] = 1;  // device id
    buf[1] = 4;  // function code
    buf[2] = 0;  // data start [HI]
    buf[3] = 16; // data start [LO]
    buf[4] = 0;  // data length [HI]
    buf[5] = 16; // data length [LO]
    int c = _crc16(buf, 0, 6);
    buf[6] = (byte)((c >> 8) & 0xFF);   // CRC16 [HI]
    buf[7] = (byte)(c & 0xFF);          // CRC16 [LO]
    Serial1.write(buf, 8);
  }

  delay(1);

  // receive
  digitalWrite(SEND_RECEIVE_PIN, LOW); // Receive
  //delay(1);
  
  int length = 0;
  for (int i=0; i<80; i++)
  {
    if (Serial1.available() <= 0) break;
    int b = Serial1.read();
    if (b < 0) break;
      
    buf[length] = b;
    length++;
  }

  bool detected = false;
  bool valid = false;
  
  if (length >= 37)
  {
    if (buf[0] == 1 && buf[1] == 4) // device id & function code
    {                            
      if (_crc16(buf, 0, 37) == 0) // check CRC16
      {
          valid = true;

          for (int i=0; i<8; i++)
          {
            _distance[i] = ((int)(buf[3+(i*2)] & 0xFF) << 8) + (int)(buf[4+(i*2)] & 0xFF);
            
            if (_distance[i] >= _min[i] && _distance[i] <= _max[i])
            {
               detected = true;
            }
          }
      }
    }
  }

  _valid = valid;
  _detected = detected;

  // output
  if (_detected) 
  {
    digitalWrite(OUTPUT_PIN, HIGH);
  }
  else
  {
    digitalWrite(OUTPUT_PIN, LOW);
  }


  // ------ User Interface (Serial) ---------

    //Received
    int length_serial = 0;
    for (int i=0; i<sizeof(buf); i++)
    {
      if (Serial.available() <= 0) break;
      int b = Serial.read();
      if (b < 0) break;
      buf[length_serial] = b;
      length_serial++;
      delay(1);
    }

    //command id = 1  send data
    if (length_serial >= 36) // write EEPROM
    {
      if (buf[0] == (byte)'{'  && buf[1] == 1 && buf[35] == (byte)'}') 
      {                            
            if (_crc8(buf, 0, 35) == 0) // check CRC8
            {
                //write EEPROM
                for(int i=0; i<8; i++)
                {

                  EEPROM.write(2*i, buf[2*i + 2]); // min[hi]
                  EEPROM.write(2*i + 1, buf[2*i + 3]); // min[lo]
                  EEPROM.write(16 + 2*i, buf[2*i + 18]); // max[hi]
                  EEPROM.write(16 + 2*i + 1, buf[2*i + 19]); // max[lo]

                  _min[i] = (int)((buf[2*i + 2] << 8) & 0xff) + (int)(buf[2*i + 3] & 0xff);
                  _max[i] = (int)((buf[2*i + 18] << 8) & 0xff) + (int)(buf[2*i + 19] & 0xff);
                }
              
                // Send    
                  buf[0] = (byte)'{';  // device id
                  buf[1] = 1;  // function code
                  buf[2] = 0;  // data start [HI]
                  int c = _crc8(buf, 0, 3);
                  buf[3]= (byte)c;
                  buf[4]= (byte)'}';
                  Serial.write(buf, 5);
            }else{}
      }else{}
    }

    
    else if (length_serial >= 4 && length_serial<37)
    {
        //0 version
       if (buf[0] == (byte)'{' && buf[1] == 0 && buf[3] == (byte)'}')
        {                                      
              if (_crc8(buf, 0, 3) == 0) // check CRC8
              {
                   // Send    
                    byte b[7];   
                    b[0] = (byte)'{';  // device id
                    b[1] = 0;  // function code
                    b[2] = device;  // data start [HI]
                    b[3] = majorversion;
                    b[4] = minorversion;
                    b[5]=  _crc8(b, 0, 5);
                    b[6]= (byte)'}';
                    Serial.write(b, 7);     
              }else  {}
        }
        else if (buf[0] == (byte)'{'  && buf[1] == 2 && buf[3] == (byte)'}') //command id = 2 read EEPROM 
        {
          if (_crc8(buf, 0, 3) == 0) // check CRC8
            {
                  // Send    
                   byte b[36];
                    
                    b[0] = (byte)'{';     
                    b[1] = 2; //command id 
                    
                    for(int i=0; i<8; i++)
                    {
                        b[2*i + 2] = EEPROM.read(2*i); // min[hi]
                        b[2*i + 3] = EEPROM.read(2*i + 1); //min[lo]

                        b[2*i + 18] = EEPROM.read(2*i + 16);  // max[hi]
                        b[2*i + 19] = EEPROM.read(2*i + 17);  // max[lo]
                    }
                    
                    int crc8 = _crc8(b, 0, 34);
                    b[34] = (byte)(crc8); 
                    
                    b[35] = (byte)'}';
                  Serial.write(b, 36);
                 // digitalWrite(LED_PIN, HIGH);  
            }else {} 
        }
        else if (buf[0] == (byte)'{'  && buf[1] == 3 && buf[3] == (byte)'}') //command id = 3 sendDistance
        {
          if (_crc8(buf, 0, 3) == 0) // check CRC8
          {
                    // Send    
                     byte b[22];
                      
                      b[0] = (byte)'{';     
                      b[1] = 3; //command id 
                      
                      for(int i=0; i<8; i++)
                      {
                           int c = _distance[i];
                           b[i+(i+2)] = (byte)((c >> 8) & 0xFF);   // CRC16 [HI]
                           b[i+(i+3)] = (byte)(c & 0xFF);          // CRC16 [LO]
                      }
                      b[18] = _valid;
                      b[19] = _detected ;
                      b[20] = _crc8(b, 0, 20);
                      b[21] = (byte)'}';
                    Serial.write(b, 22);
                    //digitalWrite(LED_PIN, HIGH);  
          }else {}  
        } else {} 
    }else{}








  delay(200);  
}

void _printHex(byte b)
{
  if (b < 0x10) { Serial.print("0"); } 
  Serial.print(b, HEX);
}


// --------- CRC8 -----------

byte _crc8(byte data[], int offset, int length)
{
    int crc = 0xFF;
    for (int i = 0; i < length; i++)
    {
         crc ^= data[i+offset];
         for (int b = 0; b < 8; b++)
         {
             if ((crc & 0x80) > 0) crc = (crc << 1) ^ 0x31;
             else crc = (crc << 1);
         }
    }
    return (byte)crc;
}


// ---------- CRC16 -----------

byte _crc16_table[] = 
{
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  1,192,128,65,0,193,129,64,0,193,129,64,1,192,128,65,
  0,193,129,64,1,192,128,65,1,192,128,65,0,193,129,64,
  0,192,193,1,195,3,2,194,198,6,7,199,5,197,196,4,
  204,12,13,205,15,207,206,14,10,202,203,11,201,9,8,200,
  216,24,25,217,27,219,218,26,30,222,223,31,221,29,28,220,
  20,212,213,21,215,23,22,214,210,18,19,211,17,209,208,16,
  240,48,49,241,51,243,242,50,54,246,247,55,245,53,52,244,
  60,252,253,61,255,63,62,254,250,58,59,251,57,249,248,56,
  40,232,233,41,235,43,42,234,238,46,47,239,45,237,236,44,
  228,36,37,229,39,231,230,38,34,226,227,35,225,33,32,224,
  160,96,97,161,99,163,162,98,102,166,167,103,165,101,100,164,
  108,172,173,109,175,111,110,174,170,106,107,171,105,169,168,104,
  120,184,185,121,187,123,122,186,190,126,127,191,125,189,188,124,
  180,116,117,181,119,183,182,118,114,178,179,115,177,113,112,176,
  80,144,145,81,147,83,82,146,150,86,87,151,85,149,148,84,
  156,92,93,157,95,159,158,94,90,154,155,91,153,89,88,152,
  136,72,73,137,75,139,138,74,78,142,143,79,141,77,76,140,
  68,132,133,69,135,71,70,134,130,66,67,131,65,129,128,64
};

int _crc16(byte data[], int offset, int length)
{
    int index;
    byte crc_Low = 255;
    byte crc_High = 255;

    for (int i = 0; i < length; i++)
    {
      index = crc_High ^ data[i+offset];
      crc_High = (byte)(crc_Low ^ _crc16_table[index]);
      crc_Low = _crc16_table[index + 256];
    }
    return (int)((crc_High << 8) + crc_Low);
}

/*
 * Change Log
 * 
 * Version 1.0-a1 (2016-10-25)
 * - add internal watchdog
 * 
 * Version 1.0-a2 (2016-11-08)
 * - User Interface (Serial)
 */

