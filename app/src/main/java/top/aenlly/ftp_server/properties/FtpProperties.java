package top.aenlly.ftp_server.properties;

import lombok.Data;

/**
 * @author Aenlly||tnw
 * @create 2024/04/02 11:14
 * @since 1.0.0
 */
@Data
public class FtpProperties {

    private String host;
    private int port;
    private String username;
    private String password;
    private String remoteDirectory; // 远程目录，可选

    private String encoding; // 字符编码
}
