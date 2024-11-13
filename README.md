# Demoshop: Implementing WebView Tracking with Firebase Analytics for GA4

## Overview

This repository contains a sample Android application designed to help developers and analysts understand how to implement WebView tracking using Firebase Analytics for Google Analytics 4 (GA4). It addresses common challenges associated with tracking user interactions within WebViews and demonstrates a recommended solution using a JavaScript interface to forward events to native code.

## Table of Contents

- [Problem Statement](#problem-statement)
- [Recommended Solution](#recommended-solution)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Implementation Details](#implementation-details)
  - [AnalyticsWebInterface.java](#1-analyticswebinterfacejava)
  - [DashboardFragment.kt](#2-dashboardfragmentkt)
  - [Web Frontend Modifications](#3-web-frontend-modifications)
- [Testing the Implementation](#testing-the-implementation)
- [Additional Notes](#additional-notes)
- [Contributing](#contributing)
- [License](#license)

## Problem Statement

When integrating web content within an Android app using `WebView`, tracking user interactions can be problematic. Using standard Google Analytics tracking (e.g., `gtag.js`) within the WebView can lead to **inflated user session counts** and inaccurate analytics data. This happens because both the WebView's JavaScript (via GTAG) and the native app send events to the same Google Analytics 4 property (under seperate Web and App streams), resulting in artifically high user and session counts. 

## Recommended Solution

To avoid inflated session counts and duplicate analytics data, it's recommended to forward analytics events from the WebView's JavaScript context to the native Android code. This can be achieved by:

1. **Implementing a JavaScript Interface**: Create a bridge between the WebView's JavaScript and the native Android code using `addJavascriptInterface`.

2. **Forwarding Events to Native Code**: In the WebView's JavaScript, instead of using `gtag.js` to log events, call the methods exposed by the JavaScript interface to log events directly in the native code using Firebase Analytics.

By following this approach, all analytics events are logged from the native side, ensuring consistent and accurate data without duplication.

## Project Structure

```
android-webviews/
├── app/
│   ├── build.gradle
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yourcompany/yourapp/
│   │   │   │   ├── AnalyticsWebInterface.java
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── ui/
│   │   │   │       ├── dashboard/
│   │   │   │       │   ├── DashboardFragment.kt
│   │   │   │       │   └── DashboardViewModel.kt
│   │   │   │       ├── home/
│   │   │   │       │   ├── HomeFragment.kt
│   │   │   │       │   └── HomeViewModel.kt
│   │   │   │       └── notifications/
│   │   │   │           ├── NotificationsFragment.kt
│   │   │   │           └── NotificationsViewModel.kt
│   │   │   └── res/...
│   │   └── androidTest/java/... (Test files)
│   └── ... (Other configurations)
├── google-tag-manager/
│   ├── event_utility_js_handler.js
│   └── logEvent_function.js
├── build.gradle
├── settings.gradle
└── ... (Other project files)
```

## Getting Started

To get a local copy up and running, follow these steps.

### Prerequisites

- Android Studio Bumblebee or later
- Android SDK
- Firebase account with a project set up for Android

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/rb-jellyfish/android-webviews.git
   ```

2. **Replace Package Name:**

   - The sample project uses the package name `com.example.demoshop`.
   - Replace all instances of `com.example.demoshop` with your own package name in the project files.
     - Update the `applicationId` in `app/build.gradle`.
     - Refactor the package name in your source code directories and files.
     - Update the `namespace` in `app/build.gradle`.

   **Example:**

   ```gradle
   android {
       namespace 'com.yourcompany.yourapp'
       defaultConfig {
           applicationId "com.yourcompany.yourapp"
           // ...
       }
   }
   ```

3. **Add Firebase to your project:**

   - Follow the [Firebase setup guide](https://firebase.google.com/docs/android/setup) to add Firebase to your Android app.
   - Download the `google-services.json` file from the Firebase console and place it in the `app/` directory.
   - **Important:** Ensure that the package name in your `google-services.json` matches your application's package name.

4. **Sync Gradle files:**

   - Android Studio should prompt you to sync the Gradle files. Click on **Sync Now**.

5. **Include JavaScript Files for Web Frontend:**

   - In the `google-tag-manager/` directory, you will find two JavaScript files that need to be included in your web frontend:
     - `logEvent_function.js`
     - `event_utility_js_handler.js`
   - Include these scripts in your web pages to enable communication between your web content and the native Android app.

## Implementation Details

### 1. `AnalyticsWebInterface.java`

This class defines the JavaScript interface that the WebView will use to communicate with the native code.

**Key Points:**

- **Methods Exposed to JavaScript:**
  - `logEvent(String name, String jsonParams)`: Logs events to Firebase Analytics.
  - `setUserProperty(String name, String value)`: Sets user properties in Firebase Analytics.
- **JSON Parsing:**
  - The `jsonToBundle` method recursively converts JSON objects to Android `Bundle` objects, handling various data types, including nested objects and arrays.

**Usage:**

```java
webView.addJavascriptInterface(
    new AnalyticsWebInterface(requireContext()), "AnalyticsWebInterface"
);
```

### 2. `DashboardFragment.kt`

This fragment sets up the `WebView` and adds the JavaScript interface.

**Key Configurations:**

- **WebView Settings:**

  ```kotlin
  webView.settings.apply {
      javaScriptEnabled = true
      domStorageEnabled = true
      mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
  }
  ```

- **Adding JavaScript Interface:**

  ```kotlin
  webView.addJavascriptInterface(
      AnalyticsWebInterface(requireContext()), "AnalyticsWebInterface"
  )
  ```

- **Loading Your Web Content:**

  ```kotlin
  webView.loadUrl("https://yourwebsite.com")
  ```

**Note:** Replace `"https://firebase.google.com/docs/analytics/webview"` with the URL of your web content.

### 3. **Web Frontend Modifications**

To enable your web pages to communicate with the native Android app, you'll need to include custom JavaScript functions. These scripts are responsible for forwarding analytics events from the web page to the native code.

#### **Include JavaScript Files**

Place the following JavaScript files in your web project's appropriate directories:

- **`logEvent_function.js`**

  ```javascript
  try {
    var db = {{Debug Mode}}
    window.logEvent = function(name, params) {
      if (!name) {
        return;
      }
  
      if (window.AnalyticsWebInterface) {
        // Call Android interface
        window.AnalyticsWebInterface.logEvent(name, JSON.stringify(params));
      } else if (window.webkit
          && window.webkit.messageHandlers
          && window.webkit.messageHandlers.firebase) {
        // Call iOS interface
        var message = {
          command: 'logEvent',
          name: name,
          parameters: params
        };
        window.webkit.messageHandlers.firebase.postMessage(message);
      } else {
        // No Android or iOS interface found
        console.log("No native APIs found.");
      }
    }
    
    window.setUserProperty = function(name, value) {
      if (!name || !value) {
        return;
      }
    
      if (window.AnalyticsWebInterface) {
        // Call Android interface
        window.AnalyticsWebInterface.setUserProperty(name, value);
      } else if (window.webkit
          && window.webkit.messageHandlers
          && window.webkit.messageHandlers.firebase) {
        // Call iOS interface
        var message = {
          command: 'setUserProperty',
          name: name,
          value: value
       };
        window.webkit.messageHandlers.firebase.postMessage(message);
      } else {
        // No Android or iOS interface found
        console.log("No native APIs found.");
      }
    }
  } catch (error) {
    if(db){console.log("Error in CHTML JS Handler Script:", error);}
  }
  ```

- **`event_utility_js_handler.js`**

  ```javascript
  (function() {
    try {
      var db = {{Debug Mode}};
      var ecommerceData = {{DLV - ecommerce}};
      var eventName = {{Event}};
  
      if (!ecommerceData || !eventName) {
        if (db) {
          console.log('Ecommerce data or event name is missing');
        }
        return;
      }
  
      // Prepare event parameters
      var eventParams = {
        currency: "AUD",
        value: 0,
        items: ecommerceData.items
      };
  
      // Call logEvent with the event name and parameters
      logEvent(eventName, eventParams);
  
      if (db) {
        console.log('Pushed to JS Handler', eventName, eventParams);
      }
    } catch(err) {
      if (db) {
        console.log('Error in ecommerce tracking:', err);
      }
    }
  })();
  ```

#### **Integrate Scripts into Your Web Pages**

1. **Include the `logEvent_function.js` at the beginning of your web page:**

   ```html
   <script src="path/to/logEvent_function.js"></script>
   ```

2. **Include the `event_utility_js_handler.js` where you want to track specific events:**

   ```html
   <script src="path/to/event_utility_js_handler.js"></script>
   ```

3. **Set Up Variables for Google Tag Manager (Optional):**

   - If you're using Google Tag Manager (GTM), you can set up variables and data layer events to pass the required data to these scripts.
   - Replace `{{Debug Mode}}`, `{{DLV - ecommerce}}`, and `{{Event}}` with the appropriate GTM variables or hard-coded values.

#### **Explanation of Scripts**

- **`logEvent_function.js`**

  - Defines the `logEvent` and `setUserProperty` functions, which check for the presence of the native interfaces.
  - Supports both Android (`AnalyticsWebInterface`) and iOS (`window.webkit.messageHandlers`).

- **`event_utility_js_handler.js`**

  - Wraps the event logging in an IIFE (Immediately Invoked Function Expression).
  - Retrieves event data from variables and calls `logEvent` with the appropriate parameters.
  - Handles errors gracefully and logs them if in debug mode.

#### **Important Notes**

- **Check for Interface Availability:**

  - The scripts check if the native interfaces are available before calling them. This prevents errors when the web page is loaded outside of the app.

- **Debug Mode:**

  - Use the `db` variable to enable or disable console logging for debugging purposes.

- **Currency and Value:**

  - Adjust the `currency` and `value` parameters as needed for your application.

- **Event Names and Parameters:**

  - Ensure that the `eventName` and `eventParams` match the events and parameters you want to track in Firebase Analytics.

## Testing the Implementation

1. **Run the App:**

   - Build and run the app on an emulator or physical device.

2. **Navigate to the WebView:**

   - Use the app's navigation to open the `DashboardFragment`, which contains the WebView.

3. **Interact with the Web Content:**

   - Perform actions on the web page that trigger analytics events.

4. **Verify in Firebase Analytics:**

   - Go to the Firebase console and navigate to the Analytics dashboard.
   - Use the **DebugView** to verify that events are being logged correctly.

## Additional Notes

- **Security Considerations:**

  - Be cautious when adding JavaScript interfaces to WebViews.
  - Only expose necessary methods to prevent potential security risks.
  - Ensure the WebView loads content from trusted sources.

- **Sensitive Files:**

  - **`google-services.json`:**
    - Contains sensitive information about your Firebase project.
    - Do not commit this file to version control.
    - Provide instructions for other developers to obtain their own `google-services.json`.

- **Package Name Replacement:**

  - Replace all instances of `com.example.demoshop` with your own package name throughout the project.

- **Debugging:**

  - Enable WebView debugging during development:

    ```kotlin
    WebView.setWebContentsDebuggingEnabled(true)
    ```

- **API Level Compatibility:**

  - The `addJavascriptInterface` method is safe to use starting from API level 17 (Jelly Bean MR1). For devices running lower API levels, implement appropriate checks.

    ```kotlin
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        webView.addJavascriptInterface(
            AnalyticsWebInterface(requireContext()), "AnalyticsWebInterface"
        )
    }
    ```

- **Event Parameter Limitations:**

  - Firebase Analytics has limitations on the size and number of parameters for events. Ensure that the data you send complies with Firebase's guidelines.

- **User Privacy:**

  - Always comply with user privacy regulations and policies.
  - Provide options for users to opt-out of analytics tracking if required.

