package wasan.problems;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

//☆
/**
 * 他クラスを統合的に扱って和算図形問題にタグ付けを行い、その結果を表示するクラスです。 <br>
 * Javaの標準クラスであるCanvasクラスをサブクラス化して利用します。
 * 
 * @author Takuma Tsuchihashi
 */
public class NewCanvas extends Canvas {

	// ☆
	/**
	 * 画像処理を利用するためのImageProcessingクラス変数です。<br>
	 * この変数を用いることでImageProcessingクラス内のメソッドなどを呼び出すことができます。
	 */
	public ImageProcessing imgProc;

	// ☆
	/**
	 * Hough変換を利用するためのHoughTransformクラス変数です。<br>
	 * この変数を用いることでHoughTransformクラス内のメソッドなどを呼び出すことができます。
	 */
	public HoughTransform houghTrans;

	// ☆
	/**
	 * 要素分析を利用するためのElementAnalysisクラス変数です。<br>
	 * この変数を用いることでElementAnalysisクラス内のメソッドなどを呼び出すことができます。
	 */
	public ElementAnalysis elemAnal;

	// ☆
	/**
	 * タグ付けを利用するためのAutomaticTagクラス変数です。<br>
	 * この変数を用いることでAutomaticTagクラス内のメソッドなどを呼び出すことができます。
	 */
	public AutomaticTag autoTag;

	// ☆
	/**
	 * 図形問題の画像ファイルパスを指定し、表示するキャンバス(NewCanvas)のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param filePath
	 *            入力とする図形問題の画像ファイルパスを表すString型変数
	 */
	public NewCanvas(String filePath) {
		imgProc = new ImageProcessing(filePath);// 図形問題の画像ファイルパスを指定し、画像処理(ImageProcessing)のインスタンスを生成するコンストラクタです。
		houghTrans = new HoughTransform(imgProc);// 画像処理(ImageProcessing)を指定し、Hough変換(HoughTransform)のインスタンスを生成するコンストラクタです。
		elemAnal = new ElementAnalysis(houghTrans);// Hough変換(HoughTransform)を指定し、要素分析(ElementAnalysis)のインスタンスを生成するコンストラクタです。
		autoTag = new AutomaticTag(imgProc, elemAnal);// 画像処理(ImageProcessing)と要素分析(ElementAnalysis)を指定し、タグ付け(AutomaticTag)のインスタンスを生成するコンストラクタです。

		scanGeometricElement();
		detectGraphicElement();
		tagProblem();
	}

	// ☆
	/**
	 * 自動タグ付けが完了した図形問題に関する不要なオブジェクトをメモリから解放します。<br>
	 * 複数の図形問題に対して連続的に自動タグ付けを行う際、OutOfMemoryErrorを回避します。
	 */
	public void freeResource() {
		imgProc.freeResource();
		houghTrans.freeResource();
		elemAnal.freeResource();
		autoTag.freeResource();
	}

	// ☆
	/**
	 * Hough変換を利用して図形問題から幾何要素(点・線分・円)を抽出します。<br>
	 * 幾何要素は円→線分→点の順に抽出されます。
	 */
	private void scanGeometricElement() {
		// 以下、円の抽出です。
		houghTrans.voteFieldCircle();//
		elemAnal.scanCircle(10);// Hough変換を利用して図形問題から円を仮抽出します。
		imgProc.removeCircle(elemAnal.scannedCircle);// 図形問題の画像から抽出した円を除去します。

		// 以下、線分の抽出です。
		houghTrans.voteFieldLine();//
		elemAnal.scanLine(15);// Hough変換を利用して図形問題から線分を仮抽出します。
		imgProc.removeLine(elemAnal.scannedLine);// 図形問題の画像から抽出された線分を除去します。

		// 以下、点の抽出です。
		elemAnal.scanPoint();// Hough変換を利用して仮抽出された線分と円に基づき、図形問題から点を仮抽出します。
	}

