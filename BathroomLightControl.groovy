/**
 *  Bathroom Light Control
 *
 *  Controls bathroom lights:
 *  -- Turn on light when motion is detected.
 *  -- Turn off bathroom light after x minutes of no motion detection
 *  -- Extend off timer to x minutes if humidity sensor is above the average for the room and trending upward (user in the shower?)
 *
 *  Developed / Tested with Aeon Multisensor which by default sends humidty every 8 minutes.
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Rayzurbock.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 *  The latest version of this file can be found on GitHub at:
 *  http://github.com/rayzurbock/SmartThings-BathroomLightControl
 * 
 *  Version 1.0.1 (2014-11-26)
 
 */definition(
    name: "Bathroom Light Control",
    namespace: "rayzurbock",
    author: "brian@rayzurbock.com",
    description: "Control lights in the bathroom based on motion, extend when humidty rises (Shower in use)",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Configure") {
		def inputoffaftermotion = [
			name:	"motionOff",
			type:	"number",
			title:  "Turn off after motion is inactive (minutes)",
			defaultValue:	5,
            required:	true
		]
		def inputoffafterhumidity = [
			name:	"humidityOff",
			type:	"number",
			title:  "Turn off after motion is inactive and humidity is higher than average (minutes)",
			defaultValue:	10,
            required: true
		]
        def inputpollsforhumidity = [
			name:	"humiditypollsforaverage",
			type:	"number",
			title:  "How many humidity sensor readings should determine the average/normal humidity for the room?",
			defaultValue:	4,
            required:	true
		]
		input "lightswitch", "capability.switch", title: "Light Switch", required: true
        input "motionSensor", "capability.motionSensor", title: "Motion Sensor(s)", required: true
        input "humiditySensor", "capability.relativeHumidityMeasurement", title: "Humidity Sensor(s)", required: true
        input inputoffaftermotion
        input inputoffafterhumidity
        input inputpollsforhumidity
		paragraph "Bathroom Light Control Version 1.0.1"
		paragraph "http://github.com/rayzurbock"
	}
}

def installed() {
	initialize()
    state.offtime = settings.motionOff * 60
    state.mode = "motion"
    state.trigger = "none"
    state.humiditytrend = "Unknown"
	state.loglevel = 2 //0 = off, 1 = on, 2 = debug
    if (state.loglevel > 0){TRACE("Installed with settings: ${settings}")}
}

def updated() {
	unsubscribe()
	initialize()
    if (state.lastHumidity == null) { state.lastHumidity = 0 }
    if (state.loglevel > 0){TRACE("Updated with settings: ${settings}")}
}

def initialize() {
	subscribe(lightswitch, "switch", SwitchEvent)
	subscribe(motionSensor, "motion", MotionEvent)
    subscribe(humiditySensor, "humidity", HumidityEvent)
    state.averagehumiditymaxpollcount = settings.humiditypollsforaverage //How many polls should we base our average off of?
    state.averagehumiditycurrentpollcount = 0
    state.averageHumidity = 0
    state.humiditytrend = "stay" //Trend = stay, down, or up; start app with stay
    if (state.loglevel == 2){DEBUG("Initialized")}
}

def SwitchEvent(evt) {
	state.lastSwitchStatus = evt.value
    if (evt.value == "on") {
        if (state.trigger == "none") { 
            state.trigger = "manual" 
        }
        if (state.loglevel == 2){DEBUG("SWITCH:${evt.value}, trigger:${state.trigger}, mode: ${state.mode}, lasthumidity:${state.lastHumidity}, avghumidity:${state.averageHumidity}, trend:${state.humiditytrend}")}
        if ((state.lastHumidity > state.averageHumidity && state.humiditytrend == "up") && state.mode == "motion" && state.trigger == "manual") {
            if (state.loglevel == 2){DEBUG("Using humidity off timer (1)")}
            unschedule()
            state.mode = "humidity"
            state.offtime = settings.humidityOff * 60
            runIn(state.offtime, "lightsOut")
        } else {
            if (((state.lastHumidity <= state.averageHumidity) || (state.humiditytrend == "down")) && (!(state.humiditytrend == "stay")) && (state.mode == "motion") && (state.trigger == "manual")) {
                //Light came on, but humidity is below the threshold, set off time to motion off timer.
                if (state.loglevel == 2){DEBUG("Using motion off timer (1)")}
                unschedule()
                state.mode = "motion"
                state.offtime = settings.motionOff * 60
                runIn(state.offtime, "lightsOut")
            }
            if (state.humiditytrend == "stay") {
                if (state.mode == "motion") {
                    //Light came on, but humidity is below the threshold, set off time to motion off timer.
                    if (state.loglevel == 2){DEBUG("Using motion off timer (2)")}
                    unschedule()
                    state.offtime = settings.motionOff * 60
                    runIn(state.offtime, "lightsOut")
                }
                if (state.mode == "humidity") {
                    if (state.loglevel == 2){DEBUG("Using humidity off timer (2)")}
                    unschedule()
                    state.offtime = settings.humidityOff * 60
                    runIn(state.offtime, "lightsOut")
                }
            }
        }
        if ((state.lastHumidity > state.averageHumidity && state.humiditytrend == "up") && (state.mode == "humidity")) {
           if (state.loglevel == 2){DEBUG("Using humidity off timer (3)")}
           unschedule()
           state.mode = "humidity"
           state.offtime = settings.humidityOff * 60
           runIn(state.offtime, "lightsOut")
        }
        if ((state.lastHumidity <= state.averageHumidity || state.humiditytrend == "down") && !(state.humiditytrend == "stay") && (state.mode == "humidity")) {
           if (state.loglevel == 2){DEBUG("Using motion off timer (3)")}
           unschedule()
           state.mode = "motion"
           state.offtime = settings.motionOff * 60
           runIn(state.offtime, "lightsOut")
        }
    }    
    if (evt.value == "off") {
        if (state.loglevel == 2){DEBUG("Light was turned off, unscheduling off timer events")}
        unschedule()
        state.trigger = "none"
    }
}

