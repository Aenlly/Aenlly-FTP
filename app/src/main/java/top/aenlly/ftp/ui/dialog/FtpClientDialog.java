package top.aenlly.ftp.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import org.apache.commons.net.ftp.FTPClient;
import top.aenlly.ftp.R;
import top.aenlly.ftp.constant.CacheConstant;
import top.aenlly.ftp.constant.FtpConstant;
import top.aenlly.ftp.databinding.DialogFtpClientBinding;
import top.aenlly.ftp.properties.FtpClientProperties;
import top.aenlly.ftp.utils.cache.SharedPreferencesUtils;

import java.io.IOException;

public class FtpClientDialog extends Dialog {

    private DialogFtpClientBinding binding;

    private FTPClient ftpClient;
    public FtpClientDialog(@NonNull Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DialogFtpClientBinding.inflate(inflater, null, false);
        SharedPreferencesUtils.init(FtpConstant.FTP_CLIENT,context);
        bindCache();
        setContentView(R.layout.dialog_ftp_client);
        registerForBtn();
    }

    /**
     * 绑定缓存
     */
    private void bindCache() {
        binding.etClientPort.setText(SharedPreferencesUtils.getString(FtpConstant.FTP_CLIENT, CacheConstant.PORT));
        binding.etClientHost.setText(SharedPreferencesUtils.getString(FtpConstant.FTP_CLIENT,CacheConstant.HOST));
        binding.etClientUsername.setText(SharedPreferencesUtils.getString(FtpConstant.FTP_CLIENT,CacheConstant.USER_NAME));
        binding.etClientPassword.setText(SharedPreferencesUtils.getString(FtpConstant.FTP_CLIENT,CacheConstant.PASSWORD));
        binding.etClientEncoding.setText(SharedPreferencesUtils.getString(FtpConstant.FTP_CLIENT,CacheConstant.ENCODING));
        binding.rdrgpClientConnect.check(SharedPreferencesUtils.getInt(FtpConstant.FTP_CLIENT,CacheConstant.CONNECT));
        binding.rdrgpClientMod.check(SharedPreferencesUtils.getInt(FtpConstant.FTP_CLIENT,CacheConstant.MOD));
    }

    /**
     * 注册单击事件
     */
    private void registerForBtn() {
        binding.btnClientCancel.setOnClickListener(v-> this.dismiss());
        binding.btnClientOk.setOnClickListener(v-> {

        });
    }

    void startFtpClient() throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(FtpClientProperties.host,FtpClientProperties.port);
        ftpClient.login(FtpClientProperties.username,FtpClientProperties.password);
    }
}
