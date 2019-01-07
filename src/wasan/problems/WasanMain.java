package wasan.problems;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

public class WasanMain {
	public static void main(String[] args) throws IOException {
		File folder = new File("dat/input/problems");
		File[] file = folder.listFiles();
		int fileNum = file.length;
		// int fileNum = 1;

		NewFrame[] frame = new NewFrame[fileNum];
		NewCanvas[] canvas = new NewCanvas[fileNum];

		int displayNum = 3;
		for (int i = 0; i < fileNum; i++) {
			if (i > displayNum) {
				try {
					int index = i - (displayNum + 1);

					frame[index].removeNotify();
					frame[index] = null;

					canvas[index].freeResource();
					canvas[index] = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String filePath = String.valueOf(file[i]);
			// String filePath = folder.getPath() + "\\" + "026" + ".PNG";
			String imgName = filePath.split("\\\\", 0)[3];// Åô
			String imgNum = imgName.substring(0, imgName.indexOf("."));// Åô

			File tag = new File("dat/output/tag/tag_" + imgNum + ".txt");

			try (FileOutputStream fos = new FileOutputStream(tag, false);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					PrintStream ps = new PrintStream(bos)) {
				System.setOut(ps);

				frame[i] = new NewFrame();
				canvas[i] = new NewCanvas(filePath);

				frame[i].add(canvas[i]);
				frame[i].addWindowListener(new NewAdapter());// frame.setUndecorated(true);
				frame[i].addNotify();

				Insets insets = frame[i].getInsets();
				BufferedImage canvasImg = canvas[i].imgProc.originalImg;
				int canvasWidth = canvasImg.getWidth();
				int canvasHeight = canvasImg.getHeight();
				int frameWidth = canvasWidth * 2 + insets.left + insets.right;
				int frameHeight = canvasHeight + insets.top + insets.bottom;

				frame[i].setSize(frameWidth, frameHeight);
				frame[i].setVisible(true);

				saveDisplay(canvas[i], canvas[i].imgProc.imgName);

			} catch (IOException e) {
				StackTraceElement[] ste = e.getStackTrace();
				System.err.println("ó·äOî≠ê∂ : " + e.getClass().getName());
				System.err.println("ó·äOì‡óe : " + e.getMessage());
				System.err.println("î≠ê∂èÍèä : " + ste[ste.length - 1]);
				System.exit(0);
			}
		}

		System.out.println();
		System.out.println("complete");

		System.exit(0);
	}

	private static void saveDisplay(NewCanvas canvas, String imgName) {
		BufferedImage resultImg = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D g2 = (Graphics2D) resultImg.getGraphics();
		canvas.paint(g2);

		try {
			ImageIO.write(resultImg, "png", new File("dat/output/image/display/display_" + imgName));
		} catch (Exception e) {
			//
		}
	}
}

