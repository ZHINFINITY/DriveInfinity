# Debug logging is a user-facing setting, so Log calls must survive R8.
# Never add -assumenosideeffects for android.util.Log here.
-keep class com.infinity.drive.core.common.SafeLog { *; }
-keep class android.util.Log { *; }

# TDLib is reached through JNI, so its classes and native callbacks must stay.
-keep class org.drinkless.tdlib.** { *; }
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Room entities are constructed reflectively by generated code.
-keep class com.infinity.drive.data.local.entity.** { *; }
-keep class com.infinity.drive.data.local.dao.** { *; }

# kotlinx.serialization keeps generated serializers on the classes themselves.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.infinity.drive.data.remote.telegram.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.infinity.drive.data.remote.telegram.** {
    kotlinx.serialization.KSerializer serializer(...);
}
