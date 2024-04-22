package top.aenlly.ftp_server.ui.ftpserver;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
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
import top.aenlly.ftp_server.command.STOR;
import top.aenlly.ftp_server.properties.FtpProperties;

import java.util.LinkedList;
import java.util.Map;
@Slf4j
public class FtpServerService extends Service {

    private FtpServer server;

    public FtpServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            startFtp();
        } catch (FtpException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
    }

    @SuppressLint("ResourceAsColor")
    private void startFtp() throws FtpException {

        boolean success = false;

        FtpServerFactory serverFactory = new FtpServerFactory();
        // 设置用户管理器
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());

        UserManager userManager = userManagerFactory.createUserManager();
        // 设置用户/密码/目录
        BaseUser user = new BaseUser();
        user.setName(FtpProperties.username);
        user.setPassword(FtpProperties.password);
        user.setHomeDirectory(FtpProperties.remoteDirectory);
        user.authorize(new WriteRequest());
        LinkedList<Authority> authorities = new LinkedList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        userManager.save(user);
        serverFactory.setUserManager(userManager);
        // 创建监听器
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(FtpProperties.port);
        factory.setServerAddress(FtpProperties.host);
        // 向服务器添加监听器
        serverFactory.addListener("default", factory.createListener());
        // 重写命令执行
        CommandFactoryFactory commandFactoryFactory = new CommandFactoryFactory();
        Map<String, Command> commandMap = commandFactoryFactory.getCommandMap();
        commandMap.put("STOR", new STOR(this.getApplicationContext()));

        serverFactory.setCommandFactory(commandFactoryFactory.createCommandFactory());

        // 设置文件系统的字符编码为 UTF-8

        // 创建FTP服务器
        server = serverFactory.createServer();

        // 启动FTP服务器
        try {
            server.start();
            success = true;
            // refreshTheAlbum();
        } catch (FtpServerConfigurationException | FtpException e) {
            log.error("Failed to start",e);
        }

        // 发送广播
        Intent intent = new Intent("ftp-server-service");
        intent.putExtra("success", success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}