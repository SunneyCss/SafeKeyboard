package com.kh.keyboard;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;

/**
 * @author shanshan.cao
 * @date 2018-06-8
 */
public class KhKeyboardView {
	private Activity     mContext;
	private View         parentView;
	/**
	 * 字母键盘view
	 */
	private KeyboardView mLetterView;
	/**
	 * 数字键盘View
	 */
	private KeyboardView mNumberView;
	/**
	 * 符号键盘View
	 */
	private KeyboardView mSymbolView;
	/**
	 * 数字键盘
	 */
	private Keyboard     mNumberKeyboard;
	/**
	 * 字母键盘
	 */
	private Keyboard     mLetterKeyboard;
	/**
	 * 符号键盘
	 */
	private Keyboard     mSymbolKeyboard;

	/**
	 * 是否数字键盘
	 */
	private       boolean isNumber = true;
	/**
	 * 是否大写
	 */
	public static boolean isUpper  = false;
	/**
	 * 是否是符号
	 */
	private       boolean isSymbol = false;
	private EditText mEditText;
	private View     headerView;

	public KhKeyboardView(Activity context, View view) {
		mContext = context;
		parentView = view;

		mNumberKeyboard = new Keyboard(mContext, R.xml.keyboard_numbers);
		mLetterKeyboard = new Keyboard(mContext, R.xml.keyboard_word);
		mSymbolKeyboard = new Keyboard(mContext, R.xml.keyboard_symbol);
		mNumberView = (KeyboardView) parentView.findViewById(R.id.keyboard_view_number);
		mLetterView = (KeyboardView) parentView.findViewById(R.id.keyboard_view_letter);
		mSymbolView = (KeyboardView) parentView.findViewById(R.id.keyboard_view_symbol);

		mLetterView.setKeyboard(mLetterKeyboard);
		mLetterView.setEnabled(true);
		mLetterView.setPreviewEnabled(true);
		mLetterView.setOnKeyboardActionListener(listener);

		mNumberView.setKeyboard(mNumberKeyboard);
		mNumberView.setEnabled(true);
		mNumberView.setPreviewEnabled(false);
		mNumberView.setOnKeyboardActionListener(listener);

		mSymbolView.setKeyboard(mSymbolKeyboard);
		mSymbolView.setEnabled(true);
		mSymbolView.setPreviewEnabled(false);
		mSymbolView.setOnKeyboardActionListener(listener);

		headerView = parentView.findViewById(R.id.keyboard_header);
	}

	private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
		@Override
		public void onPress(int primaryCode) {
			Log.d("primaryCode", "onPress--" + primaryCode);
			//这里全部设置为false,应为使用PoppupWindow显示自定义键盘后，点击若在上面再显示一个浮层，会报错闪退
			if (primaryCode == Keyboard.KEYCODE_SHIFT) {
				List<Keyboard.Key> keyList = mLetterKeyboard.getKeys();
				mLetterView.setPreviewEnabled(false);
			} else if (primaryCode == Keyboard.KEYCODE_DELETE) {
				mLetterView.setPreviewEnabled(false);
			} else if (primaryCode == 32 || primaryCode == -2 || primaryCode == 90001) {
				mLetterView.setPreviewEnabled(false);
			} else {
				mLetterView.setPreviewEnabled(false);
			}
		}

