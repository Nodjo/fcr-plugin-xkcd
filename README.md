fcr-plugin-xkcd
===============

This is the code of the Xkcd plugin for Fast Comic Reader. It can be found [here](https://play.google.com/store/apps/details?id=nodjo.plugin.xkcd). This show how to implement a non-daily webcomic.

What is Fast Comic Reader (FCR)
-------------------------------
It is an Android app which allows to view webcomics.

There's a main app which is generic and contains the engine.

And there are plugins for each comic (actually, one plugin can allow to view any number of comics).

The main app is available [here](https://play.google.com/store/apps/details?id=nodjo.fcr).

This repository is an example a non-daily Fast Comic Reader plugin, in this case one for Xkcd.


What is a non-daily comic
-------------------------
A comic which does NOT qualify as a daily comic, which means that there is NOT: one strip per day, from a certain date in the past, until now.

How do I code a plugin for FCR?
-------------------------------
You may pull this one and adapt it. It's pretty straightforward. Basically, there are two interfaces to implement:

###AndroidManifest.xml

Your manifest must include the following:
An Application which contains the following parameters:

    android:description=[author's name]  
    android:icon=[application icon]  
    android:label=[application name]  

This application must include any number of services, containing the following parameters:

    android:icon=[comic icon]  
    android:label=[comic name]
    
And these services must respond to the following intent filter

    <intent-filter>
       <action android:name="nodjo.fcr.plugin" />
     </intent-filter>

Et voilà! Such an app will be detected as a plugin by FCR, and will try to interact with it using these informations from the manifest, and through an AIDL interface

###IComic.aidl

You can find this one under src/main/java/nodjo/fcr/comics/IComic.aidl. It is self-explanatory.

Xkcd happens to provide a json stream which serves as an API. In order to parse the json stream, [Jsoup](https://github.com/jhy/jsoup) has been used. You may not need it and can removing when implementing a plugin for another comic.

###Last but not least

From the main app, the user can search for more plugins to install. This opens the [Play Store with the search string: "comic fcr"](https://play.google.com/store/search?q=comic%20fcr&c=apps). Make sure your plugin will be listed there!
