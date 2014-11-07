package com.diandi.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.diandi.CustomApplication;
import com.diandi.MyMessageReceiver;
import com.diandi.R;
import com.diandi.bean.User;
import com.diandi.ui.fragment.ContactFragment;
import com.diandi.ui.fragment.DiandiFragment;
import com.diandi.ui.fragment.RecentFragment;
import com.diandi.util.factory.OverridePendingFactory;
import com.diandi.view.residemenu.ResideMenu;
import com.diandi.view.residemenu.ResideMenuItem;
import com.nineoldandroids.view.ViewHelper;
import com.umeng.fb.FeedbackAgent;


import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import view.drawmenu.DragLayout;

public class TestActivity extends ActivityBase implements EventListener {
    private static DragLayout mDragLayout;
    public final static int INFOR_REFREFLASH = 100;
    private static long firstTime;


    private ImageView iv_recent_tips, iv_contact_tips, iv_diandi_tips;//消息提示
    private Button[] mTabs;
    private Button mDiandiBtn, mRecentBtn, mContanctBtn;
    private View currentButton;
    private DiandiFragment diandiFragment;
    private ContactFragment contactFragment;
    private RecentFragment recentFragment;
    private Fragment[] fragments;
    private int index;
    private int currentTabIndex;


    private View.OnClickListener diandiOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            diandiFragment= null;
            if (diandiFragment == null)
                diandiFragment = new DiandiFragment();
            ft.replace(R.id.fragment_container, diandiFragment, TAG);
            ft.commit();
            setButton(view);
        }
    };
    private View.OnClickListener recentOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            RecentFragment newsFatherFragment = null;
            if (newsFatherFragment == null)
                newsFatherFragment = new RecentFragment();
            ft.replace(R.id.fragment_container, newsFatherFragment, TAG);
            ft.commit();
            setButton(view);
        }
    };
    private View.OnClickListener contanctOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ContactFragment newsFatherFragment = new ContactFragment();
            ft.replace(R.id.fragment_container, newsFatherFragment, TAG);
            ft.commit();
            setButton(view);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findView();
        initView();
    }

    @Override
    void findView() {
        setContentView(R.layout.activity_main3);
        mDragLayout = (DragLayout) findViewById(R.id.main_drag);

        mDiandiBtn = (Button) findViewById(R.id.btn_diandi);
        mRecentBtn = (Button) findViewById(R.id.btn_message);
        mContanctBtn = (Button) findViewById(R.id.btn_contract);
        iv_diandi_tips = (ImageView) findViewById(R.id.iv_diandi_tips);
        iv_recent_tips = (ImageView) findViewById(R.id.iv_recent_tips);
        iv_contact_tips = (ImageView) findViewById(R.id.iv_contact_tips);
    }

    public DragLayout getDragLayout() {
        return mDragLayout;
    }

    @Override
    void initView() {
        bindEvent();
        mDiandiBtn.performClick();
        mDragLayout.setDragListener(new DragLayout.DragListener() {
            @Override
            public void onOpen() {

            }

            @Override
            public void onClose() {

            }

            @Override
            public void onDrag(float percent) {
                ViewHelper.setAlpha(diandiFragment.getUserAvatarImg(),1-percent);
            }
        });
    }

    @Override
    void bindEvent() {
        mDiandiBtn.setOnClickListener(diandiOnClickListener);
        mRecentBtn.setOnClickListener(recentOnClickListener);
        mContanctBtn.setOnClickListener(contanctOnClickListener);
    }

    private void setButton(View v) {
        if (currentButton != null && currentButton.getId() != v.getId()) {
            currentButton.setEnabled(true);
            currentButton.setSelected(false);
        }
        v.setEnabled(false);
        v.setSelected(true);
        currentButton = v;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //小圆点提示
        if (BmobDB.create(this).hasUnReadMsg()) {
            iv_recent_tips.setVisibility(View.VISIBLE);
        } else {
            iv_recent_tips.setVisibility(View.GONE);
        }
        if (BmobDB.create(this).hasNewInvite()) {
            iv_contact_tips.setVisibility(View.VISIBLE);
        } else {
            iv_contact_tips.setVisibility(View.GONE);
        }
        iv_diandi_tips.setVisibility(View.GONE);
        MyMessageReceiver.ehList.add(this);// 监听推送的消息
        //清空
        MyMessageReceiver.mNewNum = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyMessageReceiver.ehList.remove(this);
    }

    @Override
    public void onMessage(BmobMsg message) {
        // 声音提示
        boolean isAllow = CustomApplication.getInstance().getSpUtil().isAllowVoice();
        if (isAllow) {
            CustomApplication.getInstance().getMediaPlayer().start();
        }
        iv_recent_tips.setVisibility(View.VISIBLE);
        //保存接收到的消息-并发送已读回执给对方
        BmobChatManager.getInstance(this).saveReceiveMessage(true, message);
        if (currentTabIndex == 0) {
            //当前页面如果为会话页面，刷新此页面
            if (recentFragment != null) {
                recentFragment.refresh();
            }
        }

    }

    @Override
    public void onNetChange(boolean isNetConnected) {
        // TODO Auto-generated method stub
        if (isNetConnected) {
            ShowToast(R.string.network_tips);
        }
    }

    @Override
    public void onAddUser(BmobInvitation message) {
        // 声音提示
        boolean isAllow = CustomApplication.getInstance().getSpUtil().isAllowVoice();
        if (isAllow) {
            CustomApplication.getInstance().getMediaPlayer().start();
        }
        iv_contact_tips.setVisibility(View.VISIBLE);
        if (currentTabIndex == 1) {
            if (contactFragment != null) {
                contactFragment.refresh();
            }
        } else {
            String tickerText = message.getFromname() + "请求添加好友";
            boolean isAllowVibrate = CustomApplication.getInstance().getSpUtil().isAllowVibrate();
            BmobNotifyManager.getInstance(this).showNotify(isAllow, isAllowVibrate, R.drawable.icc_launcher, tickerText, message.getFromname(), tickerText, NewFriendActivity.class);
        }
    }

    @Override
    public void onOffline() {
        showOfflineDialog(this);
    }

    @Override
    public void onReaded(String conversionId, String msgTime) {
        // TODO Auto-generated method stub
    }

    /**
     * 连续按两次返回键就退出
     */
    @Override
    public void onBackPressed() {
        if (firstTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            ShowToast("再按一次退出程序");
        }
        firstTime = System.currentTimeMillis();
    }

}