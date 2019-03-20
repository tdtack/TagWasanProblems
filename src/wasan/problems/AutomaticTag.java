package wasan.problems;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * �a�Z�}�`���ւ̎����^�O�t���A�����x�N�g�������Ɋւ���N���X�ł��B
 * ImageProcessing�N���X��ElementAnalysis�N���X�𗘗p���A�}�`��肩�瓾��ꂽ���Ɋ�Â��ă^�O�t�����s���܂��B
 * �܂��A�}�`���ɕt�^���ꂽ�^�O�̍��v��������x�N�g���𐶐����܂��B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class AutomaticTag {

	/**
	 * ImageProcessing�N���X�𗘗p���邽�߂̕ϐ��ł��B
	 * ���̕ϐ���p���邱�Ƃ�ImageProcessing�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	private ImageProcessing imgProc;

	/**
	 * ElementAnalysis�N���X�𗘗p���邽�߂̕ϐ��ł��B
	 * ���̕ϐ���p���邱�Ƃ�ElementAnalysis�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	private ElementAnalysis elemAnal;

	/**
	 * CharacterRecognition�N���X�𗘗p���邽�߂̕ϐ��ł��B
	 * ���̕ϐ���p���邱�Ƃ�CharacterRecognition�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	private CharacterRecognition charRec;

	// �ȉ��A�􉽗v�f�̃^�O��\���܂��B
	
	/** �􉽗v�f�́u�_�v�̃^�O��\���܂��B */
	private String pointTag = "�_";
	/** �􉽗v�f�́u�����v�̃^�O��\���܂��B */
	private String lineTag = "����";
	/** �􉽗v�f�́u�~�v�̃^�O��\���܂��B�܂��A���̃^�O�͐}�`�v�f�̃^�O�Ƃ��Ă������܂��B*/
	private String circleTag = "�~";
	
	// �ȉ��A�}�`�v�f(n�p�`�̂�)�̃^�O��\���܂��B
	
	/** �}�`�v�f�́un�p�`�v�̃^�O��\���܂��B�^�O�̓��e�́u�O�p�`�v�u�l�p�`�v�u�܊p�`�v�u�Z�p�`�v�ł��B */
	private String[] polygonTag = { "�O�p�`", "�l�p�`", "�܊p�`", "�Z�p�`" };
	/** n�p�`�̂����A�����I�ȎO�p�`�̃^�O��\���܂��B�^�O�̓��e�́u�O�p�`�v�u���O�p�`�v�u�񓙕ӎO�p�`�v�u���p�O�p�`�v�ł��B */
	private String[] triangleTag = { polygonTag[0], "���O�p�`", "�񓙕ӎO�p�`", "���p�O�p�`" };
	/** n�p�`�̂����A�����I�Ȏl�p�`�̃^�O��\���܂��B�^�O�̓��e�́u�l�p�`�v�u�����`�v�u�����`�v�u�H�`�v�u���r��`�v�ł��B */
	private String[] quadrangleTag = { polygonTag[1], "�����`", "�����`", "�H�`", "���r��`" };
	/** n�p�`�̂����A�����I�Ȍ܊p�`�̃^�O��\���܂��B�^�O�̓��e�́u�܊p�`�v�u���܊p�`�v�ł��B */
	private String[] pentagonTag = { polygonTag[2], "���܊p�`" };
	/** n�p�`�̂����A�����I�ȘZ�p�`�̃^�O��\���܂��B�^�O�̓��e�́u�Z�p�`�v�u���Z�p�`�v�ł��B */
	private String[] hexagonTag = { polygonTag[3], "���Z�p�`" };
	
	// �ȉ��A�}�`�v�f(n�p�`�̂�)�̃^�O���v��\���܂��B

	/** n�p�`�̂����A�����I�ȎO�p�`�̃^�O���v��ێ����܂��B(�O�p�`�E���O�p�`�E�񓙕ӎO�p�`�E���p�O�p�`) */
	private int[] triangleNum = new int[triangleTag.length];
	/** n�p�`�̂����A�����I�Ȏl�p�`�̃^�O���v��ێ����܂��B(�l�p�`�E�����`�E�����`�E�H�`�E���r��`) */
	private int[] quadrangleNum = new int[quadrangleTag.length];
	/** n�p�`�̂����A�����I�Ȍ܊p�`�̃^�O���v��ێ����܂��B(�܊p�`�E���܊p�`) */
	private int[] pentagonNum = new int[pentagonTag.length];
	/** n�p�`�̂����A�����I�ȘZ�p�`�̃^�O���v��ێ����܂��B(�Z�p�`�E���Z�p�`) */
	private int[] hexagonNum = new int[hexagonTag.length];
	
	//
	
	/** �����Ɖ~�̊֌W���̃^�O��\���܂��B(�uX��Y���ڂ���v�E�uX��Y��1�_�Ō����v�E�uX��Y��2�_�Ō����v) */
	private String[] relationLCTag = { "X��Y���ڂ���.", "X��Y��1�_�Ō����.", "X��Y��2�_�Ō����." };
	/** n�p�`���猩���~�Ƃ̊֌W���̃^�O��\���܂��B(�uX��Y�ɓ��ڂ���v�E�uX��Y�̓����ɑ��݂���v�E�uX��Y���݂��ɗאڂ���v) */
	private String[] relationPCTag = { "X��Y�ɓ��ڂ���.", "X��Y�̓����ɑ��݂���.", "X��Y���݂��ɗאڂ���." };
	/** �~���猩��n�p�`�Ƃ̊֌W���̃^�O��\���܂��B(�uX��Y�ɓ��ڂ���v�E�uX��Y�̓����ɑ��݂���v�E�uX��Y���݂��ɏd�Ȃ荇���v) */
	private String[] relationCPTag = { "X��Y�ɓ��ڂ���.", "X��Y�̓����ɑ��݂���.", "X��Y���݂��ɏd�Ȃ荇��." };
	/** n�p�`���m�̊֌W���̃^�O��\���܂��B(�uX��Y�ɓ��ڂ���v�E�uX��Y�̓����ɑ��݂���v�E�uX��Y���݂��ɗאڂ���v�E�uX��Y���݂��ɏd�Ȃ荇���v) */
	private String[] relationPPTag = { "X��Y�ɓ��ڂ���.", "X��Y�̓����ɑ��݂���.", "X��Y���݂��ɗאڂ���.", "X��Y���݂��ɏd�Ȃ荇��." };
	/** �~���m�̊֌W���̃^�O��\���܂��B(�uX��Y�̓����Őڂ���v�E�uX��Y�̓����ɑ��݂���v�E�uX��Y���݂��ɊO�ڂ���v�E�uX��Y���݂��ɏd�Ȃ荇���v) */
	private String[] relationCCTag = { "X��Y�̓����Őڂ���.", "X��Y�̓����ɑ��݂���.", "X��Y���݂��ɊO�ڂ���.", "X��Y���݂��ɏd�Ȃ荇��." };
	
	/** �����Ɖ~�̊֌W���̃^�O���v��ێ����܂��B(�uX��Y���ڂ���v�E�uX��Y��1�_�Ō����v�E�uX��Y��2�_�Ō����v) */
	private int[] relationLCNum = new int[relationLCTag.length];
	/** n�p�`���猩���~�Ƃ̊֌W���̃^�O���v��ێ����܂��B(�uX��Y�ɓ��ڂ���v�E�uX��Y�̓����ɑ��݂���v�E�uX��Y���אڂ���v) */
	private int[][] relationPCNum = new int[polygonTag.length][relationPCTag.length];
	/** �~���猩��n�p�`�Ƃ̊֌W���̃^�O���v��ێ����܂��B(�uX��Y�ɓ��ڂ���v�E�uX��Y�̓����ɑ��݂���v�E�uX��Y���d�Ȃ荇���v) */
	private int[][] relationCPNum = new int[polygonTag.length][relationCPTag.length];
	/** n�p�`���m�̊֌W���̃^�O���v�@��ێ����܂��B(�uX��Y�ɓ��ڂ���v�E�uX��Y�̓����ɑ��݂���v) */
	private int[][] relationPPNum1 = new int[16][2];
	/** n�p�`���m�̊֌W���̃^�O���v�A��ێ����܂��B(�uX��Y���אڂ���v�E�uX��Y���d�Ȃ荇���v) */
	private int[][] relationPPNum2 = new int[10][2];
	/** �~���m�̊֌W���̃^�O���v�@��ێ����܂��B(�uX��Y�̓����Őڂ���v�E�uX��Y�̓����ɑ��݂���v) */
	private int[] relationCCNum1 = new int[2];
	/** �~���m�̊֌W���̃^�O���v�A��ێ����܂��B(�uX��Y���O�ڂ���v�E�uX��Y���d�Ȃ荇���v) */
	private int[] relationCCNum2 = new int[2];

	/**
	 * ImageProcessing��ElementAnalysis���w�肵�AAutomaticTag�I�u�W�F�N�g���쐬���܂��B
	 * 
	 * @param _imgProc  ImageProcessing�N���X
	 * @param _elemAnal ElementAnalysis�N���X
	 */
	public AutomaticTag(ImageProcessing _imgProc, ElementAnalysis _elemAnal) {
		this.imgProc = _imgProc;
		this.elemAnal = _elemAnal;
	}

	/**
	 * 
	 */
	public void freeResource() {
		this.imgProc = null;
		this.elemAnal = null;
	}

	public void printTag() {
		System.out.println("< �}�`�v�f >");
		printElementTag();

		System.out.println("< �}�`�v�f���m�̊֌W�� >");
		printRelationTag();

		// System.out.println("< �����v�f >");
		// printCharacterTag();
	}

	private void printElementTag() {
		int polygonNum = 0;

		ArrayList<MyPoint> pointList = this.elemAnal.detectedPoint;
		ArrayList<MyPolygon> polygonList = this.elemAnal.detectedPolygon;
		ArrayList<MyCircle> circleList = this.elemAnal.detectedCircle;

		for (int i = 0; i < polygonList.size(); i++) {
			String polygonName = polygonList.get(i).getPolygonName();

			System.out.print("�E " + polygonTag[polygonList.get(i).vertex.size() - 3] + " > ");
			System.out.print(polygonName + "(");
			for (int j = 0; j < polygonList.get(i).vertex.size(); j++) {
				System.out.print("P[" + pointList.indexOf(polygonList.get(i).vertex.get(j)) + "]");
			}
			System.out.println(")");

			switch (polygonList.get(i).vertex.size()) {
			case 3:
				for (int k = 1; k < triangleTag.length; k++) {
					if (polygonName.equals(triangleTag[k])) {
						triangleNum[k]++;
						break;
					}
				}
				break;
			case 4:
				for (int k = 1; k < quadrangleTag.length; k++) {
					if (polygonName.equals(quadrangleTag[k])) {
						quadrangleNum[k]++;
						break;
					}
				}
				break;
			case 5:
				for (int k = 1; k < pentagonTag.length; k++) {
					if (polygonName.equals(pentagonTag[k])) {
						pentagonNum[k]++;
						break;
					}
				}
				break;
			case 6:
				for (int k = 1; k < hexagonTag.length; k++) {
					if (polygonName.equals(hexagonTag[k])) {
						hexagonNum[k]++;
						break;
					}
				}
				break;
			}

			polygonNum++;

			boolean polygonNumSet = false;
			if (i == polygonList.size() - 1) {
				System.out.println("�E " + polygonTag[polygonList.get(i).vertex.size() - 3] + " �~ " + polygonNum);
				polygonNumSet = true;
			} else if (polygonList.get(i).vertex.size() < polygonList.get(i + 1).vertex.size()) {
				System.out.println("�E " + polygonTag[polygonList.get(i).vertex.size() - 3] + " �~ " + polygonNum);
				polygonNumSet = true;
			}

			if (polygonNumSet) {
				switch (polygonList.get(i).vertex.size()) {
				case 3:
					triangleNum[0] = polygonNum;
					break;
				case 4:
					quadrangleNum[0] = polygonNum;
					break;
				case 5:
					pentagonNum[0] = polygonNum;
					break;
				case 6:
					hexagonNum[0] = polygonNum;
					break;
				}
				polygonNum = 0;
			}
		}

		for (int i = 0; i < circleList.size(); i++) {
			System.out.println("�E " + circleTag + " > " + circleTag + "(" + "C[" + i + "]" + ")");
		}
		if (circleList.size() > 0) {
			System.out.println("�E " + circleTag + " �~ " + circleList.size());
		}
		System.out.println();
	}

	private void printRelationTag() {
		printRelationLC();// R1
		printRelationPC();// R2
		printRelationPP();// R4
		printRelationCP();// R3
		printRelationCC();// R5

		System.out.println();
	}

	private void printRelationLC() {
		ArrayList<MyLine> lineList = this.elemAnal.detectedLine;
		ArrayList<MyCircle> circleList = this.elemAnal.detectedCircle;

		for (int i = 0; i < circleList.size(); i++) {
			MyCircle c = circleList.get(i);
			for (int j = 0; j < lineList.size(); j++) {
				MyLine l = lineList.get(j);

				if (l.contactCircle(c)) {
					relationLCNum[0]++;
				} else if (l.intersectCircle1(c)) {
					relationLCNum[1]++;
				} else if (l.intersectCircle2(c)) {
					relationLCNum[2]++;
				}
			}
		}
	}

	private void printRelationPC() {
		ArrayList<MyPoint> pointList = this.elemAnal.detectedPoint;
		ArrayList<MyCircle> circleList = this.elemAnal.detectedCircle;
		ArrayList<MyPolygon> polygonList = this.elemAnal.detectedPolygon;

		for (int i = 0; i < polygonList.size(); i++) {
			MyPolygon pg = polygonList.get(i);
			for (int j = 0; j < circleList.size(); j++) {
				MyCircle c = circleList.get(j);

				String name1 = pg.getPolygonName() + "(";
				for (int k = 0; k < pg.vertex.size(); k++) {
					name1 += "P[" + pointList.indexOf(pg.vertex.get(k)) + "]";
				}
				name1 += ")";

				String name2 = "�~(C[" + circleList.indexOf(c) + "])";

				int index = pg.vertex.size() - 3;
				if (pg.inscribeCircle(c)) {// n�p�`���~�ɓ��ڂ���
					System.out.println("�E " + setRelationTag(relationPCTag[0], name1, name2));
					relationPCNum[index][0]++;
				} else if (pg.insideCircle(c)) {// n�p�`���~�̓����ɑ��݂���
					System.out.println("�E " + setRelationTag(relationPCTag[1], name1, name2));
					relationPCNum[index][1]++;
				} else if (pg.adjoinCircle(c)) {// n�p�`�Ɖ~���אڂ���
					System.out.println("�E " + setRelationTag(relationPCTag[2], name1, name2));
					relationPCNum[index][2]++;
				}
			}
		}
	}

	private void printRelationCP() {
		ArrayList<MyPoint> pointList = this.elemAnal.detectedPoint;
		ArrayList<MyPolygon> polygonList = this.elemAnal.detectedPolygon;
		ArrayList<MyCircle> circleList = this.elemAnal.detectedCircle;

		for (int i = 0; i < circleList.size(); i++) {
			MyCircle c = circleList.get(i);
			for (int j = 0; j < polygonList.size(); j++) {
				MyPolygon pg = polygonList.get(j);

				String name1 = "�~(C[" + circleList.indexOf(c) + "])";

				String name2 = pg.getPolygonName() + "(";
				for (int k = 0; k < pg.vertex.size(); k++) {
					name2 += "P[" + pointList.indexOf(pg.vertex.get(k)) + "]";
				}
				name2 += ")";

				int index = pg.vertex.size() - 3;
				if (c.inscribePolygon(pg)) {
					System.out.println("�E " + setRelationTag(relationCPTag[0], name1, name2));
					relationCPNum[index][0]++;
				} else if (c.insidePolygon(pg)) {
					System.out.println("�E " + setRelationTag(relationCPTag[1], name1, name2));
					relationCPNum[index][1]++;
				} else if (c.overlapPolygon(pg)) {
					System.out.println("�E " + setRelationTag(relationCPTag[2], name1, name2));
					relationCPNum[index][2]++;
				}
			}
		}
	}

	private void printRelationPP() {
		ArrayList<MyPoint> pointList = this.elemAnal.detectedPoint;
		ArrayList<MyPolygon> polygonList = this.elemAnal.detectedPolygon;

		for (int i = 0; i < polygonList.size(); i++) {
			MyPolygon pg1 = polygonList.get(i);
			for (int j = 0; j < polygonList.size(); j++) {
				MyPolygon pg2 = polygonList.get(j);

				String name1 = pg1.getPolygonName() + "(";
				for (int k = 0; k < pg1.vertex.size(); k++) {
					name1 += "P[" + pointList.indexOf(pg1.vertex.get(k)) + "]";
				}
				name1 += ")";

				String name2 = pg2.getPolygonName() + "(";
				for (int k = 0; k < pg2.vertex.size(); k++) {
					name2 += "P[" + pointList.indexOf(pg2.vertex.get(k)) + "]";
				}
				name2 += ")";

				if (i != j) {
					int index = (pg1.vertex.size() - 3) * 4 + (pg2.vertex.size() - 3);
					if (pg1.inscribePolygon(pg2)) {
						System.out.println("�E " + setRelationTag(relationPPTag[0], name1, name2));
						relationPPNum1[index][0]++;
					} else if (pg1.insidePolygon(pg2)) {
						System.out.println("�E " + setRelationTag(relationPPTag[1], name1, name2));
						relationPPNum1[index][1]++;
					}
				}

				if (i < j) {// �d�������Ȃ����߂̍H�v
					int index = (-(pg1.vertex.size() - 4) * (pg1.vertex.size() - 9) + 2 * pg2.vertex.size()) / 2;// �K�����񂩂瓾��index
					if (pg1.adjoinPolygon(pg2)) {
						System.out.println("�E " + setRelationTag(relationPPTag[2], name1, name2));
						relationPPNum2[index][0]++;
					} else if (pg1.overlapPolygon(pg2)) {
						System.out.println("�E " + setRelationTag(relationPPTag[3], name1, name2));
						relationPPNum2[index][1]++;
					}
				}
			}
		}
	}

	private void printRelationCC() {
		ArrayList<MyCircle> circleList = this.elemAnal.detectedCircle;

		for (int i = 0; i < circleList.size(); i++) {
			MyCircle c1 = circleList.get(i);
			for (int j = 0; j < circleList.size(); j++) {
				MyCircle c2 = circleList.get(j);

				String name1 = "�~(C[" + circleList.indexOf(c1) + "])";
				String name2 = "�~(C[" + circleList.indexOf(c2) + "])";

				if (i != j) {
					if (c1.inscribeCircle(c2)) {
						System.out.println("�E " + setRelationTag(relationCCTag[0], name1, name2));
						relationCCNum1[0]++;
					} else if (c1.insideCircle(c2)) {
						System.out.println("�E " + setRelationTag(relationCCTag[1], name1, name2));
						relationCCNum1[1]++;
					}
				}

				if (i < j) {// �d�������Ȃ����߂̍H�v
					if (c1.adjoinCircle(c2)) {
						System.out.println("�E " + setRelationTag(relationCCTag[2], name1, name2));
						relationCCNum2[0]++;
					} else if (c1.overlapCircle(c2)) {
						System.out.println("�E " + setRelationTag(relationCCTag[3], name1, name2));
						relationCCNum2[1]++;
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void printCharacterTag() {
		charRec = new CharacterRecognition(this.imgProc);
		charRec.printResult();

		System.out.println();
	}

	@SuppressWarnings("resource")
	public void generateVector() {
		File file = new File("dat/output/vector/vector.csv");

		try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw)) {
			PrintWriter pw = new PrintWriter(bw);

			if (imgProc.imgName.equals("001.PNG")) {// �ŏ������s�̖��O������
				pw = writeColumnName(pw);
			}
			pw = writeElementNum(pw);
			pw = writeRelationNum(pw);
			pw.println();

			pw.close();
		} catch (IOException e) {
			StackTraceElement[] ste = e.getStackTrace();
			System.err.println("��O���� : " + e.getClass().getName());
			System.err.println("��O���e : " + e.getMessage());
			System.err.println("�����ꏊ : " + ste[ste.length - 1]);
			System.exit(0);
		}
	}

	private PrintWriter writeColumnName(PrintWriter pw) {
		pw.print("�t�@�C����" + ",");

		pw.print(pointTag + ",");
		pw.print(lineTag + ",");

		for (int i = 0; i < triangleTag.length; i++) {
			pw.print(triangleTag[i] + ",");
		}
		for (int i = 0; i < quadrangleTag.length; i++) {
			pw.print(quadrangleTag[i] + ",");
		}
		for (int i = 0; i < pentagonTag.length; i++) {
			pw.print(pentagonTag[i] + ",");
		}
		for (int i = 0; i < hexagonTag.length; i++) {
			pw.print(hexagonTag[i] + ",");
		}
		pw.print(circleTag + ",");

		for (int i = 0; i < relationLCTag.length; i++) {
			pw.print("�u" + setRelationTag(relationLCTag[i], lineTag, circleTag) + "�v" + ",");
		}

		for (int i = 0; i < polygonTag.length; i++) {
			for (int j = 0; j < relationPCTag.length; j++) {
				pw.print("�u" + setRelationTag(relationPCTag[j], polygonTag[i], circleTag) + "�v" + ",");
			}
		}

		for (int i = 0; i < polygonTag.length; i++) {
			for (int j = 0; j < relationCPTag.length; j++) {
				pw.print("�u" + setRelationTag(relationCPTag[j], circleTag, polygonTag[i]) + "�v" + ",");
			}
		}

		for (int i = 0; i < polygonTag.length; i++) {// 4
			for (int j = 0; j < polygonTag.length; j++) {// 4
				for (int k = 0; k < 2; k++) {
					pw.print("�u" + setRelationTag(relationPPTag[k], polygonTag[i], polygonTag[j]) + "�v" + ",");
				}
			}
		}

		for (int i = 0; i < polygonTag.length; i++) {
			for (int j = i; j < polygonTag.length; j++) {
				for (int k = 2; k < 4; k++) {
					pw.print("�u" + setRelationTag(relationPPTag[k], polygonTag[i], polygonTag[j]) + "�v" + ",");
				}
			}
		}

		for (int i = 0; i < relationCCTag.length; i++) {
			pw.print("�u" + setRelationTag(relationCCTag[i], circleTag, circleTag) + "�v" + ",");
		}

		pw.print("   ");
		pw.println();

		return pw;
	}

	/**
	 * 
	 * @param pw
	 * @return
	 */
	private PrintWriter writeElementNum(PrintWriter pw) {
		pw.print(imgProc.imgName + ",");

		pw.print(calcConstant(this.elemAnal.detectedPoint.size(), 0.5) + ",");
		pw.print(calcConstant(this.elemAnal.detectedLine.size(), 0.5) + ",");

		for (int i = 0; i < triangleNum.length; i++) {
			pw.print(calcConstant(triangleNum[i], ((i == 0) ? 1 : 2)) + ",");
		}

		for (int i = 0; i < quadrangleNum.length; i++) {
			pw.print(calcConstant(quadrangleNum[i], ((i == 0) ? 1 : 2)) + ",");
		}

		for (int i = 0; i < pentagonNum.length; i++) {
			pw.print(calcConstant(pentagonNum[i], ((i == 0) ? 1 : 2)) + ",");
		}

		for (int i = 0; i < hexagonNum.length; i++) {
			pw.print(calcConstant(hexagonNum[i], ((i == 0) ? 1 : 2)) + ",");
		}

		pw.print(calcConstant(this.elemAnal.detectedCircle.size(), 1) + ",");

		return pw;
	}

	private PrintWriter writeRelationNum(PrintWriter pw) {
		for (int i = 0; i < relationLCNum.length; i++) {
			pw.print(calcConstant(relationLCNum[i], 2) + ",");
		}

		for (int i = 0; i < relationPCNum.length; i++) {
			for (int j = 0; j < relationPCNum[i].length; j++) {
				pw.print(calcConstant(relationPCNum[i][j], 3) + ",");
			}
		}

		for (int i = 0; i < relationCPNum.length; i++) {
			for (int j = 0; j < relationCPNum[i].length; j++) {
				pw.print(calcConstant(relationCPNum[i][j], 3) + ",");
			}
		}

		for (int i = 0; i < relationPPNum1.length; i++) {
			for (int j = 0; j < relationPPNum1[i].length; j++) {
				pw.print(calcConstant(relationPPNum1[i][j], 3) + ",");
			}
		}

		for (int i = 0; i < relationPPNum2.length; i++) {
			for (int j = 0; j < relationPPNum2[i].length; j++) {
				pw.print(calcConstant(relationPPNum2[i][j], 3) + ",");
			}
		}

		for (int i = 0; i < relationCCNum1.length; i++) {
			pw.print(calcConstant(relationCCNum1[i], 3) + ",");
		}

		for (int i = 0; i < relationCCNum2.length; i++) {
			pw.print(calcConstant(relationCCNum2[i], 3) + ",");
		}

		return pw;
	}

	private String setRelationTag(String tag, String X, String Y) {
		return tag.replace("X", X).replace("Y", Y);
	}

	private double calcConstant(int value, double c) {
		return c * (double) value;
	}
}
