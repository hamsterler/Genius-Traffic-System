// Pedestrian Detector 2.2 (2017-02-08)
// LeddarVu (8 segment)
// Intel Arduino 101

#include <EEPROM.h>
#include <CurieTime.h>
#include "CurieTimerOne.h"

// version
#define MAJOR_VERSION 2
#define MINOR_VERSION 1

// pins
#define SEND_RECEIVE_PIN 2  // RS485
#define OUTPUT_PIN 3        // Open Collector
#define SENSOR_POWER_PIN 4  // sensor watchdog
#define CONNECTED_PIN 5     // LED : sensor connected
#define HEART_BEAT_PIN 13   // LED : heart beat

// watchdog state
#define WATCHDOG_STATE_RUNNING 0
#define WATCHDOG_STATE_RESET 1
#define WATCHDOG_STATE_HOLD 2

// constants
#define INTERVAL 100          // in millisecond
#define TIMEOUT 150           // timeout = (TIMEOUT x INTERVAL) ms
#define WATCHDOG_OFF 20       // off time = (WATCHDOG_OFF x INTERVAL) ms
#define WATCHDOG_HOLD 3000      // hold time = (WATCHDOG_OFF x INTERVAL) ms

int _min_distance[8];
int _max_distance[8];
int _amplitude[8];
int _distance[8];
bool _connected = false;
bool _pre_detected = false;
bool _detected = false;
int _timeout_count = 0;

// watchdog
int _watchdog_state = WATCHDOG_STATE_RESET;
int _watchdog_off_count = 0;
int _watchdog_hold_count = 0;
int _watchdog_reset_count = 0;

// sensitivity
int _in_sensitivity = 0;
int _out_sensitivity = 0;
int _in_count = 0;
int _out_count = 0;

// buffer
byte _buffer[150];
int _length = 0;

// heart beat
bool _heart_beat_toggle = 0;

// timer
byte _last_timer = 0;
byte _current_timer = 0;
unsigned long _up_time = 0;

void setup() 
{  
  // define pins
  pinMode(HEART_BEAT_PIN, OUTPUT);  
  digitalWrite(HEART_BEAT_PIN, HIGH); // initial
  
  pinMode(SENSOR_POWER_PIN, OUTPUT);
  digitalWrite(SENSOR_POWER_PIN, HIGH); // power OFF sensor
  
  pinMode(CONNECTED_PIN, OUTPUT);    
  digitalWrite(CONNECTED_PIN, LOW);  
  
  pinMode(SEND_RECEIVE_PIN, OUTPUT);  
  digitalWrite(SEND_RECEIVE_PIN, LOW);
  
  pinMode(OUTPUT_PIN, OUTPUT);  
  digitalWrite(OUTPUT_PIN, LOW);

  Serial1.begin(115200);  // UART (RS485)  
  Serial.begin(115200);   // USB

  // ---- read config from EEPROM ----
  // min/max distace (cm.)
  for(int i = 0; i < 8; i++)
  {
    int ii = i+i;
    _min_distance[i] = (int)((EEPROM.read(ii) & 0xff) << 8 ) + (int)(EEPROM.read(ii + 1) & 0xff);
    _max_distance[i] = (int)((EEPROM.read(ii + 16) & 0xff) << 8 ) + (int)(EEPROM.read(ii + 17) & 0xff);
    _min_distance[i] = 50;
    _max_distance[i] = 350;
  }
  
  // sensitivity (ms)
  _in_sensitivity = (int)((EEPROM.read(34) & 0xff) << 8) + (int)(EEPROM.read(33) & 0xff);
  _out_sensitivity = (int)((EEPROM.read(36) & 0xff) << 8) + (int)(EEPROM.read(35) & 0xff);  
  _in_sensitivity = 2000;
  _out_sensitivity = 5000;

  CurieTimerOne.start(1000*INTERVAL, &onTimer); // 100ms
}

void loop()
{  
  if (_last_timer != _current_timer)
  {
    _last_timer = _current_timer;

    onWatchdog();

    // --------- LED Status ----------    
    // connected
    if (_connected) digitalWrite(CONNECTED_PIN, HIGH);
    else digitalWrite(CONNECTED_PIN, LOW);

    // detected
    if (_detected) { digitalWrite(OUTPUT_PIN, HIGH); }
    else { digitalWrite(OUTPUT_PIN, LOW); }       
    
    // heart beat
    digitalWrite(HEART_BEAT_PIN, _heart_beat_toggle);
    _heart_beat_toggle = !_heart_beat_toggle;
  }

  // api
  onAPI(); 

  delay(1);
}

// timer callback function
void onTimer()
{
  _current_timer++;
}

