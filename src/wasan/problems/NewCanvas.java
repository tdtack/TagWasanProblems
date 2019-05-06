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

//��
/**
 * ���N���X�𓝍��I�Ɉ����Ęa�Z�}�`���Ƀ^�O�t�����s���A���̌��ʂ�\������N���X�ł��B <br>
 * Java�̕W���N���X�ł���Canvas�N���X���T�u�N���X�����ė��p���܂��B
 * 
 * @author Takuma Tsuchihashi
 */
public class NewCanvas extends Canvas {

	// ��
	/**
	 * �摜�����𗘗p���邽�߂�ImageProcessing�N���X�ϐ��ł��B<br>
	 * ���̕ϐ���p���邱�Ƃ�ImageProcessing�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	public ImageProcessing imgProc;

	// ��
	/**
	 * Hough�ϊ��𗘗p���邽�߂�HoughTransform�N���X�ϐ��ł��B<br>
	 * ���̕ϐ���p���邱�Ƃ�HoughTransform�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	public HoughTransform houghTrans;

	// ��
	/**
	 * �v�f���͂𗘗p���邽�߂�ElementAnalysis�N���X�ϐ��ł��B<br>
	 * ���̕ϐ���p���邱�Ƃ�ElementAnalysis�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	public ElementAnalysis elemAnal;

	// ��
	/**
	 * �^�O�t���𗘗p���邽�߂�AutomaticTag�N���X�ϐ��ł��B<br>
	 * ���̕ϐ���p���邱�Ƃ�AutomaticTag�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	public AutomaticTag autoTag;

	// ��
	/**
	 * �}�`���̉摜�t�@�C���p�X���w�肵�A�\������L�����o�X(NewCanvas)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param filePath
	 *            ���͂Ƃ���}�`���̉摜�t�@�C���p�X��\��String�^�ϐ�
	 */
	public NewCanvas(String filePath) {
		imgProc = new ImageProcessing(filePath);// �}�`���̉摜�t�@�C���p�X���w�肵�A�摜����(ImageProcessing)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B
		houghTrans = new HoughTransform(imgProc);// �摜����(ImageProcessing)���w�肵�AHough�ϊ�(HoughTransform)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B
		elemAnal = new ElementAnalysis(houghTrans);// Hough�ϊ�(HoughTransform)���w�肵�A�v�f����(ElementAnalysis)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B
		autoTag = new AutomaticTag(imgProc, elemAnal);// �摜����(ImageProcessing)�Ɨv�f����(ElementAnalysis)���w�肵�A�^�O�t��(AutomaticTag)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B

		scanGeometricElement();
		detectGraphicElement();
		tagProblem();
	}

	// ��
	/**
	 * �����^�O�t�������������}�`���Ɋւ���s�v�ȃI�u�W�F�N�g�����������������܂��B<br>
	 * �����̐}�`���ɑ΂��ĘA���I�Ɏ����^�O�t�����s���ہAOutOfMemoryError��������܂��B
	 */
	public void freeResource() {
		imgProc.freeResource();
		houghTrans.freeResource();
		elemAnal.freeResource();
		autoTag.freeResource();
	}

	// ��
	/**
	 * Hough�ϊ��𗘗p���Đ}�`��肩��􉽗v�f(�_�E�����E�~)�𒊏o���܂��B<br>
	 * �􉽗v�f�͉~���������_�̏��ɒ��o����܂��B
	 */
	private void scanGeometricElement() {
		// �ȉ��A�~�̒��o�ł��B
		houghTrans.voteFieldCircle();//
		elemAnal.scanCircle(10);// Hough�ϊ��𗘗p���Đ}�`��肩��~�������o���܂��B
		imgProc.removeCircle(elemAnal.scannedCircle);// �}�`���̉摜���璊�o�����~���������܂��B

		// �ȉ��A�����̒��o�ł��B
		houghTrans.voteFieldLine();//
		elemAnal.scanLine(15);// Hough�ϊ��𗘗p���Đ}�`��肩������������o���܂��B
		imgProc.removeLine(elemAnal.scannedLine);// �}�`���̉摜���璊�o���ꂽ�������������܂��B

		// �ȉ��A�_�̒��o�ł��B
		elemAnal.scanPoint();// Hough�ϊ��𗘗p���ĉ����o���ꂽ�����Ɖ~�Ɋ�Â��A�}�`��肩��_�������o���܂��B
	}

	// ��
	/**
	 * �}�`��肩�璊�o���ꂽ�􉽗v�f(�_�E�����E�~)�Ɋ�Â��āA�}�`�v�f(n�p�`�E�~)�𕪐͂��܂��B<br>
	 * �܂��A�}�`���̌��摜����A�􉽗v�f�𒊏o�����摜�ƕ����v�f�𒊏o�����摜�𐶐����܂��B
	 */
	private void detectGraphicElement() {
		elemAnal.relateElement();
		elemAnal.detectElement();//

		imgProc.clipImage(elemAnal.detectedLine, elemAnal.detectedCircle);// �}�`���Ɋ܂܂��􉽗v�f�A�}�`�v�f�𒊏o�����摜����ѕ����v�f�𒊏o�����摜�𐶐����܂��B

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

	// ��
	/**
	 * �}�`��肩�番�͂��ꂽ�}�`�v�f(n�p�`�E�~)�Ɋ�Â��āA�^�O��t�^���܂��B<br>
	 * �܂��A�}�`���ɕt�^�����^�O��������x�N�g���𐶐����܂��B
	 */
	private void tagProblem() {
		autoTag.printTag();// �}�`���ɕt�^���ꂽ�^�O�̓��e���e�L�X�g�t�@�C���ɏ������݁A�܂��̓R���\�[���ɕ\�����܂��B
		autoTag.generateVector();// �}�`���ɕt�^���ꂽ�^�O�Ɋ�Â��A�����x�N�g���𐶐����܂��B

		System.out.println("--------------------------------------------------");
	}

	// ��
	/**
	 * �}�`���Ɋ܂܂��􉽗v�f(�_�E�����E�~)�̒��o���ʂ��L�����o�X�ɕ`�悵�A�\�����܂��B<br>
	 * �L�����o�X�̍����ɂ͊􉽗v�f�̒��o���ʁA�E���ɂ͊􉽗v�f�̐؂�o�����ʂ��\������܂��B
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
