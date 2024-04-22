package top.aenlly.ftp_server.properties;

import lombok.Data;

/**
 * @author Aenlly||tnw
 * @create 2024/04/02 11:14
 * @since 1.0.0
 */
@Data
public class FtpProperties {

    public static String host;
    public static int port;
    public static String username;
    public static String password;
    public static String remoteDirectory; // 远程目录，可选

    public static String encoding; // 字符编码

}
