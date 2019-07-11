package top.silentstorm.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;

import java.io.File;

/**
 * Created by silentstorm on 2019/7/10.
 *
 * @author silentstorm
 * @date 2019/7/10
 */
public class QiniuUtils {
    private static UploadManager uploadManager = new UploadManager(new Configuration());

    private static void doUpload(File file, String name, String token) {
        try {
            if (name.startsWith("/")) {
                name = name.substring(1, name.length());
            }
            Response res = uploadManager.put(file, name, token);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            System.out.println(e);
            e.printStackTrace();
            Response r = e.response;
            System.out.println("error in upload: " + r.toString());
        }
    }

    public static void upload(File file, String name, String token) {
        doUpload(file, name, token);
    }
}
