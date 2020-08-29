package hk.edu.cuhk.ie.iems5722.group5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatroomActivity extends AppCompatActivity {
    private Socket socket;
    private String username;
    private AlertDialog dialog_join;
    private AlertDialog dialog_create;
    private TextInputLayout joinChatroomNameText;
    private TextInputLayout joinChatroomPasswordText;
    private TextInputLayout createChatroomNameText;
    private TextInputLayout createChatroomPasswordText;
    private ArrayList<ChatRoom> mChatRoom;
    private ListView listView;
    private ChatroomAdapter chatroomAdapter;

    private class ChatroomsGet extends AsyncTask<String, Integer, ArrayList<ChatRoom>> {

        @Override
        protected ArrayList<ChatRoom> doInBackground(String... strings) {
            return getChatrooms(strings[0], strings[1]);
        }
        @Override
        protected void onPostExecute(ArrayList<ChatRoom> chatroom) {
            super.onPostExecute(chatroom);
            mChatRoom = chatroom;
            chatroomAdapter = new ChatroomAdapter(ChatroomActivity.this, mChatRoom);
            listView.setAdapter(chatroomAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatrooms);
        Toolbar toolbar = findViewById(R.id.chatrooms_toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        String defaultURL = "http://34.229.144.199/api/get_chatrooms?username=" + username;
        try {
            socket = IO.socket("http://34.229.144.199:8001");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        socket.on("connect_result", onConnectResult);


        socket.connect();
//        socket.emit("user_connect", username);
        socket.on("new message", onNewMessage);
        socket.on("join", onJoin);
        socket.on("create", onCreate);
        listView = findViewById(R.id.chatrooms_listView);
        mChatRoom = new ArrayList<>();
        ChatroomsGet chatroomsGet = new ChatroomsGet();
        chatroomsGet.execute(defaultURL,username);


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.group_add:
                        joinDialog();
                        break;
                    case R.id.add:
                        createDialog();
                        break;
                    case R.id.exit:
                        Intent exit = new Intent(ChatroomActivity.this, LoginActivity.class);
                        startActivity(exit);
                        overridePendingTransition(R.anim.return_enter, R.anim.return_exit);
                        if (socket != null) {
                            socket.disconnect();
                            socket.off();
                        }
                        finish();
                }
                return true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatrooms_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void joinDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.join_chatrooms, null, false);
        dialog_join = new AlertDialog.Builder(this).setView(view).create();
        MaterialButton joinButton = view.findViewById(R.id.join_chatrooms_join_button);
        MaterialButton cancelButton = view.findViewById(R.id.join_chatrooms_cancel_button);
        joinChatroomNameText = view.findViewById(R.id.chatroom_name_text_join);
        joinChatroomPasswordText = view.findViewById(R.id.chatroom_password_text_join);
        final TextInputEditText joinChatroomNameEdit = view.findViewById(R.id.chatroom_name_edit_join);
        final TextInputEditText joinChatroomPasswordEdit = view.findViewById(R.id.chatroom_password_edit_join);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chatroom_name_join = joinChatroomNameEdit.getText().toString();
                String chatroom_password_join = joinChatroomPasswordEdit.getText().toString();
                if (chatroom_name_join.equals("")) {
                    joinChatroomNameText.setError(getString(R.string.null_chatroom_name));
                } else if (chatroom_password_join.equals("")) {
                    joinChatroomPasswordText.setError(getString(R.string.null_chatroom_password));
                } else {
                    JSONObject data = new JSONObject();
                    try{
                        data.put("username", username);
                        data.put("name", chatroom_name_join);
                        data.put("password", chatroom_password_join);
                        socket.emit("join", data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        joinChatroomNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                joinChatroomNameText.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        joinChatroomPasswordEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    joinChatroomPasswordText.setError(null);
                }
                return false;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_join.dismiss();
            }
        });
        dialog_join.show();
    }

    private void createDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.create_chatrooms, null, false);
        dialog_create = new AlertDialog.Builder(this).setView(view).create();
        MaterialButton createButton = view.findViewById(R.id.create_chatrooms_create_button);
        MaterialButton cancelButton = view.findViewById(R.id.create_chatrooms_cancel_button);
        createChatroomNameText = view.findViewById(R.id.chatroom_name_text_create);
        createChatroomPasswordText = view.findViewById(R.id.chatroom_password_text_create);
        final TextInputEditText createChatroomNameEdit = view.findViewById(R.id.chatroom_name_edit_create);
        final TextInputEditText createChatroomPasswordEdit = view.findViewById(R.id.chatroom_password_edit_create);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chatroom_name_create = createChatroomNameEdit.getText().toString();
                String chatroom_password_create = createChatroomPasswordEdit.getText().toString();
                if (chatroom_name_create.equals("")) {
                    createChatroomNameText.setError(getString(R.string.null_chatroom_name));
                } else if (chatroom_password_create.equals("")) {
                    createChatroomPasswordText.setError(getString(R.string.null_chatroom_password));
                } else {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", username);
                        data.put("name", chatroom_name_create);
                        data.put("password", chatroom_password_create);
                        socket.emit("create", data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        createChatroomNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                createChatroomNameText.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        createChatroomPasswordEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    createChatroomPasswordText.setError(null);
                }
                return false;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_create.dismiss();
            }
        });
        dialog_create.show();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject)args[0];
                int type = data.getInt("type");
                final String chatroom = data.getString("chatroom");
                final String mUsername = data.getString("username");
                String tmp = data.getString("message");
                if (!mUsername.equals(username)) {
                    if (type == 0) {
                        final String message = mUsername + ": " + tmp;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notification(chatroom, message, mUsername);
                            }
                        });
                    } else if (type == 1) {
                        final String message = mUsername + ": My Location";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notification(chatroom, message, mUsername);
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

//    private Emitter.Listener onConnectResult = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                JSONArray result = data.getJSONArray("result");
//                mChatRoom = new ArrayList<>();
//                for (int i = 0; i < result.length(); i++) {
//                    String name = result.getJSONObject(i).getString("name");
//                    mChatRoom.add(new ChatRoom(name, username));
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            chatroomAdapter = new ChatroomAdapter(ChatroomActivity.this, mChatRoom);
//                            listView.setAdapter(chatroomAdapter);
//                        }
//                    });
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//    };

    private Emitter.Listener onCreate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String result = data.getString("result");
                if (result.equals("Create Success")) {
                    String name = data.getString("name");
                    mChatRoom.add(new ChatRoom(name, username));
                    socket.emit("connectToBroadcast", name);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatroomAdapter.notifyDataSetChanged();
                            listView.setSelection(mChatRoom.size()-1);
                            Toast.makeText(getApplicationContext(), "Create Success", Toast.LENGTH_SHORT).show();
                            dialog_create.dismiss();
                        }
                    });
                } else if (result.equals("Chatroom Already Exists")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createChatroomNameText.setError(getString(R.string.exist_chatroom_name));
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                String result = data.getString("result");
                if (result.equals("Join Success")) {
                    String name = data.getString("name");
                    mChatRoom.add(new ChatRoom(name, username));
                    socket.emit("connectToBroadcast", name);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatroomAdapter.notifyDataSetChanged();
                            listView.setSelection(mChatRoom.size()-1);
                            Toast.makeText(getApplicationContext(), "Join Success", Toast.LENGTH_SHORT).show();
                            dialog_join.dismiss();
                        }
                    });
                } else if (result.equals("Password Wrong")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            joinChatroomPasswordText.setError(getString(R.string.wrong_chatroom_password));
                        }
                    });
                } else if (result.equals("Chatroom Does Not Exist")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            joinChatroomNameText.setError(getString(R.string.not_exist_chatroom_name));
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public ArrayList<ChatRoom>getChatrooms(String inputUrl, String mUsername) {
        ArrayList<ChatRoom> downloadChatrooms = new ArrayList<>();
        try {
            URL url = new URL(inputUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                stringBuilder.append(tmp);
            }
            bufferedReader.close();
            inputStream.close();
            inputStreamReader.close();
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            String status = jsonObject.getString("status");
            JSONArray data = jsonObject.getJSONArray("data");
            if (status.equals("OK")) {
                for (int i = 0; i < data.length(); i++) {
                    String name = data.getJSONObject(i).getString("name");
                    socket.emit("connectToBroadcast", name);
                    downloadChatrooms.add(new ChatRoom(name, mUsername));
                }
            } else {
                return downloadChatrooms;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return downloadChatrooms;
    }
    public void notification(String chatroom, String message, String username) {
        String id = "channel_01";
        String name = "channel_name";
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new Notification.Builder(this, id)
                    .setChannelId(id)
                    .setContentTitle(chatroom)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.baseline_notifications_24).build();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id)
                    .setContentTitle(chatroom)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.baseline_notifications_24)
                    .setOngoing(true)
                    .setChannelId(id);
            notification = builder.build();
        }
        notificationManager.notify(1, notification);
    }
}

