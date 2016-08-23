package image;

import java.awt.TextField;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class ImageViewerFrame extends JFrame {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	int DEFAULT_WIDTH = 400;
	int DEFAULT_HEIGHT = 300;
	private JLabel label;
	private TextField textfield;
	JButton button;

	public ImageViewerFrame(String path) {
		setTitle("ImageViewer");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		label = new JLabel();
		textfield = new TextField();
		button = new JButton();
		button.setActionCommand("input");
		add(label);
//		add(textfield);
		String name = path;
		label.setText("�Զ�ʶ����֤��ʧ�ܣ����ֶ�������֤�룺");
		label.setIcon(new ImageIcon(name));
	}
//
//	public String actionPerformed(ActionEvent e) {
//		// disableΪ��һ�����������ʱ����Ӧ��Ϣ
//		if ("input".equals(e.getActionCommand())) {
//			return textfield.getText();
//		}
//		return null;
//
//	}

	public void showImage() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		this.toFront();
	}

}