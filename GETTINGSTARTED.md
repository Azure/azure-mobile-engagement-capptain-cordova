## Getting Started with Cordova Plugin for Capptain
#### App Creation
Create a Sample Cordova App
``````sh
cordova create <myAppName> <myAppBundleId> <myAppShortName>
```
Add the platform you'd like to support (iOS in this case)
``````sh
cordova platform add ios
```
Retrieve your credentials for the Capptain Portal to configure the Capptain plugin
``````sh
cordova plugin add capptain-cordova 	--variable CAPPTAIN_IOS_APP_ID=$CAPPTAIN_IOS_APP_ID --variable CAPPTAIN_IOS_SDK_KEY=$CAPPTAIN_IOS_SDK_KEY --variable CAPPTAIN_IOS_REACH_ICON=$CAPPTAIN_IOS_REACH_ICON --variable CAPPTAIN_ANDROID_APP_ID=$CAPPTAIN_ANDROID_APP_ID --variable CAPPTAIN_ANDROID_GOOGLE_PROJECT_NUMBER=$CAPPTAIN_ANDROID_GOOGLE_PROJECT_NUMBER --variable CAPPTAIN_ANDROID_REACH_ICON=$CAPPTAIN_ANDROID_REACH_ICON --variable CAPPTAIN_REDIRECT_URL=myapp --variable CAPPTAIN_ENABLE_LOG=true
```
#### Send a screen to the Capptain portal
Edit `www/js/index.js`to add the call to Capptain to declare a new activity once the ``deviceReady``event is received.
``````js
 onDeviceReady: function() {
        app.receivedEvent('deviceready');
        Capptain.startActivity("myPage",{});
    },
```
Launch your application...
``````sh
cordova run ios
```
... a new session should appear in the Capptain portal and you should see the following log in your iOS application
 ``````log
[Capptain] Agent: Session started
[Capptain] Agent: Activity 'myPage' started
[Capptain] Connection: Established
[Capptain] Connection: Sent: appInfo
[Capptain] Connection: Sent: startSession
[Capptain] Connection: Sent: activity name='myPage'
```
#### Extend your app to add Reach Support
Edit `www/js/index.js`to add the call to Capptain to request push new notification, and declare an handler
``````js
 onDeviceReady: function() {
        app.receivedEvent('deviceready');
        Capptain.registerForPushNotification();
        Capptain.onOpenURL(function(_url) { alert(_url); });
        Capptain.startActivity("myPage",{});
    },
```
Launch your application...
``````sh
cordova run ios
```
... a popup should appear to the user to allow notifications

You can then create a Reach campaign with an Action URL ``myapp://test``

Activating the campaign shoud trigger the webview, and when the action button is being pressed, the alert box in the handler should be triggered 



