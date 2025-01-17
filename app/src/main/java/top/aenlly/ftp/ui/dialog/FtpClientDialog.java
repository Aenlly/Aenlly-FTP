package top.aenlly.ftp.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import top.aenlly.ftp.R;
import top.aenlly.ftp.constant.CacheConstant;
import top.aenlly.ftp.constant.FtpConstant;
import top.aenlly.ftp.databinding.DialogFtpClientBinding;
import top.aenlly.ftp.properties.FtpClientProperties;
import top.aenlly.ftp.ui.ftpserver.FtpServerService;
import top.aenlly.ftp.utils.cache.SharedPreferencesUtils;

public class FtpClientDialog extends Dialog {

    private DialogFtpClientBinding binding;

    private Context context;

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
        Intent ftpIntent = new Intent(context, FtpServerService.class);
        binding.btnClientCancel.setOnClickListener(v-> this.dismiss());
        binding.btnClientOk.setOnClickListener(v-> {
            if (!verifyParameter()) {
                return;
            }
            initProperties();
            context.startService(ftpIntent);
        });
    }

    void initProperties() {
        FtpClientProperties.username = binding.etClientUsername.getText().toString();
        FtpClientProperties.password = binding.etClientPassword.getText().toString();
        FtpClientProperties.port = Integer.parseInt(binding.etClientPort.getText().toString());
        FtpClientProperties.host = binding.etClientHost.getText().toString();
        FtpClientProperties.encoding = binding.etClientEncoding.getText().toString();
        FtpClientProperties.connect = ((RadioButton)findViewById(binding.rdrgpClientConnect.getCheckedRadioButtonId())).getText().toString();
        FtpClientProperties.mode = binding.rdrgpClientMod.getCheckedRadioButtonId() == R.id.rdbtn_true;
        flushedCache();
    }

    /**
     * 刷新缓存
     */
    void flushedCache() {
        SharedPreferencesUtils.putString(FtpConstant.FTP_CLIENT,CacheConstant.HOST, binding.etClientHost.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.FTP_CLIENT,CacheConstant.PORT, binding.etClientPort.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.FTP_CLIENT,CacheConstant.USER_NAME, binding.etClientUsername.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.FTP_CLIENT,CacheConstant.PASSWORD, binding.etClientPassword.getText().toString());
        SharedPreferencesUtils.putString(FtpConstant.FTP_CLIENT,CacheConstant.ENCODING, binding.etClientEncoding.getText().toString());
        SharedPreferencesUtils.putBoolean(FtpConstant.FTP_CLIENT,CacheConstant.MOD, binding.rdrgpClientMod.getCheckedRadioButtonId() == R.id.rdbtn_true);
        SharedPreferencesUtils.putString(FtpConstant.FTP_CLIENT,CacheConstant.CONNECT, ((RadioButton)findViewById(binding.rdrgpClientConnect.getCheckedRadioButtonId())).getText().toString());
    }


    boolean verifyParameter() {
        if (binding.etClientHost.getText().toString().isEmpty()) {
            binding.etClientHost.setError("请填写服务器地址");
            return false;
        }
        if (binding.etClientPort.getText().toString().isEmpty()) {
            binding.etClientPort.setError("请填写端口");
            return false;
        }
        if (binding.etClientEncoding.getText().toString().isEmpty()) {
            binding.etClientEncoding.setError("请填写编码格式");
            return false;
        }
        return true;
    }
}
