import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import utils.CustomFileUtil;
import web.Browser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 由于微博太过非典型，试试爬豆瓣
 */
public class crawlDouban {
    private static final String SEARCH_PAGE_URL = "https://www.douban.com/search";

    public static void main(String[] args) throws Exception {
        Browser browser = new Browser(BrowserVersion.CHROME);

        Map<String, String> searchParam = new HashMap<>();
        searchParam.put("q", "蛤");
        searchParam.put("cat", "1005");
        WebResponse searchPage = browser.getData(SEARCH_PAGE_URL , searchParam);
        String searchPageStr = searchPage.getContentAsString();
        CustomFileUtil.writeFile(searchPageStr, CustomFileUtil.getRootPath() + java.io.File.separator +
                "doubanSearchResult.html");
    }


}
