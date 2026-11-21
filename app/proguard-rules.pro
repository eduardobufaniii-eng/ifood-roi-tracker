# iFood ROI Tracker - ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends dagger.hilt.android.HiltApplication

# Keep Room entities and DAOs
-keep class com.ifoodroitracker.data.entity.** { *; }
-keep class com.ifoodroitracker.data.db.** { *; }

# Keep ViewModels
-keep class com.ifoodroitracker.ui.viewmodel.** { *; }

# Keep Services
-keep class com.ifoodroitracker.service.** { *; }
-keep class com.ifoodroitracker.receiver.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Gson for serialization
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep OpenCSV
-keep class com.opencsv.** { *; }

# Keep Accessibility Service
-keep class android.accessibilityservice.AccessibilityService
-keep class android.view.accessibility.AccessibilityNodeInfo

# Keep Overlay/WindowManager
-keep class android.view.WindowManager
-keep class android.graphics.PixelFormat

# Don't warn about missing classes
-dontwarn com.google.android.gms.**
-dontwarn androidx.**
-dontwarn kotlin.**
-dontwarn org.jetbrains.**