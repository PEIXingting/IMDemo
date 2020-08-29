package hk.edu.cuhk.ie.iems5722.group5;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {
    private Socket socket;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextInputLayout passwordTextInput;
    private TextInputLayout usernameTextInput;
    private TextInputLayout registerUsernameTextInput;
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        usernameTextInput = findViewById(R.id.username_text_login);
        usernameEditText = findViewById(R.id.username_edit_login);
        passwordTextInput = findViewById(R.id.password_text_login);
        passwordEditText = findViewById(R.id.password_edit_login);
        MaterialButton loginButton = findViewById(R.id.login_button);
        MaterialButton registerButton = findViewById(R.id.register_button);

        try {
            socket = IO.socket("http://34.229.144.199:8000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

//        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on("login", onLogin);
        socket.on("registerResult", onRegisterResult);
        socket.connect();



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });




        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login(usernameEditText.getText(), passwordEditText.getText());
            }
        });

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernameTextInput.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    passwordTextInput.setError(null);
                }
                return false;
            }
        });



    }

    @Override
    protected void onDestroy() {
        if(socket != null) {
            socket.disconnect();
            socket.off();
        }
        super.onDestroy();
    }

    private void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.register,null,false);
        dialog = new AlertDialog.Builder(this).setView(view).create();
        MaterialButton submitButton = view.findViewById(R.id.submit_button);
        MaterialButton cancelButton = view.findViewById(R.id.cancel_button);
        registerUsernameTextInput = view.findViewById(R.id.username_text_register);
        final TextInputEditText registerUsernameEditText = view.findViewById(R.id.username_edit_register);
        final TextInputLayout registerPasswordTextInput = view.findViewById(R.id.password_text_register);
        final TextInputEditText registerPasswordEditText = view.findViewById(R.id.password_edit_register);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username_register = registerUsernameEditText.getText().toString();
                String password_register = registerPasswordEditText.getText().toString();
                if (username_register.equals("")) {
                    registerUsernameTextInput.setError(getString(R.string.register_null_username));
                } else if (password_register.equals("")) {
                    registerPasswordTextInput.setError(getString(R.string.register_null_password));
                } else {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", username_register);
                        data.put("password", password_register);
                        socket.emit("register", data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();


        registerUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registerUsernameTextInput.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerPasswordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    registerPasswordTextInput.setError(null);
                }
                return false;
            }
        });
    }



    private void Login(@Nullable Editable username, @Nullable Editable password) {
        String Username = username.toString();
        String Password = password.toString();
        if (Username.equals("")) {
            usernameTextInput.setError(getString(R.string.login_null_username));
        } else if (Password.equals("")) {
            passwordTextInput.setError(getString(R.string.login_null_password));
        } else {
            JSONObject data = new JSONObject();
            try {
                data.put("username", Username);
                data.put("password", Password);
                socket.emit("login", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                String result = data.getString("result");
                if (result.equals("Login Success")) {
                    final String username = data.getString("username");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext()  ,"Login Success", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, ChatroomActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);
                            finish();
                        }
                    });
                } else if (result.equals("Username Does not Exist")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usernameTextInput.setError(getString(R.string.not_exist_user_name));
                        }
                    });
                } else if (result.equals("Password Wrong")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            passwordTextInput.setError(getString(R.string.login_error_password));

                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onRegisterResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                String result = data.getString("result");
                if (result.equals("Register Success")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Register Success", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                } else if (result.equals("User Already Exists")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            registerUsernameTextInput.setError(getString(R.string.register_error_username));
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}

