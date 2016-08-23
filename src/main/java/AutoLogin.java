import image.CaptchaIdentify;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class AutoLogin {

	private static String TARGET_URL = "https://passport.zhaopin.com/org/login";

	public static void main(String[] args)
			throws FailingHttpStatusCodeException, MalformedURLException,
			IOException {
		String imagePath = "G:/page/liepin_captcha/5239.jpg";

		File file = new File("G:/page/liepin_captcha");
		String[] fileNames = file.list();
		for (String fileName : fileNames) {
			String filePath = file.getAbsolutePath() + "/" + fileName;
			System.out.println(filePath);
			CaptchaIdentify captchaIdentifier = new CaptchaIdentify(filePath);
		}
	}
}
