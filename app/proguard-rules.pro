-keepattributes *Annotation*
-keep class com.jnetaol.findai.data.model.** { *; }
-dontwarn javax.annotation.**
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