def MotionEvent(evt) {
	state.lastMotionStatus = evt.value
    if (state.loglevel == 2){DEBUG("MotionEvent():${evt.value}, Trigger:${state.trigger}")}
    if (evt.value == "inactive") {
      if (state.loglevel == 2){TRACE("Motion inactive triggered")}
      if (state.mode == "motion"){
      	unschedule()
        state.offtime = settings.motionOff * 60
        if (state.loglevel == 2){DEBUG("Motion Sensor | Scheduling off based on inactivity ${state.offtime}")}
        runIn(state.offtime, "lightsOut")
      }
      if (state.mode == "humidity") {
        unschedule()
        state.offtime = settings.humidityOff * 60
        if (state.loglevel == 2){DEBUG("Motion Sensor | Scheduling off based on inactivity + humidity ${state.offtime}")}
        runIn(state.offtime, "lightsOut")
      }
    } else {
        if (state.trigger == "none" || state.trigger == "manual") {
            state.trigger = "motion"
    	    unschedule()
            lightsOn()
        }
    }
}
def HumidityEvent(evt) {
	// def currentHumidity = Double.parseDouble(evt.value.replace("%", ""))
    state.humiditytrend = "stay"
    if ((currentHumidity > state.lastHumidity) && (currentHumidity - state.lastHumidity > 1)) { state.humiditytrend = "up" }
    if ((currentHumidity < state.lastHumidity) && (state.lastHumidity - currentHumidity > 1)) { state.humiditytrend = "down" }
    if (!(state.averageHumidity > 0)) { state.humiditytrend = "stay" } //AverageHumidity hasn't been established yet; stay.
	if (state.loglevel == 2){DEBUG("HumidityEvent():current(${currentHumidity}),last(${state.lastHumidity}),trend(${state.humiditytrend})")}
    state.lastHumidity = currentHumidity
    //Start Check Humidity Average
    if (state.averagehumiditycurrentpollcount == state.averagehumiditymaxpollcount) {
        //Average the data
        state.averageHumidity = (state.averageHumidityCalc / state.averagehumiditymaxpollcount)
        if (state.loglevel == 2){DEBUG("Average Humidity (${state.averagehumiditymaxpollcount} polls) = ${state.averageHumidity}, Trend: ${state.humiditytrend}")}
        state.averageHumidityCalc = 0
        state.averagehumiditycurrentpollcount = 0
    } else {
      //Collect more data
      if (state.averageHumidityCalc > 0) {
          state.averageHumidityCalc = (state.averageHumidityCalc + state.lastHumidity)
      } else {
          state.averageHumidityCalc = state.lastHumidity
          state.averageHumidity = state.lastHumidity
      }
      state.averagehumiditycurrentpollcount = state.averagehumiditycurrentpollcount + 1
      if (state.loglevel == 2){DEBUG("Collected Humidity on poll # ${state.averagehumiditycurrentpollcount} of ${state.averagehumiditymaxpollcount}")}
      if (state.loglevel == 2){DEBUG("Current Average Humidity is ${state.averageHumidityCalc / state.averagehumiditycurrentpollcount}, Trend: ${state.humiditytrend}")}
    }
    //End Check Humidity Average
    if (lightswitch.latestValue('switch') == "on") {
        if ((state.lastHumidity > state.averageHumidity && state.humiditytrend == "up") && state.mode == "motion") {
    	    if (state.loglevel > 0){TRACE("Humidity >= Threshold; adjusting light timeout")}
            state.mode = "humidity"
            state.offtime = settings.humidityOff * 60
            if (state.loglevel == 2){DEBUG("Humidity Sensor | Scheduling off based on inactivity + humidity ${state.offtime}")}
            unschedule()
            runIn(state.offtime, "lightsOut")
        }
        if ((state.lastHumidity <= state.averageHumidity || state.humiditytrend == "down") && state.mode == "humidity") {
       	    if (state.loglevel > 0){TRACE("Humidity < Threshold; adjusting light timeout")}
            state.mode = "motion"
            state.offtime = settings.motionOff * 60
            if (state.loglevel == 2){DEBUG("Humidity Sensor | Scheduling off based on inactivity ${state.offtime}")}
            unschedule()
            runIn(state.offtime, "lightsOut")
        }
    }
}

def lightsOn() {
	TRACE("lightsOn()");
	lightswitch.on()
}
def lightsOut() {
	TRACE("lightsOut()");
	lightswitch.off()
    state.trigger = "none"
}
def TRACE(message){
  log.trace("BathroomLightControl: ${message}")
}
def DEBUG(message){
  log.debug("BathroomLightControl: ${message}")
}
