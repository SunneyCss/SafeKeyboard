package com.kh.keyboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static android.os.Build.VERSION_CODES.HONEYCOMB_MR2;

/**
 * @author shanshan.cao.o
 * @date 2018/6/8
 */
public class CustomKeyboardEditText extends EditText implements View.OnClickListener {
	protected View           contentView;
	protected PopupWindow    popWindow;
	private   KhKeyboardView keyboardUtil;

	private Window mWindow;
	private View   mDecorView;
	private View   mScrollView;

	/**
	 * 输入框在键盘被弹出时，要被推上去的距离
	 */
	private int mScrollDistance = 50;

	/**
	 * 未知宽高
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

	public CustomKeyboardEditText(Context context) {
		super(context);
		initAttributes(context);
		initKeyboard(context);
	}

	public CustomKeyboardEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttributes(context);
		initKeyboard(context);
	}

	public CustomKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttributes(context);
		initKeyboard(context);
	}

	private void initAttributes(Context context) {
		initScreenParams(context);
		this.setLongClickable(false);
		this.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		removeCopyAbility();
		if (this.getText() != null) {
			this.setSelection(this.getText().length());
		}
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showKeyboard();
			}
		});
	}

	/**
	 * 键盘初始化
	 */
	private void initKeyboard(Context context) {
		if (popWindow == null) {
			contentView = LayoutInflater.from(context).inflate(R.layout.keyboard_key_board_popu, null);
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
				if (CustomKeyboardEditText.this.isFocused()) {
					CustomKeyboardEditText.this.clearFocus();
					CustomKeyboardEditText.this.setFocusable(false);
					CustomKeyboardEditText.this.setFocusableInTouchMode(false);
				}
			}
		});
		try {
			if (keyboardUtil == null) {
				keyboardUtil = new KhKeyboardView((Activity) context, contentView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hideKeyboard() {
		if (null != popWindow) {
			if (popWindow.isShowing()) {
				popWindow.dismiss();
			}
		}
	}

	/**
	 * 隐藏系统的软键盘
	 */
	private void hideSysInput() {
		if (this.getWindowToken() != null) {
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void showKeyboard() {
		if (popWindow == null || popWindow.isShowing()) {
			return;
		}
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.requestFocus();
		keyboardUtil.showKeyboard(this);
		popWindow.showAtLocation(this.mDecorView, Gravity.BOTTOM, 0, 0);
		popWindow.update();
		if (null != mDecorView && null != mScrollView) {
			int[] pos = new int[2];
			// 计算弹出的键盘的尺寸
			getLocationOnScreen(pos);
			float height = dpToPx(getContext(), 300);
			Rect outRect = new Rect();
			// 然后该View有个getWindowVisibleDisplayFrame()方法可以获取到程序显示的区域，
			// * 包括标题栏，但不包括状态栏。
			mDecorView.getWindowVisibleDisplayFrame(outRect);
			// 获得view空间，也就是除掉标题栏
			int screen = realContentHeight;
			// outRect.top表示状态栏（通知栏)
			mScrollDistance = (int) ((pos[1] + getMeasuredHeight() - outRect.top) - (screen - height));
			if (mScrollDistance > 0) {
				mScrollView.scrollBy(0, mScrollDistance);
			}
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.mWindow = ((Activity) getContext()).getWindow();
		this.mDecorView = this.mWindow.getDecorView();
		this.mScrollView = this.mWindow.findViewById(Window.ID_ANDROID_CONTENT);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		hideKeyboard();
		popWindow = null;
		keyboardUtil = null;
		mDecorView = null;
		mScrollView = null;
		mWindow = null;
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

	@TargetApi(HONEYCOMB)
	private void removeCopyAbility() {
		if (Build.VERSION.SDK_INT >= HONEYCOMB) {
			this.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					return false;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {

				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					return false;
				}
			});
		}
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

	@Override
	public void onClick(View v) {
		try {
			int i = v.getId();
			if (i == R.id.keyboard_finish) {
				hideKeyboard();
			} else if (i == R.id.keyboard_back_hide) {
				hideKeyboard();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
