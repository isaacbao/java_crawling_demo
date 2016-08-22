package image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import utils.CustomFileUtil;
import utils.CustomStringUtils;
import utils.MapValueComparator;


/**
 * 用于验证码识别的类 This class is used for identify captcha
 * 
 * @author pc
 * 
 */
public class CaptchaIdentify {

	private String originImageFilePath;
	private String grayImageFilePath;
	private String binaryImageFilePath;
	private String noNoiseImageFilePath;

	public CaptchaIdentify(String originImagePath) {
		String dir = CustomStringUtils.getDir(originImagePath);
		String originFileName = CustomStringUtils.getFileName(originImagePath);
		originImageFilePath = originImagePath;

		File grayDir = new File(dir + "\\gray\\");
		if (!grayDir.exists()) {
			grayDir.mkdirs();
		}
		grayImageFilePath = dir + "\\gray\\" + originFileName;

		File binaryDir = new File(dir + "\\binary\\");
		if (!binaryDir.exists()) {
			binaryDir.mkdirs();
		}
		binaryImageFilePath = dir + "\\binary\\" + originFileName;

		File noNoiseDir = new File(dir + "\\noNoise\\");
		if (!noNoiseDir.exists()) {
			noNoiseDir.mkdirs();
		}
		noNoiseImageFilePath = dir + "\\noNoise\\" + originFileName;
	}

	public CaptchaIdentify() {
		originImageFilePath = "E:\\captchaImage\\code.gif";
		grayImageFilePath = "E:\\captchaImage\\graycode.gif";
		binaryImageFilePath = "E:\\captchaImage\\binarycode.gif";
	}

	public CaptchaIdentify(String originImagePath, String grayImagePath,
			String binaryImagePath) {
		originImageFilePath = originImagePath;
		grayImageFilePath = grayImagePath;
		binaryImageFilePath = binaryImagePath;
	}

	public BufferedImage openImage(String imagePath) throws IOException {
		File originImageFile = new File(imagePath);
		return ImageIO.read(originImageFile);
	}

	public BufferedImage openImage(File file) throws IOException {
		return ImageIO.read(file);
	}

	/**
	 * 用最傻逼的取RGB平均值作为灰度的方法进行灰度化，大多数时候黄色等浅色调字符会消失
	 * 
	 * @param image
	 * @throws IOException
	 */
	public void simpleGraying(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage grayImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		String dir = CustomStringUtils.getDir(grayImageFilePath);
		CustomFileUtil.makeInexistentDirs(dir);
		File newFile = new File(grayImageFilePath);
		ImageIO.write(grayImage, "gif", newFile);
	}

