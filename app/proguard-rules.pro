# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Firebase Rules
# ============================================

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.internal.firebase-auth-api.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Realtime Database
-keep class com.google.firebase.database.** { *; }
-keep class com.google.firebase.database.connection.** { *; }
-dontwarn com.google.firebase.database.**

# Firebase Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase In-App Messaging
-keep class com.google.firebase.inappmessaging.** { *; }
-dontwarn com.google.firebase.inappmessaging.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ============================================
# TensorFlow Lite Rules
# ============================================

-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.Interpreter { *; }
-keep class org.tensorflow.lite.Interpreter$Options { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep model files in assets
-keep class * implements java.io.Serializable { *; }

# ============================================
# Room Database Rules
# ============================================

# Keep Room entities
-keep @androidx.room.Entity class * { *; }
-keep class com.android.tomatoapp.WorkProgramEntity { *; }
-keep class com.android.tomatoapp.PlantMonitoringEntity { *; }

# Keep Room database
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase_Impl { *; }
-keep class * extends androidx.room.RoomDatabase_Impl$* { *; }

# ============================================
# Material Calendar View
# ============================================

-keep class com.prolificinteractive.materialcalendarview.** { *; }
-dontwarn com.prolificinteractive.materialcalendarview.**

# ============================================
# MPAndroidChart
# ============================================

-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ============================================
# CameraX Rules
# ============================================

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ============================================
# AndroidX and Support Libraries
# ============================================

-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================
# Application Classes
# ============================================

# Keep all Activities
-keep class com.android.tomatoapp.** extends android.app.Activity { *; }
-keep class com.android.tomatoapp.** extends androidx.appcompat.app.AppCompatActivity { *; }

# Keep all custom views
-keep class com.android.tomatoapp.** extends android.view.View { *; }

# Keep User model class (used with Firebase)
-keep class com.android.tomatoapp.User { *; }

# Keep error handler
-keep class com.android.tomatoapp.FirebaseErrorHandler { *; }

# Keep utility classes
-keep class com.android.tomatoapp.PhoneUtils { *; }

# Keep notification classes
-keep class com.android.tomatoapp.notifications.** { *; }

# ============================================
# JSON and Data Models
# ============================================

# Keep classes used with JSON serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class org.json.** { *; }

# ============================================
# Reflection
# ============================================

# Keep classes that use reflection
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ============================================
# Native Methods
# ============================================

-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================
# Enums
# ============================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================
# R class
# ============================================

-keepclassmembers class **.R$* {
    public static <fields>;
}

# ============================================
# Remove logging in release (optional)
# ============================================

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
