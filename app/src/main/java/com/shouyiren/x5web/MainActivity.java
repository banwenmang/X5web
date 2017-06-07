package com.shouyiren.x5web;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.tencent.smtt.sdk.TbsVideo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 作者：ZhouJianxing on 2017/6/6 16:44
 * email:727933147@qq.com
 */
public class MainActivity extends AppCompatActivity {

    private static String[] sTitles = null;
    private static boolean sMainInitialized = false;

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // add constant here
    private static final int TBS_WEB = 0;
    private static final int FULL_SCREEN_VIDEO = 1;
    private static final int PLAY_VIDEO = 2;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // for view init
    private Context mContext = null;
    private SimpleAdapter mGridAdapter;
    private GridView mGridView;

    private ArrayList<HashMap<String, Object>> mItems;

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        if (!sMainInitialized) {
            this.new_init();
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity OnResume
    @Override
    protected void onResume() {
        this.new_init();

        // this.mGridView.setAdapter(mGridAdapter);
        super.onResume();
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // initiate new UI content
    private void new_init() {
        mItems = new ArrayList<>();
        this.mGridView = (GridView) this.findViewById(R.id.item_grid);

        if (mGridView == null)
            throw new IllegalArgumentException("the mGridView is null");

        sTitles = getResources().getStringArray(R.array.index_titles);
        int[] iconResource = {R.drawable.tbsweb, R.drawable.fullscreen,
                R.drawable.play_video};

        HashMap<String, Object> item;
        for (int i = 0; i < sTitles.length; i++) {
            item = new HashMap<>();
            item.put("title", sTitles[i]);
            item.put("icon", iconResource[i]);

            mItems.add(item);
        }
        this.mGridAdapter = new SimpleAdapter(this, mItems,
                R.layout.function_block, new String[]{"title", "icon"},
                new int[]{R.id.Item_text, R.id.Item_bt});
        if (null != this.mGridView) {
            this.mGridView.setAdapter(mGridAdapter);
            this.mGridAdapter.notifyDataSetChanged();
            this.mGridView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> gridView, View view,
                                        int position, long id) {
                    Intent intent;
                    switch (position) {
                        case PLAY_VIDEO: {
                            //判断当前Tbs播放器是否已经可以使用。
                            if (TbsVideo.canUseTbsPlayer(MainActivity.this)) {

                                //直接调用播放接口，传入视频流的url
                                TbsVideo.openVideo(MainActivity.this, "http://192.168.3.108:8080/alert_icon.mp4");
                            }
                        }
                        break;
                        case FULL_SCREEN_VIDEO: {
                            intent = new Intent(MainActivity.this,
                                    FullScreenActivity.class);
                            MainActivity.this.startActivity(intent);
                        }
                        break;

                        case TBS_WEB: {
                            intent = new Intent(MainActivity.this,
                                    BrowserActivity.class);

                            Uri uri = Uri.parse("https://www.oschina.net/");
                            intent.setData(uri);
                            MainActivity.this.startActivity(intent);
                        }
                        break;
                    }
                }
            });
        }
        sMainInitialized = true;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Activity menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds mItems to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                this.tbsSuiteExit();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void tbsSuiteExit() {
        // exit TbsSuite?
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("X5功能演示");
        dialog.setPositiveButton("OK", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Process.killProcess(Process.myPid());
            }
        });
        dialog.setMessage("quit now?");
        dialog.create().show();
    }
}

