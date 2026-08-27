# Phone Battery Complication for Wear OS

[![CI Build](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/actions/workflows/build.yml/badge.svg)](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/actions/workflows/build.yml)
[![Latest Release](https://img.shields.io/github/v/release/amoledwatchfaces/Phone-Battery-Complication-Wear-OS?logo=github&color=blue)](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/releases)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Wear_OS-green.svg?logo=android&logoColor=white)](https://developer.android.com/wear)

A clean, modern Wear OS complication and tile to display your connected Android Phone's battery level (%) on your watch face. 

---

## Previews

<p align="center">
  <img src="https://user-images.githubusercontent.com/92080649/222390961-f3c7017a-7532-455d-be49-23d0e72f5a7c.png" width="23%" alt="Preview 1" />
  <img src="https://user-images.githubusercontent.com/92080649/222390991-118d5bfc-51c1-464e-826c-e12809d5ab0c.png" width="23%" alt="Preview 2" />
  <img src="https://user-images.githubusercontent.com/92080649/222391030-450e261b-8b4b-4fbe-8096-68e1d876568d.png" width="23%" alt="Preview 3" />
  <img src="https://user-images.githubusercontent.com/92080649/222391061-a982e60b-24d2-4866-9015-3cd5b9b89dc4.png" width="23%" alt="Preview 4" />
</p>

---

## Installation

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

## Tech Stack

This project is built using modern Android development practices:
* **Jetpack Compose / Wear Compose**: Declarative UI layout for both Mobile companion and Wear OS applications.
* **Wear Material 3**: Leveraging the latest Material 3 styles and design elements on Wear OS.
* **Coroutines & Flow**: Asynchronous programming and data streams.
* **Hilt**: Dependency injection for clean architecture.
* **DataStore**: Modern, reliable key-value preferences storage.
* **ProtoLayout & Tiles**: Custom Tiles support for quick access to battery complications.

---

## Development Setup

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
   Import the `Phone_Battery_Complication_AS` folder. Android Studio will automatically sync the Gradle configuration.

4. **Build & Run:**
   * Run the `:wear` target to deploy on a Wear OS device or emulator.
   * Run the `:mobile` target to deploy the companion app on a connected phone.

---

## Help Us Translate

We welcome community translations! You can contribute translations by updating resources:

* **[Store Listings Translations](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/tree/master/Store-listings)**
* **[Mobile App Strings](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/tree/master/mobile/src/main/res)**
* **[Wear OS App Strings](https://github.com/amoledwatchfaces/Phone-Battery-Complication-Wear-OS/tree/master/wear/src/main/res)**

---

## License

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