	// ☆
	/**
	 * 図形問題から抽出された幾何要素(点・線分・円)に基づいて、図形要素(n角形・円)を分析します。<br>
	 * また、図形問題の元画像から、幾何要素を抽出した画像と文字要素を抽出した画像を生成します。
	 */
	private void detectGraphicElement() {
		elemAnal.relateElement();
		elemAnal.detectElement();//

		imgProc.clipImage(elemAnal.detectedLine, elemAnal.detectedCircle);// 図形問題に含まれる幾何要素、図形要素を抽出した画像および文字要素を抽出した画像を生成します。

		try {
			ImageIO.write(imgProc.elementImg, "png", new File("dat/output/image/element/element_" + imgProc.imgName));
			ImageIO.write(imgProc.characterImg, "png",
					new File("dat/output/image/character/character_" + imgProc.imgName));
		} catch (IOException e) {
			StackTraceElement[] ste = e.getStackTrace();
			System.err.println("例外発生 : " + e.getClass().getName());
			System.err.println("例外内容 : " + e.getMessage());
			System.err.println("発生場所 : " + ste[ste.length - 1]);
			System.exit(0);
		}
	}

	// ☆
	/**
	 * 図形問題から分析された図形要素(n角形・円)に基づいて、タグを付与します。<br>
	 * また、図形問題に付与したタグから特徴ベクトルを生成します。
	 */
	private void tagProblem() {
		autoTag.printTag();// 図形問題に付与されたタグの内容をテキストファイルに書き込み、またはコンソールに表示します。
		autoTag.generateVector();// 図形問題に付与されたタグに基づき、特徴ベクトルを生成します。

		System.out.println("--------------------------------------------------");
	}

	// ☆
	/**
	 * 図形問題に含まれる幾何要素(点・線分・円)の抽出結果をキャンバスに描画し、表示します。<br>
	 * キャンバスの左側には幾何要素の抽出結果、右側には幾何要素の切り出し結果が表示されます。
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		int imageWidth = imgProc.originalImg.getWidth();
		int imageHeight = imgProc.originalImg.getHeight();

		g2.drawImage(imgProc.characterImg, 0, 0, imageWidth, imageHeight, this);
		g2.drawImage(imgProc.elementImg, imageWidth, 0, imageWidth, imageHeight, this);

		int fontSize = 16;
		g2.setFont(new Font("Arial", Font.PLAIN, fontSize));
		g2.setStroke(new BasicStroke(3.0f));

		g2.setColor(new Color(255, 0, 0));
		for (int i = 0; i < elemAnal.detectedCircle.size(); i++) {
			MyCircle c = elemAnal.detectedCircle.get(i);
			g2.drawOval((int) (c.center.x - c.radius), (int) (c.center.y - c.radius), (int) (2 * c.radius),
					(int) (2 * c.radius));
			g2.drawString("c[" + i + "]", (int) c.circum[0].x - fontSize, (int) c.circum[0].y);
		}
		for (int i = 0; i < elemAnal.detectedLine.size(); i++) {
			MyLine l = elemAnal.detectedLine.get(i);

			g2.drawLine((int) l.start.x, (int) l.start.y, (int) l.end.x, (int) l.end.y);
			g2.drawString("l[" + i + "]", (int) ((l.start.x + l.end.x) / 2 - fontSize / 2),
					(int) ((l.start.y + l.end.y) / 2 + fontSize / 2));
		}

		g2.setColor(new Color(0, 255, 0));
		for (int i = 0; i < elemAnal.detectedPoint.size(); i++) {
			MyPoint p = elemAnal.detectedPoint.get(i);
			g2.fillOval((int) (p.x - fontSize / 4), (int) (p.y - fontSize / 4), fontSize / 2, fontSize / 2);
			g2.drawOval((int) (p.x - fontSize / 4), (int) (p.y - fontSize / 4), fontSize / 2, fontSize / 2);

			g2.drawString("p[" + i + "]", (int) p.x - fontSize / 2, (int) p.y + fontSize);
		}
	}
}