	/**
	 * 通过加权法对RGB三色进行加权后算出灰度值，以此灰度值进行图片灰度化
	 * 
	 * @param image
	 * @throws IOException
	 */
	public void weightGraying(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage grayImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				final int color = image.getRGB(i, j);

				// 从原来32位的颜色值中分离出各占8位的R,G,B值
				final int r = (color >> 16) & 0xff;
				final int g = (color >> 8) & 0xff;
				final int b = color & 0xff;

				// 加权灰度化
				int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
				// System.out.println(i + " : " + j + " " + gray);
				int newPixel = colorToRGB(255, gray, gray, gray);
				grayImage.setRGB(i, j, newPixel);

			}
		}
		String dir = CustomStringUtils.getDir(grayImageFilePath);
		CustomFileUtil.makeInexistentDirs(dir);
		File newFile = new File(grayImageFilePath);
		ImageIO.write(grayImage, "gif", newFile);
	}

	/**
	 * 二值化图片
	 * 
	 * @param image
	 * @throws IOException
	 */
	public void binaryzation(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage binaryImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_BINARY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				// System.out.println(rgb);
				binaryImage.setRGB(i, j, rgb);
			}
		}
		String dir = CustomStringUtils.getDir(binaryImageFilePath);
		CustomFileUtil.makeInexistentDirs(dir);
		File newFile = new File(binaryImageFilePath);
		ImageIO.write(binaryImage, "gif", newFile);
	}

	/**
	 * 统计图片颜色像数分布
	 * 
	 * @param image
	 * @return
	 */
	public Map<Integer, Integer> colorStatistic(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		Map<Integer, Integer> statisticMap = new HashMap<>();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				Integer count = statisticMap.get(rgb);
				if (null == count) {
					statisticMap.put(rgb, 0);
				} else {
					statisticMap.put(rgb, count + 1);
				}
			}
		}

		// System.out.println(statisticMap.toString());
		return statisticMap;
	}

	/**
	 * 统计频率最高的5种颜色
	 * 
	 * @param statisticMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TreeMap<Integer, Integer> getTopFiveColor(
			Map<Integer, Integer> statisticMap) {
		MapValueComparator mapValueComparator = new MapValueComparator(
				statisticMap);
		TreeMap<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>(
				mapValueComparator);
		sortedMap.putAll(statisticMap);
		Set<Entry<Integer, Integer>> entries = sortedMap.entrySet();
		Iterator iterator = null;

		TreeMap<Integer, Integer> topFiveColorMap = new TreeMap<Integer, Integer>(
				mapValueComparator);
		if (entries != null) {
			iterator = entries.iterator();

			for (int i = 0; i < 5; i++) {
				Map.Entry<Integer, Integer> entry = (Entry<Integer, Integer>) iterator
						.next();
				topFiveColorMap.put(entry.getKey(), entry.getValue());
			}
		}

		// System.out.println(topFiveColorMap);
		return topFiveColorMap;
	}

	/**
	 * 去除噪点
	 * 
	 * @param image
	 * @throws IOException
	 */
	public void removeNoise(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = image.getRGB(x, y);

				if (rgb == -1) {
					continue;
				}

				int weight = 0;
				int up = y - 1;
				int down = y + 1;
				int left = x - 1;
				int right = x + 1;

				if (0 <= left && left < width) {
					if (0 <= up && up < height) {
						if (image.getRGB(left, up) != -1) {
							weight += 1;
						}
					}

				}

				if (0 <= up && up < height) {
					if (image.getRGB(x, up) != -1) {
						weight += 1;
					}
				}

				if (0 <= right && right < width) {
					if (0 <= up && up < height) {
						if (image.getRGB(right, up) != -1) {
							weight += 1;
						}
					}

				}

				if (0 <= left && left < width) {
					if (image.getRGB(left, y) != -1) {
						weight += 1;
					}
				}

				if (0 <= right && right < width) {
					if (image.getRGB(right, y) != -1) {
						weight += 1;
					}
				}

				if (0 <= left && left < width) {
					if (0 <= down && down < height) {
						if (image.getRGB(left, down) != -1) {
							weight += 1;
						}
					}

				}

				if (0 <= down && down < height) {
					if (image.getRGB(x, down) != -1) {
						weight += 1;
					}
				}

				if (0 <= right && right < width) {
					if (0 <= down && down < height) {
						if (image.getRGB(right, down) != -1) {
							weight += 1;
						}
					}
				}

				if (weight == 0) {
					image.setRGB(x, y, -1);
				}
			}
		}
		String dir = CustomStringUtils.getDir(noNoiseImageFilePath);
		CustomFileUtil.makeInexistentDirs(dir);
		File newFile = new File(noNoiseImageFilePath);
		ImageIO.write(image, "gif", newFile);
	}

	/**
	 * 去除图片边缘的噪点
	 * 
	 * @param image
	 *            要去除噪点的图片
	 * @throws IOException
	 */
	public void removeBorderNoise(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int x = 0; x < width; x++) {
			image.setRGB(x, 0, -1);
			image.setRGB(x, height - 1, -1);
		}
		for (int y = 0; y < height; y++) {
			image.setRGB(0, y, -1);
			image.setRGB(width - 1, y, -1);

		}
		String dir = CustomStringUtils.getDir(noNoiseImageFilePath);
		CustomFileUtil.makeInexistentDirs(dir);
		File newFile = new File(noNoiseImageFilePath);
		ImageIO.write(image, "gif", newFile);
	}

	/**
	 * 进行验证码识别
	 * 
	 * @param image
	 *            要识别的验证码
	 * @return 验证码图片
	 */
	public String identify(BufferedImage image) {

		Tesseract instance = initialTesseractInstance();

		String result = null;

		try {
			result = instance.doOCR(image);
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		}

		result = postProcessResult(result);
		System.out.println(result);
		return result;
	}

	/**
	 * @return
	 */
	public Tesseract initialTesseractInstance() {
		Tesseract instance = new Tesseract();
		URL url = this.getClass().getClassLoader().getResource("/");
		String dir;
		if (null == url) {
			url = this.getClass().getClassLoader().getResource(".");
		}
		dir = url.getPath();
		// String system_name = System.getProperty("os.name");
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			dir = dir.substring(1);
		}
		instance.setDatapath(dir + "tessdata");
		return instance;
	}

	/**
	 * 进行验证码识别
	 * 
	 * @param filePath
	 *            要识别的验证码图片地址
	 * @return 验证码
	 */
	public String identify(String filePath) {
		File imageFile = new File(filePath);
		Tesseract instance = initialTesseractInstance();

		String result = null;

		try {
			result = instance.doOCR(imageFile);
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		}

		result = postProcessResult(result);
		System.out.println(result);
		return result;
	}

	/**
	 * 对识别出来的验证码字符串进行处理，去除空格和换行符等无意义字符，以及判断字符串是否四位英文数字
	 * 
	 * @param result
	 *            验证码字符串
	 * @return 处理后的验证码字符串
	 */
	private String postProcessResult(String result) {
		int length = result.length();
		if (!CustomStringUtils.isCaptcha(result)) {
			StringBuilder resultBuilder = new StringBuilder(result);
			for (int i = length - 1; i >= 0; i--) {
				char tempChar = resultBuilder.charAt(i);
				if (!CustomStringUtils.isDigitorLetter(tempChar)) {
					resultBuilder.deleteCharAt(i);
				}
			}
			if (4 == resultBuilder.length()) {
				result = resultBuilder.toString();
			} else {
				// 识别失败，输个错的重新输入
				result = "FAIL";
			}
		}
		return result;

	}

	/**
	 * 进行验证码识别
	 * 
	 * @return 识别结果
	 * @throws IOException
	 */
	public String identify() throws IOException {
		this.weightGraying(this.openImage(this.getOriginImageFilePath()));
		this.binaryzation((this.openImage(this.getGrayImageFilePath())));
		this.removeBorderNoise(this.openImage(this.getBinaryImageFilePath()));
		return this.identify(this.openImage(this.noNoiseImageFilePath));
	}

	/**
	 * 进行验证码识别
	 * 
	 * @return 识别结果
	 * @throws IOException
	 */
	public String identifyLiepin() throws IOException {
		this.weightGraying(this.openImage(this.getOriginImageFilePath()));
		this.binaryzation((this.openImage(this.getGrayImageFilePath())));
		// this.removeBorderNoise(this.openImage(this.getBinaryImageFilePath()));
		return this.identify(this.openImage(this.noNoiseImageFilePath));
	}

	public String getOriginImageFilePath() {
		return originImageFilePath;
	}

	public void setOriginImageFilePath(String originImageFilePath) {
		this.originImageFilePath = originImageFilePath;
	}

	public String getGrayImageFilePath() {
		return grayImageFilePath;
	}

	public void setGrayImageFilePath(String grayImageFilePath) {
		this.grayImageFilePath = grayImageFilePath;
	}

	public String getBinaryImageFilePath() {
		return binaryImageFilePath;
	}

	public void setBinaryImageFilePath(String binaryImageFilePath) {
		this.binaryImageFilePath = binaryImageFilePath;
	}

	/**
	 * 把代表颜色的四通道，4个8bit的整型合成为ImageIO能识别的一个32位的整型
	 * 
	 * @param alpha
	 *            alpha通道的值
	 * @param red
	 *            红色值
	 * @param green
	 *            绿色值
	 * @param blue
	 *            蓝色值
	 * @return ImageIO能识别的32位颜色值
	 */
	private static int colorToRGB(int alpha, int red, int green, int blue) {

		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;

	}

}