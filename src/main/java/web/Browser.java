package web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import utils.CustomFileUtil;
import utils.CustomStringUtils;

/**
 * 该类用于模拟浏览器各种行为 . This class is used for simulating browser's behavior.
 *
 * @author pc
 */
public class Browser {
    private final WebClient webClient;

    Logger log_file = Logger.getLogger("FILE");
    Logger log_consle = Logger.getLogger("CONSOLE");

    public Browser() {
        webClient = new WebClient();
        initWebClient();
    }

    public Browser(BrowserVersion browser_version) {
        webClient = new WebClient(browser_version);
        initWebClient();
    }

    /**
     * 初始化浏览器的默认配置
     */
    public void initWebClient() {
        webClient.getOptions().setTimeout(60000);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setGeolocationEnabled(false);
        webClient.getOptions().setPopupBlockerEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
    }

    /**
     * 打开一个网页并执行js代码
     *
     * @param targetURL 目标网页的URL
     * @return 成功打开的网页
     * @throws FailingHttpStatusCodeException
     * @throws IOException
     * @throws MalformedURLException
     */
    public HtmlPage openPageWithJS(String targetURL)
            throws FailingHttpStatusCodeException, MalformedURLException,
            IOException {
        webClient.setJavaScriptTimeout(15000);
        webClient.waitForBackgroundJavaScript(100000);
        HtmlPage page = webClient.getPage(targetURL);

        return page;
    }

    /**
     * 获取登录框表单
     *
     * @param page   当前登录页面
     * @param formId 登录表单的id
     * @return 登录表单
     */
    @SuppressWarnings("unused")
    public HtmlForm getLoginForm(HtmlPage page, String formId) {
        final HtmlForm loginForm = (HtmlForm) page
                .<HtmlForm>getElementById(formId);
        return loginForm;
    }

    /**
     * 填写登录表单
     *
     * @param loginFormInfomation 登录信息
     * @param loginForm           登录表单
     * @throws IOException
     */
    public void fillLoginForm(Map<String, String> loginFormInfomation,
                              HtmlForm loginForm) throws IOException {
        log_consle.error(loginFormInfomation.toString());
        log_file.error(loginFormInfomation.toString());
        fillNormalInputField("LoginName", loginFormInfomation.get("username"),
                loginForm);
        fillPasswordInputField("Password", loginFormInfomation.get("password"),
                loginForm);
        fillNormalInputField("CheckCode", loginFormInfomation.get("captcha"),
                loginForm);

    }

    /**
     * 填写普通输入框
     *
     * @param fieldName 输入框的名称
     * @param content   要填写的内容
     * @param loginForm 输入框所属的登录表单
     */
    public void fillNormalInputField(String fieldName, String content,
                                     final HtmlForm loginForm) {
        final HtmlTextInput field = loginForm.getInputByName(fieldName);
        field.setValueAttribute(content);
    }

    /**
     * 填写输入框
     *
     * @param fieldId 输入框的id
     * @param content 要填写的内容
     * @param page    输入框所在页面
     */
    public void fillInputField(String fieldId, String content,
                               final HtmlPage page) {
        final HtmlInput field = (HtmlInput) page.getElementById(fieldId);
        field.setValueAttribute(content);
    }

    /**
     * @param tag_name        xpath的tag name
     * @param attribute       xpath的属性
     * @param attribute_value xpath属性值
     * @param page            元素所在页面
     * @throws IOException
     * @Description: 点击页面某元素
     * @author rongyang_lu
     * @date 2015年8月13日 下午3:59:26
     */
    protected HtmlPage clickElement(String tag_name, String attribute,
                                    String attribute_value, HtmlPage page) throws IOException {
        // final DomElement field = page.getFirstByXPath("//@" + attribute +
        // "='"
        // + attribute_value + "'");
        final DomElement field = page.getFirstByXPath("//" + tag_name + "[@"
                + attribute + "='" + attribute_value + "']");
        return field.click();
    }

    /**
     * 填写密码输入框
     *
     * @param fieldName 输入框的名称
     * @param content   要填写的内容
     * @param loginForm 输入框所属的登录表单
     */
    public void fillPasswordInputField(String fieldName, String content,
                                       final HtmlForm loginForm) {
        final HtmlPasswordInput field = loginForm.getInputByName(fieldName);
        field.setValueAttribute(content);
    }

