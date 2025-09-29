# iTravel-Guide


This repository now contains two experiences for exploring famous destinations in Huế:

1. **Static web content** originally shipped with the project.
2. **Android application (`android-app/`)** that provides an interactive Google Maps style interface. Tap any marker to focus on the location, read curated descriptions (Vietnamese and English), and toggle languages directly from the map.

## Android app overview

The Android client is built with **Kotlin**, **Jetpack Compose**, and **Google Maps Compose**. It loads the existing `data/places.json` metadata and accompanying reading materials from the `assets/` directory so you get the same curated content inside the native experience.

### Key features
- Google Maps powered home screen with markers for all points of interest.
- Tap a marker to recenter the camera and open a detail sheet with bilingual descriptions.
- Switch to the new **Novel** tab inside the detail sheet to read short-form fiction inspired by each landmark.
- Use the **Generate questions** button to synthesize fill-in-the-blank prompts from the story using the built-in offline AI heuristic.
- Quick language toggle (Vietnamese ↔ English) from the summary card.
- Graceful fallbacks when quiz functionality or long-form content is unavailable.

### Getting started
1. Open the project in **Android Studio Flamingo (or newer)**.
2. Import the `android-app/` Gradle project.
3. Add a valid [Google Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key) by replacing the placeholder value in `app/src/main/AndroidManifest.xml`.
4. Sync Gradle and run the `app` configuration on an emulator or Android device (API 24+).

> **Note:** The Gradle wrapper JAR is not checked in. Android Studio will download the correct Gradle distribution automatically, or you can regenerate it locally with `gradle wrapper`.

### Project structure (`android-app/app/src/main`)
```
java/com/itravelguide
 ├── MainActivity.kt           # Google Map screen composition
 ├── MainViewModel.kt          # Loads places + location handling
 ├── data/                     # JSON models and repository
 └── ui/                       # Compose UI components & theme
```

Assets live under `android-app/app/src/main/assets/` and include:
- `places.json` (metadata for each destination)
- `read/` (Vietnamese & English descriptions)
- `novels/` (bilingual short stories tailored for each point of interest)
- `quiz/` (future quiz content stubs)

## License

This project is licensed under the terms of the [MIT License](LICENSE).
=======
## Android ứng dụng bản đồ

Thư mục `androidApp/` chứa mã nguồn ứng dụng Android mới được xây dựng bằng Kotlin và Jetpack Compose. Màn hình chính hiển thị bản đồ tương tác tương tự Google Maps:

- Hiển thị các địa danh từ dữ liệu `data/places.json` ngay trên bản đồ thông qua Marker.
- Nhấn vào Marker sẽ di chuyển camera và mở thẻ thông tin ngắn gọn về địa danh đã chọn.
- Nhấn vào bất kỳ điểm nào trên bản đồ để đặt Marker di động giúp người dùng lên kế hoạch di chuyển tới vị trí đó.

### Cách chạy

1. Mở thư mục `androidApp/` bằng Android Studio Flamingo (hoặc mới hơn).
2. Cập nhật khóa Google Maps API tại `app/src/main/AndroidManifest.xml` (thay `YOUR_GOOGLE_MAPS_API_KEY`).
3. Đồng bộ Gradle và chạy ứng dụng trên thiết bị thật hoặc trình giả lập có Google Play Services.

> Lưu ý: Kho lưu trữ không bao gồm `gradle-wrapper.jar`. Nếu cần, hãy chạy lệnh `./gradlew wrapper` bằng Gradle cài sẵn để tạo tập tin này.

