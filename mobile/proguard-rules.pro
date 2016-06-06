# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# https://firebase-dot-devsite.googleplex.com/docs/auth/android/start/#proguard
-keepattributes Signature
-keepattributes *Annotation*

# FIREBASE: This rule will properly ProGuard all the model classes in the
# package com.yourcompany.models. Modify to fit the structure of your app.
-keepclassmembers class com.harlie.radiotheater.radiomysterytheater.** {
  *;
}

-keepclassmembers class at.grabner.circleprogress.CircleProgressView.** {
  *;
}

