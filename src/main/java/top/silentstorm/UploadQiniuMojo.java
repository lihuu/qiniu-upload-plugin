package top.silentstorm;

import top.silentstorm.utils.QiniuUtils;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ibm
 * @goal upload-qiniu
 */
@Mojo(name = "upload-qiniu")
public class UploadQiniuMojo extends AbstractMojo {
    /**
     * 上传的时候所需要的key
     */
    @Parameter(property = "uploadqiniu.key")
    private String accessKey;

    /**
     * 上传时所需的secret
     */
    @Parameter(property = "uploadqiniu.secret")
    private String accessSecret;
    /**
     * 对应的存储空间的名称
     */
    @Parameter(property = "uploadqiniu.bucket")
    private String bucket;

    /**
     * 是否是递归上传
     */
    @Parameter(property = "uploadqiniu.recursive", defaultValue = "false")
    private boolean recursive;

    @Parameter(property = "uploadqiniu.targetDir", defaultValue = "target")
    private String targetDir;

    /**
     * 默认上传只能是新增文件,这样的情况下同名的会上传失败。
     * 如果是覆盖模式
     */
    @Parameter(property = "uploadqiniu.insertOnly", defaultValue = "true")
    private boolean insertOnly;


    private void getAllFiles(List<File> fileList, File file, boolean recursive) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (recursive) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        getAllFiles(fileList, f, true);
                    } else {
                        fileList.add(f);
                    }
                }

            } else {
                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    fileList.add(f);
                }
            }
        } else {
            fileList.add(file);
            return;
        }
    }


    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Start to execute");

        if (StringUtils.isNullOrEmpty(accessKey)) {
            throw new MojoExecutionException("key is required");
        }

        if (StringUtils.isNullOrEmpty(accessSecret)) {
            throw new MojoExecutionException("secret is required");
        }

        if (StringUtils.isNullOrEmpty(bucket)) {
            throw new MojoExecutionException("bucket is required");
        }

        StringMap stringMap = new StringMap();
        String token = "";
        if (insertOnly) {
            stringMap.put("insertOnly", 1);
            token = Auth.create(accessKey, accessSecret).uploadToken(bucket, null, 600, stringMap);
        }
        File file = new File(targetDir);
        if (file.isDirectory() && !recursive) {
            throw new MojoExecutionException("Directory is not support");
        }

        String fileName;
        if (file.isDirectory()) {
            List<File> fileList = new ArrayList<>();
            getAllFiles(fileList, file, recursive);
            for (File f : fileList) {
                fileName = f.getName();
                getLog().info("to upload :" + fileName);
                if (insertOnly) {
                    QiniuUtils.upload(f, fileName, token);
                } else {
                    //创建可以覆盖的上传
                    token = Auth.create(accessKey, accessSecret).uploadToken(bucket, fileName, 300, stringMap);
                    QiniuUtils.upload(f, fileName, token);
                }
            }

        } else {
            fileName = file.getName();
            getLog().info("to upload :" + fileName);
            if (!insertOnly) {
                token = Auth.create(accessKey, accessSecret).uploadToken(bucket, fileName, 300, stringMap);
            }
            QiniuUtils.upload(file, fileName, token);
        }
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public void setInsertOnly(boolean insertOnly) {
        this.insertOnly = insertOnly;
    }
}