// handle watchdog
void onWatchdog()
{
  switch(_watchdog_state)
  {
    case WATCHDOG_STATE_RUNNING: 
    {
      onSensor();
      
      if (!_connected) 
      {
        digitalWrite(SENSOR_POWER_PIN, HIGH);   // power OFF sensor
        _watchdog_state = WATCHDOG_STATE_RESET; // move to RESET state
        _watchdog_off_count = 0;
        _watchdog_reset_count++;
      }
    }
    break;
    case WATCHDOG_STATE_RESET:
    {
      _watchdog_off_count++;
      if (_watchdog_off_count > WATCHDOG_OFF) 
      {
        digitalWrite(SENSOR_POWER_PIN, LOW);   // power ON sensor
        _watchdog_state = WATCHDOG_STATE_HOLD; // move to HOLD state
        _watchdog_hold_count = 0;
      }
    }
    break;
    case WATCHDOG_STATE_HOLD:
    {
      onSensor();
      
      _watchdog_hold_count++;
      if (_watchdog_hold_count > WATCHDOG_HOLD)
      {
        _watchdog_state = WATCHDOG_STATE_RUNNING; // move to RUNNING state
      }
    }
    break;
  }
}

// request the data from LeddarVu via RS485
void onSensor()
{
  // send
  digitalWrite(SEND_RECEIVE_PIN, HIGH); // RS485 sending state
  {    
    _buffer[0] = 1;  // device id
    _buffer[1] = 0x41;  // function code
    int c = CRC16(_buffer, 0, 2);
    _buffer[2] = (byte)((c >> 8) & 0xFF);   // CRC16 [HI]
    _buffer[3] = (byte)(c & 0xFF);          // CRC16 [LO]
    Serial1.write(_buffer, 4);
  }

  delay(1); // wait for sending data

  // receive
  digitalWrite(SEND_RECEIVE_PIN, LOW); // RS485 receiving state
  delay(1); // avoid bounce
  
  _length = 0;
  for (int i=0; i<150; i++)
  {
    if (Serial1.available() <= 0) break;
    int b = Serial1.read();
    if (b < 0) break;
      
    _buffer[_length] = b;
    _length++;
  }
  
  // parse 
  bool valid = false;  
  bool detected = false;  
  if (_length >= 60)
  {  
    if (_buffer[0] == 1 && _buffer[1] == 0x41 && _buffer[2] == 8) // device id & function code
    {                 
      if (CRC16(_buffer, 0, 60) == 0) // check CRC16
      {               
        valid = true;
        int offset = 3;
        for (int i=0; i<8; i++)
        {
          _distance[i] = _buffer[offset] + (_buffer[offset+1] << 8); // distance[LO][HI]
          _amplitude[i] = _buffer[offset+2] + (_buffer[offset+3] << 8); // amplitude[LO][HI]
          offset += 6;
          if (_distance[i] >= _min_distance[i] && _distance[i] <= _max_distance[i]) { detected = true; }
        }
      }
    }
  }

  // set _connected
  if (valid) 
  { 
    _timeout_count = 0; 
    _connected = true; 
    _pre_detected = detected;
  }
  else 
  {
    // timeout
    if (_timeout_count > TIMEOUT) 
    { 
      _pre_detected = false; 
      _connected = false; 
    }
    else 
    { 
      _timeout_count++; // no changed 
    }
  }
    
  // set _detected
  if (_pre_detected) 
  { 
    if (_in_count < _in_sensitivity) _in_count += INTERVAL; 
    _out_count = 0; 
  }
  else 
  { 
    _in_count = 0; 
    if (_out_count < _out_sensitivity) _out_count += INTERVAL; 
  }
    
  if (_in_count >= _in_sensitivity) _detected = _pre_detected;
  else if(_out_count >= _out_sensitivity) _detected = _pre_detected;
}

