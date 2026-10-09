# CutWaste ✂️📏

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Offline First](https://img.shields.io/badge/Privacy-100%25%20Offline-green.svg)](#privacy--offline)

**CutWaste** is a modern, privacy-focused Android application designed for woodworkers, metalworkers, carpenters, plumbers, and DIY enthusiasts. It calculates precise material consumption, remaining stock, blade kerf loss, and waste percentage when making linear cuts on stock materials.

---

## 💡 Why CutWaste?

| **Tool** | **Core Question Answered** |
| :--- | :--- |
| **CutList** | *"What pieces do I need to cut?"* |
| **CutWaste** | *"How much total stock material will remain or be wasted after making these planned cuts?"* |

When planning a project, calculating kerf loss (material turned into sawdust/shavings by the saw blade width) and total stock remaining is vital to avoid unexpected material shortages and minimize workshop waste.

---

## ✨ Key Features

- 📐 **Precise Stock & Cut Input**: Enter stock length, choose material types (Wood, Metal, Pipe, PVC, Wire, Cable, Fabric, etc.), and add multiple cut segments with length and quantity.
- 🪚 **Smart Kerf Loss Calculation**: Accounts for saw blade thickness per cut. Automatically applies the rule: `Kerf Loss = (Total Cuts - 1) × Kerf Per Cut` (the final cut produces no subsequent kerf loss).
- 🔄 **Multi-Unit Conversions**: Supports Metric (**mm**, **cm**, **m**) and Imperial (**in**, **ft**) units with exact high-precision `BigDecimal` conversions.
- 📊 **Material Bar Visualization**: Color-coded visual breakdown showing used material, remaining stock, or shortage warnings.
- 🧮 **Step-by-Step Formula Breakdown**: Displays a transparent, human-readable formula explanation using standard mathematical steps.
- 📜 **Calculation History & Favorites**: Local history powered by Jetpack DataStore. Search, filter, favorite, duplicate, and restore past calculations.
- 📌 **Quick Presets & Project Notes**: Pre-fill common stock measurements and save custom notes alongside calculation records.
- 📤 **Share & Export**: Export detailed formatted cut summaries to share with suppliers or teammates.
- 🎨 **Material You & Dark Theme**: Supports Android Dynamic Color and Light/Dark/System themes.
- 🔒 **100% Offline & Private**: Zero network permissions, no tracking, no analytics, no ads.

---

## 🧮 Calculation Logic & Formulas

1. **Total Required Cut Length**:
   $$\text{Required Length} = \sum (\text{Cut Length}_i \times \text{Quantity}_i)$$

2. **Kerf Loss Rule**:
   $$\text{Kerf Loss} = \begin{cases} (\text{Total Cuts} - 1) \times \text{Kerf Per Cut} & \text{if Total Cuts} > 1 \\ 0 & \text{otherwise} \end{cases}$$

3. **Total Consumed Material**:
   $$\text{Total Consumed} = \text{Required Length} + \text{Kerf Loss}$$

4. **Remaining Material vs Shortage**:
   - If $\text{Stock Length} \ge \text{Total Consumed}$:
     $$\text{Remaining} = \text{Stock Length} - \text{Total Consumed}$$
   - If $\text{Total Consumed} > \text{Stock Length}$:
     $$\text{Shortage} = \text{Total Consumed} - \text{Stock Length}$$

5. **Waste Percentage**:
   $$\text{Waste \%} = \left( \frac{\text{Remaining}}{\text{Stock Length}} \right) \times 100$$

---

## 🪵 Supported Materials

- 🪵 **Wood & Timber Boards**
- 🔧 **Metal Bars & Rods**
- 🚰 **Pipes & PVC Profiles**
- 🔌 **Wire & Electrical Cables**
- 🧵 **Fabric & Textiles**
- 🖼️ **Trim, Molding & Aluminum Extrusions**

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) 2.2.10
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (BOM 2026.02.01)
- **Architecture**: MVVM with `StateFlow` & Coroutines
- **Navigation**: Navigation Compose
- **Data Persistence**: Preferences DataStore + `kotlinx.serialization`
- **Math Engine**: High-precision `java.math.BigDecimal`
- **Target SDK**: Android 16 (API 37) | **Min SDK**: API 24 (Android 7.0)

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio Ladybug](https://developer.android.com/studio) or newer
- JDK 17
- Android SDK 37

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/CutWaste.git
   cd CutWaste
   ```

2. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 🔒 Privacy & Offline

CutWaste operates **100% locally on your device**.
- No internet access required
- No data leaves your device
- No third-party trackers, telemetries, or advertisements

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
