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
	        implementation 'com.gitee.jiang_li_jie_j:awesome-downloader:v1.2.2-alpha'
	}

```


#### 使用说明

1.申请读写权限，网络权限

2.初始化下载器，传入Application的context

kotlin：
```kotlin
    //默认模式启动（与页面绑定，页面销毁时，下载器也会结束生命）传入FragmentActivity或Fragment
    AwesomeDownloader.initWithDefaultMode(requireActivity())

    //前台服务模式启动（独立启动，直至服务被kill或关闭）传入能创建服务的ContextWrapper
    AwesomeDownloader.initWithServiceMode(this)
```
java：
```java    
	//默认模式启动（与页面绑定，页面销毁时，下载器也会结束生命）传入FragmentActivity或Fragment
	AwesomeDownloader.INSTANCE.initWithDefaultMode(this);

	//前台服务模式启动（独立启动，直至服务被kill或关闭）传入能创建服务的ContextWrapper
	AwesomeDownloader.INSTANCE.initWithServiceMode(this);
```
3.下载文件 

kotlin:
 ```kotlin
	 	val url = "https://images.gitee.com/uploads/images/2020/0919/155031_538a3406_5577115.png"
        //获取应用私有照片储存路径
        val filePath = PathSelector(applicationContext).getPicturesDirPath()
        //加入下载队列
        AwesomeDownloader.enqueue(url,filePath,"test.png")
 ```
java：
```java
    String url = "https://images.gitee.com/uploads/images/2020/0919/155031_538a3406_5577115.png";
	//获取应用私有照片储存路径
    String filePath = new PathSelector(applicationContext).getPicturesDirPath();
	//加入下载队列
    AwesomeDownloader.INSTANCE.enqueue(url, filePath, "test.png");
```
4.下载控制

kotlin:
```kotlin
        //停止全部
        AwesomeDownloader.stopAll()
        //恢复下载
        AwesomeDownloader.resume()
        //取消当前
        AwesomeDownloader.cancel()
        //取消全部
        AwesomeDownloader.cancelAll()
```

5.添加监听&移除监听

kotlin:
```kotlin
        //添加监听
        AwesomeDownloader.addOnProgressChangeListener{ progress ->
            //do something...
        }.addOnStopListener{ downloadBytes, totalBytes ->
            //do something...
        }.addOnFinishedListener{ filePath, fileName ->
            //do something...
        }.addOnErrorListener{ exception ->
            //do something...
        }
        
         //移除全部进度监听
         AwesomeDownloader.removeAllOnProgressChangeListener()
         
         //移除最后一个进度监听
         AwesomeDownloader.onDownloadProgressChange.removeLast()
         
         //移除最前的进度监听
         AwesomeDownloader.onDownloadProgressChange.removeFirst()
```

java:
```java
        //添加监听
        AwesomeDownloader.INSTANCE
        .addOnProgressChangeListener(progress -> {
            //do something
            return null;
        }).addOnStopListener((downloadBytes, totalBytes) -> {
            //do something
            return null;
        }).addOnFinishedListener((filePath, fileName) -> {
            //do something
            return null;
        }).addOnErrorListener(exception -> {
            //do something
            return null;
        });
        
         //移除全部进度监听
         AwesomeDownloader.INSTANCE.removeAllOnProgressChangeListener();
         
         //移除最后一个进度监听
         AwesomeDownloader.INSTANCE.getOnDownloadProgressChange()
         .removeLast();
         
         //移除最前的进度监听
         AwesomeDownloader.INSTANCE.getOnDownloadProgressChange()
         .removeFirst();
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



7.查询下载任务

kotlin:
```kotlin
        lifecycleScope.launch(Dispatchers.IO) {
            //获取全部任务信息
            AwesomeDownloader.queryAllTaskInfo()
            //获取完成的任务信息
            AwesomeDownloader.queryFinishedTaskInfo()
            //获取完成的任务信息
            AwesomeDownloader.queryUnfinishedTaskInfo()
          	//根据id删除数据库中的任务记录
            AwesomeDownloader.deleteById(id)
        }

		//获取当前下载中的任务
		AwesomeDownloader.getDownloadingTask()

		//获取队列中的任务
		AwesomeDownloader.getDownloadQueueArray()

```

java:
```java
	//获取全部任务信息
	AwesomeDownloader.INSTANCE.getAllTaskInfoLiveData().getValue();
		
	//获取完成的任务信息
	AwesomeDownloader.INSTANCE.getFinishedTaskInfoLiveData().getValue();

	//获取完成的任务信息
	AwesomeDownloader.INSTANCE.getUnfinishedTaskInfoLiveData().getValue();

	//获取当前下载中的任务
	AwesomeDownloader.INSTANCE.getDownloadingTask();

	//获取队列中的任务
	AwesomeDownloader.INSTANCE.getDownloadQueueArray();
```

