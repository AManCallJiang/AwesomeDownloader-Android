# AwesomeDownloader

#### 介绍
 **_AwesomeDownloader 是基于OkHttp和kotlin协程实现的下载器，它能在后台进行下载任务并轻松地让您在下载文件时获取进度，它能随时停止、恢复、取消任务，还可以方便地查询下载的任务和已完成的任务的信息。_** 

#### 功能&特性


 :star: **下载文件**

 :star: **监听下载**

 :star: **断点续传**

 :star: **随时控制下载**

 :star: **查询下载任务 (支持返回LiveData)**

 :star: **可通过通知栏显示下载情况** 

 :star: **下载多媒体文件加入多媒体库** 

 :star: **自动/手动清除缓存文件** 

 :star: **支持链式调用**


#### 导入依赖

1. 把它添加到你的根目录build.gradle中，在repositories的最后:
```groovy

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

```

2. 添加依赖：
![version](https://jitpack.io/v/com.gitee.jiang_li_jie_j/awesome-downloader.svg)
```groovy
	dependencies {
	        implementation 'com.gitee.jiang_li_jie_j:awesome-downloader:v1.2.1-alpha'
	}

```


#### 使用说明

1.申请读写权限，网络权限

2.初始化下载器，传入Application的context

kotlin：
```kotlin
	 AwesomeDownloader.init(applicationContext)
```
java：
```java    
	 AwesomeDownloader.INSTANCE.init(getApplicationContext());
```
3.下载文件 

kotlin:
 ```kotlin
	 val url = "https://images.gitee.com/uploads/images/2020/0914/131423_f1aaba0b_1899542.png"
        //获取应用外部照片储存路径
        val filePath = PathSelector(applicationContext).getPicturesDirPath()
        val fileName = "test.png"
        //加入下载队列
        AwesomeDownloader.enqueue(url, filePath, fileName)
```
java：
```java
        String url="https://images.gitee.com/uploads/images/2020/0914/131423_f1aaba0b_1899542.png";
        //获取应用外部照片储存路径
        String filePath = new PathSelector(getApplicationContext()).getPicturesDirPath();
        String fileName = "test.png";
        //加入下载队列
        AwesomeDownloader.INSTANCE.enqueue(url,filePath,fileName);

```
4.下载控制

kotlin:
```kotlin
        //停止全部
        AwesomeDownloader.stopAll()
        //恢复下载
        AwesomeDownloader.resumeAndStart()
        //取消当前
        AwesomeDownloader.cancel()
        //取消全部
        AwesomeDownloader.cancelAll()
```

5.设置监听

kotlin:
```kotlin
        AwesomeDownloader.setOnProgressChange { progress ->
            //do something...
        }.setOnStop { downloadBytes, totalBytes ->
            //do something...
        }.setOnFinished { filePath, fileName ->
            //do something...
        }.setOnError { exception ->
            //do something...
        }
```

6.设置自定义通知栏

（默认显示的通知栏）

![默认显示的通知栏](https://images.gitee.com/uploads/images/2020/0919/155031_538a3406_5577115.png)

设置中确保showNotification为true
```kotlin
 AwesomeDownloader.option.showNotification = true
```
调用setNotificationSender()

override 抽象类NotificationSender 的三个方法

kotlin:
```kotlin
 AwesomeDownloader.setNotificationSender(object : NotificationSender(applicationContext) {
                //创建显示任务下载进度的Notification
                override fun buildDownloadProgressNotification(
                    progress: Int,
                    fileName: String
                ): Notification {
                    return NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_adb_24)
                        .setContentTitle("$fileName 下载中")
                        .setContentText("$progress%")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                }

                //创建显示任务下载停止的Notification
                override fun buildDownloadStopNotification(fileName: String): Notification {
                    return NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_adb_24)
                        .setContentTitle("$fileName Stop")
                        .setContentText("Stop")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                }

                //创建显示任务下载完成的Notification
                override fun buildDownloadDoneNotification(
                    filePath: String,
                    fileName: String
                ): Notification {
                    Log.d(TAG, "buildDownloadDoneNotification: start")
                    return if (isImageFile(fileName)) {
                        val bitmap =
                            BitmapFactory.decodeFile("$filePath/$fileName")
                        Log.d(TAG, "buildDownloadDoneNotification: done")
                        NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_adb_24)
                            .setContentTitle("$fileName Done")
                            .setContentText("Done")
                            .setStyle(
                                NotificationCompat.BigPictureStyle()
                                    .bigPicture(bitmap)
                                    .bigLargeIcon(null)
                            )
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build()

                    } else {
                        NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_adb_24)
                            .setContentTitle("$fileName Done")
                            .setContentText("Done")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build()
                    }
                }
            })
```

_(通过setNotificationSender()设置的通知栏)_

![自定义显示的通知栏](https://images.gitee.com/uploads/images/2020/0919/153803_33f283b0_5577115.png)

_(通知栏效果可能因为Android版本不同和手机厂商不同而效果不一致)_
