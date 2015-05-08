# Overview #
This app was developed as part of a small research project at the German University of Göttingen.
It measures the amount of CO2 emitted by a car in an accurate way.
This is achieved by using the On Board Diagnostics II - interface, which is made available to the Android phone by a suitable OBD-to-Bluetooth-adapter.
The app is supposed to run while driving and takes combustion-related data-"snapshots" combined with GPS coordinates and time stamps.
Tuples of raw data are uploaded to a server based on a local caching policy.
The server stores the raw data in a relational data base and provides a web interface for detailed analysis regarding when, where and how CO2 was emitted.


# Technical Infrastructure #
The Android app is targeted at version 2.3 due to some serious issues we encountered with the Bluez-Bluetooth stack of the previous version.
The server is composed of a web interface implemented in Java Server Faces (JSF) 1.2 and a REST-based interface implemented with plain-old Servlets; both running on Tomcat 6.
The data is stored in a MySQL-DB. Note that the server part is not open source but a deployed version can be accessed freely under http://134.76.21.30/CarbonTrackerWS.

# Basis #
Our work is based on:
  * this pretty cool (but undocumented) app http://code.google.com/p/android-obd-reader/
  * this conference paper http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=5718558

# Organization / Contribution #
The project is not maintained anymore, but the final version is regarded stable. Anyone interested is free to pick up the code base.

# Warning / Disclaimer #
Depending on your country, it might be illegal to interfere with the OBD interface while driving. Also, there is absolutely no warranty (see license) and we are not accountable
for any legal trouble or possibly damage to your car. USE AT OWN RISK.