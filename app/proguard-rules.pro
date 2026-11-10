# Android default ProGuard rules
-keep class androidx.** { *; }
-keep class android.** { *; }
-keep class com.example.clockstf.** { *; }

# Keep R classes
-keep class **.R$* { *; }

# Keep classes that extend Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.appwidget.AppWidgetProvider

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}