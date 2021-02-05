# TagAndCardCoderDecoder
### Android 高频&amp;超高频标签/卡的编解码方式

#### 标签
> 1.高频：图创编解码
> 2.超高频：感创编解码、深圳图书馆解码、远望谷编解码

#### 卡
> 1.高频：银行卡编解码、身份证编解码
> 2.超高频：深圳图书馆解码


## To get this project into your project:
### Gradle
Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.yuxpro:TagAndCardCoderDecoder:1.0.0'
	}

### Maven
Step 1. Add the JitPack repository to your build file

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>

Step 2. Add the dependency

	<dependency>
	    <groupId>com.github.yuxpro</groupId>
	    <artifactId>TagAndCardCoderDecoder</artifactId>
	    <version>1.0.0</version>
	</dependency>

