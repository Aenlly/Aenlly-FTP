package top.aenlly.ftp_server;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import top.aenlly.ftp_server.cache.SharedPreferencesUtils;
import top.aenlly.ftp_server.constant.FtpConstant;
import top.aenlly.ftp_server.databinding.FragmentFtpServerBinding;
import top.aenlly.ftp_server.properties.FtpProperties;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FtpServerFragment} factory method to
 * create an instance of this fragment.
 */
public class FtpServerFragment extends Fragment {

    private FragmentFtpServerBinding binding;

    private FtpProperties ftpProperties;

    private FtpServer server;

    ActivityResultLauncher<Intent> launcher;

    private Context context;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFtpServerBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context=view.getContext();
        SharedPreferencesUtils.init(context);
        bindCache();
        registerForActivityResult();
        registerForBtn();
    }

    void registerForBtn() {
        binding.etDataDir.setOnClickListener(view1 -> {
            // 启动Activity并处理结果
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            launcher.launch(intent);
        });

        binding.btnStart.setOnClickListener(view1 -> {
            try {
                    initProp();
                    startFtp();
            } catch (FtpException e) {
                throw new RuntimeException(e);
            }
        });

        binding.btnStop.setOnClickListener(view1->{
            server.stop();
            binding.tvTooltip.setText("未启用");
            binding.btnStart.setVisibility(View.VISIBLE);
            binding.btnStop.setVisibility(View.GONE);
        });
    }

    /**
     * 注册查看活动结果
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
                        directoryPath = directoryPath.replace("/tree/primary:", directory.getAbsolutePath()+"/");
                        binding.etDataDir.setText(directoryPath);
                        // 将路径显示在EditText中或者做其他操作
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @SuppressLint("ResourceAsColor")
    private void startFtp() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        // 设置用户管理器
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());

        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName(ftpProperties.getUsername());
        user.setPassword(ftpProperties.getPassword());
        user.setHomeDirectory(ftpProperties.getRemoteDirectory());
        user.authorize(new WriteRequest());
        LinkedList<Authority> authorities = new LinkedList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        userManager.save(user);
        serverFactory.setUserManager(userManager);
        // 创建监听器
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(ftpProperties.getPort());
        factory.setServerAddress(ftpProperties.getHost());
        // 向服务器添加监听器
        serverFactory.addListener("default", factory.createListener());

        // 设置文件系统的字符编码为 UTF-8
        // 创建FTP服务器
        server = serverFactory.createServer();

        // 启动FTP服务器
        try {
            server.start();
            binding.tvTooltip.setText("已启用:" + ftpProperties.getHost() + ":" + ftpProperties.getPort());
            binding.btnStart.setVisibility(View.GONE);
            binding.btnStop.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            binding.tvTooltip.setText("启动失败：" + e.getMessage());
        }
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4 && inetAddress.toString().contains("192.168")) {
                        binding.btnStart.setClickable(true);
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
     * 初始化道具
     */
    void initProp() {
        ftpProperties = new FtpProperties();
        ftpProperties.setUsername(binding.etUsername.getText().toString());
        ftpProperties.setPassword(binding.etPassword.getText().toString());
        ftpProperties.setPort(Integer.parseInt(binding.etPort.getText().toString()));
        ftpProperties.setRemoteDirectory(binding.etDataDir.getText().toString());
        ftpProperties.setEncoding(binding.etEncoding.getText().toString());
        ftpProperties.setHost(getLocalIpAddress());
        flushedCache();
    }

    @SuppressLint("ResourceAsColor")
    void bindCache(){
        binding.btnStart.setBackgroundColor(R.color.blue);
        binding.etPort.setText(SharedPreferencesUtils.getString(FtpConstant.PORT));
        binding.etDataDir.setText(SharedPreferencesUtils.getString(FtpConstant.REMOTE_DIRECTORY));
        binding.etUsername.setText(SharedPreferencesUtils.getString(FtpConstant.USER_NAME));
        binding.etPassword.setText(SharedPreferencesUtils.getString(FtpConstant.PASSWORD));
        binding.etEncoding.setText(SharedPreferencesUtils.getString(FtpConstant.ENCODING));
    }

    void flushedCache(){
        SharedPreferencesUtils.putString(FtpConstant.PORT,binding.etPort.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.REMOTE_DIRECTORY, binding.etDataDir.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.USER_NAME, binding.etUsername.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.PASSWORD, binding.etPassword.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.ENCODING, binding.etEncoding.getText().toString());
    }
}