# Al-Noor Digital Quran ProGuard / R8 release rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi & Retrofit DTOs
-keepclassmembers class com.example.data.remote.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
