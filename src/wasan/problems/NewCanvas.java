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
	public ImageProcessing imgProc;// �摜�����Ɋւ���ϐ�
	public HoughTransform houghTrans;// Hough�ϊ��Ɋւ���ϐ�
	public ElementAnalysis elemAnal;// Hough�ϊ��Ɋ�Â������o�Ɋւ���ϐ�
	public AutomaticTag autoTag;
	// Image�̕ϐ��ŕ\����������̂�2�p�ӂ���?

	public NewCanvas(String filePath) {
		// �@�摜�ǂݍ���
		// �AHough�ϊ��Œ��o
		// �BHough�ϊ��œ����f�[�^�𒆐S�ɕ␳�������Ȃ���A���o�̍ŏI����
		imgProc = new ImageProcessing(filePath);// �摜�����N���X
		houghTrans = new HoughTransform(imgProc);// (��Ɋ􉽗v�f�̒��o)
		elemAnal = new ElementAnalysis(houghTrans);// (�􉽗v�f�Ɋ�Â����}�`�v�f�̒��o)
		autoTag = new AutomaticTag(imgProc, elemAnal);

		scanGeometricElement();
		detectGraphicElement();
		tagProblem();
	}

	private void scanGeometricElement() {
		// �~�̌��o����r���܂�
		houghTrans.voteFieldCircle();// fieldCircle[][]�Ƃ��������o�ϐ��ɒl������̂�void�^
		elemAnal.scanCircle(10);// circleelemAnalned�Ƃ��������o�ϐ��ɓ����̂�void�^
		imgProc.removeCircle(elemAnal.scannedCircle);// editingImg�Ƃ��������o�ϐ������ǂ���̂�void�^

		// �����̌��o����r���܂�
		houghTrans.voteFieldLine();// fieldLine[][]�Ƃ��������o�ϐ��ɒl������̂�void�^
		elemAnal.scanLine(15);// lineelemAnalned�Ƃ��������o�ϐ��ɓ����̂�void�^
		imgProc.removeLine(elemAnal.scannedLine);// 6

		// �_�̌��o����r���܂�
		elemAnal.scanPoint();
	}

	private void detectGraphicElement() {
		// �֘A���̒�`
		elemAnal.relateElement();
		elemAnal.detectElement();// ������Detected�̃��X�g�ɒǉ������(���o�Ɋւ���ŏI����)

		// �􉽗v�f�ƕ����̕���
		imgProc.clipImage(elemAnal.detectedLine, elemAnal.detectedCircle);// �c���ꂽ�����̌��o//�ŏ����當���摜�쐬�̊֐��ł������̂ł�?

		try {
			ImageIO.write(imgProc.elementImg, "png", new File("dat/output/image/element/element_" + imgProc.imgName));
			ImageIO.write(imgProc.characterImg, "png",
					new File("dat/output/image/character/character_" + imgProc.imgName));
		} catch (IOException e) {
			StackTraceElement[] ste = e.getStackTrace();
			System.err.println("��O���� : " + e.getClass().getName());
			System.err.println("��O���e : " + e.getMessage());
			System.err.println("�����ꏊ : " + ste[ste.length - 1]);
			System.exit(0);
		}
	}

	private void tagProblem() {
		// �^�O����
		autoTag.printTag();// �R���\�[���ɕ\�����A�J�E���g����
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
		// void draw (repaint�Ŏ����ďo���A���ɕK�v�Ȃ�?)
	}
}

