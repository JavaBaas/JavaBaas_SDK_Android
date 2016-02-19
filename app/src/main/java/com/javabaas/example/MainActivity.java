package com.javabaas.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.javabaas.IUploader;
import com.javabaas.JBCloud;
import com.javabaas.JBFile;
import com.javabaas.JBQuery;
import com.javabaas.callback.CloudCallback;
import com.javabaas.callback.CountCallback;
import com.javabaas.callback.DeleteCallback;
import com.javabaas.callback.FileUploadCallback;
import com.javabaas.callback.FindCallback;
import com.javabaas.callback.RequestCallback;
import com.javabaas.callback.SaveCallback;
import com.javabaas.JBObject;
import com.javabaas.CustomResponse;
import com.javabaas.ResponseEntity;
import com.javabaas.exception.JBException;
import com.javabaas.util.Utils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean isSync = false;
    private EditText amountEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Switch) findViewById(R.id.switch1)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSync = isChecked;
            }
        });
        amountEt = (EditText) findViewById(R.id.amount);
        //initFile();
    }

    private void initFile(){
        JBFile.setUploader(new IUploader() {
            @Override
            public void upload(CustomResponse entity, final JBFile jbFile, final FileUploadCallback callback) {
                UploadManager uploadManager = new UploadManager();
                String name = "", token = "";
                try {
                    JSONObject jsonObject = new JSONObject(entity.getData());
                    JSONObject data = jsonObject.getJSONObject("data");
                    name = data.getString("name");
                    token = data.getString("token");
                } catch (JSONException e) {
                }
                if (Utils.isBlankString(token) || Utils.isBlankString(name))
                    return;

                UpCompletionHandler completionHandler = new UpCompletionHandler() {
                    @Override
                    public void complete(String name, ResponseInfo responseInfo, JSONObject jsonObject) {
                        //如果请求成功
                        if (responseInfo.isOK()) {
                            try {
                                JSONObject data = jsonObject.getJSONObject("data");
                                JSONObject file = data.getJSONObject("file");
                                if (file != null)
                                    jbFile.putAll(JSON.parseObject(file.toString() , JBObject.class));
                                if (callback != null)
                                    callback.done(jbFile);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            if (callback != null)
                                callback.error(null);
                        }
                    }
                };
                UpProgressHandler upProgressHandler = new UpProgressHandler(){

                    @Override
                    public void progress(String key, double percent) {
                        if (callback != null)
                            callback.onProgress(percent);
                    }
                };
                if (jbFile.getData() != null)
                    uploadManager.put(jbFile.getData(), name, token, completionHandler, new UploadOptions(null, null, false, upProgressHandler,
                            null));
                else if (jbFile.getFile() != null){
                    uploadManager.put(jbFile.getFile(), name, token, completionHandler, new UploadOptions(null, null, false, upProgressHandler,
                            null));
                }
            }
        });
    }

    public void onSave(View view) {
        final JBObject testC = new JBObject("testC");
        testC.put("testA", "测试A");
        testC.put("testB", "测试B");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        testC.save();
                        msg.obj = "同步保存对象成功 id=" + testC.getId();
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步保存对象失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else
            testC.saveInBackground(new SaveCallback() {
                @Override
                public void done(JBObject object) {
                    showToast(MainActivity.this ,"异步保存对象成功 id=" + testC.getId());
                }

                @Override
                public void error(JBException e) {
                    showToast(MainActivity.this ,"异步保存对象失败");
                    e.printStackTrace();
                }
            });
    }

    public void onDelete(View view) {
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        JBObject.deleteById("testC", "");
                        msg.obj = "同步删除成功";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步删除失败 "+e.getResponseErrorMsg();
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else
            JBObject.deleteByIdInBackground("testC", "4c14152e0fbf4aa08bfbfb0dd329a74d", new DeleteCallback() {
                @Override
                public void done() {
                    showToast(MainActivity.this , "异步删除成功");
                }

                @Override
                public void error(JBException e) {
                    showToast(MainActivity.this , "异步删除失败 "+e.getResponseErrorMsg());
                }
            });
    }

    public void onQuery(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    List<JBObject> list = null;
                    Message msg = Message.obtain();
                    try {
                        list = jbQuery.find();
                        msg.obj = "同步查询成功,共 " + list.size() + "个结果";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步查询失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else
            jbQuery.findInBackground(new FindCallback<JBObject>() {
                @Override
                public void done(List<JBObject> result) {
                    Toast.makeText(MainActivity.this, "异步查询成功共 " + result.size() + "个结果", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(JBException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "异步查询失败", Toast.LENGTH_SHORT).show();
                }
            });


    }

    public void onCount(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        jbQuery.whereEqualTo("testA", "测试A");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        int count = jbQuery.count();
                        msg.obj = "同步查询成功,共 " + count + "个";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步查询失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else {
            jbQuery.countInBackground(new CountCallback() {
                @Override
                public void done(int count) {
                    Toast.makeText(MainActivity.this, "异步查询成功,共 " + count + "个", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(JBException e) {
                    Toast.makeText(MainActivity.this, "异步查询失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onCloud(View view) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("episodeId", "1a23a933c4d24b04856046bb21b4af93");
        params.put("score", 8);
        JBCloud.callFunctionInBackground("addEpisodeScore", params, new CloudCallback() {
            @Override
            public void done(ResponseEntity responseEntity) {
                showToast(MainActivity.this , "调用成功 " + responseEntity.getMessage());
            }

            @Override
            public void error(JBException e, ResponseEntity responseEntity) {
                showToast(MainActivity.this , "调用失败 " + e.getMessage());
            }
        });
    }


    public void onUpload(View view) {
        JBFile jbFile = new JBFile(new File(Environment.getExternalStorageDirectory() , "share_pic.jpg"));
        jbFile.saveInBackground(new FileUploadCallback() {
            @Override
            public void done(JBFile jbFile) {
                System.out.println("上传成功   "+jbFile.getId());

            }

            @Override
            public void error(JBException e) {
                System.out.println("上传失败");

            }

            @Override
            public void onProgress(double percent) {
                System.out.println("上传  " + percent);
            }
        });
    }


    public void onDeleteByQuery(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        jbQuery.whereEqualTo("testA", "测试A");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        jbQuery.deleteAll();
                        msg.obj = "同步删除成功";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步删除失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else {
            jbQuery.deleteAllInBackground(new DeleteCallback() {
                @Override
                public void done() {
                    Toast.makeText(MainActivity.this, "异步删除成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(JBException e) {
                    Toast.makeText(MainActivity.this, "异步删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void onIncrement(View view) {
        final JBObject testC = JBObject.createWithoutData("testC", "928ca972d4e04912ab9ef6f1de214a02");
        String s = amountEt.getText().toString();
        final Integer amount ;
        if (TextUtils.isEmpty(s))
            amount = 1;
        else
            amount = Integer.parseInt(s);
        if (isSync){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        testC.incrementKey("testInt" , amount);
                        msg.obj = "同步自增"+amount+"成功";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步自增"+amount+"失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        }else {
            testC.incrementKeyInBackground("testInt", amount, new RequestCallback() {
                @Override
                public void done() {
                    showToast(MainActivity.this , "异步自增"+amount+"成功");
                }

                @Override
                public void error(JBException e) {
                    showToast(MainActivity.this ,"异步自增"+amount+"失败");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    public static Toast toast = null;

    public static void showToast(Context context ,String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }


    public void onSignUp(View view) {
        startActivity(new Intent(this , UserActivity.class));
    }

    public void onRegexQuery(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        jbQuery.whereMatches("testA" , "^z");
        //jbQuery.whereStartsWith("testA" , "张");
        //jbQuery.whereEndsWith("testA" , "四");
        //jbQuery.whereContains("testA" , "Z");
        jbQuery.findInBackground(new FindCallback<JBObject>() {
            @Override
            public void done(List<JBObject> result) {
                for (JBObject jbObject : result) {
                    System.out.println(((String) jbObject.get("testA")));
                }
            }

            @Override
            public void error(JBException e) {
                System.out.println(e.getResponseErrorCode());
            }
        });
    }
}
