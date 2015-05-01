    
#Cordova plugin for Capptain
----

Introduction
--
This plugin integrates the [Capptain SDK] into your Cordova/PhoneGap Application. It supports both *reporting* and *push* features. 

*Please refer to the Capptain SDK for more information about the various Capptain concepts*.

Supported Platforms
--
* iOS
* Android

Installation
--
To install the plugin, just add it to your Cordova project using your proper Capptain credentials through Cordova variables.
```sh
cd <your project>
cordova plugin add capptain-cordova --variable KEY=<value>
```
#### Generic Variables

- `$CAPPTAIN_ENABLE_LOG` : `true`|`false`, enable both plugin and capptain native logs
- `$CAPPTAIN_REDIRECT_URL` : the url schemes of your application when using redirect actions in your campaign. Must be the url prefix without :// (ex: `myapp` to handle urls such as `myapp://shop`)

#### iOS Variables
- `$CAPPTAIN_IOS_APP_ID` : the Capptain ID of your iOS application
- `$CAPPTAIN_IOS_SDK_KEY` : the Capptain sdk key 
- `$CAPPTAIN_IOS_REACH_ICON` : the icon used for reach notification : must be the name of the resource with its extension (ex: `icon.png`)


#### Android Variables
- `$CAPPTAIN_ANDROID_APP_ID` : the Capptain ID of your Android application
- `$CAPPTAIN_ANDROID_REACH_ICON` : the icon used for reach notification : must be the name of the resource without any extension, nor drawable prefix  (ex: `icon`)
- `$CAPPTAIN_GOOGLE_PROJECT_NUMBER` : the project number used as the GCM (Google Cloud Messaging) sender ID
 
To remove the plugin,
```sh
cordova plugin rm capptain-cordova
```

Methods
--
Once the `deviceready` event has been triggered by the Cordova framework, a `Capptain` object is available to interact with the native Capptain SDK.

* Capptain.startActivity
* Capptain.endActivity
* Capptain.sendAppInfo
* Capptain.startJob
* Capptain.endJob
* Capptain.sendEvent
* Capptain.onOpenURL
* Capptain.registerForRemoteNotification
* Capptain.getStatus

### Capptain.startActivity

Start a new activty  with the corresponding extra infos object.
```javascript
Capptain.startActivity(_activityName, _extraInfos,[ _success], [_failure]);
```
##### Params
* `_activityName`: the name of the activity
* `_extraInfos`: a json object containing the extra infos attached to this activity

### Capptain.endActivity
Ends the current Actvity. Would trigger a new session on the next startActivity
```javascript
Capptain.endActivity([ _success], [_failure]);
```
### Capptain.sendEvent
Send an event  with the corresponding extra infos object.
```javascript
sendEvent(_eventName, _extraInfos,[ _success], [_failure]);
```
##### Params
* `_eventName`: the name of the event
* `_extraInfos`: a json object containing the extra infos attached to this event

### Capptain.startJob
Start an new job  with the corresponding extra infos object.
```javascript
Capptain.startJob(_jobName, _extraInfos,[ _success], [_failure]);
```
##### Params
* `_jobName`: the name of the job
* `_extraInfos`: a json object containing the extra infos attached to this job

### Capptain.endJob
End a job previously created by startJob
```javascript
Capptain.endJob(_jobName,[ _success], [_failure]);
```
##### Params
* `_jobName`: the name of the job

### Capptain.sendAppInfo
Send App Infos atttached to the currente device.
```javascript
Capptain.sendAppInfo( _appInfos,[ _success], [_failure]);
```
##### Params
* `_appInfos`: the json object containing the app infos to be sent

### Capptain.onOpenURL
Set an event handler when an application specific URL is triggered (from a push campaign for example). The URL scheme must match the one defined in the `$CAPPTAIN_REDIRECT_URL` setting
```javascript
Capptain.onOpenURL( _urlHandler,[ _success], [_failure]);
```
#####Params
* `_urlHandler`:  the handler that is passed the url that has been triggerd

#####Example
```javascript
	Capptain.onOpenURL(function(_url) {
			console.log("user triggered url/action "+_url);
		});
```
### Capptain.registerForPushNotification
Register the application to receive push notifications on iOS (this function does nothing on the other platforms)
```javascript
Capptain.registerForPushNotification( [_success], [_failure]);
```
### Capptain.getStatus
Returns information about the Capptain library
```javascript
Capptain.getStatus( _statusCallback, [_failure]);
```
##### Params
* `_statusCallback`:  the handler that is passed a json object containing information about the Capptain library

##### Example
```javascript
	Capptain.getStatus(function(_info) {
		    console.log("Capptain SDK Version : "+_info.capptainVersion);
		      console.log("Capptain plugin Version : "+_info.pluginVersion);
		});
```


History
----

1.0.3
* Fixed typos in the readme.md
* Using capptain-cordova as the new plugin Id
 

1.0.2
* Replaced hooks with Cordova Variables
* Using cordova-plugin-ms-capptain as the new plugin Id

1.0.1
* Updated Capptain iOS SDK to 1.16.2
* Updated Capptain Android SDK to 2.4.1
* Using swizzling to remove ld warning on iOS
* Adding deferred Remote Notification

1.0.0
* Initial Release
    

[Capptain SDK]:HTTP://WWW.CAPPTAIN.COM