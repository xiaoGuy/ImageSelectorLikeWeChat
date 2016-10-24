package com.xiaoguy.imageselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.adapter.SentListAdapter;
import com.xiaoguy.imageselector.ui.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 预览发送的图片
 * <p>
 * Created by XiaoGuy on 2016/10/24.
 */

public class SentImageViewerActivity extends AppCompatActivity {

    private static final String TAG = SentImageViewerActivity.class.getSimpleName();
    private static final String SENT = "sent";
//    private static final String PATH = "path";

    @BindView(R.id.btn_back)
    ImageView mBtnBack;
    @BindView(R.id.text_title)
    TextView mTextTitle;
    @BindView(R.id.btn_finish)
    Button mBtnSend;
    @BindView(R.id.recyclerView_sent_list)
    RecyclerView mRecyclerViewSentList;

    private ArrayList<String> mSentImages;
    private String mSentImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_sent);
        ButterKnife.bind(this);

        initData();
        initVew();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mSentImages = intent.getStringArrayListExtra(SENT);
//            mSentImage = intent.getStringExtra(PATH);
        }
    }

    private void initVew() {
        mTextTitle.setText(R.string.sent_image);
        mBtnSend.setVisibility(View.GONE);

        if (mSentImages == null) {
            Log.e(TAG, "mSentImages == null");
            return;
        }

        mRecyclerViewSentList.setAdapter(new SentListAdapter(mSentImages));
        mRecyclerViewSentList.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewSentList.addItemDecoration
                (new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    public static void startPreviewSentActivity(Activity activity, ArrayList<String> sentImages) {
        Intent intent = new Intent(activity, SentImageViewerActivity.class);
        intent.putStringArrayListExtra(SENT, sentImages);
        activity.startActivity(intent);
    }

//    public static void startPreviewSentActivity(Activity activity, String path) {
//        Intent intent = new Intent(activity, SentImageViewerActivity.class);
//        intent.putExtra(PATH, path);
//        activity.startActivity(intent);
//    }

    @OnClick(R.id.btn_back)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
