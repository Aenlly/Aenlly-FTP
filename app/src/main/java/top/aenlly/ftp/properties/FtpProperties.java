package top.aenlly.ftp.properties;

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
    public static String remoteDir; // 远程目录
    public static String compressDir; // 压缩地址
    public static boolean compressState; // 压缩状态，是否启用
    public static boolean compressThumbState; // 略缩图状态，是否启用
    public static String encoding; // 字符编码
    public static String[] imageFormat; // 压缩匹配格式
}
