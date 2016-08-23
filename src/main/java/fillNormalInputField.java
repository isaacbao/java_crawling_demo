import image.CaptchaIdentify;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

//import image.CaptchaIdentify;

public class fillNormalInputField {

	private static String TARGET_URL = "https://passport.zhaopin.com/org/login";

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		String imagePath = "E:\\captchaImage\\code.gif";
		HtmlPage page = openPage(TARGET_URL);
		saveImage(page, "checkimg", imagePath);

		login(imagePath, page);

	}

	private static void login(String imagePath, HtmlPage page) throws IOException {
		String username = "";
		String password = "";
		String captchaString = new CaptchaIdentify(imagePath).identify();

		String formId = "form1";
		final HtmlForm loginForm = (HtmlForm) page.<HtmlForm> getElementById(formId);

		fillNormalInputField("LoginName", username, loginForm);
		fillPasswordInputField("Password", password, loginForm);
		fillNormalInputField("CheckCode", captchaString, loginForm);

		final HtmlButtonInput button = loginForm.getInputByName("Submit");
		final HtmlPage page2 = button.click();
		System.out.println(page2.asText());
	}

	public static void fillNormalInputField(String fieldName, String content,final HtmlForm loginForm) {
		final HtmlTextInput field = loginForm.getInputByName(fieldName);
		field.setValueAttribute(content);
	}
	
	public static void fillPasswordInputField(String fieldName, String content,final HtmlForm loginForm) {
		final HtmlPasswordInput field = loginForm.getInputByName(fieldName);
		field.setValueAttribute(content);
	}

	public static HtmlPage openPage(String targetURL) throws IOException, MalformedURLException {
		try (final WebClient webClient = new WebClient()) {
			webClient.getOptions().setUseInsecureSSL(true);
			final HtmlPage page = webClient.getPage(targetURL);

			return page;
		}
	}

	public static void saveImage(HtmlPage page, String imgId, String savePath) throws IOException {
		HtmlImage image = (HtmlImage) page.<HtmlImage> getElementById(imgId);
		File imageFile = new File(savePath);
		image.saveAs(imageFile);
	}
}
