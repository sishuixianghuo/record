
#EIM Android application progurad configuration
-dontpreverify

#The specified target package can always be the root package.Additionally specifying the -allowaccessmodification
#option allows access permissions of classes and class members to be broadened, opening up the opportunity to
#repackage all obfuscated classes
-repackageclasses ''
-allowaccessmodification

#print detail log
-verbose

#These options let obfuscated applications or libraries produce stack traces that can still be deciphered later on:
#We're keeping all source file attributes, but we're replacing their values by the string "SourceFile". We could
#use any string. This string is already present in all class files, so it doesn't take up any extra space.
#We're also keeping the line number tables of all methods. Whenever both of these attributes are present, the Java
#run-time environment will include line number information when printing out exception stack traces.The information
#will only be useful if we can map the obfuscated names back to their original names, so we're saving the mapping
#to a file out.map. The information can then be used by the ReTrace tool to restore the original stack trace. for
#ReTrace tool information, please refer to http://docs.huihoo.com/proguard/manual/retrace/index.html.
-printmapping out.map
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

#Some code may make further use of introspection to figure out the enclosing methods of anonymous inner classes.
#In that case, the corresponding attribute has to be preserved as well:
-keepattributes EnclosingMethod

#If your application,library, etc., uses annotations, you may want to preserve them in the processed output.
#Annotations are represented by attributes that have no direct effect on the execution of the code. However, their
#values can be retrieved through introspection, allowing developers to adapt the execution behavior accordingly.
#By default, ProGuard treats annotation attributes as optional, and removes them in the obfuscation step. If they
#are required, you'll have to specify this explicitly:
-keepattributes *Annotation*

#The "Exceptions" attribute has to be preserved, so the compiler knows which exceptions methods may throw.
#The "InnerClasses" attribute (or more precisely, its source name part) has to be preserved too, for any inner classes that can be referenced from outside the library. The javac compiler would be unable to find the inner classes otherwise.
#The "Signature" attribute is required to be able to access generic types when compiling in JDK 5.0 and higher.
#keeping the "Deprecated" attribute and the attributes for producing useful stack traces
-keepattributes Exceptions,InnerClasses,Signature,Deprecated

-ignorewarnings

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize

#-dontwarn android.support.v4.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class sun.misc.Unsafe { *; }
-keep class com.touchsprite.android.bean.** { *; }

-keep class * extends java.lang.annotation.Annotation { *; }
-keep class * extends com.touchsprite.android.bean.BaseBean { *; }
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}



# bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

#keep public classes and their public/protected members
-keep public class * {
    public protected *;
}

#The -keepclassmembernames option for the class$ methods is not strictly necessary. These methods are inserted
#by the javac compiler and the jikes compiler respectively, to implement the .class construct. ProGuard will
#automatically detect them and deal with them, even when their names have been obfuscated. However, older
#versions of ProGuard and other obfuscators may rely on the original method names. It may therefore be helpful
#to preserve them, in case these other obfuscators are ever used for further obfuscation of the library.
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

#If your application,library, etc., contains native methods, you'll want to preserve their names and their
#classes' names, so they can still be linked to the native library
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#we're not forcing preservation of all serializable classes, just preservation of the listed members of classes
#that are actually used.Sometimes, the serialized data are stored, and read back later into newer versions of the
#serializable classes. One then has to take care the classes remain compatible with their unprocessed versions
#and with future processed versions. In such cases, the relevant classes will most likely have serialVersionUID
#fields. The following options should then be sufficient to ensure compatibility over time.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#keep 3rd party supporting jars classes
#-dontwarn org.joda.time.**
#-keep class org.joda.time.** {*;}

#-dontwarn android.support.v4.app.**
-keep class android.support.v4.app.** {*;}

#-dontwarn org.apache.http.entity.mime.**
-keep class org.apache.http.entity.mime.** {*;}


#-dontwarn org.apache.http.**
-keep class org.apache.http.** {*;}

#-dontwarn com.google.gson.**
-keep class com.google.gson.** {*;}

# Gson specific classes
-keep class sun.misc.Unsafe { *; }

#ormlite
-keep class com.j256.ormlite.** { *; }
-keep class com.j256.ormlite.android.** { *; }
-keep class com.j256.ormlite.field.** { *; }
-keep class com.j256.ormlite.stmt.** { *; }

#umeng
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep class com.umeng.**

-keep public class com.touchsprite.android.R$*{
    public static final int *;
}

-keep public class com.umeng.fb.ui.ThreadView {
}

-dontwarn com.umeng.**

-dontwarn org.apache.commons.**

-keep public class * extends com.umeng.**

-keep class com.umeng.** {*; }

#alipay
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.mobilesecuritysdk.*
-keep class com.ut.*

#fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }

#imageloader
-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.** { *; }


#buyly
-keep public class com.tencent.bugly.**{*;}

#v4
-dontwarn android.support.v4.**

-dontwarn **CompatHoneycomb

-dontwarn **CompatHoneycombMR2

-dontwarn **CompatCreatorHoneycombMR2

-keep interface android.support.v4.app.** { *; }

-keep class android.support.v4.** { *; }

-keep public class * extends android.support.v4.**

-keep public class * extends android.app.Fragment


#JavaBean
-dontwarn com.alibaba.fastjson.**

-keep class com.alibaba.fastjson.** { *; }

-keepattributes Signature


#retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#okhttp
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**


#RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}


#2.rxjava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}