# This repository is deprecated. Please see the refreshed [OpenRC-Turbo](https://github.com/OpenFTC/OpenRC-Turbo)

[![Discord](https://img.shields.io/discord/377144270034829324.svg?style=for-the-badge)](https://discord.gg/ameFTnC)

# OpenRC

OpenRC is a modified version of the official [FTC SDK](https://github.com/ftctechnh/ftc_app) (Software Development Kit)
that provides various enhancements to the FTC development experience.

Despite the name Open*RC*, this repository also contains a fully-functional reverse-engineered version of the FTC Driver Station app, of the same version as the main SDK in this repo (v3.7)

Check out our [documentation on the OpenFTC website](https://www.openftc.org/openrc/).

## Legality for competition use

 - The unofficial Driver Station app contained herein is *absolutely* ***not*** legal for competition, per \<RS06\> in the 2018-19 GM1.

 - OpenRC, in its default configuration, is also illegal for competition per \<RS09\> in the 2018-19 GM1. However, because the real value of OpenRC applies even if you
don't use it at a competition, we added a feature that allows you to deploy a [**competition-legal**](https://www.openftc.org/OpenRC/legality)
configuration of the app from within the OpenRC project (no copying code around). It builds using the untouched libraries that
ship with the official app.

  You can learn how to use this feature by reading [our build variants guide](https://www.openftc.org/openrc/buildvariants).
Always make sure to test this feature for yourself at least a week before competition. Don't ignore the part about doing
a Gradle sync.


---

[Sign up for our mailing list](http://eepurl.com/dgZbKj) for notifications about updates and upcoming new projects.

[Join us on the OpenFTC Discord](https://discord.gg/Q3CgrxU) for real-time support or to ask questions.

OpenRC is currently based on version 3.6 of the SDK. Do not attempt to manually update beyond that yourself. OpenRC will
be updated within a week of new official app releases.

---

The OpenRC family of Robot Controller apps are brought to you by the OpenFTC community. They are based on the official
[FTC SDK](https://github.com/ftctechnh/ftc_app) (Software Development Kit), but the AAR files used there have been
converted to first-class modules in the OpenRC Android Studio project. This makes it easy to see and modify almost the
entirety of the Robot Controller app's source code. In addition, the history in Git shows all changes that have been
made to the core code since OpenRC's inception. This is a very useful supplement to the changelogs that FIRST provides -
teams can see exactly what code has been changed and determine how those changes will affect them.

This system allows pull requests and enhancements to the code of the entire SDK, and can allow teams to understand the
structure and functionality of the whole system. Enhancements will be considered as long as they _do not force teams to
change their workflow._ Changes made in the OpenRC should keep it possible for teams to move from the official SDK to
OpenRC with no code changes required.

To request a new feature, you can open an issue on this repository. If there's a large enough call for the feature, it's
very likely to be added to the list for a future release.

This version keeps the OnBotJava and Blocks programming system. If your team doesn't use these, you may want to look
into [OpenRC-turbo](https://github.com/OpenFTC/OpenRC-turbo), which features faster deploy times.

# Release Notes
To see the release notes for FIRST's releases of this SDK, see [doc/FIRST_CHANGELOG.md](doc/FIRST_CHANGELOG.md)

## 2.0 beta 4
* CRITICAL BUG FIX: Stock mode was broken in 2.0 beta 3
* Fixed incompatiblity with version 3.1 of the Android Gradle Plugin

## 2.0 beta 3
* Updated to version 3.7 of the official SDK, with a fix for Android 7.0 Nougat
* Bug fix: USB devices plugged in while the app is running were not detected until "restart robot" was selected
* Added new dialog that displays on first startup, which warns that OpenRC is not legal for competition use, and points
  the user to the stock build variant
* On about screen, replaced completely broken app build time with correct app installation time (guest contribution by @antgo2!)
* Changed About OpenRC dialog to reflect the GDC ruling
* Updated README.md to reflect the GDC ruling and add links to website
* Made link in About OpenRC dialog clickable

## 2.0 beta 2
* CRITICAL BUG FIX: The app wouldn't automatically restart when it ought to. Now it will.
* Updated Gradle from version 4.1 to version 4.5
* Updated the ancient Gradle Wrapper
* Tiny text fixes

## 2.0 beta 1
* Renamed from OpenFTC-app and OpenFTC-app-turbo to OpenRC and OpenRC-turbo. OpenFTC is now solely the name of the team
  and community that develops these apps and others.
* ‎Added stockDebug build configuration that will build an app without any of the OpenFTC modifications enabled
* ‎Large deployment time improvements for both Turbo and Full variants (accomplished by pushing Vuforia once from a new
  Gradle task, instead of every time bundled inside the APK)
* ‎Added an easy way to keep your Vuforia key off of GitHub (which would violate the Vuforia Developer Agreement)
* ‎Fix for Instant Run (It's recommended that you continue to leave instant run turned off in the Android Studio
  settings. In addition, be aware that changes applied with the instant run lightning bolt button may not persist when
  you restart the app.)
* ‎Updated to version 3.6 of the official RC app
* Migrated to version 3 of the Android Gradle plugin (Android Studio 3.0 or later is now required)
* ‎Ran Android Studio's formatting tool on the entire project. If you made any changes to files outside of TeamCode, you
  will almost certainly have merge conflicts because of this.
* ‎Show OpenRC version on about screen
* ‎Added splash screen
* ‎Removed all Vuforia targets except for the VuMarks used in Relic Recovery (for additional size reduction and lower
  deployment times)
* Show OpenRC version when the robot controller inspection screen is viewed from the driver station

## 1.1
* Updated the app to be based on version 3.5 of the official SDK.

## Pre-1.1
* Extracted the aar files and associated source jars into full, easily changed source code modules.
* Warn user if the driver station app is installed on the device
* Fixed bug with log viewer colorization
* Add option that allows the user to choose whether lines should wrap in the log viewer (they don't by default now)
* Changed the icon to make the app more easily distinguishable from the official one
* Added the OpenFTC version to the main screen
* Added "About OpenFTC" button to the about screen
* Allowed Gradle to use more RAM (up to 2GB) for faster build times


## Welcome!
This GitHub repository contains the source code that is used to build an Android app to control a *FIRST* Tech Challenge
competition robot.  To use this SDK, download/clone the entire project to your local computer. If you are new to the
*FIRST* Tech Challenge software and control system, you should visit the online wiki to learn how to install, configure,
and use the software and control system:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;https://github.com/ftctechnh/ftc_app/wiki

Note that the wiki is an "evergreen" document that is constantly being updated and edited.  It contains the most current
information about the *FIRST* Tech Challenge software and control system.

## Downloading the Project
It is important to note that this repository is large and can take a long time and use a lot of space to download. If
you would like to save time and space, there are some options that you can choose to download only the most current
version of the Android project folder:

* If you are a git user, *FIRST* recommends that you use the --depth command line argument to only clone the most
current version of the repository:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`git clone --depth=1 https://github.com/ftctechnh/ftc_app.git`

* Or, if you prefer, you can use the "Download Zip" button available through the main repository page.  Downloading the
project as a .ZIP file will keep the size of the download manageable.

Once you have downloaded and uncompressed (if needed) your folder, you can use Android Studio to import the folder
("Import project (Eclipse ADT, Gradle, etc.)").

## Getting Help
### User Documentation and Tutorials
*FIRST* maintains an online wiki with information and tutorials on how to use the *FIRST* Tech Challenge software and
robot control system.  You can access the wiki at the following address:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;https://github.com/ftctechnh/ftc_app/wiki

### Javadoc Reference Material
The Javadoc reference documentation for the FTC SDK is now available online.  Visit the following URL to view the FTC
SDK documentation as a live website:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;http://ftctechnh.github.io/ftc_app/doc/javadoc/index.html    

Documentation for the FTC SDK is also included with this repository.  There is a subfolder called "doc" which contains
several subfolders:

 * The folder "apk" contains the .apk files for the FTC Driver Station and FTC Robot Controller apps.
 * The folder "javadoc" contains the JavaDoc user documentation for the FTC SDK.

### Online User Forum
For technical questions regarding the SDK, please visit the FTC Technology forum:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;http://ftcforum.usfirst.org/forumdisplay.php?156-FTC-Technology
