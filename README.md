# AwesomeDownloader

#### 介绍
 **_AwesomeDownloader 是基于Retrofit和kotlin协程实现的下载器，它能在后台进行下载任务并轻松地让您在下载文件时获取进度，它能随时停止、恢复、取消任务，还可以方便地查询下载的任务和已完成的任务的信息。_** 

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
```groovy
	dependencies {
	        implementation 'com.gitee.jiang_li_jie_j:awesome-downloader:Tag'
	}

```


#### 使用说明

1.申请读写权限

2.  初始化下载器，传入Application的context

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
	 val url =
            "https://images.gitee.com/uploads/images/2020/0914/131423_f1aaba0b_1899542.png"
        //获取应用外部照片储存路径
        val filePath = PathSelector(applicationContext).getPicturesDirPath()
        val fileName = "test.png"
        //加入下载队列
        AwesomeDownloader.enqueue(url, filePath!!, fileName)
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
