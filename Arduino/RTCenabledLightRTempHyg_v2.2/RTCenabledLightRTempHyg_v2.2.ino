/* This is the backbone for the Arduino based remote thermometer and
    hygrometer, eventually supposed to be able to talk to RPi by radio
    interface. Rventually, RTC and gas analysers could be included.

    Copyright 2021, Erik Hedlund – LightR
*/

// Library includes:
#include <LiquidCrystal.h>
#include <DHT.h>

#include <ThreeWire.h>
#include <RtcDS1302.h>
//#include <RtcTemperature.h>
//#include <RtcUtility.h>
#include <RtcDateTime.h>

//#include <RH_ASK.h>
//#ifdef RH_HAVE_HARDWARE_SPI
//#include <SPI.h> // Not actually used but needed to compile
//#endif

// Define constants and initialise stuff
#define DHTPIN 7       // sensor is on pin 7 (proven to work)
#define DHTTYPE DHT11  // sensor type

#define countof(a) (sizeof(a) / sizeof(a[0]))

// These constants can be disposed of if more memory is needed
const int  dispBrp = 3; //rs = 9, en = 12, d4 = 2, d5 = 4, d6 = 5, d7 = 8, sclk = 11, io = 10, ce = 6, satInd = LED_BUILTIN,

// First we get the RTC to work
ThreeWire myWire(10, 11, 6); // myWire(io, sclk, ce); IO (DAT), SCLK (CLK), CE (RST)
RtcDS1302<ThreeWire> Rtc(myWire);

DHT dht(DHTPIN, DHTTYPE);
LiquidCrystal lcd(9, 12, 2, 4, 5, 8); // lcd(rs, en, d4, d5, d6, d7);

//RH_ASK RFdriver;

//int dispBrp  = 3;
int dispBr   = 255; // Presented brightness
int dayBr    = 127; // Set point for daytime brightness,
int eveBr    = 15;  // for evening (medium) brightness,
int nightBr  = 5;   // and for nighttime use.
int dayOnly  = 0;
int dispTime = 0;   // 0 == daytime, 1 == evening, 2 == night


//String clrStr = "                    ";  // 20 blank spaces for clearing a row of the display (unnecessary)
String tempStr = " T*: ";                  // Only for allocation purposes, will be set later.
String hygStr = "RH+ ";                    // These are as shown if sensor is saturated. 

// Read data stored here
float  hum;  // Relative humidity (%)
float  temp; // Temperature (°C)

// internal counters used to moderate serial and LCD output
int iter = 0;
//int refresher = 0;
int refreshRate = 30; // refresh whole screen once every minute at sampling once every 0.5s. Seconds update every second as expected.
//int timeIt = 0;
// How often do we want to publish data? (publish every sFrc loop)
int sFrc = 8;
// Delay between reads from sensor (ms)
//int senseDelay = 495;
unsigned long senseDelay = 500;

//int secFrc = 1000/senseDelay;

// Initiate full refresh of LCD
int writeFullDisp = 1;
// Display control: only rewrite full LCD when saturation condition changes
int firstChange = 0;
// Saturation condition
int sat = 0;
// Will only be '1' during startup
int firstRun = 1;

int locit = 0;

float cumhum  = 0;
float cumtemp = 0;

unsigned long start = 0;
unsigned long endt  = 0;
unsigned long delta = 0;

char noconf = ' ';

RtcDateTime now    = RtcDateTime("Feb  3 2002", "06:00:00");
//RtcDateTime oldnow = RtcDateTime("Jan  2 2001", "11:00:00");

