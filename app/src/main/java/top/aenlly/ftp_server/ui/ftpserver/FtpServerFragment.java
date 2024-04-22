package top.aenlly.ftp_server.ui.ftpserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import top.aenlly.ftp_server.R;
import top.aenlly.ftp_server.cache.SharedPreferencesUtils;
import top.aenlly.ftp_server.constant.FtpConstant;
import top.aenlly.ftp_server.databinding.FragmentFtpServerBinding;
import top.aenlly.ftp_server.properties.FtpProperties;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class FtpServerFragment extends Fragment {


    private ActivityResultLauncher<Intent> launcher;

    private FragmentFtpServerBinding binding;

    private Context context;

    /**
     * FTP服务器广播接收器
     */
    private final BroadcastReceiver ftpServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 广播接收处理
            boolean isStart = intent.getBooleanExtra("success", false);
            if (isStart) {
                binding.tvTooltip.setText("已启用:" + FtpProperties.host + ":" + FtpProperties.port);
                binding.btnStart.setVisibility(View.GONE);
                binding.btnStop.setVisibility(View.VISIBLE);
                return;
            }
            binding.tvTooltip.setText("启动失败：请检查端口是否被占用和配置是否已经填写完整");
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFtpServerBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();
        SharedPreferencesUtils.init(context);
        bindCache();
        registerForActivityResult();
        registerForBtn();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 注册广播通知
        LocalBroadcastManager.getInstance(context).registerReceiver(ftpServerReceiver, new IntentFilter("ftp-server-service"));
    }

    void registerForBtn() {
        binding.etDataDir.setOnClickListener(view1 -> {
            // 启动Activity并处理结果
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            launcher.launch(intent);
        });
        Intent intent = new Intent(context, FtpServerService.class);
        binding.btnStart.setOnClickListener(view1 -> {
            initProperties();
            if (!verifyParameter()) {
                return;
            }
            context.startService(intent);
        });

        binding.btnStop.setOnClickListener(view1 -> {
            context.stopService(intent);
            binding.tvTooltip.setText("未启用");
            binding.btnStart.setVisibility(View.VISIBLE);
            binding.btnStop.setVisibility(View.GONE);
        });
    }


    /**
     * 注册查看活动结果，用于选择目录并填充路径到组件上显示，避免使用者输入错误
     */
    void registerForActivityResult() {
        // 在Activity中定义一个ActivityResultLauncher
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri treeUri = null;
                        if (result.getData() != null) {
                            treeUri = result.getData().getData();
                        }
                        File directory = Environment.getExternalStorageDirectory();
                        String directoryPath = treeUri.getPath(); // 这是选定的文件目录路径
                        directoryPath = directoryPath.replace("/tree/primary:", directory.getAbsolutePath() + "/");
                        // 将路径显示在EditText中或者做其他操作
                        binding.etDataDir.setText(directoryPath);
                    }
                });
    }


    /**
     * 获取本地 IP 地址
     *
     * @return {@link String}
     */
    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    // debug时使用10.9，实际应用使用192.168
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4 && inetAddress.toString()
                            .contains(Debug.isDebuggerConnected() ? "10.9" : "192.168")) {
                        // 找到了IPv4地址
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * init 属性
     */
    void initProperties() {
        FtpProperties.username = binding.etUsername.getText().toString();
        FtpProperties.password = binding.etPassword.getText().toString();
        FtpProperties.port = Integer.parseInt(binding.etPort.getText().toString());
        FtpProperties.remoteDirectory = binding.etDataDir.getText().toString();
        FtpProperties.encoding = binding.etEncoding.getText().toString();
        FtpProperties.host = getLocalIpAddress();
        flushedCache();
    }

    /**
     * 绑定缓存
     */
    @SuppressLint("ResourceAsColor")
    void bindCache() {
        binding.btnStart.setBackgroundColor(R.color.blue);
        binding.etPort.setText(SharedPreferencesUtils.getString(FtpConstant.PORT));
        binding.etDataDir.setText(SharedPreferencesUtils.getString(FtpConstant.REMOTE_DIRECTORY));
        binding.etUsername.setText(SharedPreferencesUtils.getString(FtpConstant.USER_NAME));
        binding.etPassword.setText(SharedPreferencesUtils.getString(FtpConstant.PASSWORD));
        binding.etEncoding.setText(SharedPreferencesUtils.getString(FtpConstant.ENCODING));
    }

    /**
     * 刷新缓存
     */
    void flushedCache() {
        SharedPreferencesUtils.putString(FtpConstant.PORT, binding.etPort.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.REMOTE_DIRECTORY, binding.etDataDir.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.USER_NAME, binding.etUsername.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.PASSWORD, binding.etPassword.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.ENCODING, binding.etEncoding.getText().toString());
    }

    boolean verifyParameter() {
        if (binding.etDataDir.getText().toString().isEmpty()) {
            binding.etDataDir.setError("请选择上传目录");
            return false;
        }
        if (binding.etUsername.getText().toString().isEmpty()) {
            binding.etUsername.setError("请填写账号");
            return false;
        }
        if (binding.etPassword.getText().toString().isEmpty()) {
            binding.etPassword.setError("请填写密码");
            return false;
        }
        if (binding.etPort.getText().toString().isEmpty()) {
            binding.etPort.setError("请填写端口");
            return false;
        }
        if (binding.etEncoding.getText().toString().isEmpty()) {
            binding.etEncoding.setError("请填写编码");
            return false;
        }
        if (FtpProperties.host == null) {
            Toast.makeText(context, "请先打开热点！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}