    /**
     * 下载图片
     *
     * @param page     图片所在页面
     * @param imgId    图片的id
     * @param savePath 保存图片的本地路径
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void saveImage(HtmlPage page, String imgId, String savePath)
            throws IOException {
        System.out.println(page.asText());
        //调试的时候可以用这行把页面保存下来慢慢分析
//		CustomFileUtil.writeFile(page.asText(), CustomFileUtil.getRootPath()
//				+ "page/pageWithImage.html");
        HtmlImage image = (HtmlImage) page.<HtmlImage>getElementById(imgId);
        String dir = CustomStringUtils.getDir(savePath);
        CustomFileUtil.makeInexistentDirs(dir);
        File imageFile = new File(savePath);
        image.saveAs(imageFile);
    }


    /**
     * 发送post请求
     *
     * @param post_data post请求中的entity-body
     * @return
     * @throws IOException
     * @author rongyang_lu
     * @date 2015年8月17日 上午10:09:43
     */
    public WebResponse postData(String url, String post_data,
                                Map<String, String> additionalHeaders) throws IOException,
            SocketException {
        WebRequest request = new WebRequest(new URL(url), HttpMethod.POST);
        request.setCharset("UTF-8");
        request.setRequestBody(post_data);
        request.setAdditionalHeaders(additionalHeaders);
        WebResponse response = webClient.loadWebResponse(request);
        return response;
    }

    /**
     * 发送post请求
     *
     * @param post_data post请求中的entity-body
     * @return
     * @throws IOException
     * @author rongyang_lu
     * @date 2015年8月17日 上午10:09:43
     */
    public WebResponse postData(String url, String post_data)
            throws IOException {

        WebRequest request = new WebRequest(new URL(url), HttpMethod.POST);
        request.setRequestBody(post_data);
        WebResponse response = webClient.loadWebResponse(request);
        return response;
    }

    /**
     * 发送get请求
     * @param url 请求的url地址（也可以直接把参数拼好丢到url里，get_param直接给个null）
     * @return
     */
    public WebResponse getData(String url) {
        return getData(url, null, null);
    }


    /**
     * 发送get请求
     * @param url 请求的url地址
     * @param getParam get请求的参数
     * @return
     */
    public WebResponse getData(String url, Map<String,String> getParam) {
        String getParamStr = encodeURLData(getParam);
        return getData(url, getParamStr, null);
    }

    /**
     * 发送get请求
     * @param url 请求的url地址
     * @param getParam get请求的参数
     * @return
     */
    public WebResponse getData(String url, String getParam) {
        return getData(url, getParam, null);
    }

    /**
     * 发送get请求获数据
     */
    public WebResponse getData(String url, String getParam, Map<String, String> additionalHeaders) {
        if (!CustomStringUtils.isEmpty(getParam)) {
            url = url + "?" + getParam;
        }
        WebRequest request;
        WebResponse response = null;
        try {
            request = new WebRequest(new URL(url), HttpMethod.GET);
            if (additionalHeaders != null) {
                request.setAdditionalHeaders(additionalHeaders);
            }
            response = webClient.loadWebResponse(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public CookieManager getCookieManager() {
        return webClient.getCookieManager();
    }

    /**
     * @return webClient
     */
    public WebClient getWebClient() {
        return webClient;
    }

    /**
     * 读取需要发布的职位信息，将其转变为能以Content-Type:
     * application/x-www-form-urlencoded格式post的数据
     *
     * @param raw_data 职位信息
     * @return
     * @throws IOException
     * @author rongyang_lu
     * @date 2015年8月17日 上午10:07:18
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String generatePostData(Map<String, String> raw_data)
            throws IOException {
        StringBuilder post_data = new StringBuilder();

        Set entry_set = raw_data.entrySet();
        Iterator<Entry> iterator = entry_set.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            post_data.append(entry.getKey());
            post_data.append("=");
            post_data.append(entry.getValue());
            post_data.append("&");
        }
        post_data.deleteCharAt(post_data.lastIndexOf("&"));
        return post_data.toString();
    }

    /**
     * 生成urlencoded之后的http请求参数
     * @param rawData 转换前的参数
     * @return 转换失败会返回null
     */
    public String encodeURLData(Map<String, String> rawData) {
        StringBuilder post_data = new StringBuilder();

        Set<Entry<String, String>> entry_set = rawData.entrySet();
        Iterator<Entry<String, String>> iterator = entry_set.iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            try {
                post_data.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));

                post_data.append("=");
                String value = entry.getValue();
                if (null != value && !"null".equals(value)) {
                    post_data.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            post_data.append("&");
        }
        post_data.deleteCharAt(post_data.lastIndexOf("&"));
        return post_data.toString();
    }

}
