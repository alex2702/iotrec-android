# IoT Rec (Android)

Android application for IoT Rec project. The app is written in Kotlin.

Users can select personal preferences from a two-level collection.

The application then continuously scans for Bluetooth Low Energy beacons in the vicinity, so called "things". Scanning and parsing of beacons is realized with [android-beacon-library](https://github.com/AltBeacon/android-beacon-library). After discovering a beacon, the app checks with the API (see [iotrec-backend](https://github.com/alex2702/iotrec-backend)) if any information is available on a given "thing" and if the system is issuing a recommendation to the user.

Backend communication is realized with [OkHttp](https://github.com/square/okhttp), [Retrofit](https://github.com/square/retrofit), and [Moshi](https://github.com/square/moshi).

The app also includes user tests ("experiments") that were conducted with some users to verify the utility of the application.

`build.apk` is a compiled install package.

## Screenshots

<p float="left">
  <img src="/screenshots/list.png" width="334" />
  <img src="/screenshots/details.png" width="334" /> 
  <img src="/screenshots/reco.png" width="334" />
  <img src="/screenshots/experiment.png" width="334" />
  <img src="/screenshots/cat_level2.png" width="334" />
  <img src="/screenshots/resque.png" width="334" />
</p>