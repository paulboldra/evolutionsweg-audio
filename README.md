proof-of-concept for a audio-guide app for Evolutionsweg

# Evolutionsweg Android App: Installation and Testing Guide

This guide provides detailed instructions on how to set up, install, and test the Evolutionsweg Android application using Android Studio.

## Project Overview

The application provides a location-aware audio guide for the "Evolutionsweg" walk. It uses the phone's GPS to detect when the user enters predefined zones (geofences) and automatically plays the corresponding audio file for that location. The core functionality relies on background location access to provide a seamless experience, even when the phone's screen is off.

## Prerequisites

-   **Android Studio:** The latest stable version, downloadable from the [official Android developer website](https://developer.android.com/studio).
-   **Physical Android Device (Recommended):** An Android phone (Android 8.0 / API 26 or higher) is highly recommended for accurate, real-world testing.
-   **Android Emulator (Alternative):** If a physical device is unavailable, an emulator can be used to simulate location data.

---

## Step 1: Project Setup in Android Studio

1.  **Create a New Project:**
    * Open Android Studio.
    * Select **File > New > New Project**.
    * Choose the **Empty Views Activity** template and click **Next**.
    * Configure the project:
        * **Name:** `EvolutionswegApp`
        * **Package name:** (e.g., `com.example.evolutionswegapp`)
        * **Language:** `Kotlin`
        * **Minimum SDK:** `API 26: Android 8.0 (Oreo)`
    * Click **Finish**.

2.  **Add Dependencies:**
    * Wait for the project to build.
    * Open the `build.gradle.kts (Module :app)` file.
    * Add the Google Play Services for location dependency inside the `dependencies { ... }` block:
        ```groovy
        implementation("com.google.android.gms:play-services-location:21.3.0")
        ```
    * A banner will appear at the top of the file. Click **Sync Now**.

3.  **Copy the Code Files:**
    * **`AndroidManifest.xml`**: Open `app/src/main/AndroidManifest.xml` and replace its entire content with the provided code.
    * **`activity_main.xml`**: Navigate to `app/src/main/res/layout/activity_main.xml`. Replace its content with the layout code.
    * **`strings.xml`**: Go to `app/src/main/res/values/strings.xml` and replace its content with the provided strings.
    * **`MainActivity.kt`**: Open your main activity file (e.g., `app/src/main/java/com/example/evolutionswegapp/MainActivity.kt`). Replace its content with the provided `MainActivity.kt` code.
    * **`GeofenceBroadcastReceiver.kt`**: In the project view, right-click on your package folder (e.g., `com.example.evolutionswegapp`) and select **New > Kotlin Class/File**. Name the file `GeofenceBroadcastReceiver` and paste the provided receiver code into it.

---

## Step 2: Testing on a Physical Device (Recommended)

This method uses the device's actual GPS for the most accurate results.

1.  **Enable Developer Options & USB Debugging:**
    * On your phone, go to **Settings > About phone**.
    * Tap the **Build number** seven times until "You are now a developer!" appears.
    * Go back to **Settings > System > Developer options** and enable **USB debugging**.

2.  **Run the App:**
    * Connect your phone to your computer with a USB cable.
    * In the Android Studio toolbar, select your device from the dropdown menu.
    * Click the **Run 'app'** button (the green triangle ▶️).
    * The app will be installed and launched on your device.

3.  **Test Geofencing:**
    * When the app opens, grant the location permissions it requests. Start with "While using the app," and then, when prompted, grant "Allow all the time" for background access.
    * Physically walk to one of the specified coordinates. As you enter the 50-meter radius, the geofence should trigger, a notification should appear, and the audio should begin playing.

---

## Step 3: Testing on the Android Emulator

Use this method to test the app's logic without being physically present at the locations.

1.  **Create an Emulator:**
    * In Android Studio, go to **Tools > Device Manager**.
    * Click **Create Device**, choose a phone model (e.g., Pixel 7), and click **Next**.
    * Select a system image (e.g., Tiramisu - API 33). Download it if necessary. Click **Next** and then **Finish**.

2.  **Run the App on the Emulator:**
    * Select your new emulator from the device dropdown in the toolbar and click **Run 'app'**.

3.  **Simulate Location:**
    * Once the app is running in the emulator, click the three-dot menu (`...`) on the side panel to open **Extended Controls**.
    * Select the **Location** tab.
    * To test the first location, enter its coordinates:
        * **Latitude:** `49.339757`
        * **Longitude:** `8.761990`
    * Click the **Set Location** button.
    * The emulator will update its position, triggering the geofence. The app should show a notification and play the audio for the first location.
    * Repeat this process for the remaining coordinates to verify that all geofences are working correctly.
