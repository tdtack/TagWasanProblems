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

public class NewCanvas extends Canvas {
	public ImageProcessing imgProc;// 画像処理に関する変数
	public HoughTransform houghTrans;// Hough変換に関する変数
	public ElementAnalysis elemAnal;// Hough変換に基づいた検出に関する変数
	public AutomaticTag autoTag;
	// Imageの変数で表示させるものを2つ用意する?

	public NewCanvas(String filePath) {
		// ①画像読み込み
		// ②Hough変換で抽出
		// ③Hough変換で得たデータを中心に補正も加えながら、抽出の最終決定
		imgProc = new ImageProcessing(filePath);// 画像処理クラス
		houghTrans = new HoughTransform(imgProc);// (主に幾何要素の抽出)
		elemAnal = new ElementAnalysis(houghTrans);// (幾何要素に基づいた図形要素の抽出)
		autoTag = new AutomaticTag(imgProc, elemAnal);

		scanGeometricElement();
		detectGraphicElement();
		tagProblem();
	}

	private void scanGeometricElement() {
		// 円の検出から排除まで
		houghTrans.voteFieldCircle();// fieldCircle[][]というメンバ変数に値を入れるのでvoid型
		elemAnal.scanCircle(10);// circleelemAnalnedというメンバ変数に入れるのでvoid型
		imgProc.removeCircle(elemAnal.scannedCircle);// editingImgというメンバ変数を改良するのでvoid型

		// 線分の検出から排除まで
		houghTrans.voteFieldLine();// fieldLine[][]というメンバ変数に値を入れるのでvoid型
		elemAnal.scanLine(15);// lineelemAnalnedというメンバ変数に入れるのでvoid型
		imgProc.removeLine(elemAnal.scannedLine);// 6

		// 点の検出から排除まで
		elemAnal.scanPoint();
	}

	private void detectGraphicElement() {
		// 関連性の定義
		elemAnal.relateElement();
		elemAnal.detectElement();// ここでDetectedのリストに追加される(検出に関する最終決定)

		// 幾何要素と文字の分類
		imgProc.clipImage(elemAnal.detectedLine, elemAnal.detectedCircle);// 残された文字の検出//最初から文字画像作成の関数でもいいのでは?

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

	private void tagProblem() {
		// タグ結果
		autoTag.printTag();// コンソールに表示しつつ、カウントする
		autoTag.generateVector();

		System.out.println("--------------------------------------------------");
	}

	public void freeResource() {
		imgProc.freeResource();
		houghTrans.freeResource();
		elemAnal.freeResource();
		autoTag.freeResource();
	}

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

		// g2.dispose();
	}

	public void update(Graphics g) {
		// void draw (repaintで自動呼出し、特に必要なし?)
	}
}

