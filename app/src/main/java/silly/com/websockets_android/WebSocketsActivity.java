package silly.com.websockets_android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by SillySnnall on 2018/5/21.
 */

public class WebSocketsActivity extends Activity {

    private EditText edit;
    private Button button;
    private TextView text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_sockets);
        edit = findViewById(R.id.edit);
        button = findViewById(R.id.button);
        text = findViewById(R.id.text);
        init();
    }

    private WebSocketClient mSocketClient;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            text.setText(text.getText() + "\n" + msg.obj);
        }
    };

    private void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocketClient = new WebSocketClient(new URI("ws://192.168.100.168:2017/")) {
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
            }
        }).start();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSocketClient != null) {
                    mSocketClient.send(edit.getText().toString().trim());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocketClient != null) {
            mSocketClient.close();
        }
    }
}
