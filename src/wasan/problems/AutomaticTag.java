package wasan.problems;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 和算図形問題への自動タグ付け、ベクトル作成に関するクラスです。
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class AutomaticTag {

	/** このクラスでImageProcessingクラスを利用する際に使います。 */
	private ImageProcessing imgProc;

	/** このクラスでElementAnalysisクラスを利用する際に使います。 */
	private ElementAnalysis elemAnal;

	/** このクラスでCharacterRecognitionクラスを利用する際に使います。 */
	private CharacterRecognition charRec;

	/**
	 * 以下、幾何要素(点・線分・円)のタグを表す変数です。(円のみ、図形要素のタグとしても扱います。)
	 */
	/** 点のタグを表します。 */
	private String pointTag = "点";
	/** 線分のタグを表します。 */
	private String lineTag = "線分";
	/** 円のタグを表します。 */
	private String circleTag = "円";

	/**
	 * 以下、図形要素(n角形)のタグを表す変数です。(円のみ、幾何要素のタグを利用します。)
	 */
	/** n角形のタグを表します。(三角形・四角形・五角形・六角形) */
	private String[] polygonTag = { "三角形", "四角形", "五角形", "六角形" };
	/** 特徴的な三角形のタグを表します。(三角形・正三角形・二等辺三角形・直角三角形) */
	private String[] triangleTag = { polygonTag[0], "正三角形", "二等辺三角形", "直角三角形" };
	/** 特徴的な四角形のタグを表します。(四角形・正方形・長方形・菱形・等脚台形) */
	private String[] quadrangleTag = { polygonTag[1], "正方形", "長方形", "菱形", "等脚台形" };
	/** 特徴的な五角形のタグを表します。(五角形・正五角形) */
	private String[] pentagonTag = { polygonTag[2], "正五角形" };
	/** 特徴的な六角形のタグを表します。(六角形・正六角形) */
	private String[] hexagonTag = { polygonTag[3], "正六角形" };

	/**
	 * 以下、図形要素(n角形)のタグ合計を保持する配列です。
	 */
	/** 特徴的な三角形のタグ合計を保持します。(三角形・正三角形・二等辺三角形・直角三角形) */
	private int[] triangleNum = new int[triangleTag.length];
	/** 特徴的な四角形のタグ合計を保持します。(四角形・正方形・長方形・菱形・等脚台形) */
	private int[] quadrangleNum = new int[quadrangleTag.length];
	/** 特徴的な五角形のタグ合計を保持します。(五角形・正五角形) */
	private int[] pentagonNum = new int[pentagonTag.length];
	/** 特徴的な六角形のタグ合計を保持します。(六角形・正六角形) */
	private int[] hexagonNum = new int[hexagonTag.length];

	/**
	 * 以下、図形要素(n角形・円)同士の関係性のタグを表す配列です。(XとYを置き換えて利用します。)
	 */
	/** 線分と円の関係性のタグを表します。(「XとYが接する」・「XとYが1点で交わる」・「XとYが2点で交わる」) */
	private String[] relationLCTag = { "XとYが接する.", "XとYが1点で交わる.", "XとYが2点で交わる." };
	/** n角形から見た円との関係性のタグを表します。(「XがYに内接する」・「XがYの内部に存在する」・「XとYが互いに隣接する」) */
	private String[] relationPCTag = { "XがYに内接する.", "XがYの内部に存在する.", "XとYが互いに隣接する." };
	/** 円から見たn角形との関係性のタグを表します。(「XがYに内接する」・「XがYの内部に存在する」・「XとYが互いに重なり合う」) */
	private String[] relationCPTag = { "XがYに内接する.", "XがYの内部に存在する.", "XとYが互いに重なり合う." };
	/** n角形同士の関係性のタグを表します。(「XがYに内接する」・「XがYの内部に存在する」・「XとYが互いに隣接する」・「XとYが互いに重なり合う」) */
	private String[] relationPPTag = { "XがYに内接する.", "XがYの内部に存在する.", "XとYが互いに隣接する.", "XとYが互いに重なり合う." };
	/** 円同士の関係性のタグを表します。(「XがYの内側で接する」・「XがYの内部に存在する」・「XとYが互いに外接する」・「XとYが互いに重なり合う」) */
	private String[] relationCCTag = { "XがYの内側で接する.", "XがYの内部に存在する.", "XとYが互いに外接する.", "XとYが互いに重なり合う." };

	/**
	 * 以下、幾何要素(線分・円)同士、図形要素(n角形・円)同士の関係性のタグ合計を保持する配列です。
	 */
	/** 線分と円の関係性のタグ合計を保持します。(「XとYが接する」・「XとYが1点で交わる」・「XとYが2点で交わる」) */
	private int[] relationLCNum = new int[relationLCTag.length];
	/** n角形から見た円との関係性のタグ合計を保持します。(「XがYに内接する」・「XがYの内部に存在する」・「XとYが隣接する」) */
	private int[][] relationPCNum = new int[polygonTag.length][relationPCTag.length];
	/** 円から見たn角形との関係性のタグ合計を保持します。(「XがYに内接する」・「XがYの内部に存在する」・「XとYが重なり合う」) */
	private int[][] relationCPNum = new int[polygonTag.length][relationCPTag.length];
	/** n角形同士の関係性のタグ合計①を保持します。(「XがYに内接する」・「XがYの内部に存在する」) */
	private int[][] relationPPNum1 = new int[16][2];
	/** n角形同士の関係性のタグ合計②を保持します。(「XとYが隣接する」・「XとYが重なり合う」) */
	private int[][] relationPPNum2 = new int[10][2];
	/** 円同士の関係性のタグ合計①を保持します。(「XがYの内側で接する」・「XがYの内部に存在する」) */
	private int[] relationCCNum1 = new int[2];
	/** 円同士の関係性のタグ合計②を保持します。(「XとYが外接する」・「XとYが重なり合う」) */
	private int[] relationCCNum2 = new int[2];

	/**
	 * AutomaticTagオブジェクトを作成します。
	 * 
	 * @param _imgProc ImageProcessingクラス
	 * @param _elemAnal ElementAnalysisクラス
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
		System.out.println("< 図形要素 >");
		printElementTag();

		System.out.println("< 図形要素同士の関係性 >");
		printRelationTag();

		// System.out.println("< 文字要素 >");
		// printCharacterTag();
	}

	private void printElementTag() {
		int polygonNum = 0;

		ArrayList<MyPoint> pointList = this.elemAnal.detectedPoint;
		ArrayList<MyPolygon> polygonList = this.elemAnal.detectedPolygon;
		ArrayList<MyCircle> circleList = this.elemAnal.detectedCircle;

		for (int i = 0; i < polygonList.size(); i++) {
			String polygonName = polygonList.get(i).getPolygonName();

			System.out.print("・ " + polygonTag[polygonList.get(i).vertex.size() - 3] + " > ");
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
				System.out.println("・ " + polygonTag[polygonList.get(i).vertex.size() - 3] + " × " + polygonNum);
				polygonNumSet = true;
			} else if (polygonList.get(i).vertex.size() < polygonList.get(i + 1).vertex.size()) {
				System.out.println("・ " + polygonTag[polygonList.get(i).vertex.size() - 3] + " × " + polygonNum);
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
			System.out.println("・ " + circleTag + " > " + circleTag + "(" + "C[" + i + "]" + ")");
		}
		if (circleList.size() > 0) {
			System.out.println("・ " + circleTag + " × " + circleList.size());
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

				String name2 = "円(C[" + circleList.indexOf(c) + "])";

				int index = pg.vertex.size() - 3;
				if (pg.inscribeCircle(c)) {// n角形が円に内接する
					System.out.println("・ " + setRelationTag(relationPCTag[0], name1, name2));
					relationPCNum[index][0]++;
				} else if (pg.insideCircle(c)) {// n角形が円の内部に存在する
					System.out.println("・ " + setRelationTag(relationPCTag[1], name1, name2));
					relationPCNum[index][1]++;
				} else if (pg.adjoinCircle(c)) {// n角形と円が隣接する
					System.out.println("・ " + setRelationTag(relationPCTag[2], name1, name2));
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

				String name1 = "円(C[" + circleList.indexOf(c) + "])";

				String name2 = pg.getPolygonName() + "(";
				for (int k = 0; k < pg.vertex.size(); k++) {
					name2 += "P[" + pointList.indexOf(pg.vertex.get(k)) + "]";
				}
				name2 += ")";

				int index = pg.vertex.size() - 3;
				if (c.inscribePolygon(pg)) {
					System.out.println("・ " + setRelationTag(relationCPTag[0], name1, name2));
					relationCPNum[index][0]++;
				} else if (c.insidePolygon(pg)) {
					System.out.println("・ " + setRelationTag(relationCPTag[1], name1, name2));
					relationCPNum[index][1]++;
				} else if (c.overlapPolygon(pg)) {
					System.out.println("・ " + setRelationTag(relationCPTag[2], name1, name2));
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
						System.out.println("・ " + setRelationTag(relationPPTag[0], name1, name2));
						relationPPNum1[index][0]++;
					} else if (pg1.insidePolygon(pg2)) {
						System.out.println("・ " + setRelationTag(relationPPTag[1], name1, name2));
						relationPPNum1[index][1]++;
					}
				}

				if (i < j) {// 重複させないための工夫
					int index = (-(pg1.vertex.size() - 4) * (pg1.vertex.size() - 9) + 2 * pg2.vertex.size()) / 2;// 階差数列から得たindex
					if (pg1.adjoinPolygon(pg2)) {
						System.out.println("・ " + setRelationTag(relationPPTag[2], name1, name2));
						relationPPNum2[index][0]++;
					} else if (pg1.overlapPolygon(pg2)) {
						System.out.println("・ " + setRelationTag(relationPPTag[3], name1, name2));
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

				String name1 = "円(C[" + circleList.indexOf(c1) + "])";
				String name2 = "円(C[" + circleList.indexOf(c2) + "])";

				if (i != j) {
					if (c1.inscribeCircle(c2)) {
						System.out.println("・ " + setRelationTag(relationCCTag[0], name1, name2));
						relationCCNum1[0]++;
					} else if (c1.insideCircle(c2)) {
						System.out.println("・ " + setRelationTag(relationCCTag[1], name1, name2));
						relationCCNum1[1]++;
					}
				}

				if (i < j) {// 重複させないための工夫
					if (c1.adjoinCircle(c2)) {
						System.out.println("・ " + setRelationTag(relationCCTag[2], name1, name2));
						relationCCNum2[0]++;
					} else if (c1.overlapCircle(c2)) {
						System.out.println("・ " + setRelationTag(relationCCTag[3], name1, name2));
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

			if (imgProc.imgName.equals("001.PNG")) {// 最初だけ行の名前を入れる
				pw = writeColumnName(pw);
			}
			pw = writeElementNum(pw);
			pw = writeRelationNum(pw);
			pw.println();

			pw.close();
		} catch (IOException e) {
			StackTraceElement[] ste = e.getStackTrace();
			System.err.println("例外発生 : " + e.getClass().getName());
			System.err.println("例外内容 : " + e.getMessage());
			System.err.println("発生場所 : " + ste[ste.length - 1]);
			System.exit(0);
		}
	}

	private PrintWriter writeColumnName(PrintWriter pw) {
		pw.print("ファイル名" + ",");

		// E1
		pw.print(pointTag + ",");
		pw.print(lineTag + ",");

		// E2
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

		// R1
		for (int i = 0; i < relationLCTag.length; i++) {
			pw.print("「" + setRelationTag(relationLCTag[i], lineTag, circleTag) + "」" + ",");
		}

		// R2
		for (int i = 0; i < polygonTag.length; i++) {
			for (int j = 0; j < relationPCTag.length; j++) {
				pw.print("「" + setRelationTag(relationPCTag[j], polygonTag[i], circleTag) + "」" + ",");
			}
		}

		// R3
		for (int i = 0; i < polygonTag.length; i++) {
			for (int j = 0; j < relationCPTag.length; j++) {
				pw.print("「" + setRelationTag(relationCPTag[j], circleTag, polygonTag[i]) + "」" + ",");
			}
		}

		// R4_1
		for (int i = 0; i < polygonTag.length; i++) {// 4
			for (int j = 0; j < polygonTag.length; j++) {// 4
				for (int k = 0; k < 2; k++) {
					pw.print("「" + setRelationTag(relationPPTag[k], polygonTag[i], polygonTag[j]) + "」" + ",");
				}
			}
		}

		// R4_2
		for (int i = 0; i < polygonTag.length; i++) {
			for (int j = i; j < polygonTag.length; j++) {
				for (int k = 2; k < 4; k++) {
					pw.print("「" + setRelationTag(relationPPTag[k], polygonTag[i], polygonTag[j]) + "」" + ",");
				}
			}
		}

		// R5_1～R5_2
		for (int i = 0; i < relationCCTag.length; i++) {
			pw.print("「" + setRelationTag(relationCCTag[i], circleTag, circleTag) + "」" + ",");
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

		// E1
		pw.print(calcConstant(this.elemAnal.detectedPoint.size(), 0.5) + ",");
		pw.print(calcConstant(this.elemAnal.detectedLine.size(), 0.5) + ",");

		// E2
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
		// R1
		for (int i = 0; i < relationLCNum.length; i++) {
			pw.print(calcConstant(relationLCNum[i], 2) + ",");
		}

		// R2
		for (int i = 0; i < relationPCNum.length; i++) {
			for (int j = 0; j < relationPCNum[i].length; j++) {
				pw.print(calcConstant(relationPCNum[i][j], 3) + ",");
			}
		}

		// R3
		for (int i = 0; i < relationCPNum.length; i++) {
			for (int j = 0; j < relationCPNum[i].length; j++) {
				pw.print(calcConstant(relationCPNum[i][j], 3) + ",");
			}
		}

		// R4_1
		for (int i = 0; i < relationPPNum1.length; i++) {
			for (int j = 0; j < relationPPNum1[i].length; j++) {
				pw.print(calcConstant(relationPPNum1[i][j], 3) + ",");
			}
		}

		// R4_2
		for (int i = 0; i < relationPPNum2.length; i++) {
			for (int j = 0; j < relationPPNum2[i].length; j++) {
				pw.print(calcConstant(relationPPNum2[i][j], 3) + ",");
			}
		}

		// R5_1
		for (int i = 0; i < relationCCNum1.length; i++) {
			pw.print(calcConstant(relationCCNum1[i], 3) + ",");
		}

		// R5_2
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

