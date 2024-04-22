package top.aenlly.ftp_server.utils.images;

import android.content.Context;
import android.media.MediaScannerConnection;
import lombok.extern.slf4j.Slf4j;
import top.aenlly.ftp_server.constant.FtpConstant;
import top.aenlly.ftp_server.properties.FtpProperties;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import java.io.File;
import java.util.List;

/**
 * @author Aenlly||tnw
 * @create 2024/04/22 14:24
 * @since 1.0.0
 */

public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * 同步压缩
     *
     * @param context              上下文
     * @param photos               照片
     * @param targetPath           目标路径
     * @param onCompressListener   在压缩侦听器上
     */
    public static void syncCompress(Context context, List<File> photos,String targetPath,OnCompressListener onCompressListener){
        Luban.with(context)
                // 加载原图
                .load(photos)
                // 未压缩的阈值
                .ignoreBy(1024*3)
                // 目标目录
                .setTargetDir(targetPath)
                // 压缩回调接口
                .setCompressListener(onCompressListener)
                // 重命名
                .setRenameListener(s -> {
                    String[] split = s.split("/");
                    return split[split.length-1];
                })
                .launch();
    }

    @Slf4j
    public static class DefaultOnCompressListener implements OnCompressListener{
        private Context context;

        public DefaultOnCompressListener(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onSuccess(File file) {
            // 刷新媒体管理，让图片/视频加入到媒体库中
            if (FtpProperties.compressThumbState){
                MediaScannerConnection.scanFile(this.context,
                        new String[]{file.getParent()},
                        FtpConstant.FILE_FORMATS,
                        (path, uri) -> {});
            }
        }

        @Override
        public void onError(Throwable throwable) {
            log.error(throwable.getMessage());
        }
    }
}
