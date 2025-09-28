# iTravel-Guide
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
