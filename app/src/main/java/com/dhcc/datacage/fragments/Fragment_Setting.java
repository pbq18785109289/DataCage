package com.dhcc.datacage.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.allen.supertextviewlibrary.SuperTextView;
import com.dhcc.datacage.R;
import com.dhcc.datacage.activity.LoginActivity;
import com.dhcc.datacage.activity.MainActivity;
import com.dhcc.datacage.activity.setting.UpdatePwd_Activity;
import com.dhcc.datacage.utils.DataCleanUtil;
import com.dhcc.datacage.utils.MyApp;
import com.dhcc.datacage.utils.SnackbarUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.content.Context.MODE_PRIVATE;
import static com.dhcc.datacage.R.mipmap.on;

/**
 * 设置的Fragment
 *
 * @author pengbangqin
 */
public class Fragment_Setting extends Fragment implements View.OnClickListener {
    @Bind(R.id.cb_autoLogin)
    CheckBox cbAutoLogin;
    @Bind(R.id.tv_updatePwd)
    SuperTextView tvUpdatePwd;
    @Bind(R.id.tv_clearCache)
    SuperTextView tvClearCache;
    @Bind(R.id.tv_closeLocate)
    SuperTextView tvCloseLocate;
    @Bind(R.id.tv_version_update)
    SuperTextView tvVersionUpdate;
    @Bind(R.id.tv_exit)
    SuperTextView tvExit;
    private SharedPreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_setting, null);
        ButterKnife.bind(this, view);
        sp= getActivity().getSharedPreferences("user",getActivity().MODE_PRIVATE);
        //设置自动登录的选中状态
        cbAutoLogin.setChecked(sp.getBoolean("AUTO_ISCHECK",false));
        getCacheSize();
        initGPS();
        return view;
    }

    /**
     * 获取缓存大小
     */
    private void getCacheSize() {
        try {
            String size= DataCleanUtil.getTotalCacheSize(getActivity());
            tvClearCache.setRightString(size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化GPS打开关闭控件信息
     */
    private void initGPS() {
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            tvCloseLocate.setRightString("GPS已打开");
        } else {
            tvCloseLocate.setRightString("GPS已关闭");
        }
    }

    @OnCheckedChanged(R.id.cb_autoLogin)
    public void OnCheckedChanged(CompoundButton buttonView, boolean isChecked){
        //更改自动登录状态
        sp.edit().putBoolean("AUTO_ISCHECK",isChecked).commit();
    }

    @OnClick({R.id.tv_updatePwd, R.id.tv_clearCache, R.id.tv_closeLocate, R.id.tv_version_update, R.id.tv_exit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_updatePwd://修改密码
                Intent i1 = new Intent(getActivity(), UpdatePwd_Activity.class);
                startActivity(i1);
                break;
            case R.id.tv_clearCache:
                showClearDialog();
                break;
            case R.id.tv_closeLocate:
                requestLocationPermission();
                break;
            case R.id.tv_version_update:
                showUpdateDialog();
                break;
            case R.id.tv_exit:
                showExitDialog();
                break;
        }
    }

    /**
     * 弹出版本更新对话框
     */
    private void showUpdateDialog() {
        new AlertDialog.Builder(getActivity())
//                .setIcon(R.mipmap.exit)
                .setTitle("提示")
                .setMessage("检测到有新的版本，是否更新？")
                .setNegativeButton("取消", null)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "更新中...", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }

    /**
     * 弹出确认清楚缓存对话框
     */
    private void showClearDialog() {
        new AlertDialog.Builder(getActivity())
//                .setIcon(R.mipmap.exit)
                .setTitle("确认清除缓存?")
//                .setMessage("确认清除缓存?")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            //清除操作
                            DataCleanUtil.clearAllCache(getActivity());
//                        DataClearManager.cleanApplicationData(getActivity(),"");
                            String size=DataCleanUtil.getTotalCacheSize(getActivity());
                            tvClearCache.setRightString(size);
//                        getCacheSize();
                            SnackbarUtils.Long(tvExit,"缓存清除成功").backColor(getColorPrimary()).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).create().show();
    }
    /** 获取系统状态栏颜色 **/
    public int getColorPrimary(){
        return getResources().getColor(R.color.colorPrimary);
    }

    /**
     * 关闭GPS
     */
    private void closeGPS() {
        // 转到手机设置界面，用户设置GPS
        Intent intent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
    }

    /**
     * 弹出确认退出对话框
     */
    private void showExitDialog() {
        new AlertDialog.Builder(getActivity())
//                .setIcon(R.mipmap.exit)
                .setTitle("退出账号")
                .setMessage("确认退出账号?")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();//结束当前Activity
                        Intent startMain = new Intent(getActivity(), LoginActivity.class);
                        //是从设置中跳转到登录界面
                        MyApp.isExitLogin=true;
                        startMain.putExtra("isExitLogin",true);
                        startActivity(startMain);
                    }
              }).create().show();
    }

    /**
     * 申请SD卡权限，单个的。
     */
    private void requestLocationPermission() {
        AndPermission.with(this)
                .requestCode(100)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                .rationale(rationaleListener)
                .send();
    }

    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("友好提醒")
                    .setMessage("您已拒绝过定位权限，没有定位权限无法为您推荐附近妹子，请把定位权限赐给我吧！")
                    .setPositiveButton("好，给你", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.resume();
                        }
                    })
                    .setNegativeButton("我拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.cancel();
                        }
                    }).show();
        }
    };

    @PermissionYes(100)
    private void getLocationYes() {
        closeGPS();
        Toast.makeText(getActivity(), "获取定位权限成功", Toast.LENGTH_SHORT).show();
    }

    @PermissionNo(100)
    private void getLocationNo() {
        Toast.makeText(getActivity(), "获取定位权限失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 这个Activity中没有Fragment，这句话可以注释。
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();
        getCacheSize();
        initGPS();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
