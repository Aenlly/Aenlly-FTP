package top.aenlly.ftp_server.constant;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Aenlly||tnw
 * @create 2024/04/15 16:55
 * @since 1.0.0
 */

public interface FtpConstant {

    String USER_NAME = "username";
    String PASSWORD = "password";
    String PORT = "port";
    String UPLOAD_DIR = "upload_dir";
    String COMPRESS_DIR = "compress_dir";
    String COMPRESS_STATE = "compress_state";
    String COMPRESS_THUMB_STATE = "compress_thumb_state";
    String ENCODING = "encoding";

    String[] IMAGE_FORMATS = {"image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/tiff", "image/svg+xml", "image/x-adobe-dng", "image/x-canon-cr2", "image/x-nikon-nef", "image/x-sony-arw"};

    String[] VIDEO_FORMATS = {"video/mp4", "video/avi", "video/quicktime", "video/x-matroska", "video/x-ms-wmv", "video/x-flv", "video/3gpp", "video/webm", "video/mpeg"};

    String[] FILE_FORMATS = Stream.concat(Arrays.stream(FtpConstant.IMAGE_FORMATS), Arrays.stream(FtpConstant.VIDEO_FORMATS))
            .toArray(String[]::new);

}
