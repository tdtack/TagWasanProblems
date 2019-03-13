package wasan.problems;

/**
 * 画像処理の中でもHough変換のみに特化したクラス
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 和算図形問題に含まれる幾何要素(線分・円)の認識で活用するHough変換に関するクラスです。
 * 
 * @author Kazushi Ahara, Takuma Tsuchihashi
 *
 */
public class HoughTransform {

	/** HoughTransformクラス内で画像処理を利用する。 */
	private ImageProcessing imgProc;

	/** 直線抽出に用いられる変数・配列 */
	private int[][] fieldLine;
	private int maxWidth, maxHeight;

	private double cos[], sin[];
	private int maxRho, maxTheta;

	/** 線分抽出に用いられる配列・リスト */
	private int[][] subArray;
	private int[] subsubArray;
	private int[] sub2subArray;
	private int[] sub3subArray;
	private int[] sub4subArray;
	private int[] sub5subArray;

	private ArrayList<MyLine> houghLine;

	/** 円抽出に用いられる変数・配列 */
	private int[][][] fieldCircle;
	private double maxRadius;
	private double[][] radius;

	private static class Parameter {
		/**
		 * getFieldLine前半アルゴリズムを選択<br>
		 * 0: rho の前後のデータを見て,山形になっているかどうかを見る
		 */
		static int gFD1method = 0;
		/**
		 * getFieldLine前半パラメータ1<br>
		 * method=0の時: para1 : rhoを動かす幅<br>
		 */
		static int gFD1para1 = 10;
		/**
		 * getFieldLine前半パラメータ2<br>
		 * method=0の時: para2 : 山の高さ(割合)<br>
		 */
		static int gFD1para2 = 2;
		/**
		 * getFieldLine後半アルゴリズム選択<br>
		 * 0: 山を見つけたら長方形に切り取る<br>
		 * 1: 山を見つけたら蝶の形に切り取る
		 */
		static int gFD2method = 0;
		/**
		 * getFieldLine後半パラメータ1<br>
		 * method=0の時: para1: 長方形の幅(theta方向)<br>
		 * method=1の時: para1: theta方向の範囲
		 */
		static int gFD2para1 = 10;
		/**
		 * getFieldLine後半パラメータ2<br>
		 * method=0の時: para2: 長方形の高さ(rho方向)<br>
		 */
		static int gFD2para2 = 45;
		/**
		 * メソッドrestoreLineの変数connectの値
		 */
		static int restoreLineConnect = 60;
		/**
		 *
		 */
		static int fillGapSubArraySize = 7;
	}

	public HoughTransform(ImageProcessing _imgProc) {
		imgProc = _imgProc;

		maxWidth = imgProc.originalImg.getWidth();// a処理する画像の横幅
		maxHeight = imgProc.originalImg.getHeight();// a処理する画像の縦幅

		// 線分抽出に利用するもの
		// maxRho = (int) Math.floor(Math.sqrt(maxWidth * maxWidth + maxHeight *
		// maxHeight));
		maxRho = (int) Math.hypot(maxWidth, maxHeight);// a線分Hough変換におけるρの最大値
		maxTheta = 180;// a線分Hough変換におけるθの最大値

		int lengTheta = convTheta(maxTheta);
		int lengRho = convRho(maxRho);

		fieldLine = new int[lengTheta][lengRho];
		cos = new double[lengTheta];
		sin = new double[lengTheta];// a計算の高速化
		for (int t = 0; t < maxTheta; t++) {
			cos[convTheta(t)] = Math.cos(Math.PI * t / maxTheta);
			sin[convTheta(t)] = Math.sin(Math.PI * t / maxTheta);
		}

		// a円抽出に利用するもの
		maxRadius = Math.min(maxWidth, maxHeight) / 2;// a
		fieldCircle = new int[(int) maxWidth][(int) maxHeight][(int) maxRadius + 1];
		radius = new double[(int) maxWidth][(int) maxHeight];// a計算の高速化
		for (int x = 0; x < maxWidth; x++) {
			for (int y = 0; y < maxHeight; y++) {
				radius[x][y] = Math.hypot(x, y);
			}
		}

		houghLine = new ArrayList<MyLine>();
	}