void setup() {
  start = micros();
  //#ifdef RH_HAVE_SERIAL
  //  Serial.begin(9600);    // Debugging only
  //#endif
  //  if (!RFdriver.init()) {
  //    Serial.println("RF driver successfully initiated...");
  //  } else {
  //    Serial.println("RF driver failed to initiate!");
  //  }
  //#ifdef RH_HAVE_SERIAL
  //    Serial.println("init failed");
  //#else
  //    ;
  //#endif
  // initialize PWM brightness and contrast controls
  pinMode(dispBrp, OUTPUT);
  // pinMode(dispConp, OUTPUT);  // Not used right now, resistor used instead
  pinMode(LED_BUILTIN, OUTPUT);
  // Set up general communication over usb and initialise the sensor
  Serial.begin(9600);
  Serial.print("* Saturation indicator pin allocation: ");
  Serial.print(LED_BUILTIN);
  Serial.println(" *");


  dht.begin();
  // set up the LCD's number of columns and rows:
  lcd.begin(20, 2);
  delay(10);
  // turn the screen on and set contrast
  analogWrite(dispBrp, dispBr);
  // analogWrite(dispConp, dispCon);
  // Print a message to the LCD.
  lcd.print(" Welcome to LightR");
  lcd.setCursor(0, 1);
  lcd.print(" Temp and humidity");
  lcd.setCursor(0, 0);
  digitalWrite(LED_BUILTIN, HIGH);

  Rtc.Begin();

//  oldnow = RtcDateTime(__DATE__, __TIME__); // "19:00:00"
//  Serial.print("Compiled at: ");
//  printDateTime(oldnow);
//  Serial.println();

  delay(100);
  if (!Rtc.GetIsRunning()) {
    Serial.println("RTC was not actively running, starting now");
    Rtc.SetIsRunning(true);
  }
  Serial.print("Time from RTC 1: ");
  now = Rtc.GetDateTime();
  printDateTime(now);
  Serial.println();


  if (!Rtc.IsDateTimeValid() ) // || (now < oldnow)
  {
    Serial.println("RTC lost confidence in the DateTime!");
    lcd.setCursor(19, 0);
    noconf = '*';
    lcd.print(noconf);
    //    Rtc.SetIsWriteProtected(false);
    //    Rtc.SetDateTime(oldnow+10);
    //    Rtc.SetIsWriteProtected(true);
  } //else if (now < oldnow)  //
  //  {
  //    if (Rtc.GetIsWriteProtected())
  //    {
  //      Serial.println("RTC was write protected, enabling writing now");
  //      Rtc.SetIsWriteProtected(false);
  //      Serial.println("RTC is other than compile time!  (Updating DateTime)");
  //      Rtc.SetDateTime(oldnow+10);
  //      Rtc.SetIsWriteProtected(true);
  //      Serial.println("RTC protection, writing disabled");
  //    } else {
  //      Serial.println("RTC is other than compile time!  (Updating DateTime)");
  //      Rtc.SetDateTime(oldnow+10);
  //      Rtc.SetIsWriteProtected(true);
  //      Serial.println("RTC protection, writing disabled");
  //    }
  //  }

  endt  = micros();
  delta = endt - start;
  delay(5000 - delta / 1000);
  digitalWrite(LED_BUILTIN, LOW);

  Serial.print("Time from RTC 2: ");
  now = Rtc.GetDateTime();
  printDateTime(now);
  Serial.println();

  if (now.Hour() >= 9 && now.Hour() < 20) { //|| dayOnly != 0
    Serial.print("It is daytime... Setting brightness to ");
    dispBr = dayBr;
    dispTime = 0;
    Serial.println(dispBr);
  } else if (now.Hour() < 23) { // && dayOnly != 0
    Serial.print("Evening time... Setting brightness to ");
    dispBr = eveBr;
    dispTime = 1;
    Serial.println(dispBr);
  } else {  //if (now.Hour() > 22) {
    Serial.println("Nighttime... Setting brightness to ");
    dispBr = nightBr;
    dispTime = 2;
    Serial.println(dispBr);
  }
  analogWrite(dispBrp, dispBr);

  Serial.print("Read RTC data in setup: ");
  printRtcData();

  //// read data
  //    uint8_t buff[20];
  //    const uint8_t count = sizeof(buff);
  //    // get our data
  //    uint8_t gotten = Rtc.GetMemory(buff, count);
  //
  //    if (gotten != count)
  //    {
  //        Serial.print("something didn't match, count = ");
  //        Serial.print(count, DEC);
  //        Serial.print(", gotten = ");
  //        Serial.print(gotten, DEC);
  //        Serial.println();
  //    }
  //
  //    Serial.print("data read from RTC (");
  //    Serial.print(gotten);
  //    Serial.print(") = \"");
  //    // print the string, but terminate if we get a null
  //    for (uint8_t ch = 0; ch < gotten && buff[ch]; ch++)
  //    {
  //        Serial.print((char)buff[ch]);
  //    }
  //    Serial.println("\"");
}

