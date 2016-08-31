package com.dou361.update.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.dou361.download.DownloadManager;
import com.dou361.download.ParamsManager;
import com.dou361.update.UpdateHelper;
import com.dou361.update.bean.Update;
import com.dou361.update.server.DownloadingService;
import com.dou361.update.util.FileUtils;
import com.dou361.update.util.InstallUtil;
import com.dou361.update.util.NetworkUtil;
import com.dou361.update.util.ResourceUtils;
import com.dou361.update.util.UpdateConstants;
import com.dou361.update.util.UpdateSP;

import java.io.File;

/**
 * ========================================
 * <p/>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p/>
 * 作 者：陈冠明
 * <p/>
 * 个人网站：http://www.dou361.com
 * <p/>
 * 版 本：1.0
 * <p/>
 * 创建日期：2016/6/17
 * <p/>
 * 描 述：
 * <p/>
 * <p/>
 * 修订历史：
 * <p/>
 * ========================================
 */
public class UpdateDialogActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private View jjdxm_update_wifi_indicator;
    private TextView jjdxm_update_content;
    private CheckBox jjdxm_update_id_check;
    private Button jjdxm_update_id_ok;
    private Button jjdxm_update_id_cancel;
    private Update mUpdate;
    private int mAction;
    private String mPath;
    private Context mContext;
    private String text;
    //是否已经下载完成
    private boolean finshDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        @LayoutRes int layoutId = UpdateSP.getDialogLayout();
        if (layoutId > 0) {
            setContentView(layoutId);
        } else {
            setContentView(ResourceUtils.getResourceIdByName(mContext, "layout", "jjdxm_update_dialog"));
        }
        Intent intent = getIntent();
        mUpdate = (Update) intent.getSerializableExtra(UpdateConstants.DATA_UPDATE);
        mAction = intent.getIntExtra(UpdateConstants.DATA_ACTION, 0);
        mPath = intent.getStringExtra(UpdateConstants.SAVE_PATH);
        String updateContent = null;
        jjdxm_update_wifi_indicator = findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_wifi_indicator"));
        jjdxm_update_content = (TextView) findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_content"));
        jjdxm_update_id_check = (CheckBox) findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_id_check"));
        jjdxm_update_id_ok = (Button) findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_id_ok"));
        jjdxm_update_id_cancel = (Button) findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_id_cancel"));
        if (jjdxm_update_wifi_indicator != null) {
            if (NetworkUtil.isConnectedByWifi()) {
                //WiFi环境
                jjdxm_update_wifi_indicator.setVisibility(View.INVISIBLE);
            } else {
                jjdxm_update_wifi_indicator.setVisibility(View.VISIBLE);
            }
        }
        finshDown = (DownloadManager.getInstance(mContext).state(mUpdate.getUpdateUrl()) == ParamsManager.State_FINISH);
        if (finshDown) {
            //已经下载
            if (TextUtils.isEmpty(mPath)) {
                String url = mUpdate.getUpdateUrl();
                mPath = DownloadManager.getInstance(mContext).getDownPath() + File.separator + url.substring(url.lastIndexOf("/") + 1, url.length());
            }
            if (mUpdate.getApkSize() > 0) {
                text = getText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_dialog_installapk")) + "";
            } else {
                text = "";
            }
        } else {
            if (mUpdate.getApkSize() > 0) {
                text = getText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_targetsize")) + FileUtils.HumanReadableFilesize(mUpdate.getApkSize());
            } else {
                text = "";
            }
        }
        if (jjdxm_update_id_check != null) {
            if (UpdateHelper.getInstance().getUpdateType() == UpdateHelper.UpdateType.checkupdate) {
                //手动更新
                jjdxm_update_id_check.setVisibility(View.GONE);
            } else {
                jjdxm_update_id_check.setVisibility(View.VISIBLE);
            }
        }
        if (mAction == 0) {
            updateContent = getText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_newversion"))
                    + mUpdate.getVersionName() + "\n"
                    + text + "\n\n"
                    + getText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_updatecontent")) + "\n" + mUpdate.getUpdateContent() +
                    "\n";
            jjdxm_update_id_ok.setText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_updatenow"));
            jjdxm_update_content.setText(updateContent);
        } else {
            finshDown = true;
            updateContent = getText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_newversion"))
                    + mUpdate.getVersionName() + "\n"
                    + text + "\n\n"
                    + getText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_updatecontent")) + "\n" + mUpdate.getUpdateContent() +
                    "\n";
            jjdxm_update_id_ok.setText(ResourceUtils.getResourceIdByName(mContext, "string", "jjdxm_update_installnow"));
            jjdxm_update_content.setText(updateContent);
        }
        if (jjdxm_update_id_check != null) {
            jjdxm_update_id_check.setOnCheckedChangeListener(this);
        }
        jjdxm_update_id_ok.setOnClickListener(this);
        jjdxm_update_id_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_id_ok")) {
            if (finshDown) {
                InstallUtil.installApk(mContext, mPath);
            } else {
                Intent intent = new Intent(mContext, DownloadingService.class);
                intent.putExtra(UpdateConstants.DATA_ACTION, UpdateConstants.START_DOWN);
                intent.putExtra(UpdateConstants.DATA_UPDATE, mUpdate);
                startService(intent);
            }
            finish();
        } else if (id == ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_id_cancel")) {
            if (UpdateSP.isForced()) {
                finish();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        UpdateSP.setIgnore(isChecked ? mUpdate.getVersionCode() + "" : "");
    }
}
