# <img src="screenshots/app_icon.png" width="48" align="center" alt="Icon"> Phone Battery Complication for Wear OS

[![Build & Release](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/actions/workflows/build-and-release.yml)
[![Platform](https://img.shields.io/badge/Platform-Wear_OS-brightgreen?logo=android&logoColor=white)](https://developer.android.com/wear)
[![Latest Release](https://img.shields.io/github/v/release/amoledwatchfaces/Phone-Battery-Complication-Wear-OS?logo=github&color=blue)](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/releases)
[![License](https://img.shields.io/badge/License-GPLv3-orange.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Privacy Policy](https://img.shields.io/badge/Privacy--Policy-Read-blue?logo=googleplay&logoColor=white)](https://amoledwatchfaces.github.io/apps/privacy/phonebatterycomplication.html)

Wear OS complication and tile for displaying connected Phone Battery level (%). Add to any watch face!

---

## 🧩 Included Complication Services

This suite provides 10 custom complications for your watch faces:

| Complication Service | Supported Wear OS Complication Types | Description |
|:---|:---|:---|
| **Phone Battery** | `RANGED_VALUE`, `SHORT_TEXT`, `LONG_TEXT` | Displays the battery percentage of your connected Android phone. |
| **Watch Battery** | `RANGED_VALUE`, `SHORT_TEXT`, `LONG_TEXT`, `ICON`, `SMALL_IMAGE` | Displays your smartwatch's battery level in multiple layout formats. |
| **Watch Battery Temperature** | `SHORT_TEXT` | Displays your smartwatch's battery temperature (e.g. °C/°F). |
| **Watch Battery Voltage** | `SHORT_TEXT` | Displays your smartwatch's battery voltage (V). |
| **Phone Notifications** | `SMALL_IMAGE`, `LONG_TEXT` | Displays active notification icons from your phone statusbar. (2x2 / 8x1) |
| **Phone Notifications (4x)** | `SHORT_TEXT` | Displays maximum 4 active notification icons from your phone statusbar |
| **Phone Notifications Preview** | `LONG_TEXT` | Displays a detailed text preview of the latest notification from your phone. |
| **Upcoming Event** | `LONG_TEXT`, `SHORT_TEXT` | Displays details or title of the next scheduled event on your phone's calendar. |
| **Event Timer** | `LONG_TEXT`, `SHORT_TEXT` | Displays time remaining for the current or next calendar event on your phone. |
| **Now Playing** | `SMALL_IMAGE`, `PHOTO_IMAGE` | Displays the media playback artwork or notification icon for the active media stream on your phone. |

---

## 📸 Previews

<p align="center">
  <img src="screenshots/wear (1).png" width="22%" alt="Preview 1" />
  <img src="screenshots/wear (2).png" width="22%" alt="Preview 2" />
  <img src="screenshots/wear (3).png" width="22%" alt="Preview 3" />
  <img src="screenshots/wear (4).png" width="22%" alt="Preview 4" />
</p>

<details>
<summary><b>Show more Wear OS previews</b></summary>
<br>
<p align="center">
  <img src="screenshots/wear (5).png" width="22%" alt="Preview 5" />
  <img src="screenshots/wear (6).png" width="22%" alt="Preview 6" />
</p>
</details>

---

## ⚙️ App Settings & Configuration

The companion mobile app allows you to configure synchronization of different data eg. Calendar Events Sync for Upcoming Event Complication, Notifications Sync for Notification Complications, etc.

<p align="center">
  <img src="screenshots/mobile (2).png" width="25%" alt="Settings 2" />
  <img src="screenshots/mobile (3).png" width="25%" alt="Settings 3" />
</p>

---

## 🚀 Installation & Releases

### Google Play Store
Get the app directly on your Android phone and Wear OS smartwatch from the Google Play Store:

<p align="left">
  <a href="https://play.google.com/store/apps/details?id=com.weartools.phonebattcomp">
    <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="220" />
  </a>
</p>

### Sideloading
Alternatively, you can download the compiled `.apk` packages directly from our [GitHub Releases](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/releases) page to side-load.

---

## 🛠️ Tech Stack

This project is built using modern Android and Wear OS development practices:
* **Jetpack Compose / Wear Compose**: Declarative UI layout for both Mobile companion and Wear OS applications.
* **Wear Material 3**: Leveraging the latest Material 3 styles and design elements on Wear OS.
* **Coroutines & Flow**: Safe asynchronous data streaming.
* **Hilt**: Dependency injection architecture.
* **DataStore**: Modern, transactional preferences storage.

---

## 💻 Development Setup

If you want to build the project locally or contribute changes:

1. **Prerequisites:**
   * Android Studio (Koala / Ladybug or newer recommended)
   * JDK 21
   * Gradle 9.7+

2. **Clone the repository:**
   ```bash
   git clone https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS.git
   cd Phone-Battery-Complication-Wear-OS/Phone_Battery_Complication_AS
   ```

3. **Open in Android Studio:**
   Import the `Phone_Battery_Complication_AS` folder. Android Studio will sync the Gradle configuration automatically.

4. **Build & Run:**
   * Run the `:wear` target to deploy on a Wear OS device or emulator.
   * Run the `:mobile` target to deploy the companion app on a connected phone.

---

## 🌐 Help Us Translate

We welcome community translations! You can contribute translations by updating resources:

* **[Store Listings Translations](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/tree/master/Store-listings)**
* **[Mobile App Strings](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/tree/master/mobile/src/main/res)**
* **[Wear OS App Strings](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/tree/master/wear/src/main/res)**

---

## 📄 License

```text
Phone Battery Complication - Wear OS
Copyright 2022-2026 amoledwatchfaces™
support@amoledwatchfaces.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
