package top.aenlly.ftp.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import org.apache.commons.net.ftp.FTPClient;
import top.aenlly.ftp.R;
import top.aenlly.ftp.databinding.DialogFtpClientBinding;
import top.aenlly.ftp.properties.FtpClientProperties;

import java.io.IOException;

public class FtpClientDialog extends Dialog {

    private DialogFtpClientBinding binding;

    private FTPClient ftpClient;
    public FtpClientDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_ftp_client);

    }

    void startFtpClient() throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(FtpClientProperties.host,FtpClientProperties.port);
        ftpClient.login(FtpClientProperties.username,FtpClientProperties.password);
    }
}
