# RTCenabledLightRTempHyg

Initial commit of code for Arduino. Use your favourite Arduino interface to compile and push this to your Arduino. This code works on an Arduino Uno, the Italian one ;)

Current physical implementation requires the use of all 12 GPIO pins on the classic Arduino Uno board. Pins 0 and 1 are unused and the five analog inputs are free. +5 V is provided for sensor, RTC clock, and a two-row LCD screen. Please note that this implementation is for a 20 character wide screen. If you have a narrower screen, you may want to adjust strings and output accordingly.

## Schematic
A simple schematic for how to connect the separate devices will be supplied later. As this implementation was incrementally realised the pinout may not be the most efficient. Feel free to optimise. 