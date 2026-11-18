# Keep Reflection and Gson classes
-keepattributes Signature
-keepattributes InnerClasses

# Keep JSON exporter class
-keep class com.bubo.veilscanner.JsonExporter { *; }

# Keep necessary Android classes
-keep class android.app.** { *; }
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Preserve Gson annotations
-keepattributes *Annotation*
-keep class com.google.gson.**
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson-specific annotations
-dontwarn sun.misc.**
-keep class com.google.gson.*** { *; }

# Keep utility classes
-keep class com.bubo.veilscanner.** { *; }