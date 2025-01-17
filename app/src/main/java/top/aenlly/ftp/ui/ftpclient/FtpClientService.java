package top.aenlly.ftp.ui.ftpclient;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import top.aenlly.ftp.properties.FtpClientProperties;

import java.io.IOException;

@Slf4j
public class FtpClientService  extends Service {
    private final IBinder binder = new FtpClientService.LocalBinder();

    private FTPClient ftpClient;

    private static final String FTP_CONNECT="ftp";
    private static final String SFTP_CONNECT="sftp";
    private static final String FTPS_CONNECT="ftps";

    public class LocalBinder extends Binder {
        FtpClientService getService() {
            // 返回当前服务实例，允许客户端调用服务中的公共方法
            return FtpClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startFtp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("FTP Client started");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void startFtp() throws IOException {

        boolean success = false;
        switch (FtpClientProperties.connect){
            case FTP_CONNECT:
                ftpClient=new FTPClient();
                break;
            case SFTP_CONNECT:
                ftpClient=new FTPSClient();
                break;
            default:
        }
        ftpClient.setControlEncoding(FtpClientProperties.encoding);
        ftpClient.connect(FtpClientProperties.host, FtpClientProperties.port);
        ftpClient.login(FtpClientProperties.username, FtpClientProperties.password);
        // 发送广播
        Intent intent = new Intent("ftp-server-client");
        intent.putExtra("success", success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
