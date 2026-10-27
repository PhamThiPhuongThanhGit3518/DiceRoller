# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*

# Gson serialization & models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-keep class com.phuongthanh.diceroller.features.customdice.model.** { *; }
-keep class com.phuongthanh.diceroller.features.party.model.** { *; }
-keep class com.phuongthanh.diceroller.features.roulette.model.** { *; }
-keep class com.phuongthanh.diceroller.features.freeroll.model.** { *; }
-keep class com.phuongthanh.diceroller.features.classic.model.** { *; }
-keep class com.phuongthanh.diceroller.features.settings.model.** { *; }

# Coroutines & DataStore
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.datastore.** { *; }

# Google Mobile Ads & Cyber Ads
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }
-keep class com.cyber.ads.** { *; }

# Custom Views
-keep class com.phuongthanh.diceroller.core.widget.** { *; }