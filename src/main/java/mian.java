import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebResponse;
import utils.CustomFileUtil;
import web.Browser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rongyang_lu on 2016/9/2.
 */
public class mian {

    public static void main(String[] args) {
        int a = 5;
        try {
            a = a();
            int b = a + 1;
            System.out.println(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int a() throws Exception {
        throw new Exception();
    }
}
