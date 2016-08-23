package image;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.CustomFileUtil;

public class CaptchaIdentifyTest {
	String originImagePath = "E:\\captchaImage\\code.gif";
	CaptchaIdentify captchaIdentifier;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws IOException {
		captchaIdentifier = new CaptchaIdentify(originImagePath);
		// captchaIdentifier = new CaptchaIdentify(originImagePath,
		// grayImagePath, binaryImagePath);
	}

	@After
	public void tearDown() {
	}


	@Test
	public void batchTestTrueIdentify() throws Exception {
		String dirPath = CustomFileUtil.getRootPath();
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		String[] fileNames = dir.list();

		int length = files.length;
		for (int i = 0; i < length; i++) {
			System.out.println(fileNames[i]);
			if (files[i].isDirectory()) {
				continue;
			}
			captchaIdentifier = new CaptchaIdentify(dirPath + File.separator
					+ fileNames[i]);
			captchaIdentifier.identify();
		}
	}
}
