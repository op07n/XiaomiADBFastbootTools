# Xiaomi ADB/Fastboot Tools

## Modules

* **Uninstaller** - Remove pre-installed apps and services on demand
* **Camera2** - Enable Camera2 and EIS (TWRP required)
* **Device properties** - Retrieve tons of statistics and information about your device
* **Screen density** - Tweak screen density by overriding the DPI value
* **Flasher** - Flash any partition with an image, boot to any image or flash a Fastboot ROM (unlocked bootloader required)
* **Wiper** - Wipe the cache or perform a factory reset
* **OEM Unlocker & Locker** - Lock or unlock the bootloader (Unlocking is supported by the Android One devices only)
* **ROM Downloader** - Get links to the latest Fastboot ROMs or download them right away
* **Rebooter** - Advanced rebooting options using ADB/Fastboot

![](screenshot.png)

## Download the executable JAR from [here](https://github.com/Saki-EU/XiaomiADBFastbootTools/releases/latest).

**Warning: Use the program at your own risk. Removing apps which aren't listed in the Uninstaller by default may brick your device.**

## Instructions

### Loading a device in ADB mode

1. Enable developer options in Android.

    * MIUI: Go to Settings > About device and tap ‘MIUI version’ seven times to enable developer options.
    * Android One: Go to Settings > System > About device and tap ‘Build number’ seven times to enable developer options.

2. Enable USB debugging in Android.

    * MIUI: Go to Settings > Additional settings > Developer options and enable USB debugging as well as USB debugging (Security settings).
    * Android One: Go to Settings > System > Developer options and enable USB debugging.

3. Connect your device to your computer and launch the application. The device will ask for authorisation right away, which you have to allow.

4. Load your device by clicking Menu > Check for device in the application. Your device info should appear.

### Loading a device in Fastboot mode

1. Put your device into Fastboot mode by holding power and volume down simultaneously until the Fastboot splash screen comes up.

    * If your device is loaded in ADB mode, you can enter Fastboot mode by clicking Menu > Reboot device to Fastboot.

2. Connect your device to your computer and launch the application.

3. Load your device by clicking Menu > Check for device in the application. Your device info should appear.

## Frequently Asked Questions

* **Q:** The tool doesn't launch on my computer, is there anything I should have installed?

    * **A:** Yes, the tool was developed for Java and needs the Java Runtime Environment to run. You can install Java from [here](https://java.com/en/download/). On Linux, OpenJFX is needed alongside OpenJRE to run the application.

* **Q:** The tool on Windows doesn't detect my device even though it's connected and USB debugging is enabled. What could be the issue?

    * **A:** Windows most likely does not recognise your device in ADB. Install the universal ADB drivers from [here](http://dl.adbdriver.com/upload/adbdriver.zip).

* **Q:** Do I need an unlocked bootloader or root access to use the tool?

    * **A:** The Image Flasher, Wiper and Camera2 modules require an unlocked bootloader but everything else works without rooting or unlocking.

* **Q:** Do uninstalled system apps affect OTA updates?

    * **A:** No, you are free to install updates without the fear of bricking your device or losing data.

* **Q:** Why does the Uninstaller hang on some apps?

    * **A:** There are many factory apps Global MIUI doesn't let you uninstall but China MIUI does. If you try to uninstall such an app, the tool will hang. Disconnect your device, uninstall the app then connect and load your device again to proceed.

* **Q:** How do I regain uninstalled system apps?

    * **A:** When you uninstall apps, you erase them from `/data` so you need to factory reset (wipe data) to make your device reinstall them (from `/system`).

* **Q:** The tool is called Xiaomi ADB/Fastboot Tools. Does that mean it only works with Xiaomi devices?

    * **A:** ADB and Fastboot are universal interfaces for Android devices but some of the algorithms and methods used in the app are specific to Xiaomi devices, so yes.

* **Q:** Does this replace MiFlash or MiUnlock?

    * **A:** No. Fastboot ROM flashing is available, but implementing other features in such a simple tool would only make it unnecessarily complex.