		@Override
		public void onRelease(int primaryCode) {
			Log.d("primaryCode", "onRelease--" + primaryCode);
		}

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			Log.d("primaryCode", "onKey--" + primaryCode);
			try {
				if (mEditText == null) {
					return;
				}
				Editable editable = mEditText.getText();
				int start = mEditText.getSelectionStart();
				if (primaryCode == Keyboard.KEYCODE_CANCEL) {
					// 隐藏键盘
					hideKeyboard();
				} else if (primaryCode == Keyboard.KEYCODE_DELETE || primaryCode == -35) {

					// 回退键,删除字符
					if (editable != null && editable.length() > 0) {
						if (start > 0) {
							editable.delete(start - 1, start);
						}
					}
				} else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
					// 大小写切换
					changeKeyBoard();
					mLetterView.setKeyboard(mLetterKeyboard);
				} else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
					// 数字与字母键盘互换
					if (isNumber) {
						showLetterView();
					} else {
						showNumberView();
					}
				} else if (primaryCode == 90001) {
					//字母与符号切换
					if (isSymbol) {
						showLetterView();
					} else {
						showSymbolView();
					}
				} else {
					// 输入键盘值
					editable.insert(start, Character.toString((char) primaryCode));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onText(CharSequence text) {

		}

		@Override
		public void swipeLeft() {

		}

		@Override
		public void swipeRight() {

		}

		@Override
		public void swipeDown() {

		}

		@Override
		public void swipeUp() {

		}
	};

	/**
	 * 字母-符号,显示符号
	 */
	private void showSymbolView() {
		try {
			if (mLetterView != null && mNumberView != null && mSymbolView != null) {
				isSymbol = true;
				isNumber = false;
				mSymbolView.setVisibility(View.VISIBLE);
				mLetterView.setVisibility(View.INVISIBLE);
				mNumberView.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数字-字母,显示字母键盘
	 */
	private void showLetterView() {
		try {
			if (mLetterView != null && mNumberView != null && mSymbolView != null) {
				isNumber = false;
				isSymbol = false;
				mLetterView.setVisibility(View.VISIBLE);
				mNumberView.setVisibility(View.INVISIBLE);
				mSymbolView.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数字-字母, 显示数字键盘
	 */
	private void showNumberView() {
		try {
			if (mLetterView != null && mNumberView != null && mSymbolView != null) {
				isNumber = true;
				isSymbol=false;
				mNumberView.setVisibility(View.VISIBLE);
				mLetterView.setVisibility(View.INVISIBLE);
				mSymbolView.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 切换大小写
	 */
	private void changeKeyBoard() {
		List<Keyboard.Key> keyList = mLetterKeyboard.getKeys();
		if (isUpper) {
			// 大写切换小写
			isUpper = false;
			for (Keyboard.Key key : keyList) {
				Drawable icon = key.icon;
				if (key.label != null && isLetter(key.label.toString())) {
					key.label = key.label.toString().toLowerCase();
					key.codes[0] = key.codes[0] + 32;
				}
			}
		} else {
			// 小写切换成大写
			isUpper = true;
			for (Keyboard.Key key : keyList) {
				if (key.label != null && isLetter(key.label.toString())) {
					key.label = key.label.toString().toUpperCase();
					key.codes[0] = key.codes[0] - 32;
				}
			}
		}
	}

	/**
	 * 判断是否是字母
	 */
	private boolean isLetter(String str) {
		String wordStr = "abcdefghijklmnopqrstuvwxyz";
		return wordStr.contains(str.toLowerCase());
	}

	public void hideKeyboard() {
		try {
			int visibility = mLetterView.getVisibility();
			if (visibility == View.VISIBLE) {
				headerView.setVisibility(View.GONE);
				mLetterView.setVisibility(View.GONE);
			}
			visibility = mNumberView.getVisibility();
			if (visibility == View.VISIBLE) {
				headerView.setVisibility(View.GONE);
				mNumberView.setVisibility(View.GONE);
			}
			visibility = mSymbolView.getVisibility();
			if (visibility == View.VISIBLE) {
				headerView.setVisibility(View.GONE);
				mSymbolView.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示键盘
	 */
	public void showKeyboard(EditText editText) {
		try {
			this.mEditText = editText;
			int inputText = mEditText.getInputType();
			headerView.setVisibility(View.VISIBLE);
			switch (inputText) {
				case InputType.TYPE_CLASS_NUMBER:
				case InputType.TYPE_CLASS_PHONE:
				case InputType.TYPE_NUMBER_FLAG_DECIMAL:
					showNumberView();
					break;
				default:
					showLetterView();
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
