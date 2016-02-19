package com.javabaas.sample;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.javabaas.JBObject;
import com.javabaas.callback.LoginCallback;
import com.javabaas.callback.RequestCallback;
import com.javabaas.callback.SaveCallback;
import com.javabaas.callback.SignUpCallback;
import com.javabaas.JBUser;
import com.javabaas.exception.JBException;

public class UserActivity extends AppCompatActivity {

    private EditText nicknameEt;
    private EditText passwordEt;
    private EditText usernameEt;
    private EditText loginUsernameEt;
    private EditText loginPasswordEt;
    private TextView userInfoTv;
    private View registerLayout;
    private View loginLayout;
    private View userInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        nicknameEt = (EditText) findViewById(R.id.nickname_et);
        passwordEt = (EditText) findViewById(R.id.password_et);
        usernameEt = (EditText) findViewById(R.id.username_et);

        loginUsernameEt = (EditText) findViewById(R.id.login_username_et);
        loginPasswordEt = (EditText) findViewById(R.id.login_password_et);

        registerLayout = findViewById(R.id.register_layout);
        loginLayout = findViewById(R.id.login_layout);
        userInfoLayout = findViewById(R.id.user_info_layout);

        userInfoTv = (TextView) findViewById(R.id.user_info_tv);
        setUserInfo();
    }

    private void setUserInfo(){
        JBUser currentUser = JBUser.getCurrentUser();
        String t = "";
        if (currentUser != null){
            loginLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.GONE);
            for (String s : currentUser.keySet()) {
                t = t + s +" : "+(currentUser.get(s) + "\n");
            }
            userInfoLayout.setVisibility(View.VISIBLE);
        }else {
            loginLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.VISIBLE);
            userInfoLayout.setVisibility(View.GONE);
        }
        userInfoTv.setText(t);
    }

    public void onSignUp(View view) {
        JBUser jbUser = new JBUser();
        jbUser.setUsername(usernameEt.getText().toString());
        jbUser.setPassword(passwordEt.getText().toString());
        jbUser.put("nickName" , nicknameEt.getText().toString());
        jbUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(JBUser jbUser) {
                MainActivity.showToast(UserActivity.this , "注册成功 "+jbUser.getId());
                setUserInfo();
            }

            @Override
            public void error(JBException e) {
                MainActivity.showToast(UserActivity.this , "注册失败 "+e.getResponseErrorMsg());
                e.printStackTrace();
            }
        });
    }

    public void onLogin(View view) {
        String password = loginPasswordEt.getText().toString();
        String username = loginUsernameEt.getText().toString();
        JBUser.loginWithUsernameInBackground(username, password, new LoginCallback() {
            @Override
            public void done(JBUser jbUser) {
                MainActivity.showToast(UserActivity.this , "登录成功  "+ jbUser.getId());
                setUserInfo();
            }

            @Override
            public void error(JBException e) {
                MainActivity.showToast(UserActivity.this , "登录失败  "+ e.getResponseErrorCode() +"   "+ e.getResponseErrorMsg());
            }
        });
    }

    public void onLogout(View view) {
        JBUser.logout();
        MainActivity.showToast(UserActivity.this , "退出成功");
        setUserInfo();
    }

    public void onChangePsw(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 70;
        params.rightMargin = 70;
        final EditText oldPswEt = new EditText(this);
        oldPswEt.setHint("旧密码");
        oldPswEt.setLayoutParams(params);
        final EditText newPswEt = new EditText(this);
        newPswEt.setHint("新密码");
        newPswEt.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(oldPswEt);
        layout.addView(newPswEt);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JBUser.updatePassword(oldPswEt.getText().toString(), newPswEt.getText().toString(), new RequestCallback() {
                            @Override
                            public void done() {
                                MainActivity.showToast(UserActivity.this , "修改成功");
                            }

                            @Override
                            public void error(JBException e) {
                                MainActivity.showToast(UserActivity.this , "修改失败 "+e.getResponseErrorMsg());
                            }
                        });
                    }
                })
                .setTitle("修改密码")
                .create();
        alertDialog.show();
    }

    public void onChangeNickname(View view) {
        final JBUser currentUser = JBUser.getCurrentUser();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 70;
        params.rightMargin = 70;
        final EditText oldPswEt = new EditText(this);
        oldPswEt.setHint(((String) currentUser.get("nickName")));
        oldPswEt.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(oldPswEt);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentUser.put("nickName" , oldPswEt.getText().toString());
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(JBObject object) {
                                setUserInfo();
                                MainActivity.showToast(UserActivity.this , "修改成功");
                            }

                            @Override
                            public void error(JBException e) {
                                MainActivity.showToast(UserActivity.this , "修改失败 "+ e.getResponseErrorMsg());
                            }
                        });
                    }
                })
                .setTitle("修改昵称")
                .create();
        alertDialog.show();
    }
}
