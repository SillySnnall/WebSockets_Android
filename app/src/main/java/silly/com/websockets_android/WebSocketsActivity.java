package silly.com.websockets_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by SillySnnall on 2018/5/21.
 */

public class WebSocketsActivity extends Activity {

    private EditText edit;
    private EditText ip_eidt;
    private Button button;
    private Button qidong;
    private TextView text;
    private RadioGroup radiogroup;

    private String url = "全部";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_sockets);
        edit = findViewById(R.id.edit);
        ip_eidt = findViewById(R.id.ip_eidt);
        button = findViewById(R.id.button);
        qidong = findViewById(R.id.qidong);
        text = findViewById(R.id.text);
        radiogroup = findViewById(R.id.radiogroup);

        qidong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ip_eidt.getText().toString().trim().isEmpty() || mSocketClient != null){
                    return;
                }
                init();
            }
        });

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton viewById = findViewById(checkedId);
                url = viewById.getText().toString();
            }
        });
    }

    private WebSocketClient mSocketClient;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                String txt = msg.obj.toString();
                if (txt.contains("ip:")) {
                    txt = txt.replace("ip:", "");
                    radiogroup.removeAllViews();
                    RadioButton radioButton = new RadioButton(WebSocketsActivity.this);
                    radioButton.setText("全部");
                    radiogroup.addView(radioButton);
                    radioButton.setChecked(true);
                    if (txt.contains(",")) {
                        String[] split = txt.split(",");
                        for (int i = 0; i < split.length; i++) {
                            createRadioButton(split[i]);
                        }
                    } else {
                        createRadioButton(txt);
                    }
                } else {
                    text.setText(text.getText() + "\n" + txt);
                }
            }
        }
    };


    private void createRadioButton(String txt) {
        RadioButton radioButton = new RadioButton(WebSocketsActivity.this);
        radioButton.setText(txt);
        radiogroup.addView(radioButton);
    }

    private void init() {
        try {
            mSocketClient = new WebSocketClient(new URI("ws://" + ip_eidt.getText().toString().trim() + ":2017/")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("picher_log", "打开通道" + handshakedata.getHttpStatus());
                    handler.obtainMessage(0).sendToTarget();
                }

                @Override
                public void onMessage(String message) {
                    Log.d("picher_log", "接收消息" + message);
                    handler.obtainMessage(0, message).sendToTarget();
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("picher_log", "通道关闭");
                    handler.obtainMessage(0).sendToTarget();
                }

                @Override
                public void onError(Exception ex) {
                    Log.d("picher_log", "链接错误");
                }
            };
            mSocketClient.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSocketClient != null) {
                    if (url.equals("全部")) {
                        mSocketClient.send(edit.getText().toString().trim());
                    } else {
                        mSocketClient.send("ip:" + url + "," + edit.getText().toString().trim());
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocketClient != null) {
            mSocketClient.close();
            mSocketClient = null;
        }
    }
}
