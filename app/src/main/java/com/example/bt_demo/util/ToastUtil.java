package com.example.bt_demo.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * @ClassName：ToastUtil
 * @Description：TODO< 自定义Toast--解决多次触发累计显示问题>
 * @author：jgduan
 * @email：jgduan@iflytek.com
 * @date：2015年8月12日 上午11:26:45
 * @version：v1.0
 */
public class ToastUtil {
    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable mRunnable = new Runnable() {
        public void run() {
            mToast.cancel();
            mToast = null;
        }
    };

    /**
     * 显示Toast
     *
     * @param mContext
     * @param text
     * @param duration
     */
    public static void showToast(Context mContext, String text, int duration) {

        mHandler.removeCallbacks(mRunnable);
        if (mToast != null)
            mToast.setText(text);
        else
            mToast = Toast.makeText(mContext, text, duration);
        mHandler.postDelayed(mRunnable, duration);

        mToast.show();
    }

    /**
     * 显示Toast
     *
     * @param mContext
     * @param resId
     * @param duration
     */
    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }
}
