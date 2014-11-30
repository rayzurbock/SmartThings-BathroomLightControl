SmartThings-BathroomLightControl
================================
#License
Copyright (c) 2014 Brian S. Lowrance (brian@rayzurbock.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

#Description
SmartThings app to control bathroom lights based on motion / humidity <br />
Version 1.0.2-Beta2

An app for SmartThings that will turn on a bathroom light based on motion, start a timer <br />
and turn the light off after motion has stopped for a period of time (5 minutes by default); however <br />
adjust the timer if it appears that humidity is rising in the room (ie: Shower in use). <br />

#Installation
1. Login at <a href=http://graph.api.smartthings.com>http://graph.api.smartthings.com</a>
2. Go to "My SmartApps" section and click on the "+ New SmartApp" button on the right.
3. On the "New SmartApp" page, fill out mandatory "Name" and "Description" fields (it does not matter what you put there).
4. Click the "Create" button at the bottom.
5. When a new app template opens in the IDE, replace the contents with that in the .groovy file here
6. Click the blue "Save" button above the editor window.
7. Click the "Publish" button next to it and select "For Me". You have now self-published your SmartApp.
8. Open SmartThings mobile app on iPhone or Android and go to the Dashboard.
9. Tap on the round "+" button and navigate to "My Apps" section by swiping the menu ribbon all the way to the left.
10. "Batroom Light Control" app should be available in the list of SmartApps that appears below the menu ribbon. Tap it and follow setup instructions (I recommend naming each app install with the name of the bathroom being controlled).

# Revision History
*  11/26/2014 - 1.0.1 - Initial Release
*  11/29/2014 - 1.0.2-Beta1 - BETA RELEASE (testing)

# Change Log
* 1.0.2-Beta1
  * Change log from last commit (version 1.0.1):
  * Modified pages to dynamic pages
  * Added default app launch into Status page showing humidity, light, and fan status. Status page contains Configure button.
  * Added auto-exhaust fan option (toggles on when humidity > avg. or off when humidity <= avg and humidity or light switch event occurs)
  * Removed debug code that prevented analyzing "currentHumidity" and averaging/trending as intended
  * Added in app notification event (not push notifications, but notices within the SmartThings mobile app) for testing/debugging purposes
  * Modified logged data to better detect where we are in code based on Trace/Debug log.
  * Added logging level option in code.  0 = off, 1 = on, 2 = debug.  Configured by changing variable in initialize()
  * General code cleanup (tabs/spacing, commenting, etc)
* 1.0.2-Beta2
  * Adjusted trending determination threshold from +/- 1% to +/- 0.5%
