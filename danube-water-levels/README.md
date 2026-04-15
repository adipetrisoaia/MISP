# Danube Water Levels

An Android app that displays real-time water level data for gauge stations along the Danube river, using the [PEGELONLINE](https://www.pegelonline.wsv.de/webservices/rest-api/v2/) public REST API.

## Features

- **Station List** – Browse and search Danube monitoring stations
- **Station Detail** – View current water level, 7-day historical chart, and flood warning thresholds
- **Offline Support** – Cached readings available without internet via Room database
- **Background Sync** – Periodic water level checks with flood alert notifications
- **Material Design** – Clean, modern UI with swipe-to-refresh

## Architecture

- **Language**: Kotlin
- **Pattern**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit + OkHttp
- **Local Storage**: Room Database
- **DI**: Hilt (Dagger)
- **Background Work**: WorkManager
- **Charts**: MPAndroidChart

## API

This app uses the **PEGELONLINE REST API v2** (German Federal Waterways and Shipping Administration):

| Endpoint | Description |
|---|---|
| `GET /stations.json?waters=DONAU` | List all Danube gauge stations |
| `GET /stations/{id}/W/currentmeasurement.json` | Current water level for a station |
| `GET /stations/{id}/W/measurements.json?start=P7D` | Last 7 days of measurements |

No authentication required.

## Building

1. Open the `danube-water-levels/` directory in Android Studio
2. Sync Gradle
3. Run on an emulator or device (min SDK 26 / Android 8.0)

## Testing

```bash
./gradlew test          # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## License

This project is provided as-is for educational purposes.
