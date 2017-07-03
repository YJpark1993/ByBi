package com.example.bybi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;
    private Button mBtnLogin, mBtnLogout;
    private EditText mNameView;
    private EditText mPhoneView;
    private String kakaoNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mNameView = (EditText) findViewById(R.id.name);
        mPhoneView = (EditText) findViewById(R.id.phone);
        mBtnLogin = (Button) findViewById(R.id.loginBtn);
        mBtnLogout = (Button) findViewById(R.id.logoutBtn);

        UserProfile userProfile = UserProfile.loadFromCache();
        kakaoNickname = userProfile.getNickname();
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogout();
            }
        });
    }

    private void onClickLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                redirectLoginActivity();
            }
        });
    }
    protected void redirectLoginActivity() {       //세션 연결 성공 시 SignupActivity로 넘김
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
    private void attemptLogin() {
        // Store values at the time of the login attempt.
        Log.d("xxx", "attemptLogin start");
        String name = mNameView.getText().toString();
        String phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError("유효하지 않은 입력입니다.");
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError("유효하지 않은 입력입니다.");
            focusView = mPhoneView;
            cancel = true;
        }
        mAuthTask = new UserLoginTask(name, phone);
        mAuthTask.execute((Void) null);

    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mName;
        private final String mPhone;
        private String strUrl;
        private URL Url;
        private String strCookie;
        private String result;
        private BufferedWriter writer;

        UserLoginTask(String name, String phone) {
            mName = name;
            mPhone = phone;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            strUrl = "http://52.78.181.11/signup.php"; //탐색하고 싶은 URL이다.
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String content = executeClient();
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d("xxx", "onPostExecute function");
            Intent intent = new Intent(SignupActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }

        public String executeClient() {
            Log.d("xxx", "executeClient function");
            try {
                Url = new URL(strUrl); // URL화 한다.
                HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
                conn.setRequestMethod("POST"); //
                conn.setDoOutput(true); // 쓰기모드 지정
                conn.setDoInput(true); // 읽기모드 지정
                conn.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                conn.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
                conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                //--------------------------
                //   서버로 값 전송

                OutputStream outStream = conn.getOutputStream();
                Log.d("kakaoNick : ", ""+ kakaoNickname);
                writer = new BufferedWriter(new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(
                        "kakaoNick=" + kakaoNickname.trim() + "&name=" + mName + "&phone=" + mPhone); //요청 파라미터를 입력
                writer.flush();
                writer.close();
                outStream.close();
                strCookie = conn.getHeaderField("Set-Cookie"); //쿠키데이터 보관
                InputStream is = conn.getInputStream(); //input스트림 개방

                StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                result = builder.toString();
                Log.d("result : " , result);
            } catch (MalformedURLException | ProtocolException exception) {
                exception.printStackTrace();
            } catch (IOException io) {
                io.printStackTrace();
            }
            return result;
        }

    }
}
