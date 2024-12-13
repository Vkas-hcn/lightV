-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-obfuscationdictionary mask.txt
-classobfuscationdictionary mask.txt
-packageobfuscationdictionary mask.txt

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-keep class com.google.gson.**
-keep class com.example.app.** { *; }