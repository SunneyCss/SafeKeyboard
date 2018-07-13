package safekeyboard.ly.com.safekeyboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import com.kh.keyboard.KeyBoardDialogUtils;
import com.kh.keyboard.CustomKeyboardEditText;

public class MainActivity extends AppCompatActivity {

    private KeyBoardDialogUtils    keyBoardDialogUtils;
    private CustomKeyboardEditText et_custom;
    private EditText               et_sys;
    private EditText               et_sys_keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_custom = (CustomKeyboardEditText) findViewById(R.id.et_custom);
        et_sys = (EditText) findViewById(R.id.et_sys);
        et_sys_keyboard = (EditText) findViewById(R.id.et_sys_keyboard);
        keyBoardDialogUtils = new KeyBoardDialogUtils(this);
        et_sys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyBoardDialogUtils.show(et_sys);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(keyBoardDialogUtils!=null){
            keyBoardDialogUtils.dismiss();
        }
        super.onDestroy();
    }
}
