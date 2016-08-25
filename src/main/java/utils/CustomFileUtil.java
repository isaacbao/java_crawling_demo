package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author lou
 * @ClassName CustomFileUtil
 * @Description 处理与文件操作相关的工具类
 * @date 2015年8月13日 下午2:39:27
 */
public final class CustomFileUtil {

    /**
     * @param content 文件内容
     * @param path    文件路径
     * @throws FileNotFoundException
     * @Description: 写文件
     * @author rongyang_lu
     * @date 2015年8月13日 下午2:39:43
     */
    public static void writeFile(String content, String path) {
        String dir = CustomStringUtils.getDir(path);
        makeInexistentDirs(dir);
        OutputStream output = null;
        try {
            output = new FileOutputStream(path);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            output.write(content.getBytes());
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 如果文件路径不存在，尝试创建文件夹
     *
     * @param dir
     */
    public static void makeInexistentDirs(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void saveImage(String imageUrl, String destinationFile)
            throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

//	public static String getRootPath() {
//		String tomcat_path = System.getProperty("catalina.base");
//
//		// Logger.getLogger("FILE").error("tomcat_path:" + tomcat_path);
//		if (CustomStringUtils.isEmpty(tomcat_path)) {
//			return "";
//		}
//
//		return tomcat_path + File.separator+"crawlingDemo";
//
//	}

    public static String getRootPath() {
        return "G:" + File.separator + "javaDemoFiles" + File.separator;
    }


}