// response for the request from USB port
void onAPI()
{
  _length = 0;
  for (int i=0; i<150; i++)
  {
    if (Serial.available() <= 0) break;
    int b = Serial.read();
    if (b < 0) break;
    _buffer[_length] = b;
    _length++;
  }

  if (_length > 2)
  {
    if (_buffer[0] == (byte)0xFF) // check header
    {
      byte command_id = _buffer[1];

      if (command_id == 0 && _length >= 3 && CRC8(_buffer, 0, 3) == 0) // 0. get version
      {
        _buffer[0] = 0xFF;
        _buffer[1] = command_id;
        _buffer[2] = MAJOR_VERSION;
        _buffer[3] = MINOR_VERSION;
        _buffer[4]=  CRC8(_buffer, 0, 4);
        Serial.write(_buffer, 5);
      }
      else if (command_id == 1 && _length >= 39 && CRC8(_buffer, 0, 39) == 0) // 1. set config
      {
        // write EEPROM
        for(int i=0; i<8; i++)
        {
          int ii = i+i;

          // min_distance[8]
          EEPROM.write(ii, _buffer[ii + 2]); // LO
          EEPROM.write(ii + 1, _buffer[ii + 3]); // HI
          _min_distance[i] = _buffer[ii + 2] + (_buffer[ii + 3] << 8); // [LO][HI]

          // max_distance[8]
          EEPROM.write(ii + 16, _buffer[ii + 18]); // LO
          EEPROM.write(ii + 17, _buffer[ii + 19]); // HI          
          _min_distance[i] = _buffer[ii + 18] + (_buffer[ii + 19] << 8); // [LO][HI]
        }

        // in_sensitivity
        EEPROM.write(33, _buffer[34]); // in[lo]
        EEPROM.write(34, _buffer[35]); // in[hi]
        _in_sensitivity = _buffer[34] + (_buffer[35] << 8); // [LO][HI]

        // out_sensitivity
        EEPROM.write(35, _buffer[36]); // out[lo]
        EEPROM.write(36, _buffer[37]); // out[hi]
        _out_sensitivity = _buffer[36] + (_buffer[37] << 8); // [LO][HI]

        // reply
        _buffer[0] = 0xFF;
        _buffer[1] = command_id;
        _buffer[2] = 0; // result
        _buffer[3]= CRC8(_buffer, 0, 3);
        Serial.write(_buffer, 4);
      }
      else if (command_id == 2 && _length >= 3 && CRC8(_buffer, 0, 3) == 0) // 2. get config
      {
        _buffer[0] = 0xFF;     
        _buffer[1] = command_id;
        for(int i=0; i<8; i++)
        {
          int ii = i+i;

          // min_distance[8]
          _buffer[ii+2] = EEPROM.read(ii);      // LO
          _buffer[ii+3] = EEPROM.read(ii+1);    // HI
          _min_distance[i] = _buffer[ii+2] + (_buffer[ii+3] << 8); // set to variable

          // max_distance[8]
          _buffer[ii+18] = EEPROM.read(ii+16);  // LO
          _buffer[ii+19] = EEPROM.read(ii+17);  // HI
          _max_distance[i] = _buffer[ii+18] + (_buffer[ii+19] << 8);
        }

        // in_sensitivity
        _buffer[34] = EEPROM.read(33); // LO
        _buffer[35] = EEPROM.read(34); // HI
        _in_sensitivity = _buffer[34] + (_buffer[35] << 8);

        // out_sensitivity
        _buffer[36] = EEPROM.read(35); // LO
        _buffer[37] = EEPROM.read(36); // HI
        _out_sensitivity = _buffer[36] + (_buffer[37] << 8);
        
        _buffer[38] = CRC8(_buffer, 0, 38);
        Serial.write(_buffer, 39);
      }
      else if (command_id == 3 && _length >= 3 && CRC8(_buffer, 0, 3) == 0) // 3. get detection
      {
        // Send    
        _buffer[0] = 0xFF;
        _buffer[1] = command_id;
        for(int i=0; i<8; i++)
        {
          int ii = i+i;

          // distance
          _buffer[ii+2] = (byte)(_distance[i] & 0xFF);          // LO
          _buffer[ii+3] = (byte)((_distance[i] >> 8) & 0xFF);   // HI

          // amplitude
          _buffer[ii + 18] = (byte)(_amplitude[i] & 0xFF);          // LO
          _buffer[ii + 19] = (byte)((_amplitude[i] >> 8) & 0xFF);   // HI
        }
        _buffer[34] = _connected;
        _buffer[35] = _detected;
        _buffer[36] = CRC8(_buffer, 0, 36);
        Serial.write(_buffer, 37);
      }
      else if (command_id == 100 && _length >= 3 && CRC8(_buffer, 0, 3) == 0) // 100. get system status
      {
        // Send    
        _buffer[0] = 0xFF;
        _buffer[1] = command_id;        

        // up time
        _up_time = now();
        _buffer[2] = (byte)(_up_time & 0xFF);
        _buffer[3] = (byte)((_up_time >> 8) & 0xFF);
        _buffer[4] = (byte)((_up_time >> 16) & 0xFF);
        _buffer[5] = (byte)((_up_time >> 24) & 0xFF);

        // watchdog
        _buffer[6] = (byte)(_watchdog_reset_count & 0xFF);
        _buffer[7] = (byte)((_watchdog_reset_count >> 8) & 0xFF);
        
        _buffer[8] = CRC8(_buffer, 0, 8);
        Serial.write(_buffer, 9);
      }
    }
  }
}

/*
void _printHex(byte b)
{
  if (b < 0x10) { Serial.print("0"); } 
  Serial.print(b, HEX);
}
*/

// CRC8
byte CRC8(byte data[], int offset, int length)
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


// CRC16
byte CRC16_table[] = 
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

int CRC16(byte data[], int offset, int length)
{
    int index;
    byte crc_Low = 255;
    byte crc_High = 255;
    for (int i = 0; i < length; i++)
    {
      index = crc_High ^ data[i+offset];
      crc_High = (byte)(crc_Low ^ CRC16_table[index]);
      crc_Low = CRC16_table[index + 256];
    }
    return (int)((crc_High << 8) + crc_Low);
}

/*
 * Change Log
 * 
 * Version 2.2 (2017-02-08)
 * - function 100 get system status
 * 
 * Version 2.1 (2017-01-31)
 * - using curie timer for controlling interval (100ms)
 * 
 * Version 2.0.1 (2017-01-31)
 * - new protocol
 * - amplitude data
 * - sensor watchdog
 * 
 */