void loop() {
  // Measure duration of the loop
  start = micros();

  //Read data and store it to variables hum and temp
  hum  = dht.readHumidity();
  temp = dht.readTemperature();

  if ((hum > 90 || hum < 20 || temp > 50 || temp < 0) ) { // && !firstChange
    if (hum > 90) {
      if (!sat) {
        firstChange = 1;
        sat = -1;
        digitalWrite(LED_BUILTIN, HIGH); // LED_BUILTIN/pin 13 used as saturation indicator
      }
      //Serial.println("* sat=-1, humidity saturated *");
    } else if (hum < 20) {
      if (!sat) {
        firstChange = 1;
        sat = 1;
        digitalWrite(LED_BUILTIN, HIGH);
      }
    } else { // final option - temperature out of bounds
      if (!sat) {
        firstChange = 1;
        sat = 2;
        digitalWrite(LED_BUILTIN, HIGH);
      }
    }
    //sat = 1;
    //Serial.println("* SATURATION DETECTED *");
    //Serial.print("sat variable: ");
    //Serial.println(sat);
    //if (!firstChange) {firstChange = 1;}
  } else if ((hum < 90 && hum > 20 && temp < 50 && temp > 0) ) { // && !firstChange
    if (sat != 0) {
      firstChange = 1;
      sat = 0;
      digitalWrite(LED_BUILTIN, LOW);
    }
    //Serial.print("sat variable: ");
    //Serial.println(sat);
    //if (!firstChange) {
    //  firstChange = 1;
    //writeFullDisp = 1;
    //  }
  }

  if (!isnan(temp) && !isnan(hum)) {
    cumhum  += hum;
    //    Serial.print("iter: ");
    //    Serial.print(iter);
    //    Serial.print(", refresher: ");
    //    Serial.println(refresher);
    //Serial.print(", timeIt: ");
    //Serial.print(timeIt);

    cumtemp += temp;
    //Serial.print(", cumtemp: ");
    //Serial.println(cumtemp);
  }

  // Print temp and humidity values to serial monitor as moderated
  // Sensor only updates once a second, so a higher sampling rate is
  // used but only once a second the value will be displayed

  if (iter % 2 ==  0) {
    //if (now.Second() != 0){
    //  dispDateTime(now,now,0,false);
    lcd.print(" ");
    //Serial.print("oldnow.Second()%15-2 : ");
    //Serial.println(oldnow.Second()%15-2);
    if (now.Second() % 15 - 2 == 0 ) {
//      oldnow = now;
      Serial.print("Refreshing from RTC: ");
      now = Rtc.GetDateTime();
      printDateTime(now);
      Serial.println();
    } else {
//      oldnow = now;
      now = now + 1;
    }
    dispDateTime(now, now-1, 0, false);


    //      if (oldnow.Second() == 59) {
    //        oldnow = now;
    //      } else {
    //        oldnow = now;
    //        now = now + 1;
    //      }


    //} else {
    //iter = 0;
    //writeFullDisp = 1;
    // refresher++;
  }

  if (iter == 0) {

    if (isnan(temp) || isnan(hum)) {
      locit = 0;
      lcd.clear();
      lcd.print(" Failed to read from DHT sensor!");
      lcd.setCursor(0, 1);
      //lcd.print("Trying ");
      lcd.print(" . . . Trying . . . Trying . . .");
      delay(1000);
      hum = dht.readHumidity();
      temp = dht.readTemperature();
      writeFullDisp = 1;
    }
    while (isnan(temp) || isnan(hum)) {
      //isnan = is NOT A NUMBER which return true when it is not a number
      Serial.println("* Sorry, Failed to Read Data From DHT Module *");
      //lcd.clear();
      lcd.scrollDisplayLeft();
      // lcd.print(" Failed to read from DHT sensor!  ");
      // lcd.setCursor(0, 1);
      // lcd.print("Trying . . . . . . . Trying . . . ");

      hum = dht.readHumidity();
      temp = dht.readTemperature();

      delay(250);

      locit++;
      locit = locit % 20;
      writeFullDisp = 1;
    } // else {

    if (!writeFullDisp) {
      hum  =  cumhum / sFrc;
      temp = cumtemp / sFrc;
    }

    if (sat == -1) {
      hygStr = "RH+ ";
      if (firstChange) {
        //firstChange = 1;
        writeFullDisp = 1;
        // Serial.print("* firstChange – saturated humidity *"); // Debug code, seems to work
      } else {
        writeFullDisp = 0;
      }
    } else if (sat == 1) {
      hygStr = "RH- ";
      if (firstChange) {
        //firstChange = 1;
        writeFullDisp = 1;
      } else {
        writeFullDisp = 0;
      }
    } else {
      hygStr = "RH: ";
      if (firstChange) {
        writeFullDisp = 1;
      }
    }
    if (sat == 2) {   // || ((temp < 0) || (temp > 50))
      tempStr = " T*: ";
      if (firstChange) {
        //firstChange = 1;
        writeFullDisp = 1;
      } else {
        writeFullDisp = 0;
      }
    } else {
      tempStr = "  T: ";
      if (firstChange) {
        //firstChange = 0;
        writeFullDisp = 1;
        // Serial.println("* firstChange in temp *"); //Seems to work, debug printout no longer needed
      } //else {
      //writeFullDisp = 0;
      //}
    }
    Serial.print("Humidity: ");
    Serial.print(hum);
    Serial.print(" %, Temp: ");
    Serial.print(temp);
    Serial.println(" Celsius");
    // Display stuff on the screen:
    //lcd.clear();

    // FULL REWRITE
    if (!isnan(temp) && !isnan(hum) && (writeFullDisp || firstRun)) { //  || oldnow.Second() == 0)
      lcd.clear();
      lcd.setCursor(0, 1);
      lcd.print(hygStr);
      lcd.setCursor(8, 1);
      lcd.print("%");
      lcd.print(tempStr);
      lcd.setCursor(18, 1);
      lcd.write(0xDF);
      lcd.print("C");

      //oldnow = now;
      now = Rtc.GetDateTime();
      dispDateTime(now, now-1, 0, true);
      lcd.setCursor(19, 0);
      lcd.print(noconf);
      Serial.println("* Full display refresh *");
      // refresher++;
      writeFullDisp = 0;
      if (firstChange) {
        firstChange = 0;
      }
      if (firstRun) {
        firstRun = 0;
      }

      Serial.print("Read RTC in refresher: ");
      printRtcData();
    }

    //lcd.print(tempStr);
    lcd.setCursor(4, 1);
    lcd.print(hum, 1);
    //lcd.write(0xDF);
    //lcd.print("C");
    lcd.setCursor(14, 1);
    //lcd.print(hygStr);
    lcd.print(temp, 1);
    lcd.setCursor(18, 1);
    lcd.write(0xDF);
    lcd.write("C");
    //lcd.print("%");
    //lcd.setCursor(0, 0);
    // }
    //    const char *msg = "* Testing RF messages *";
    //    RFdriver.send((uint8_t *)msg, strlen(msg));
    //    RFdriver.waitPacketSent();
    //    Serial.println(msg);
    cumhum  = 0;
    cumtemp = 0;
  }

  //    Serial.print("iter % 2 (for disp-case): ");
  //    Serial.println(iter % 2);
  //  if (iter % 2 ==  0) {
  //    dispDateTime(now,now,0,false);
  //    lcd.print(" ");
  //    oldnow = now;
  //    now = now + 1;
  //    // refresher++;
  //  }
  //  Serial.print("iter: ");
  //  Serial.print(iter);
  iter++;
  //refresher++;
  //Serial.print("timeIt (pre): ");
  //Serial.print(timeIt);
  //timeIt++;
  //Serial.print(", timeIt (post): ");
  //Serial.print(timeIt);
  iter = iter % sFrc;
  //refresher = refresher % refreshRate;
  //timeIt = timeIt % secFrc;
  //  delay(10);
  //  Serial.print(", timeIt (reset): ");
  //  Serial.println(timeIt);
  //  Serial.print("sFrc: ");
  //  Serial.print(sFrc);
  //  Serial.print(", iter: ");
  //  Serial.println(iter);

  //  Serial.print(", updated iter: ");
  //  Serial.println(iter);
  //  Serial.print(", \n");
  //  const char *msg = "* Testing RF messages *";
  //    RFdriver.send((uint8_t *)msg, strlen(msg));
  //    RFdriver.waitPacketSent();
  //    Serial.println(msg);

  // ** READING SERIAL DATA ** //
  if (Serial.available() > 0) {
    char cmd = Serial.read();
    delay(1);
    if ( cmd == 'a') { //Show the about text
      delay(1);
      Serial.println("* About *");
      Serial.println("Environment monitor - v 2.2");
      Serial.println("(c) 2021, Erik Hedlund - LightR");
      analogWrite(dispBrp, dayBr);
      //dispOn = true;
      lcd.setCursor(0, 0);
      lcd.print("Environment Monitor ");
      lcd.setCursor(0, 1);
      lcd.print("(c) 2021 LightR v2.2");
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          Serial.println("* about open command correctly terminated *");
        }
      }
      while (Serial.available() == 0) {
        delay(1);
      }
      cmd = Serial.read();
      delay(1);
      if ( cmd == 'b') {
        delay(1);
        writeFullDisp = 1;
        analogWrite(dispBrp, dispBr);
      }
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          Serial.println("* about close command correctly terminated *");
        }
      }
    } else if ( cmd == 'b') {
      delay(1);
      writeFullDisp = 1;
      analogWrite(dispBrp, dispBr);
      while (Serial.available() > 0) {
        char cmd = Serial.read();
        if ( cmd == '\n' ) {
          Serial.println("* unopened about close command correctly terminated *");
        }
      }
    } else if (cmd == 'd') {
      int dTogg = Serial.parseInt();
      delay(1);
      if ( dTogg == 1 ) { // Equivalent to dTogg != 0
        analogWrite(dispBrp, dispBr);
      } else if ( dTogg == 2 ) {
        //Toggle daytime brightness only mode.
        dayOnly = dispBr;
        analogWrite(dispBrp, dayBr);
      } else if ( dTogg == 3 ) {
        // Set to only use daytime (high intensity) display illumination
        //          if (dayOnly) {
        //Toggle usage of schedule
        if (dayOnly) {
          analogWrite(dispBrp, dayOnly);
          dayOnly = 0;
        }
        //          } else {
        //            //Toggle daytime brightness only mode.
        //            dayOnly = dispBr;
        //            analogWrite(dispBrp, dayBr);
        //          }
      } else { // Equivalent to dTogg == 0 => Turn light off
        analogWrite(dispBrp, 0);
      }
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          Serial.println("* Light toggle command correctly terminated *");
        }
      }

    } else if (cmd == 's') {
      unsigned long setEpoch = Serial.parseInt();
      //unsigned long expEpoch = 1618527188034;
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          Serial.println("* Time set command correctly terminated *");
          Serial.print("Received Epoch: ");
          Serial.println(setEpoch);
          //Serial.print("Expected Epoch: ");
          //Serial.println(expEpoch);
          //if (setEpoch == expEpoch) {
            //Serial.println("Received data matches expected value");
            //Update RTC
            RtcDateTime timeToSet;
            timeToSet.InitWithEpoch32Time(setEpoch);
            Rtc.SetIsWriteProtected(false);
            Rtc.SetDateTime(timeToSet);
            Rtc.SetIsWriteProtected(true);
          //}
        }       
      }
      //Serial.print("Received Epoch (no endline): ");
      //Serial.println(setEpoch);
    } else if (cmd == 'l') {
         nightBr = Serial.parseInt();
      delay(1);
      analogWrite(dispBrp,nightBr);
//      Serial.print("* Night brightness set to: ");
//      Serial.print(nightBr);
//      Serial.println(" *");
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          if (dispTime == 0) {
            dispBr = dayBr;            
          } else if (dispTime == 1) {
            dispBr = eveBr;
          } else if (dispTime == 2) {
            dispBr = nightBr;
          }
          analogWrite(dispBrp,dispBr);
          Serial.println("* Night brightness set command correctly terminated *");
        } else if (cmd == 'l') {
          nightBr = Serial.parseInt();
          delay(1);
          analogWrite(dispBrp,nightBr);
        }
      }
    } else if (cmd == 'm') {
      eveBr = Serial.parseInt();
      delay(1);
      analogWrite(dispBrp,eveBr);
//      Serial.print("* Evening brightness set to: ");
//      Serial.print(eveBr);
//      Serial.println(" *");
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          if (dispTime == 0) {
            dispBr = dayBr;            
          } else if (dispTime == 1) {
            dispBr = eveBr;
          } else if (dispTime == 2) {
            dispBr = nightBr;
          }
          analogWrite(dispBrp,dispBr);
          Serial.println("* Evening brightness set command correctly terminated *");
        } else if (cmd == 'm') {
          eveBr = Serial.parseInt();
          delay(1);
          analogWrite(dispBrp,eveBr);
        }
      }
    } else if (cmd == 'h') {
      dayBr = Serial.parseInt();
      delay(1);
      analogWrite(dispBrp,dayBr);
//      Serial.print("* Daytime brightness set to: ");
//      Serial.print(dayBr);
//      Serial.println(" *");
      while (Serial.available() > 0) {
        cmd = Serial.read();
        delay(1);
        if ( cmd == '\n' ) {
          if (dispTime == 0) {
            dispBr = dayBr;            
          } else if (dispTime == 1) {
            dispBr = eveBr;
          } else if (dispTime == 2) {
            dispBr = nightBr;
          }
          analogWrite(dispBrp,dispBr);
          Serial.println("* Day brightness set command correctly terminated *");
        } else if (cmd == 'h') {
          dayBr = Serial.parseInt();
          delay(1);
          analogWrite(dispBrp,dayBr);
        }
      }
    }
  }


  //Finalise the duration measurement
  endt = micros();
  delta = endt - start;
  //Serial.print("*** loop duration: ");
  //Serial.print(delta);
  //Serial.println(" us ***");
  //Serial.print("*** delay set: ");
  //Serial.print(senseDelay - delta/500);
  //Serial.println(" ms ***");
  if (delta < 50000) {
    delay(senseDelay - delta / 500); // Delay 0.50 s (or other selected time) between data points.
  } else {
    delay(senseDelay);
  }
}


