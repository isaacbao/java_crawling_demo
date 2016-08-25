import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import utils.CustomFileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by rongyang_lu on 2016/8/25.
 */
public class crawlZhilian {
    private static String SEARCH_PAGE_URL = "https://passport.zhaopin.com/account/login";

    public static void mains(String[] args) throws Exception {
        HtmlPage loginPage = openPage(SEARCH_PAGE_URL);
        isNeedCaptcha(loginPage);
        login(loginPage);

    }

    /**
     * 分析登录页面是否需要验证码
     */
    private static void isNeedCaptcha(HtmlPage loginPage) {

    }

    private static void login(HtmlPage page) throws Exception {
        String username = "";
        String password = "";

        String formId = "form1";
        final HtmlForm loginForm;
        loginForm = (HtmlForm) page.<HtmlForm>getElementById(formId);

        fillNormalInputField("LoginName", username, loginForm);
        fillPasswordInputField("Password", password, loginForm);

        final HtmlButtonInput button = loginForm.getInputByName("Submit");
        final HtmlPage page2 = button.click();
        CustomFileUtil.writeFile(page2.asText(), CustomFileUtil.getRootPath());
    }

    public static void fillNormalInputField(String fieldName, String content, final HtmlForm loginForm) {
        final HtmlTextInput field = loginForm.getInputByName(fieldName);
        field.setValueAttribute(content);
    }

    public static void fillPasswordInputField(String fieldName, String content, final HtmlForm loginForm) {
        final HtmlPasswordInput field = loginForm.getInputByName(fieldName);
        field.setValueAttribute(content);
    }

    public static HtmlPage openPage(String targetURL) throws IOException, MalformedURLException {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);
        final HtmlPage page = webClient.getPage(targetURL);

        return page;

    }

    public static void saveImage(HtmlPage page, String imgId, String savePath) throws IOException {
        HtmlImage image;
        image = (HtmlImage) page.<HtmlImage>getElementById(imgId);
        File imageFile = new File(savePath);
        image.saveAs(imageFile);
    }


}
