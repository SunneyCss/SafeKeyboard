package com.kh.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import android.widget.PopupWindow;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static android.os.Build.VERSION_CODES.HONEYCOMB_MR2;

/**
 * @author shanshan.cao.o
 * @date 2018-06-8
 */
public class KeyBoardDialogUtils implements View.OnClickListener {
	protected View           contentView;
	protected PopupWindow    popWindow;
	protected Activity       mContext;
	private   EditText       mEditText;
	private   KhKeyboardView keyboardUtil;

	private Window mWindow;
	private View   mDecorView;
	private View   mScrollView;

	/**
	 * 输入框在键盘被弹出时，要被推上去的距离
	 */
	private int mScrollDistance = 50;

	/**
	 * 屏幕宽高
	 */
	public static int screenWidth       = -1;
	public static int screenHeight      = -1;
	/**
	 * 不包含导航栏的高度
	 */
	public static int screenHeightNoNav = -1;
	/**
	 * 实际内容高度， 计算公式:屏幕高度-导航栏高度-电量栏高度
	 */
	public static int realContentHeight = -1;

	public static float density    = 1.0f;
	public static int   densityDpi = 160;

	public KeyBoardDialogUtils(Activity mContext) {
		try {
			this.mContext = mContext;
			this.mWindow = mContext.getWindow();
			this.mDecorView = this.mWindow.getDecorView();
			this.mScrollView = this.mWindow.findViewById(Window.ID_ANDROID_CONTENT);
			initScreenParams(mContext);
			initView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		if (popWindow == null) {
			contentView = LayoutInflater.from(mContext).inflate(R.layout.keyboard_key_board_popu, null);
			popWindow =
					new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			contentView.findViewById(R.id.keyboard_finish).setOnClickListener(this);
			contentView.findViewById(R.id.keyboard_back_hide).setOnClickListener(this);
		}
		popWindow.setFocusable(true);
		popWindow.setOutsideTouchable(true);
		popWindow.setAnimationStyle(R.style.keyboard_popupAnimation);
		popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (mScrollDistance > 0) {
					int temp = mScrollDistance;
					mScrollDistance = 0;
					if (null != mScrollView) {
						mScrollView.scrollBy(0, -temp);
					}
				}
				keyboardUtil.hideKeyboard();
				if (mEditText != null && mEditText.isFocused()) {
					mEditText.clearFocus();
					mEditText.setFocusable(false);
					mEditText.setFocusableInTouchMode(false);
				}
			}
		});
		try {
			if (keyboardUtil == null) {
				keyboardUtil = new KhKeyboardView(mContext, contentView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 隐藏系统键盘
	 */
	public void hideSystemSofeKeyboard(EditText editText) {
		int sdkInt = Build.VERSION.SDK_INT;
		if (sdkInt >= HONEYCOMB) {
			try {
				Class<EditText> cls = EditText.class;
				Method setShowSoftInputOnFocus;
				setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setShowSoftInputOnFocus.setAccessible(true);
				setShowSoftInputOnFocus.invoke(editText, false);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			editText.setInputType(InputType.TYPE_NULL);
		}
		// 如果软键盘已经显示，则隐藏
		InputMethodManager imm =
				(InputMethodManager) mContext.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public void show(final EditText editText) {
		if (popWindow == null || popWindow.isShowing()) {
			return;
		}
		mEditText = editText;
		editText.setFocusable(true);
		editText.setFocusableInTouchMode(true);
		editText.requestFocus();
		keyboardUtil.showKeyboard(editText);
		popWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
		popWindow.update();
		if (null != mDecorView && null != mScrollView) {
			int[] pos = new int[2];
			// 计算弹出的键盘的尺寸
			mEditText.getLocationOnScreen(pos);
			float height = dpToPx(mContext, 300);
			Rect outRect = new Rect();
			// 然后该View有个getWindowVisibleDisplayFrame()方法可以获取到程序显示的区域，
			mDecorView.getWindowVisibleDisplayFrame(outRect);
			// * 包括标题栏，但不包括状态栏。
			int screen = realContentHeight;
			// 获得view空间，也就是除掉标题栏,outRect.top表示状态栏（通知栏)
			mScrollDistance = (int) ((pos[1] + editText.getMeasuredHeight() - outRect.top) - (screen - height));
			if (mScrollDistance > 0) {
				mScrollView.scrollBy(0, mScrollDistance);
			}
		}
	}

	public void dismiss() {
		if (popWindow != null && popWindow.isShowing()) {
			popWindow.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		try {
			int i = v.getId();
			if (i == R.id.keyboard_finish) {
				dismiss();
			} else if (i == R.id.keyboard_back_hide) {
				dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initScreenParams(Context context) {
		DisplayMetrics dMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		display.getMetrics(dMetrics);

		screenWidth = dMetrics.widthPixels;
		screenHeight = dMetrics.heightPixels;
		density = dMetrics.density;
		densityDpi = dMetrics.densityDpi;

		screenHeightNoNav = screenHeight;

		int ver = Build.VERSION.SDK_INT;

		// 新版本的android 系统有导航栏，造成无法正确获取高度
		if (ver == HONEYCOMB_MR2) {
			try {
				Method mt = display.getClass().getMethod("getRealHeight");
				screenHeightNoNav = (Integer) mt.invoke(display);
			} catch (Exception e) {
			}
		} else if (ver > HONEYCOMB_MR2) {
			try {
				Method mt = display.getClass().getMethod("getRawHeight");
				screenHeightNoNav = (Integer) mt.invoke(display);
			} catch (Exception e) {
			}
		}

		realContentHeight = screenHeightNoNav - getStatusBarHeight(context);
	}

	/**
	 * 电量栏高度
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c;
		Object obj;
		Field field;
		int x;
		int sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return sbar;
	}

	/**
	 * dp转px
	 *
	 * @param dpValue dp值
	 * @return px值
	 */
	public static int dpToPx(Context context, final float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}









