# X5web TBS腾讯浏览服务的SDK接入-Android studio #


[**我的博客**](https://x5.tencent.com/tbs/index.html)
----------

[**TBS官网**](https://x5.tencent.com/tbs/index.html)

> 普通接入参考项目中的 `App、BrowserActivity、X5WebView` 等类中相关实现。

###第一步:下载jar包并添加至项目###

###第二步:Android studio修改相关配置###

* 打开对应module中的build.gradle添加的，若配置后编译报错，需在gradle.properties加上 `Android.useDeprecatedNdk=true`

        android{
    		defaultConfig{
    			ndk{abiFilters "armeabi"}
    		}
    	}
* 在`src/main/`目录下创建`jniLibs`；在其创建 `armeabi`目录并加入`liblbs.so`文件。

![配置图](https://github.com/banwenmang/X5web/blob/master/x5web_step2.png)

###第三步：AndroidManifest.xml里权限声明###
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

	<!-- 硬件加速对X5视频播放非常重要，建议开启 -->
    <uses-permission android:name="android.permission.GET_TASKS"/>

![配置图](https://github.com/banwenmang/X5web/blob/master/x5web_step3.png)

###第四步：Application中对*初始化x5内核接口*###
    QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

        @Override
        public void onViewInitFinished(boolean b) {
            //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            Log.d("app", " onViewInitFinished is " + b);
        }

        @Override
        public void onCoreInitFinished() {
            // TODO Auto-generated method stub
        }
    };

    //x5内核初始化接口
    QbSdk.initX5Environment(getApplicationContext(), cb);

![配置图](https://github.com/banwenmang/X5web/blob/master/x5web_step4.png)

###第五步：创建X5WebView继承*SDK中WebView*###
>	声明WebViewClient

	private WebViewClient client = new WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };

>   设置WebViewClient

	@SuppressLint("SetJavaScriptEnabled")
    public X5WebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.setWebViewClient(client);
        initWebViewSettings();
        this.getView().setClickable(true);
    }
>   初始化WebViewSettings

	private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
    }
>   覆些WebView中drawChild

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean ret = super.drawChild(canvas, child, drawingTime);
        canvas.save();
        Paint paint = new Paint();
        paint.setColor(0x7fff0000);
        paint.setTextSize(24.f);
        paint.setAntiAlias(true);
        if (getX5WebViewExtension() != null) {
            canvas.drawText(this.getContext().getPackageName() + "-pid:"
                    + android.os.Process.myPid(), 10, 50, paint);
            canvas.drawText(
                    "X5  Core:" + QbSdk.getTbsVersion(this.getContext()), 10,
                    100, paint);
        } else {
            canvas.drawText(this.getContext().getPackageName() + "-pid:"
                    + android.os.Process.myPid(), 10, 50, paint);
            canvas.drawText("Sys Core", 10, 100, paint);
        }
        canvas.drawText(Build.MANUFACTURER, 10, 150, paint);
        canvas.drawText(Build.MODEL, 10, 200, paint);
        canvas.restore();
        return ret;
    }
###第六步：调整cookie的使用###
>com.tencent.smtt.sdk.CookieManager和com.tencent.smtt.sdk.CookieSyncManager的相关接口的调用，在接入SDK后，需要放到创建X5的WebView之后（也就是X5内核加载完成）进行；否则，cookie的相关操作只能影响系统内核。

>可参项目`BrowserActivity`类中 `initWebViewSetting()`

    /**
     * init WebView
     */
    private void initWebViewSetting() {
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mWebView.loadUrl(mHomeUrl);
        } else {
            mWebView.loadUrl(mIntentUrl.toString());
        }

        // print the time of webView load
        TbsLog.d("time-cost", "cost time: " + (System.currentTimeMillis() - time));

        // manage Cookie
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();
    }

###第七步：兼容视屏播放###
1) 享受页面视屏的完整播放体验，需如下声明：
>
	页面的Activity需要声明android:configChanges="orientation|screenSize|keyboardHidden"

2) 视屏为了避免闪屏问题，需如下声明：

1.  Activity在onCreate时需要设置:

		getWindow().setFormat(PixelFormat.TRANSLUCENT);（这个对宿主没什么影响，建议声明）
2.  以下接口禁止(直接或反射)调用，避免视频画面无法显示：

    *webview.setLayerType();*
    *webview.setDrawingCacheEnabled(true);*
###第八步：避免输入法界面弹出后遮挡输入光标的问题
**方法一**：*在AndroidManifest.xml中设置*

	android:windowSoftInputMode="stateHidden|adjustResize"

**方法二**：*在代码中动态设置*

	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


----------
## 补充：Tbs视频播放器接入说明 ##
1.	第一步：AndroidManifest需要如下的注册：

    	<!--视频-->
        <activity
            android:name="com.tencent.smtt.sdk.VideoActivity"
            android:alwaysRetainTaskState="true"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:exported="false"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="com.tencent.smtt.tbs.video.PLAY"/>
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </activity>

2.	第二步：通过TbsVideo调用播放视频，如下：
>public static boolean canUseTbsPlayer(Context context)
//判断当前Tbs播放器是否已经可以使用。

>public static void openVideo(Context context, String videoUrl)
//直接调用播放接口，传入视频流的url

>public static void openVideo(Context context, String videoUrl, Bundle extraData)
//extraData对象是根据定制需要传入约定的信息，没有需要可以传如null

		//判断当前Tbs播放器是否已经可以使用。
    	if (TbsVideo.canUseTbsPlayer(MainActivity.this)) {

        	//直接调用播放接口，传入视频流的url
        	TbsVideo.openVideo(MainActivity.this, "http://192.168.3.108:8080/alert_icon.mp4");
    	}



**希望对大家有帮助**

**源码附上**

[**https://github.com/banwenmang/X5web/tree/master**](https://github.com/banwenmang/X5web/master)
