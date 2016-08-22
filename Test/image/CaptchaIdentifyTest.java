package image;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

	/**
	 * Test of TessBaseAPIRect method, of class TessDllLibrary.
	 * 
	 * @throws java.lang.Exception
	 */
	// @Test
	public void testColorStatistic() throws Exception {
		captchaIdentifier.colorStatistic(null);
	}

	// @Test
	public void testGetTopFiveColor() throws Exception {
		captchaIdentifier.getTopFiveColor(captchaIdentifier
				.colorStatistic(null));
	}

	// @Test
	public void testGraying() throws Exception {
		// captchaIdentifier.simpleGraying(captchaIdentifier.openImage(captchaIdentifier.getOriginImageFilePath()));
		captchaIdentifier.weightGraying(captchaIdentifier
				.openImage(captchaIdentifier.getOriginImageFilePath()));
	}

	// @Test
	public void testBinarization() throws Exception {
		captchaIdentifier.binaryzation((captchaIdentifier
				.openImage(captchaIdentifier.getGrayImageFilePath())));
	}

	// @Test
	public void testIdentify() throws Exception {
		captchaIdentifier.identify("E:\\captchaImage\\binarycode.gif");
	}

	// @Test
	public void testRemoveNoise() throws Exception {
		captchaIdentifier.removeNoise(captchaIdentifier
				.openImage("E:\\captchaImage\\binarycode1.gif"));
	}

	// @Test
	public void testTrueIdentify() throws Exception {
		captchaIdentifier.identify();
	}

	@Test
	public void batchTestTrueIdentify() throws Exception {
		String dirPath = "E:\\captchaImage\\zhilian";
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		String[] fileNames = dir.list();

		int length = files.length;
		for (int i = 0; i < length; i++) {
			System.out.println(fileNames[i]);
			if (files[i].isDirectory()) {
				continue;
			}
			captchaIdentifier = new CaptchaIdentify(dirPath + "\\"
					+ fileNames[i]);
			captchaIdentifier.identify(captchaIdentifier
					.getBinaryImageFilePath());
		}
	}
}
