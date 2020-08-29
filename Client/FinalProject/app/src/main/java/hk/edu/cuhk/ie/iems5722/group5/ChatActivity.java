package hk.edu.cuhk.ie.iems5722.group5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {
    private ImageButton location;
    private ImageButton send;
    private EditText editText;
    private String mRoomName;
    private String mUsername;
    private String latng;
    private String locationName;
    private String address;
    private int selectLocation = 0;
    private ListView listView;
    private ArrayList<Messages> mMessages;
    private MessagesAdapter messagesAdapter;
    private Socket socket;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);




        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        listView = findViewById(R.id.chat_listview);
        final Intent intent = getIntent();

        mRoomName = intent.getStringExtra("name");
        mUsername = intent.getStringExtra("username");

        toolbar.setTitle(mRoomName);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);

        try {
            socket = IO.socket("http://34.229.144.199:8002");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on("update", onUpdate);
        socket.on("join", onJoin);
        socket.on("leave", onLeave);
        socket.connect();

        JSONObject data_join = new JSONObject();
        try {
            data_join.put("chatroom", mRoomName);
            data_join.put("username", mUsername);
            socket.emit("join", data_join);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        location = findViewById(R.id.add_location);
        send = findViewById(R.id.send_button);

        final EditText editText = findViewById(R.id.editText_chat);

        mMessages = new ArrayList<>();
        String defaultURL = "http://34.229.144.199/api/get_messages?chatroom_name=" + mRoomName + "&page=1";
        ChatTaskGet chatTaskGet = new ChatTaskGet();
        chatTaskGet.execute(defaultURL, mUsername);


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean isTop = false;
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == SCROLL_STATE_IDLE && isTop) {
                    int current_page = mMessages.get(0).getCurrent_page();
                    int total_page = mMessages.get(0).getTotal_page();

                    if (current_page < total_page) {
                        String URL = "http://34.229.144.199/api/get_messages?chatroom_name=" + mRoomName + "&page=" + (current_page+1);
                        ChatTaskGet chatTaskGet1 = new ChatTaskGet();
                        chatTaskGet1.execute(URL, mUsername);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                isTop = i == 0;
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString();

                int current_page;
                int total_page;

                if (!msg.isEmpty()) {
                    if (mMessages.isEmpty()) {
                        current_page = 1;
                        total_page = 1;
                    } else {
                        current_page = mMessages.get(0).getCurrent_page();
                        total_page = mMessages.get(0).getTotal_page();
                    }
                    Date date = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
                    String time = simpleDateFormat.format(date);
                    int type = 0;
                    mMessages.add(new Messages(type, mUsername, msg, time, current_page, total_page));
                    messagesAdapter.notifyDataSetChanged();
                    listView.setSelection(mMessages.size()-1);
                    String submit = "type=0&chatroom=" + mRoomName + "&username=" + mUsername + "&message=" + msg + "&time=" + time;
                    ChatTaskPost chatTaskPost = new ChatTaskPost();
                    chatTaskPost.execute(submit);
                    editText.setText("");
                }

            }
        });


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatActivity.this  , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},1);
                } else {
                    Intent intent1 = new Intent(ChatActivity.this, LocationActivity.class);
                    intent1.putExtra("type", selectLocation);
//                    intent1.putExtra("chatroom", mRoomName);
//                    intent1.putExtra("username", mUsername);
                    startActivityForResult(intent1,200);
                    overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);
                }
            }
        });



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject data_leave = new JSONObject();
                try {
                    data_leave.put("chatroom", mRoomName);
                    data_leave.put("username", mUsername);
                    socket.emit("leave", data_leave);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (socket != null) {
                    socket.disconnect();
                    socket.off();
                }
                finish();

                overridePendingTransition(R.anim.return_enter, R.anim.return_exit);
            }
        });


    }
    @Override
    protected void onDestroy() {
        if (socket != null) {
            JSONObject data_leave = new JSONObject();
            try {
                data_leave.put("chatroom", mRoomName);
                data_leave.put("username", mUsername);
                socket.emit("leave", data_leave);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.disconnect();
            socket.off();
        }
        super.onDestroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent1 = new Intent(ChatActivity.this, LocationActivity.class);
                intent1.putExtra("type", selectLocation);
//                intent1.putExtra("chatroom", mRoomName);
//                intent1.putExtra("username", mUsername);
                startActivityForResult(intent1, 200);
                overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Need Permission", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200) {
                latng = data.getExtras().getString("latng");
                locationName = data.getExtras().getString("name");
                address = data.getExtras().getString("address");
                int type = 2;
                String msg_upload = "https://apis.map.qq.com/tools/poimarker?type=0!$marker=coord:" + latng + ";title:" + locationName + ";addr:" + address + "!$key=EOEBZ-FOHAW-A3GRM-OYLTD-BA3JV-H4BMU!$referer=myapp";
                String msg = "https://apis.map.qq.com/tools/poimarker?type=0&marker=coord:" + latng + ";title:" + locationName + ";addr:" + address + "&key=EOEBZ-FOHAW-A3GRM-OYLTD-BA3JV-H4BMU&referer=myapp";

                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
                String time = simpleDateFormat.format(date);
                int current_page = mMessages.get(0).getCurrent_page();
                int total_page = mMessages.get(0).getTotal_page();
                mMessages.add(new Messages(type, mUsername, msg, time, current_page, total_page));
                messagesAdapter.notifyDataSetChanged();
                listView.setSelection(mMessages.size()-1);
                String URL = "type=1&chatroom=" + mRoomName + "&username=" + mUsername + "&message=" + msg_upload + "&time=" + time ;
                ChatTaskPost chatTaskPost = new ChatTaskPost();
                chatTaskPost.execute(URL);

        }


//        Toast.makeText(getApplicationContext(), latng, Toast.LENGTH_SHORT).show();
    }

    private class ChatTaskGet extends AsyncTask<String, Integer, ArrayList<Messages>> {

        @Override
        protected ArrayList<Messages> doInBackground(String... strings) {
            return download(strings[0], strings[1]);
        }
        @Override
        protected void onPostExecute(ArrayList<Messages> messages) {
            super.onPostExecute(messages);
            Collections.reverse(messages);
            messages.addAll(mMessages);
            mMessages = messages;
            messagesAdapter = new MessagesAdapter(ChatActivity.this, mMessages);
            listView.setAdapter(messagesAdapter);
        }
    }

    private class ChatTaskPost extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            return upload(strings[0]);
        }
        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            if (!status.equals("OK")) {
                Toast.makeText(getApplicationContext(), "Failed to send", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static ArrayList<Messages> download(String inputUrl, String mUsername) {
        ArrayList<Messages> messagesDownload = new ArrayList<>();
        try {
            URL url = new URL(inputUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                stringBuilder.append(tmp);
            }
            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            String status = jsonObject.getString("status");
            JSONObject data = jsonObject.getJSONObject("data");
            int current_page = data.getInt("current_page");
            JSONArray messages = data.getJSONArray("messages");
            int total_page = data.getInt("total_pages");
            if (status.equals("OK")) {
                for (int i = 0; i < messages.length(); i++) {
                    String message = messages.getJSONObject(i).getString("message");
                    String username = messages.getJSONObject(i).getString("username");
                    String time = messages.getJSONObject(i).getString("time");
                    int temp = messages.getJSONObject(i).getInt("type");
                    if (temp == 0) {
                        if (username.equals(mUsername)) {
                            int type = 0;
                            messagesDownload.add(new Messages(type, username, message, time, current_page, total_page));
                        } else {
                            int type = 1;
                            messagesDownload.add(new Messages(type, username, message, time, current_page, total_page));
                        }
                    } else if (temp == 1) {
                        if (username.equals(mUsername)) {
                            int type = 2;
                            messagesDownload.add(new Messages(type, username, message, time, current_page, total_page));
                        } else {
                            int type = 3;
                            messagesDownload.add(new Messages(type, username, message, time, current_page, total_page));
                        }
                    }
                }
            } else {
                return messagesDownload;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return messagesDownload;
    }

    public static String upload(String params) {
        String status = null;
        try {
            URL url = new URL("http://34.229.144.199/api/send_message");
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Char-Set", "UTF-8");
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(params.getBytes());
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String tmp;
                while ((tmp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(tmp);
                }
                inputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                status = jsonObject.getString("status");
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    private Emitter.Listener onUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                final JSONObject data = (JSONObject)args[0];
                String update_username = data.getString("username");
                String update_chatroom = data.getString("chatroom");
                String update_time = data.getString("time");
                String update_message = data.getString("message");
                int i;
                final int update_type = data.getInt("type");
                if (update_type == 0) {
                    if (!update_username.equals(mUsername) && update_chatroom.equals(mRoomName)) {
                        i = 1;
                        mMessages.add(new Messages(i, update_username, update_message, update_time, -1, -1));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messagesAdapter.notifyDataSetChanged();
                                listView.setSelection(mMessages.size()-1);
                            }
                        });
                    }
                } else if (update_type == 1) {
                    if (!update_username.equals(mUsername) && update_chatroom.equals(mRoomName)) {
                        i = 3;
                        mMessages.add(new Messages(i, update_username, update_message, update_time, -1, -1));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messagesAdapter.notifyDataSetChanged();
                                listView.setSelection(mMessages.size()-1);
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject)args[0];
                String join_username = data.getString("username");
                String join_chatroom = data.getString("chatroom");
                int i = 4;
                String join_log = join_username + " joined";
                if (!join_username.equals(mUsername) && join_chatroom.equals(mRoomName)) {
                    mMessages.add(new Messages(i, null, join_log, null, -1, -1));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messagesAdapter.notifyDataSetChanged();
                            listView.setSelection(mMessages.size()-1);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onLeave = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject)args[0];
                String leave_username = data.getString("username");
                String leave_chatroom = data.getString("chatroom");
                int i = 4;
                String join_log = leave_username + " left";
                if (!leave_username.equals(mUsername) && leave_chatroom.equals(mRoomName)) {
                    mMessages.add(new Messages(i, null, join_log, null, -1, -1));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messagesAdapter.notifyDataSetChanged();
                            listView.setSelection(mMessages.size()-1);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