void dispDateTime(const RtcDateTime& dt, const RtcDateTime& odt, int r, boolean wAll)
{
  char datestring[20];

  if ( dt.Month() < 10) {
    snprintf_P(datestring,
               countof(datestring),
               PSTR(" %2u/%u/%-5u%2u:%02u:%02u"),
               dt.Day(),
               dt.Month(),
               dt.Year(),
               dt.Hour(),
               dt.Minute(),
               dt.Second() );
  } else {
    snprintf_P(datestring,
               countof(datestring),
               PSTR("%2u/%u/%-4u%3u:%02u:%02u"),
               dt.Day(),
               dt.Month(),
               dt.Year(),
               dt.Hour(),
               dt.Minute(),
               dt.Second() );
  }

  //Serial.print(datestring);
  if ((dt.Day() != odt.Day()) || wAll) {
    Serial.print(" - Rewriting everything: ");
    // refresher = -1;
    iter = 0;
    writeFullDisp = 1;
    //printDateTime(dt);
    Serial.print('[');
    Serial.print(datestring);
    Serial.println(']');
    lcd.setCursor(0, r);
    lcd.print(datestring);
    lcd.print(noconf);
  } else if (dt.Hour() != odt.Hour()) {
    Serial.print(" - Hour changed: ");
    // refresher = -1;
    iter = 0;
    writeFullDisp = 1;
    printDateTime(dt);
    Serial.println();

    if (dt.Hour() >= 9 && dt.Hour() < 20) {
      Serial.print("Good Morning! Setting brightness to ");
      dispBr = dayBr;
      dispTime = 0;
      Serial.println(dispBr);
    } else if (dt.Hour() >= 20 && dt.Hour() < 23) {
      Serial.print("Evening time... Setting brightness to ");
      dispBr = eveBr;
      dispTime = 1;
      Serial.println(dispBr);
    } else if (dt.Hour() < 9 || dt.Hour() >= 23) { // Will make sense when numbers have been replaced by variables
      Serial.println("Evening time... Setting brightness to ");
      dispBr = nightBr;
      dispTime = 2;
      Serial.println(dispBr);
    }
    analogWrite(dispBrp, dispBr);

    if (dt.Hour() < 10) {
      lcd.setCursor(12, r);
      //lcd.print(' ');
      Serial.println(dt.Hour());
      lcd.print(dt.Hour());
    } else {
      lcd.setCursor(11, r);
      lcd.print(dt.Hour());
    }
    lcd.print(":0");
    lcd.print(dt.Minute());
    lcd.print(":0");
    lcd.print(dt.Second());
    lcd.print(noconf);
  } else if (dt.Minute() != odt.Minute()) {
    Serial.print(" - Minute changed: ");
    // refresher = -1;
    printDateTime(dt);
    Serial.println();
    lcd.setCursor(14, r);
    if (dt.Minute() < 10) {
      lcd.print(0);
      lcd.setCursor(15, r);
      lcd.print(dt.Minute());
      lcd.print(":0");
      lcd.print(dt.Second());
      lcd.print(noconf);
    } else if (dt.Minute() % 10 == 0) {
      lcd.print(dt.Minute());
      lcd.print(":0");
      lcd.print(dt.Second());
      lcd.print(noconf);
      Serial.print(" - Minute changed, rewriting: ");
      printDateTime(dt);
      Serial.println();
      iter = 0;
      writeFullDisp = 1;
    } else {
      lcd.print(dt.Minute());
      lcd.print(":0");
      lcd.print(dt.Second());
      lcd.print(noconf);
    }
  } else {

    //char sStr = datestring[2,3];  //.substring(17,18);
    Serial.print('[');
    Serial.print(datestring);
    Serial.println(']');

    if (dt.Second() < 10) {
      lcd.print(0);
      lcd.setCursor(18, r);
      lcd.print(dt.Second());
      lcd.print(noconf);
    } else {
      lcd.setCursor(17, r);
      lcd.print(dt.Second());
      lcd.print(noconf);
    }
  }
}

void printDateTime(const RtcDateTime& dt)
{
  char datestring[20];

  snprintf_P(datestring,
             countof(datestring),
             PSTR("%02u/%02u/%04u %02u:%02u:%02u"),
             dt.Day(),
             dt.Month(),
             dt.Year(),
             dt.Hour(),
             dt.Minute(),
             dt.Second() );
  Serial.print(datestring);
}

void printRtcData() {
  uint8_t buff[20];
  const uint8_t count = sizeof(buff);
  // get our data
  uint8_t gotten = Rtc.GetMemory(buff, count);

  if (gotten != count)
  {
    Serial.print("something didn't match, count = ");
    Serial.print(count, DEC);
    Serial.print(", gotten = ");
    Serial.print(gotten, DEC);
    Serial.println();
  }

  Serial.print("data read (");
  Serial.print(gotten);
  Serial.print(") = \"");
  // print the string, but terminate if we get a null
  for (uint8_t ch = 0; ch < gotten && buff[ch]; ch++)
  {
    Serial.print((char)buff[ch]);
  }
  Serial.println("\"");
}
