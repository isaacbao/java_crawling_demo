import com.sun.jna.Library;
import com.sun.jna.Native;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 云打码
 * Created by rongyang_lu on 2016/9/20.
 */
public class CloudCaptchaBreaker {

    private static String DLLPATH = "yundamaAPI-x64.dll";

    private static String USERNAME = "";
    private static String PASSWORD = "";

    private static int APPID = 1;
    private static String APPKEY = "";

    public static final int CHINESE_CHAR = 2000;

    public interface YDM extends Library {
        YDM INSTANCE = (YDM) Native.loadLibrary(DLLPATH, YDM.class);

        public void YDM_SetBaseAPI(String lpBaseAPI);

        public void YDM_SetAppInfo(int nAppId, String lpAppKey);

        public int YDM_Login(String lpUserName, String lpPassWord);

        public int YDM_DecodeByPath(String lpFilePath, int nCodeType, byte[] pCodeResult);

        public int YDM_UploadByPath(String lpFilePath, int nCodeType);

        public int YDM_EasyDecodeByPath(String lpUserName, String lpPassWord, int nAppId, String lpAppKey, String lpFilePath, int nCodeType, int nTimeOut, byte[] pCodeResult);

        public int YDM_DecodeByBytes(byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType, byte[] pCodeResult);

        public int YDM_UploadByBytes(byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType);

        public int YDM_EasyDecodeByBytes(String lpUserName, String lpPassWord, int nAppId, String lpAppKey, byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType, int nTimeOut, byte[] pCodeResult);

        public int YDM_GetResult(int nCaptchaId, byte[] pCodeResult);

        public int YDM_Report(int nCaptchaId, boolean bCorrect);

        public int YDM_EasyReport(String lpUserName, String lpPassWord, int nAppId, String lpAppKey, int nCaptchaId, boolean bCorrect);

        public int YDM_GetBalance(String lpUserName, String lpPassWord);

        public int YDM_EasyGetBalance(String lpUserName, String lpPassWord, int nAppId, String lpAppKey);

        public int YDM_SetTimeOut(int nTimeOut);

        public int YDM_Reg(String lpUserName, String lpPassWord, String lpEmail, String lpMobile, String lpQQUin);

        public int YDM_EasyReg(int nAppId, String lpAppKey, String lpUserName, String lpPassWord, String lpEmail, String lpMobile, String lpQQUin);

        public int YDM_Pay(String lpUserName, String lpPassWord, String lpCard);

        public int YDM_EasyPay(String lpUserName, String lpPassWord, long nAppId, String lpAppKey, String lpCard);
    }

    /**
     * 登录云打码系统
     *
     * @param username
     * @param password
     * @param appId    应用id
     * @param appKey   应用key
     * @return
     */
    public static int doLogin(String username, String password, int appId, String appKey) {
        YDM.INSTANCE.YDM_SetAppInfo(appId, appKey);
        int uid = YDM.INSTANCE.YDM_Login(username, password);
        return uid;
    }

    /**
     * 登录云打码系统
     *
     * @return
     */
    public static int doLogin() {
        return doLogin(USERNAME, PASSWORD, APPID, APPKEY);
    }

    /**
     * 破解验证码
     *
     * @param captchaPath 验证码图片的地址
     * @param captchaType 验证码类型，参考:http://www.yundama.com/price.html
     * @param charset     解出来的验证码是个byte，得转成string，中文用的是GBK
     * @return
     */
    public static String breakCaptcha(String captchaPath, int captchaType, String charset) {

        byte[] byteResult = new byte[30];
        int cid = YDM.INSTANCE.YDM_DecodeByPath(captchaPath, captchaType, byteResult);
        String strResult = new String(byteResult, Charset.forName(charset)).trim();

        // 返回其他错误代码请查询 http://www.yundama.com/apidoc/YDM_ErrorCode.html
        System.out.println("识别返回代码:" + cid);
        System.out.println("识别返回结果:" + strResult);

        return strResult;
    }
}
