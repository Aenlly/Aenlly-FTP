package top.aenlly.ftp_server.ui.ftpserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.Command;
import org.apache.ftpserver.command.CommandFactoryFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import top.aenlly.ftp_server.R;
import top.aenlly.ftp_server.cache.SharedPreferencesUtils;
import top.aenlly.ftp_server.command.STOR;
import top.aenlly.ftp_server.constant.FtpConstant;
import top.aenlly.ftp_server.databinding.FragmentFtpServerBinding;
import top.aenlly.ftp_server.properties.FtpProperties;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;

public class FtpServerFragment extends Fragment {

    private FtpProperties ftpProperties;

    private FtpServer server;

    ActivityResultLauncher<Intent> launcher;

    private FragmentFtpServerBinding binding;

    private Context context;

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


    void registerForBtn() {
        binding.etDataDir.setOnClickListener(view1 -> {
            // 启动Activity并处理结果
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            launcher.launch(intent);
        });

        binding.btnStart.setOnClickListener(view1 -> {
            try {
                initProperties();
                startFtp();
            } catch (FtpException e) {
                throw new RuntimeException(e);
            }
        });

        binding.btnStop.setOnClickListener(view1 -> {
            server.stop();
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


    @SuppressLint("ResourceAsColor")
    private void startFtp() throws FtpException {
        if (ftpProperties.getHost() == null) {
            Toast.makeText(context, "请先打开热点！", Toast.LENGTH_SHORT).show();
            return;
        }

        FtpServerFactory serverFactory = new FtpServerFactory();
        // 设置用户管理器
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());

        UserManager userManager = userManagerFactory.createUserManager();
        // 设置用户/密码/目录
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
        // 重写命令执行
        CommandFactoryFactory commandFactoryFactory = new CommandFactoryFactory();
        Map<String, Command> commandMap = commandFactoryFactory.getCommandMap();
        commandMap.put("STOR", new STOR(context));

        serverFactory.setCommandFactory(commandFactoryFactory.createCommandFactory());

        // 设置文件系统的字符编码为 UTF-8
        // 创建FTP服务器
        server = serverFactory.createServer();

        // 启动FTP服务器
        try {
            server.start();
            // refreshTheAlbum();
            binding.tvTooltip.setText("已启用:" + ftpProperties.getHost() + ":" + ftpProperties.getPort());
            binding.btnStart.setVisibility(View.GONE);
            binding.btnStop.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            binding.tvTooltip.setText("启动失败：" + e.getMessage());
        }
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
        ftpProperties = new FtpProperties();
        ftpProperties.setUsername(binding.etUsername.getText().toString());
        ftpProperties.setPassword(binding.etPassword.getText().toString());
        ftpProperties.setPort(Integer.parseInt(binding.etPort.getText().toString()));
        ftpProperties.setRemoteDirectory(binding.etDataDir.getText().toString());
        ftpProperties.setEncoding(binding.etEncoding.getText().toString());
        ftpProperties.setHost(getLocalIpAddress());
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

}