	public void freeResource() {
		imgProc = null;

		fieldLine = null;
		cos = null;
		sin = null;

		subArray = null;
		subsubArray = null;
		sub2subArray = null;
		sub3subArray = null;
		sub4subArray = null;
		sub5subArray = null;

		fieldCircle = null;
		radius = null;

		houghLine.clear();
	}

	/**
	 * 変数thetaを配列番号へ変換
	 * 
	 * @param theta
	 * @return 配列番号
	 */
	private int convTheta(int theta) {// 実数Θを配列番号へ拡張
		assert (0 <= theta && theta < maxTheta) : "convTheta: out of bounds error";
		return theta;
	}

	/**
	 * 変数rhoを配列番号へ変換
	 * 
	 * @param theta
	 * @return 配列番号
	 */
	private int convRho(int rho) {// 実数ρを配列番号へ拡張
		assert (-maxRho <= rho && rho < maxRho) : "convRho: out of bounds error";
		return rho + maxRho;
	}

	/**
	 * fieldLineの内容をcsvファイルに出力する ファイル名はimgPro.iに依存してつける
	 */
	void exportCSV(int x) {
		File file = new File("dat/output/hough/fieldLine" + imgProc.imgNum + "-" + x + ".csv");
		try (FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw)) {

			for (int r = -maxRho; r < maxRho; r++) {
				for (int t = 0; t < maxTheta; t++) {
					pw.print(fieldLine[convTheta(t)][convRho(r)] + ",");
				}
				pw.println();
			}
		} catch (IOException e) {
			showException(e);
		}
	}

	/**
	 * 線分に関する投票を行う。 NewCanvas.javaで使用する
	 * 
	 * @param image
	 */
	public void voteFieldLine() {//
		for (int x = 0; x < maxWidth; x++) {
			for (int y = 0; y < maxHeight; y++) {
				if ((imgProc.editingImg.getRGB(x, y) >> 16 & 0xFF) < 128) {
					for (int t = 0; t < maxTheta; t++) {
						int rho = (int) Math.floor(cos[convTheta(t)] * x + sin[convTheta(t)] * y);
						fieldLine[convTheta(t)][convRho(rho)]++;
					}
				}
			}
		}
		// exportCSV(0);
	}

	int gFLcount = 1;

	public MyLine getFieldLine() {// 投票に基づく(Θ,ρ)の取得
		int voteNum = 0;

		int theta = 0;
		int rho = 0;

		// 投票数ではなく、山の形状から判断する(ρθ平面のρ方向に着目する、山の形がθ方向よりも明確であるため)
		if (Parameter.gFD1method == 0) {
			int Dr = Parameter.gFD1para1;// 10
			int seads = Parameter.gFD1para2;// 2
			for (int t = 0; t < maxTheta; t++) {
				for (int r = -maxRho + Dr; r < maxRho - Dr; r++) {
					if (voteNum < fieldLine[convTheta(t)][convRho(r)]) {

						int t1 = fieldLine[convTheta(t)][convRho(r - Dr)] + fieldLine[convTheta(t)][convRho(r - Dr + 1)]
								+ fieldLine[convTheta(t)][convRho(r - Dr + 2)];
						int t2 = fieldLine[convTheta(t)][convRho(r - 1)] + fieldLine[convTheta(t)][convRho(r)]
								+ fieldLine[convTheta(t)][convRho(r + 1)];
						int t3 = fieldLine[convTheta(t)][convRho(r + Dr)] + fieldLine[convTheta(t)][convRho(r + Dr - 1)]
								+ fieldLine[convTheta(t)][convRho(r + Dr - 2)];

						boolean condition = ((t1 * seads < t2) && (t3 * seads < t2));
						if (condition) {
							voteNum = fieldLine[convTheta(t)][convRho(r)];
							theta = t;
							rho = r;
						}
					}
				}
			}
		}

		if (Parameter.gFD2method == 0) {
			int et = Parameter.gFD2para1;// 20;//ρθ平面において長方形を切り取ってよいか
			int er = Parameter.gFD2para2;// 50;
			for (int dt = -et; dt <= et; dt++) {
				for (int dr = -er; dr <= er; dr++) {
					int thetaD = theta + dt;
					int rhoD = rho + dr;

					if (inRange(thetaD, rhoD)) {
						fieldLine[convTheta(thetaD)][convRho(rhoD)] = 0;
					}

					if (theta < et || maxTheta - et < theta) {
						thetaD = (maxTheta - theta) + dt;
						rhoD = -rho + dr;
						if (inRange(thetaD, rhoD)) {
							fieldLine[convTheta(thetaD)][convRho(rhoD)] = 0;
						}
					}
				}
			}
		} else if (Parameter.gFD2method == 1) {
			int et = Parameter.gFD2para1;// 20;//長方形を切り取ってよいか
			double maxT = 0;
			if (theta < 90) {
				maxT = Math.max(maxHeight * cos[convTheta(theta)], maxWidth * sin[convTheta(theta)]);
			} else {
				int pi4 = (int) Math.round(Math.atan2(maxHeight, maxWidth) * 180 / Math.PI);
				maxT = Math.hypot(maxWidth, maxHeight) * sin[theta - pi4];
			}
			for (int dt = -et; dt <= et; dt++) {
				int thetaD = theta + dt;
				int rhoMin = (int) Math.floor(rho * cos[Math.abs(dt)] - maxT * sin[Math.abs(dt)]);
				int rhoMax = (int) Math.ceil(rho * cos[Math.abs(dt)] + maxT * sin[Math.abs(dt)]);
				for (int rhoD = rhoMin; rhoD <= rhoMax; rhoD++) {
					if (inRange(thetaD, rhoD)) {
						fieldLine[convTheta(thetaD)][convRho(rhoD)] = 0;
					}

					if (theta < et || maxTheta - et < theta) {
						thetaD = (maxTheta - theta) + dt;
						if (inRange(thetaD, -rhoD)) {
							fieldLine[convTheta(thetaD)][convRho(-rhoD)] = 0;
						}
					}
				}
			}
		}
		// exportCSV(gFLcount);
		// gFLcount ++ ;
		MyLine thisLine = new MyLine(theta, rho);
		houghLine.add(thisLine);
		return thisLine;
	}

	/**
	 * Hough ペアに対して線分を抽出する
	 * 
	 * @param _theta
	 * @param _rho
	 * @return
	 */
	public MyLine restoreLine(double _theta, double _rho) {// 線分の復元
		// System.out.println("(Θ,ρ)=(" + _theta + "," + _rho + ")");

		MyPoint start = new MyPoint(0, 0);// 線分の始点
		MyPoint end = new MyPoint(0, 0);// 線分の終点

		int theta = (int) Math.floor(_theta + 0.001);
		int rho = (int) Math.floor(_rho + 0.001);

		for (MyLine line1 : houghLine) {
			if (line1.theta == _theta && line1.rho == _rho) {
				double rho1 = line1.rho;
				double theta1 = line1.theta;
				for (MyLine line2 : houghLine) {
					double rho2 = line2.rho;
					double theta2 = line2.theta;
					if (theta1 != theta2
							&& (Math.abs(theta1 - theta2) <= 7.01 || Math.abs(theta1 - theta2) >= 172.99)) {
						double numX = rho1 * Math.sin(Math.toRadians(theta2)) - rho2 * Math.sin(Math.toRadians(theta1));
						double numY = -rho1 * Math.cos(Math.toRadians(theta2))
								+ rho2 * Math.cos(Math.toRadians(theta1));
						double den = Math.sin(Math.toRadians(theta2 - theta1));
						double intersectX = numX / den;
						double intersectY = numY / den;
						if (0 <= intersectX && intersectX < maxWidth && 0 <= intersectY && intersectY < maxHeight) {
							houghLine.remove(line1);
							return new MyLine(start, end);
						}
					}

				}
			}
		}

		boolean condition = (maxTheta / 4 < theta && theta <= 3 * maxTheta / 4);
		int[] data = Bright(condition, theta, rho);

		int div = 1;
		int connect = Parameter.restoreLineConnect;// 起点からつながっているピクセル数

		// 線分の始点をint配列から探索。start(Line型)を得る。
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < connect; j++) {
				if (i + j < data.length && data[i + j] > 0) {
					if (j == connect - 1) {
						int s = i * div;
						start.x = condition ? s : (rho - s * sin[convTheta(theta)]) / cos[convTheta(theta)];
						start.y = condition ? (rho - s * cos[convTheta(theta)]) / sin[convTheta(theta)] : s;
					} else {
						//
					}
				} else {
					break;
				}
			}
			if (start.x > 0 && start.y > 0) {
				break;
			}
		}

		// 線分の終点をint配列から探索。end(Line型)を得る。
		for (int i = data.length - 1; i >= 0; i--) {
			for (int j = 0; j < connect; j++) {
				if (i - j >= 0 && data[i - j] > 0) {
					if (j == connect - 1) {
						int e = i * div;
						end.x = condition ? e : (rho - e * sin[convTheta(theta)]) / cos[convTheta(theta)];
						end.y = condition ? (rho - e * cos[convTheta(theta)]) / sin[convTheta(theta)] : e;
					} else {
						//
					}
				} else {
					break;
				}
			}
			if (end.x > 0 && end.y > 0) {
				break;
			}
		}

		// System.out.println("("+start.x+","+start.y+")-("+end.x+","+end.y+")");

		File file = new File("dat/output/hough/Array" + imgProc.imgNum + ".txt");
		try (FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw)) {

			pw.println("(" + start.x + "," + start.y + ")-(" + end.x + "," + end.y + ")");
			pw.println("");
		} catch (IOException e) {
			showException(e);
		}

		return new MyLine(start, end);
	}

	/**
	 * 線分に関する配列の整理方法
	 * 
	 * @param isW   <br>
	 *              true:x軸に投射, false:y軸に投射
	 * @param theta
	 * @param rho
	 * @return
	 */
	private int[] Bright(boolean isW, double theta, double rho) {
		if (isW)
			return BrightX(theta, rho);
		else
			return BrightY(theta, rho);
	}

	private int[] BrightX(double _theta, double _rho) {
		int thetaD = (int) Math.floor(_theta);
		double origRho = _rho;
		int drMax = 6;
		int maxDr = drMax * 2 + 1;
		double[] dr = new double[maxDr];

		for (int i = 0; i < maxDr; i++) {
			dr[i] = 0.5 * (i - drMax);
		}

		int[] Array = new int[maxWidth];// 水平垂直方向の切り替え
		subArray = new int[maxDr][maxWidth];
		subsubArray = new int[maxWidth];
		sub2subArray = new int[maxWidth];
		sub3subArray = new int[maxWidth];
		sub4subArray = new int[maxWidth];
		sub5subArray = new int[maxWidth];
		for (int x = 0; x < maxWidth; x++) {
			Array[x] = 0;
			for (int r = 0; r < maxDr; r++) {
				subArray[r][x] = 0;
			}
			subsubArray[x] = 0;
			sub2subArray[x] = 0;
			sub3subArray[x] = 0;
			sub4subArray[x] = 0;
			sub5subArray[x] = 0;
		}
		for (int x = 0; x < maxWidth; x++) {
			for (int r = 0; r < maxDr; r++) {
				double rhoD = origRho + dr[r];
				int y = (int) Math.floor((rhoD - x * cos[convTheta(thetaD)]) / sin[convTheta(thetaD)]);
				if (0 <= y && y < maxHeight) {
					if ((imgProc.originalImg.getRGB(x, y) >> 16 & 0xFF) < 128) {
						subArray[r][x] = 1;
					}
				}
			}
		}
		extractSegment(maxDr, maxWidth, _theta, _rho);
		for (int x = 0; x < maxWidth; x++) {
			for (int r = 0; r < maxDr; r++) {
				if (sub4subArray[x] == 9) {
					Array[x] = 1;
				}
			}
		}
		return Array;
	}

	private int[] BrightY(double _theta, double _rho) {
		int thetaD = (int) Math.floor(_theta);
		double origRho = _rho;
		int drMax = 7;
		int maxDr = drMax * 2 + 1;
		double[] dr = new double[maxDr];
		for (int i = 0; i < maxDr; i++) {
			dr[i] = 0.5 * (i - drMax);
		}
		int[] Array = new int[maxHeight];//
		subArray = new int[maxDr][maxHeight];
		subsubArray = new int[maxHeight];
		sub2subArray = new int[maxHeight];
		sub3subArray = new int[maxHeight];
		sub4subArray = new int[maxHeight];
		sub5subArray = new int[maxHeight];
		for (int y = 0; y < maxHeight; y++) {
			Array[y] = 0;
			for (int r = 0; r < maxDr; r++) {
				subArray[r][y] = 0;
			}
			subsubArray[y] = 0;
			sub2subArray[y] = 0;
			sub3subArray[y] = 0;
			sub4subArray[y] = 0;
			sub5subArray[y] = 0;
		}
		for (int y = 0; y < maxHeight; y++) {
			for (int r = 0; r < maxDr; r++) {
				double rhoD = origRho + dr[r];
				int x = (int) Math.floor((rhoD - y * sin[convTheta(thetaD)]) / cos[convTheta(thetaD)]);
				if (0 <= x && x < maxWidth) {
					if ((imgProc.originalImg.getRGB(x, y) >> 16 & 0xFF) < 128) {
						subArray[r][y] = 1;
					}
				}
			}
		}
		extractSegment(maxDr, maxHeight, _theta, _rho);
		for (int y = 0; y < maxHeight; y++) {
			for (int r = 0; r < maxDr; r++) {
				if (sub4subArray[y] == 9) {
					Array[y] = 1;
				}
			}
		}
		return Array;
	}

	/**
	 * 2重数列から線分要素を判別する
	 * 
	 * @param _array
	 */
	private void extractSegment(int lenR, int lenX, double _theta, double _rho) {

		fillGapSubArray(lenR, lenX);
		make_subData(lenR, lenX);
		deleteArcSlash(lenR, lenX);
		exportArrayToTxt(lenR, lenX, _theta, _rho);
	}

	/**
	 * subArrayの隙間があれば埋める
	 * 
	 * @param lenR
	 * @param lenX
	 */
	private void fillGapSubArray(int lenR, int lenX) {
		for (int r = 0; r < lenR; r++) {
			int loopCount = 0;
			boolean cont = false;
			do {
				cont = false;
				int cnt0 = 1;
				int cnt1 = -1;
				int cnt2 = -1;
				int idx1 = -1;
				for (int x = 0; x < lenX; x++) {
					if (x < lenX - 1 && subArray[r][x] == subArray[r][x + 1]) {
						cnt0++;
					} else {
						if (cnt1 > 0 && cnt1 > 0 && cnt1 < Parameter.fillGapSubArraySize
								&& ((cnt0 > cnt1 + 1 && cnt1 + 1 < cnt2) || cnt0 > cnt1 * 2 || cnt1 * 2 < cnt2)) {
							for (int k = idx1; k > idx1 - cnt1 && k >= 0; k--) {
								subArray[r][k] = 1 - subArray[r][k];
							}
							cont = true;
							break;
						}
						cnt2 = cnt1;
						cnt1 = cnt0;
						cnt0 = 1;
						idx1 = x;
					}
				}
				loopCount++;
			} while (cont && loopCount < 100);
		}
	}

	/**
	 * subArrayの断面の位置をsubに,断面の大きさをsub2に記録する
	 * 
	 * @param lenR
	 * @param lenX
	 */
	private void make_subData(int lenR, int lenX) {
		for (int x = 0; x < lenX; x++) {
			subsubArray[x] = 0;
			sub2subArray[x] = 0;
			sub3subArray[x] = 0;
			for (int r = 0; r < lenR; r++) {
				subsubArray[x] += (r + 1) * subArray[r][x];
				sub2subArray[x] += subArray[r][x];
			}
			subsubArray[x] = (int) Math.ceil(1.0 * subsubArray[x] / sub2subArray[x]);
		}
	}

	/**
	 * subsubArrayの連結成分を見て,断面の位置が大きく変化しているものは消去する。<br>
	 * 継続的に増加、継続的に減少している部分を取り出してその全体からの割合を調べる。
	 * 
	 * @return
	 */
	private boolean deleteArcSlash(int lenR, int lenX) {
		boolean arrayOn = false;
		int xOn = -1;// かたまり始まりの座標
		int xIncr = -1; // 増加始まりの座標
		int rIncr = 0; // 増加の量
		int dxIncr = 0; // 増加の座標幅

		int xDecr = -1; // 減少始まりの座標
		int rDecr = 0; // 減少の量
		int dxDecr = 0; // 減少の座標幅

		int dxStay = 0;//

		int countIncrDecr = 0;// 増加または減少部分のxの個数
		int countSub2 = 0;// 幅いっぱいの部分のxの個数を数える
		for (int x = 0; x < lenX; x++) {
			if (subsubArray[x] != 0) {
				if (!arrayOn) {// かたまりはじめ
					arrayOn = true;
					xOn = x;
					xIncr = xDecr = -1;
					countSub2 = 0;
				} else {// かたまり途中
					if (Math.abs(subsubArray[x] - subsubArray[xOn]) * 10 > (x - xOn)) {
						sub3subArray[x] = 1;
					}
					if (sub2subArray[x] >= lenR - 1) {
						countSub2++;
					}
					if (xIncr == -1 && subsubArray[x - 1] == subsubArray[x] - 1) {// 増加始まり
						xIncr = x;
						sub5subArray[x] = 1;
						dxStay = 0;
					} else if (xIncr > 0) {
						if (x < maxWidth - 1) {// ☆☆☆バグ発生のため、土橋が編集
							if (x < lenX - 1 && subsubArray[x] != subsubArray[x + 1]
									&& subsubArray[x] != subsubArray[x + 1] - 1) {
								rIncr = subsubArray[x] - subsubArray[xIncr];
								dxIncr = x - xIncr;
								// System.out.println("xIncr, rIncr, dxIncr = "+xIncr+","+rIncr+","+dxIncr);
								if ((rIncr > 6 && rIncr * 20 > dxIncr) || (rIncr > 4 && rIncr * 15 > dxIncr)) {// 断続的に増加している条件
									countIncrDecr += dxIncr;
									// System.out.println("OK: countIncrDecr = "+countIncrDecr);
									for (int xx = xIncr; xx < x; xx++) {
										sub5subArray[xx] = 1;
									}
								}
								xIncr = -1;
								rIncr = dxIncr = 0;
							} else if (subsubArray[x] == subsubArray[x + 1]) {
								dxStay++;
								if (dxStay > 30) {// もはや増加とは認められず
									for (int xx = xIncr; xx < x; xx++) {
										sub3subArray[xx] = 0;
									}
									xIncr = -1;
									rIncr = dxIncr = 0;
								}
							} else if (subsubArray[x] == subsubArray[x + 1] - 1) {
								dxStay = 0;
							}
						}
					}
					if (xDecr == -1 && subsubArray[x - 1] == subsubArray[x] + 1) {// 減少始まり
						xDecr = x;
						sub5subArray[x] = 1;
						dxStay = 0;
					} else if (xDecr > 0) {
						if (x < maxWidth - 1) {// ☆☆☆バグ発生のため、土橋が編集
							if (x < lenX - 1 && subsubArray[x] != subsubArray[x + 1]
									&& subsubArray[x] != subsubArray[x + 1] + 1) {
								rDecr = subsubArray[xDecr] - subsubArray[x];
								dxDecr = x - xDecr;
								// System.out.println("xDecr, rDecr, dxDecr = "+xDecr+","+rDecr+","+dxDecr);
								if ((rDecr > 6 && rDecr * 20 > dxDecr) || (rDecr > 4 && rDecr * 15 > dxDecr)) {// 断続的に減少している条件
									countIncrDecr += dxDecr;
									// System.out.println("OK: countIncrDecr = "+countIncrDecr);
									for (int xx = xDecr; xx < x; xx++) {
										sub5subArray[xx] = 1;
									}
								}
								xDecr = -1;
								rDecr = dxDecr = 0;
							} else if (subsubArray[x] == subsubArray[x + 1]) {
								dxStay++;
								if (dxStay > 30) {// もはや減少とは認められず
									for (int xx = xDecr; xx < x; xx++) {
										sub3subArray[xx] = 0;
									}
									xDecr = -1;
									rDecr = dxDecr = 0;
								}
							} else if (subsubArray[x] == subsubArray[x + 1] + 1) {
								dxStay = 0;
							}
						}
					}
				}
			} else {
				if (arrayOn) {// かたまり終わり
					if (xIncr > 0) {
						rIncr = subsubArray[x] - subsubArray[xIncr];
						dxIncr = x - xIncr;
						// System.out.println("xIncr, rIncr, dxIncr = "+xIncr+","+rIncr+","+dxIncr);
						if ((rIncr > 6 && rIncr * 20 > dxIncr) || (rIncr > 4 && rIncr * 15 > dxIncr)) {// 断続的に増加している条件
							countIncrDecr += dxIncr;
							// System.out.println("OK: countIncrDecr = "+countIncrDecr);
							for (int xx = xIncr; xx < x; xx++) {
								sub5subArray[xx] = 1;
							}
						}
						xIncr = -1;
						rIncr = dxIncr = 0;
					}
					if (xDecr > 0) {
						rDecr = subsubArray[xDecr] - subsubArray[x];
						dxDecr = x - xDecr;
						// System.out.println("xDecr, rDecr, dxDecr = "+xDecr+","+rDecr+","+dxDecr);
						if ((rDecr > 6 && rDecr * 20 > dxDecr) || (rDecr > 4 && rDecr * 15 > dxDecr)) {// 断続的に増加している条件
							countIncrDecr += dxDecr;
							// System.out.println("OK: countIncrDecr = "+countIncrDecr);
							for (int xx = xDecr; xx < x; xx++) {
								sub5subArray[xx] = 1;
							}
						}
						xDecr = -1;
						rDecr = dxDecr = 0;
					}
					int sum = 0;
					for (int xx = xOn; xx < x; xx++) {
						sum += sub3subArray[xx];
					}
					if (x - xOn < sum * 2 || (x - xOn < 50 && x - xOn < countSub2 * 2) || x - xOn < countIncrDecr * 2) {
						for (int xx = xOn; xx < x; xx++) {
							sub4subArray[xx] = 1;
						}
					} else {
						for (int xx = xOn; xx < x; xx++) {
							sub4subArray[xx] = 9;
						}
					}
				}
				arrayOn = false;
			}
		}
		return false;
	}

	String[] subsub = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", };

	private void exportArrayToTxt(int lenR, int lenX, double _theta, double _rho) {
		File file = new File("dat/output/hough/Array" + imgProc.imgNum + ".txt");

		try (FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw)) {

			pw.println("-----------------------theta = " + _theta + ", rho = " + _rho);
			for (int r = 0; r < lenR; r++) {
				for (int x = 0; x < lenX; x++) {
					pw.print(subArray[r][x]);
				}
				pw.println();
			}
			for (int x = 0; x < lenX; x++) {
				pw.print(subsub[subsubArray[x]]);
			}
			pw.println();
			for (int x = 0; x < lenX; x++) {
				pw.print(subsub[sub2subArray[x]]);
			}
			pw.println();
			for (int x = 0; x < lenX; x++) {
				pw.print(sub3subArray[x]);
			}
			pw.println();
			for (int x = 0; x < lenX; x++) {
				pw.print(sub4subArray[x]);
			}
			pw.println();
			for (int x = 0; x < lenX; x++) {
				pw.print(sub5subArray[x]);
			}
			pw.println();
		} catch (IOException e) {
			showException(e);
		}
	}

	public void voteFieldCircle() {// 円に関する投票
		int d = 2;

		for (int x = 0; x < maxWidth; x += d) {
			for (int y = 0; y < maxHeight; y += d) {
				if ((imgProc.editingImg.getRGB(x, y) & 0xFF) < 128) {// (image.getRGB(x, y) >> 16 & 0xFF) < 128
					for (int cx = 0; cx < maxWidth; cx += d) {
						for (int cy = 0; cy < maxHeight; cy += d) {
							// double cr = radius[Math.abs(cx - x)][Math.abs(cy - y)];
							int cr = (int) radius[Math.abs(cx - x)][Math.abs(cy - y)];
							if (cr < maxRadius) {// maxRadius-1でもよし
								// fieldCircle[cx][cy][(int) cr]++;
								fieldCircle[cx][cy][cr]++;
							}
						}
					}
				}
			}
		}
	}

	public MyCircle getFieldCircle() {// 投票に基づく(x,y,r)の取得
		int voteNum = 0;// 投票数
		int X = 0;// パラメータ1
		int Y = 0;// パラメータ2
		int R = 0;// パラメータ3

		int d = 2;

		int Dr = 10;// Para.circleDr=10;
		int seads = 3;// Para.circleSeads=3;
		int minR = 30;// Para.circleMinR = 30
		double vr = 1;// Para.circleVr=0.75;
		for (int x = 0; x < maxWidth; x += d) {
			for (int y = 0; y < maxHeight; y += d) {
				// 恐らく、r方向に急激な変化がみられる
				for (int r = minR + Dr; r < maxRadius - Dr; r++) {
					if (voteNum < fieldCircle[x][y][r]) {

						int c1 = fieldCircle[x][y][r - Dr] + fieldCircle[x][y][r - Dr + 1]
								+ fieldCircle[x][y][r - Dr + 2];
						int c2 = fieldCircle[x][y][r - 1] + fieldCircle[x][y][r] + fieldCircle[x][y][r + 1];
						int c3 = fieldCircle[x][y][r + Dr - 2] + fieldCircle[x][y][r + Dr - 1]
								+ fieldCircle[x][y][r + Dr];

						boolean condition = (c1 * seads < c2 && c3 * seads < c2 && fieldCircle[x][y][r] > r * vr);
						// fieldCircle[x][y][r] > r * vr → 全周の75%

						if (condition) {
							X = x;
							Y = y;
							R = r;
							voteNum = fieldCircle[x][y][r];
						} else {
							// break;
						}
					}
				}
			}
		}

		// 余計な円を省く処理
		int pxE = 20;
		int pyE = 20;
		int radiusE = 20;
		for (int dx = -pxE; dx <= pxE; dx++) {
			for (int dy = -pyE; dy <= pyE; dy++) {
				for (int dr = -radiusE; dr <= radiusE; dr++) {
					double pxD = X + dx;
					double pyD = Y + dy;
					double radiusD = R + dr;
					if (inRange(pxD, pyD, radiusD)) {
						fieldCircle[(int) pxD][(int) pyD][(int) radiusD] = 0;
					}
				}
			}
		}

		return new MyCircle(new MyPoint(X, Y), R);
	}

	public MyCircle restoreCircle(MyPoint p, double r) {
		return new MyCircle(p, r);
	}

	private boolean inRange(double theta, double rho) {
		return (0 <= theta && theta < maxTheta) && (-maxRho <= rho && rho < maxRho);
	}

	private boolean inRange(double x, double y, double r) {
		return (0 <= x && x < maxWidth) && (0 <= y && y < maxHeight) && (0 < r && r < maxRadius);
	}

	private void showException(Exception e) {
		StackTraceElement[] ste = e.getStackTrace();
		System.err.println("例外発生 : " + e.getClass().getName());
		System.err.println("例外内容 : " + e.getMessage());
		System.err.println("発生場所 : " + ste[ste.length - 1]);
		System.exit(0);
	}
}

