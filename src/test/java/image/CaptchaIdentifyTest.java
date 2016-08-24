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
	@Test
	public void batchTestTrueIdentify() throws Exception {
		String dirPath = CustomFileUtil.getRootPath()+File.separator+ "captchaDir";
		File dir = new File(dirPath);
		boolean canRead = dir.canRead();
		String path = dir.getAbsolutePath();
		File[] files = dir.listFiles();
		String[] fileNames = dir.list();

		int length = files.length;
		for (int i = 0; i < length; i++) {
			System.out.println(fileNames[i]);
			if (files[i].isDirectory()) {
				continue;
			}
			CaptchaIdentify captchaIdentifier = new CaptchaIdentify(fileNames[i]);
			captchaIdentifier.identify();
		}
	}
}
