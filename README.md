# <img src="icon.svg" width="48" align="center" alt="Icon"> Phone Battery Complication for Wear OS

[![Build & Release](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/actions/workflows/build-and-release.yml)
[![Platform](https://img.shields.io/badge/Platform-Wear_OS-brightgreen?logo=android&logoColor=white)](https://developer.android.com/wear)
[![Latest Release](https://img.shields.io/github/v/release/amoledwatchfaces/Phone-Battery-Complication-Wear-OS?logo=github&color=blue)](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/releases)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Privacy Policy](https://img.shields.io/badge/Privacy--Policy-Read-blue?logo=googleplay&logoColor=white)](https://amoledwatchfaces.github.io/apps/privacy/phonebatterycomplication.html)

Wear OS complication and tile for displaying connected Phone Battery level (%). Add to any watch face!

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

The companion mobile app allows you to configure sync intervals and connection settings directly from your Android phone.

<p align="center">
  <img src="screenshots/mobile (1).png" width="22%" alt="Settings 1" />
  <img src="screenshots/mobile (2).png" width="22%" alt="Settings 2" />
  <img src="screenshots/mobile (3).png" width="22%" alt="Settings 3" />
  <img src="screenshots/mobile (4).png" width="22%" alt="Settings 4" />
</p>

---

## 🚀 Installation & Releases

### Google Play Store
Get the app directly on your Android phone and Wear OS smartwatch from the Google Play Store:

<p align="center">
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
* **ProtoLayout & Tiles**: Custom Tiles support for quick access to battery complications.

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
