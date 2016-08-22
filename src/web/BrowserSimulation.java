package web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import image.CaptchaIdentify;
import image.ImageViewerFrame;
import utils.CustomStringUtils;

/**
 * ��������ģ�������������Ϊ . This class is used for simulating browser's behavior.
 * 
 * @author pc
 *
 */
public class BrowserSimulation {
	private WebClient webClient = new WebClient();

	/**
	 * ��һ����SSL���ܵ���ҳ����https�� Open a webpage with SSL(like https)
	 * 
	 * @param targetURL
	 *            Ŀ����ҳ��URL
	 * @return �ɹ��򿪵���ҳ
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public HtmlPage openPageWithSSL(String targetURL) throws IOException, MalformedURLException {
		webClient.getOptions().setUseInsecureSSL(true);
		final HtmlPage page = webClient.getPage(targetURL);

		return page;
	}

	/**
	 * ��ȡ��¼���
	 * 
	 * @param page
	 *            ��ǰ��¼ҳ��
	 * @param formId
	 *            ��¼����id
	 * @return ��¼��
	 */
	public HtmlForm getLoginForm(HtmlPage page, String formId) {
		final HtmlForm loginForm = (HtmlForm) page.<HtmlForm> getElementById(formId);
		return loginForm;
	}

	/**
	 * ��д��¼��
	 * 
	 * @param loginFormInfomation
	 *            ��¼��Ϣ
	 * @param loginForm
	 *            ��¼��
	 * @throws IOException
	 */
	public void fillLoginForm(Map<String, String> loginFormInfomation, HtmlForm loginForm) throws IOException {

		fillNormalInputField("LoginName", loginFormInfomation.get("username"), loginForm);
		fillPasswordInputField("Password", loginFormInfomation.get("password"), loginForm);
		fillNormalInputField("CheckCode", loginFormInfomation.get("captcha"), loginForm);

	}

	/**
	 * ��д��ͨ�����
	 * 
	 * @param fieldName
	 *            ����������
	 * @param content
	 *            Ҫ��д������
	 * @param loginForm
	 *            ����������ĵ�¼��
	 */
	public void fillNormalInputField(String fieldName, String content, final HtmlForm loginForm) {
		final HtmlTextInput field = loginForm.getInputByName(fieldName);
		field.setValueAttribute(content);
	}

	/**
	 * ��д���������
	 * 
	 * @param fieldName
	 *            ����������
	 * @param content
	 *            Ҫ��д������
	 * @param loginForm
	 *            ����������ĵ�¼��
	 */
	public void fillPasswordInputField(String fieldName, String content, final HtmlForm loginForm) {
		final HtmlPasswordInput field = loginForm.getInputByName(fieldName);
		field.setValueAttribute(content);
	}

	/**
	 * ����ͼƬ
	 * 
	 * @param page
	 *            ͼƬ����ҳ��
	 * @param imgId
	 *            ͼƬ��id
	 * @param savePath
	 *            ����ͼƬ�ı���·��
	 * @throws IOException
	 */
	public void saveImage(HtmlPage page, String imgId, String savePath) throws IOException {
		HtmlImage image = (HtmlImage) page.<HtmlImage> getElementById(imgId);
		File imageFile = new File(savePath);
		image.saveAs(imageFile);
	}
	
	/**
	 * ������֤��
	 * @param imagePath ��֤��ͼƬ��ַ
	 * @return ��֤��
	 */
	private static String artificialPerception(String imagePath) {
		JFrame imageFrame = new ImageViewerFrame(imagePath);
		((ImageViewerFrame) imageFrame).showImage();
		Scanner scanner = new Scanner(System.in);
		System.out.println("��������֤��");
		return scanner.next();
	}


	/**
	 * ���Խ���һ�ε�¼
	 * 
	 * @param imagePath
	 *            ��֤��ͼƬ�ĵ�ַ
	 * @param browserSimulation
	 *            ģ�������
	 * @param page
	 *            ��ǰ��¼ҳ��
	 * @param isArtificialPerception
	 *            �Ƿ�������֤��
	 * @return �����¼��ť���ҳ��
	 * @throws IOException
	 */
	public HtmlPage tryOnceLogin(String imagePath, HtmlPage page,
			boolean isArtificialPerception) throws IOException {
		saveImage(page, "checkimg", imagePath);

		String formId = "form1";
		HtmlForm loginForm = getLoginForm(page, formId);

		Map<String, String> loginFormInfomation = new HashMap<>();

		loginFormInfomation.put("username", "");
		loginFormInfomation.put("password", "");

		String captcha;
		if (isArtificialPerception) {
			captcha = artificialPerception(imagePath);
		} else {
			CaptchaIdentify captchaIdentifier = new CaptchaIdentify(imagePath);
			captcha = captchaIdentifier.identify();
			if (!CustomStringUtils.isCaptcha(captcha)) {
				// TODO ���ԼӸ����������һ��,Ȼ�����Զ�ʶ��һ�£�������
				captcha = artificialPerception(imagePath);
			}
			loginFormInfomation.put("captcha", captcha);
		}

		fillLoginForm(loginFormInfomation, loginForm);

		HtmlButtonInput button = loginForm.getInputByName("Submit");
		HtmlPage pageAfterClick = button.click();
		// System.out.println(pageAfterClick.asText());
		return pageAfterClick;
	}
	
	/**
	 * �����Զ���¼
	 * @param imagePath ����洢��֤���ͼƬ��ַ
	 * @param page ��½ҳ��
	 * @param try_times �Զ�ʶ����֤��Ĵ��� 
	 * @return
	 * @throws IOException
	 */
	public HtmlPage tryAutoLogin(String imagePath,
			HtmlPage page,Integer try_times)
			throws IOException {
		HtmlPage pageAfterClick = tryOnceLogin(imagePath, page, false);

		//���ڹ涨�������޷���ȷʶ����֤�룬�����û�������֤��
		for (int i = 0; (i < try_times) && (pageAfterClick.asText().contains("��֤�����")); i++) {
			System.out.println("���Ե�" + (i + 2) + "���Զ�ʶ��");
			pageAfterClick = tryOnceLogin(imagePath, pageAfterClick, false);
		}
		if (pageAfterClick.asText().contains("��֤�����")) {
			pageAfterClick = tryOnceLogin(imagePath, pageAfterClick, true);
		}
		System.out.println(pageAfterClick.asText());
		return pageAfterClick;
	}

	/**
	 * @return webClient
	 */
	public WebClient getWebClient() {
		return webClient;
	}

}
