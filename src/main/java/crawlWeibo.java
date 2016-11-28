import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.CustomFileUtil;
import utils.JsonUtils;
import utils.RegexUtils;
import web.Browser;

/**
 * 爬新浪微博的例子
 */
public class crawlWeibo {

    private static String SEARCH_PAGE_URL = "http://s.weibo.com/weibo/";

    public static void main(String[] args) throws Exception {
        Browser browser = new Browser(BrowserVersion.INTERNET_EXPLORER);

        String keyword = "咸鱼";
        WebResponse searchPage = browser.getData(SEARCH_PAGE_URL + URLEncoder.encode(keyword, StandardCharsets
                .UTF_8.name()));
        String searchPageStr = searchPage.getContentAsString();

        // 如果想改html的保存路径，可以直接改第二个参数，或者改CustomFileUtil.getRootPath() 这个方法
        CustomFileUtil.writeFile(searchPageStr, CustomFileUtil.getRootPath() + java.io.File.separator + "searchPage" +
                ".html");


        //新浪微博的html页面数据藏在js里，而且这些js htmlunit无法识别，只能用正则表达式提取
        //先获得所有js
        Elements scripts = Jsoup.parse(searchPageStr).select("script");
        scripts.forEach(script -> {
            String scriptStr = script.toString();
            // 把 \" 换成 单引号 ，把换行符干掉
            scriptStr = scriptStr.replaceAll("\\\\\"", "'").replaceAll("(\r\n|\n)", "");

            // 用jackson预处理一下字符串，方便转换成java键值对
            scriptStr = String.valueOf(JsonStringEncoder.getInstance().quoteAsString(scriptStr));

            // 解码unicode
            scriptStr = StringEscapeUtils.unescapeJava(scriptStr);

            if (scriptStr.contains("\"pid\":\"pl_weibo_direct\"")) {
                String searchResult = extractSearchResult(scriptStr);
                Document searchResultDoc = Jsoup.parse(searchResult);
                Elements divSearchFeed = searchResultDoc.select(".search_feed");
                if (!divSearchFeed.isEmpty()) {
                    List<Weibo> weiboList = structingWeibo(searchResultDoc);
                }

            }
        });


    }

    /**
     * 从微博返回的html页面中的javascript语句里提取搜索结果
     *
     * @param scriptStr js语句
     * @return 搜索结果，提取失败则返回null
     */
    private static String extractSearchResult(String scriptStr) {
        String regex = "STK && STK\\.pageletM && STK\\.pageletM\\.view\\([\\s\\S]*\\)";
        String scriptContent = RegexUtils.getFirstMatch(regex, scriptStr);
        String json = RegexUtils.getFirstMatch("\\{[\\s\\S]*\\}", scriptContent);
        Map<String, Object> jsonMap = JsonUtils.readValue(json, HashMap.class);
        System.out.println(jsonMap);
        return jsonMap.get("html").toString();
    }

    /**
     * 把提取到的搜索结果结构化为java对象
     *
     * @param searchResultDoc 搜索结果
     * @return java对象
     */
    private static List<Weibo> structingWeibo(Document searchResultDoc) {

        Elements divFeedContents = searchResultDoc.select("div.WB_cardwrap.S_bg2.clearfix");
        List<Weibo> weiboList = new ArrayList<>();
        divFeedContents.forEach(divFeedContent -> {
            Elements aWriterHomePage = divFeedContent.select("a.W_texta.W_fb");
            String writer = aWriterHomePage.attr("title");
            String writerLink = aWriterHomePage.attr("href");
            Elements pCommentText = divFeedContent.select("p.comment_txt");
            String content = pCommentText.text();
            Elements divMedia = divFeedContent.select("div.media_box");
            Elements imgs = divMedia.select("img");
            List<String> imageUrls = new ArrayList<String>();
            imgs.forEach(img -> {
                imageUrls.add(img.attr("src"));
            });
            weiboList.add(new Weibo.WeiboBuilder().setContent(content).setImage(imageUrls).setWriter(writer)
                    .setWriterLink(writerLink).createWeibo());
        });
        System.out.println(weiboList);
        return weiboList;
    }

}

class Weibo {
    // 微博作者
    private String writer;
    // 微博作者的微博地址
    private String writerLink;
    // 内容
    private String content;
    // 微博附带的图片地址
    private List<String> imageUrls;

    public Weibo() {

    }

    public Weibo(String content, List<String> image, String writer, String writerLink) {
        this.content = content;
        this.imageUrls = image;
        this.writer = writer;
        this.writerLink = writerLink;
    }

    public static class WeiboBuilder {
        private String content;
        private List<String> image;
        private String writer;
        private String writerLink;

        public WeiboBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        public WeiboBuilder setImage(List<String> image) {
            this.image = image;
            return this;
        }

        public WeiboBuilder setWriter(String writer) {
            this.writer = writer;
            return this;
        }

        public WeiboBuilder setWriterLink(String writerLink) {
            this.writerLink = writerLink;
            return this;
        }

        public Weibo createWeibo() {
            return new Weibo(content, image, writer, writerLink);
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getWriterLink() {
        return writerLink;
    }

    public void setWriterLink(String writerLink) {
        this.writerLink = writerLink;
    }

    @Override
    public String toString() {
        return "Weibo{" +
                "content='" + content + '\'' +
                ", writer='" + writer + '\'' +
                ", writerLink='" + writerLink + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}