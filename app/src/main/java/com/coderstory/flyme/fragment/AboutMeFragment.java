package com.coderstory.flyme.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.coderstory.flyme.R;
import com.coderstory.flyme.fragment.base.BaseFragment;
import com.coderstory.flyme.utils.Misc;
import com.coderstory.flyme.utils.SharedHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;


public class AboutMeFragment extends BaseFragment {

    private SharedHelper helper;
    private ProgressDialog dialog;
    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 0:
                    final androidx.appcompat.app.AlertDialog.Builder normalDialog = new androidx.appcompat.app.AlertDialog.Builder(getMContext());
                    normalDialog.setTitle("提示");
                    normalDialog.setMessage("请先授权应用ROOT权限");
                    normalDialog.setPositiveButton("确定",
                            (dialog, which) -> System.exit(0));
                    normalDialog.show();
                    super.handleMessage(msg);
                    break;
                case 1:
                    dialog = ProgressDialog.show(getMContext(), "检测ROOT权限", "请在ROOT授权弹窗中给与ROOT权限,\n如果长时间无反应则请检查ROOT程序是否被\"省电程序\"干掉");
                    dialog.show();
                    break;
                case 2:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.cancel();
                        helper.put("isRooted", true);
                    }
                    break;
                case 3:
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getMContext());
                    dialog.setTitle("提示");
                    dialog.setMessage("本应用尚未再Xposed中启用,请启用后再试...");
                    dialog.setPositiveButton("退出", (dialog12, which) -> {
                        System.exit(0);
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                    break;

                case 4:
                    if (msg.getData().get("value").equals("{\"error\":\"0\"}")) {
                        getEditor().putString("qq", msg.getData().get("qq").toString()).apply();
                        getEditor().putString("sn", msg.getData().get("sn").toString()).apply();
                        Toast.makeText(getMContext(), "绑定成功,重启应用生效", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getMContext(), "qq" + msg.getData().get("qq").toString() + "sn" + msg.getData().get("sn").toString(), Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        Toast.makeText(getMContext(), "绑定失败:\r\n" + JSON.parseObject(msg.getData().get("value").toString()).getOrDefault("error", msg.getData().get("value").toString()), Toast.LENGTH_LONG).show();
                    }
                    // 校验返回
                    break;
                case 5:
                    // 接口调用失败
                    Toast.makeText(getMContext(), "服务器连接失败", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public static String getSerialNumber() {

        List<String> result = Shell.SU.run("getprop ro.serialno");
        return result.get(0);

    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_about_me;
    }

    @Override
    protected void setUpView() {
        helper = new SharedHelper(getMContext());

        $(R.id.bt2).setOnClickListener(v -> {
            if (!joinQQGroup("k8v9MsMgZjsyUBhmL76_tnid2opGauic")) {
                Toast.makeText(getMContext(), "拉起手Q失败", Toast.LENGTH_LONG).show();
            }
        });

        refresh();

        if (helper.getString("qq", "").equals("") || helper.getString("sn", "").equals("")) {
            $(R.id.bt4).setOnClickListener(v -> openInputDialog());
            $(R.id.bt3).setOnClickListener(v -> {
                try {
                    //第二种方式：可以跳转到添加好友，如果qq号是好友了，直接聊天
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + 26735825;//uin是发送过去的qq号码
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            $(R.id.bt1).setOnClickListener(v -> Toast.makeText(getMContext(), "尚未激活会员,不可申请", Toast.LENGTH_LONG).show());
        } else {
            $(R.id.bt4).setVisibility(View.GONE);
            $(R.id.bt3).setVisibility(View.GONE);
            $(R.id.bt1).setOnClickListener(v -> {
                if (!joinQQGroup("dNIW3xRJ8YKTdsFcJBak3_cZ0AwTBdEn")) {
                    Toast.makeText(getMContext(), "拉起手Q失败", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void refresh() {

        ((TextView) $(R.id.mark)).setText("当前版本类型: " + (helper.getString("qq", "").equals("") || helper.getString("sn", "").equals("") ? "免费版" : "完整版"));
        ((TextView) $(R.id.qq)).setText("绑定QQ: " + helper.getString("qq", "无"));

    }

    /****************
     *
     * 发起添加群流程。群号：Flyme助手和Xposed交流(717515891) 的 key 为： Dj5VgtTIdGo8nuk8wyMnYaHydxMxD6Dl
     * 调用 joinQQGroup(Dj5VgtTIdGo8nuk8wyMnYaHydxMxD6Dl) 即可发起手Q客户端申请加群 Flyme助手和Xposed交流(717515891)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }


    private void openInputDialog() {
        final EditText inputServer = new EditText(getMContext());
        inputServer.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        inputServer.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        AlertDialog.Builder builder = new AlertDialog.Builder(getMContext());
        builder.setTitle("1.付费4.8元解锁\r\n2.输入你的绑定QQ号并点击解锁!!").setView(inputServer);
        builder.setPositiveButton("解锁全功能", (dialog, which) -> {
            String _sign = inputServer.getText().toString();
            if (!_sign.isEmpty()) {
                String sn = getSerialNumber();
                new Thread(new Check(_sign, sn)).start();
            } else {
                Toast.makeText(getMContext(), "QQ号不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();

    }


    class Check implements Runnable {

        String qq;
        String sn;

        public Check(String qq, String sn) {
            this.qq = qq;
            this.sn = sn;
        }

        @Override
        public void run() {
            String path = Misc.searchApi;
            try {
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("POST");

                //数据准备
                String data = "{\n" +
                        "    \"QQ\": \"" + qq + "\",\n" +
                        "    \"sn\": \"" + sn + "\",\n" +
                        "    \"isLogin\": 1\n" +
                        "}";
                //至少要设置的两个请求头
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", data.length() + "");

                //post的方式提交实际上是留的方式提交给服务器
                connection.setDoOutput(true);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(data.getBytes());

                //获得结果码
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    //请求成功
                    InputStream is = connection.getInputStream();

                    Message msg = new Message();
                    msg.arg1 = 4;
                    Bundle data2 = new Bundle();
                    data2.putString("value", dealResponseResult(is));
                    data2.putString("qq", qq);
                    data2.putString("sn", sn);
                    msg.setData(data2);
                    myHandler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.arg1 = 5;
                    myHandler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.arg1 = 5;
                myHandler.sendMessage(msg);
            }
        }

        public String dealResponseResult(InputStream inputStream) {
            String resultData;      //存储处理结果
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len = 0;
            try {
                while ((len = inputStream.read(data)) != -1) {
                    byteArrayOutputStream.write(data, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            resultData = new String(byteArrayOutputStream.toByteArray());
            return resultData;
        }
    }

}