import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.file.File;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.CustomFileUtil;
import web.Browser;

/**
 * 爬新浪微博的例子
 */
public class crawlWeibo {

    private static String SEARCH_PAGE_URL = "http://s.weibo.com/weibo/";

    public static void main(String[] args) throws Exception {
        Browser browser = new Browser();

        String keyword = "咸鱼";
        HtmlPage searchPage = browser.openPageWithSSL(SEARCH_PAGE_URL + URLEncoder.encode(keyword, StandardCharsets
                .UTF_8.name()));
        String searchResult = searchPage.asText();
        CustomFileUtil.writeFile(searchResult, CustomFileUtil.getRootPath() + java.io.File.separator + "searchResult.html");
        Document searchResultDoc = Jsoup.parse(searchResult);
        Elements divSearchFeed = searchResultDoc.select(".search_feed");
        if (!divSearchFeed.isEmpty()) {
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

        }
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
}