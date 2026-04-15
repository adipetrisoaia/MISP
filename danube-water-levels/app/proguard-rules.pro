# Retrofit and OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.danube.waterlevels.data.api.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
