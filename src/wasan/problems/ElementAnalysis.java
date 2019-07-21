package wasan.problems;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 和算図形問題に含まれる幾何要素(点・線分・円)、図形要素(n角形・円)の認識に関するクラスです。<br>
 * 幾何要素はHough変換(HoughTransformクラス)を利用して抽出し、図形要素は抽出された幾何要素に基づいて分析されます。
 * 
 * @author Takuma Tsuchihashi
 */
public class ElementAnalysis {

	/**
	 * Hough変換を利用するためのHoughTransformクラス変数です。<br>
	 * この変数を用いることでHoughTransformクラス内のメソッドなどを呼び出すことができます。
	 */
	private HoughTransform houghTrans;

	// 以下、図形問題から仮抽出された幾何要素(点・線分・円)を保持するリストです。
	/**
	 * 図形問題から仮抽出された点を保持します。<br>
	 * 仮抽出の点を補正した情報はdetectPointに記録されます。
	 */
	public ArrayList<MyPoint> scannedPoint = new ArrayList<MyPoint>();
	/**
	 * 図形問題から仮抽出された線分を保持します。<br>
	 * 仮抽出の線分を補正した情報はdetectLineに記録されます。
	 */
	public ArrayList<MyLine> scannedLine = new ArrayList<MyLine>();
	/**
	 * 図形問題から仮抽出された円を保持します。<br>
	 * 仮抽出の円を補正した情報はdetectCircleに記録されます。
	 */
	public ArrayList<MyCircle> scannedCircle = new ArrayList<MyCircle>();

	// 以下、図形問題から仮抽出された要素を補正し、確定した幾何要素(点・線分)と図形要素(n角形・円)を保持するリストです。
	/**
	 * 図形問題から仮抽出された点を補正し、確定した点を保持します。<br>
	 * 仮抽出の点を保持するscannedPointを補正した情報を記録します。
	 */
	public ArrayList<MyPoint> detectedPoint = new ArrayList<MyPoint>();
	/**
	 * 図形問題から仮抽出された線分を補正し、確定した線分を保持します。<br>
	 * 仮抽出の線分を保持するscannedLineを補正した情報を記録します。
	 */
	public ArrayList<MyLine> detectedLine = new ArrayList<MyLine>();
	/**
	 * 図形問題から仮抽出された円を補正し、確定した円を保持します。<br>
	 * 仮抽出の円を保持するscannedCircleを補正した情報を記録します。
	 */
	public ArrayList<MyCircle> detectedCircle = new ArrayList<MyCircle>();
	/**
	 * 確定した点と線分から分析されたn角形を保持します。<br>
	 * 補正した点のdetectPoint、線分のdetectLineから認識したn角形を記録します。
	 */
	public ArrayList<MyPolygon> detectedPolygon = new ArrayList<MyPolygon>();

	/**
	 * Hough変換(HoughTransform)を指定し、要素分析(ElementAnalysis)のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param houghTrans
	 *            Hough変換を利用するためのHoughTransformクラス変数
	 */
	public ElementAnalysis(HoughTransform _houghTrans) {
		this.houghTrans = _houghTrans;
	}

	/**
	 * 自動タグ付けが完了した図形問題に関する不要なオブジェクトをメモリから解放します。<br>
	 * 複数の図形問題に対して連続的に自動タグ付けを行う際、OutOfMemoryErrorを回避します。
	 */
	public void freeResource() {
		houghTrans = null;

		this.scannedPoint.clear();
		this.scannedLine.clear();
		this.scannedCircle.clear();

		this.detectedPoint.clear();
		this.detectedLine.clear();
		this.detectedCircle.clear();
		this.detectedPolygon.clear();
	}

	/**
	 * Hough変換を利用して仮抽出された線分と円に基づき、図形問題から点を仮抽出します。<br>
	 * また、仮抽出の点の重複について修正を行います。
	 */
	public void scanPoint() {
		//
		for (int i = 0; i < this.scannedLine.size(); i++) {
			for (int j = i + 1; j < this.scannedLine.size(); j++) {
				MyLine l1 = this.scannedLine.get(i);
				MyLine l2 = this.scannedLine.get(j);

				MyPoint p = MyPoint.getIntersection1(l1, l2, 1);// 2本の線分同士の交点を取得します。
				if (p != null) {
					this.scannedPoint.add(p);
				}
			}
		}

		//
		for (int i = 0; i < this.scannedLine.size(); i++) {
			for (int j = 0; j < this.scannedCircle.size(); j++) {
				MyLine l = this.scannedLine.get(i);
				MyCircle c = this.scannedCircle.get(j);

				MyPoint p1 = MyPoint.getIntersection2(l, c, 1);// 線分と円の交点を取得します。
				if (p1 != null) {
					this.scannedPoint.add(p1);
				}

				MyPoint p2 = MyPoint.getIntersection2(l, c, 2);// 線分と円の交点を取得します。
				if (p2 != null) {
					this.scannedPoint.add(p2);
				}
			}
		}

		// 以下、線分の端点の検出
		for (int i = 0; i < this.scannedLine.size(); i++) {
			MyPoint p1 = this.scannedLine.get(i).start;
			if (p1 != null) {
				this.scannedPoint.add(p1);
			}

			MyPoint p2 = this.scannedLine.get(i).end;
			if (p2 != null) {
				this.scannedPoint.add(p2);
			}
		}

		//
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			this.scannedPoint = organizePoint(i, i + 1);// 仮抽出された線分と円に基づき、図形問題から仮抽出された点の重複を修正します。
		}
	}

	/**
	 * 仮抽出された線分と円に基づき、図形問題から仮抽出された点の重複を修正します。<br>
	 * 重複はユークリッド距離が近い2点について可能な範囲で修正されます。
	 * 
	 * @param index1
	 *            仮抽出された任意の1点のMyPointクラスリスト内インデックスを表すint型変数
	 * @param index2
	 *            任意の1点とのユークリッド距離を測る点のMyPointクラスリスト内インデックス(index2=index1+1とする)を表すint型変数
	 * @return 仮抽出された点の重複を修正したMyPointクラスリスト
	 */
	private ArrayList<MyPoint> organizePoint(int index1, int index2) {
		for (int i = index2; i < this.scannedPoint.size(); i++) {
			MyPoint p1 = this.scannedPoint.get(i);
			MyPoint p2 = this.scannedPoint.get(index1);

			if (p1.calcDistToPoint(p2) < 20) {// 2点間の距離を取得します。
				for (int j = 0; j < this.scannedLine.size(); j++) {
					MyLine l = this.scannedLine.get(j);
					if (l.start == p1) {
						l.start = p2;
						p2.x = (p1.x + p2.x) / 2;
						p2.y = (p1.y + p2.y) / 2;
					} else if (l.end == p1) {
						l.end = p2;
						p2.x = (p1.x + p2.x) / 2;
						p2.y = (p1.y + p2.y) / 2;
					}
				}
				this.scannedPoint.remove(i);
				return organizePoint(index1, i);
			}
		}

		return this.scannedPoint;
	}

	/**
	 * Hough変換を利用して図形問題から線分を仮抽出します。<br>
	 * また、仮抽出の線分の端点について修正を行います。
	 * 
	 * @param num
	 *            仮抽出する線分の上限を表すint型変数
	 */
	public void scanLine(int num) {
		for (int i = 0; i < num; i++) {
			MyLine l1 = this.houghTrans.getFieldLine();
			MyLine l2 = this.houghTrans.restoreLine(l1.theta, l1.rho);

			if ((l1.theta != 0 || l1.rho != 0) && l2.getLength() > 0) {
				this.scannedLine.add(l2);
			} else {
				break;
			}
		}
		this.scannedLine = organizeLine();// Hough変換を利用して図形問題から仮抽出された線分の端点を修正します。
	}

	/**
	 * Hough変換を利用して図形問題から仮抽出された線分の端点を修正します。<br>
	 * 端点は次数が2未満であるものについて可能な範囲で修正されます。
	 * 
	 * @return 仮抽出された線分の端点を修正したMyLineクラスリスト
	 */
	private ArrayList<MyLine> organizeLine() {
		for (int i = 0; i < this.scannedLine.size(); i++) {
			MyLine l1 = this.scannedLine.get(i);

			// 線分上の点に近い場合は置き換え
			for (int j = i + 1; j < this.scannedLine.size(); j++) {
				MyLine l2 = this.scannedLine.get(j);

				MyPoint p = MyPoint.getIntersection1(l1, l2, 2);// 2本の直線同士の交点を取得します。
				if (l1.start.isOnLine(l2)) {
					l1.start.x = p.x;
					l1.start.y = p.y;
				} else if (l1.end.isOnLine(l2)) {
					l1.end.x = p.x;
					l1.end.y = p.y;
				}
			}

			// 円上の点に近い場合は置き換え
			for (int j = 0; j < this.scannedCircle.size(); j++) {
				MyCircle c = this.scannedCircle.get(j);

				MyPoint p1 = MyPoint.getIntersection2(l1, c, 1);// 線分と円の交点を取得します。
				MyPoint p2 = MyPoint.getIntersection2(l1, c, 2);// 線分と円の交点を取得します。
				MyPoint p3 = null;

				if (p1 != null && p2 == null) {
					p3 = p1;
				} else if (p1 == null && p2 != null) {
					p3 = p2;
				}

				if (l1.start.isOnCircle(c)) {
					if (p1 != null && p2 != null) {
						p3 = (p1.calcDistToPoint(l1.start) < p2.calcDistToPoint(l1.start)) ? p1 : p2;
					}
					l1.start.x = (p3 != null) ? p3.x : l1.start.x;
					l1.start.y = (p3 != null) ? p3.y : l1.start.y;
				} else if (l1.end.isOnCircle(c)) {
					if (p1 != null && p2 != null) {
						p3 = (p1.calcDistToPoint(l1.end) < p2.calcDistToPoint(l1.end)) ? p1 : p2;
					}
					l1.end.x = (p3 != null) ? p3.x : l1.end.x;
					l1.end.y = (p3 != null) ? p3.y : l1.end.y;
				}
			}
		}

		return this.scannedLine;
	}

	/**
	 * Hough変換を利用して図形問題から円を仮抽出します。<br>
	 * 
	 * @param num
	 *            仮抽出する円の上限を表すint型変数
	 */
	public void scanCircle(int num) {
		for (int i = 0; i < num; i++) {
			MyCircle c1 = this.houghTrans.getFieldCircle();
			MyCircle c2 = this.houghTrans.restoreCircle(c1.center, c1.radius);

			if (c1.center.x != 0 || c1.center.y != 0 || c1.radius != 0) {
				this.scannedCircle.add(c2);
			} else {
				break;
			}
		}
	}

	/**
	 * 図形問題から仮抽出された点や線分を補正し、互いに関連付けます。<br>
	 * この点と線分の関連付けは、図形要素のn角形の認識を目的に行われます。
	 */
	public void relateElement() {
		relateToPoint1();// ①点から見た関係性(次数)の定義
		modifyLine();// ②次数が1の線分の調整(両端が1ならば線分ごと削除、片方が1ならば....)

		relateToPoint1();
		modifyPoint();// ③不要な点(ただの交点)の削除

		relateToPoint1();
		relateToLine();// ④線分から見た(点との)関係性
		relateToPoint2();// ⑤点と点との関係性
	}

	/**
	 * 任意の1点を通過する線分や円について、関連付けを行います。<br>
	 * これらは任意の1点に対して、線分や円との関連性が記録されます。
	 */
	private void relateToPoint1() {// ①点から見た関係性(次数)の定義
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			MyPoint p = this.scannedPoint.get(i);
			p.relatedLine.clear();
			p.relatedCircle.clear();

			for (int j = 0; j < this.scannedLine.size(); j++) {// 線分との関連性定義(点が通る線分)
				MyLine l = this.scannedLine.get(j);
				if (p.isOnLine(l)) {
					p.relatedLine.add(l);
				}
			}

			for (int j = 0; j < this.scannedCircle.size(); j++) {// 円との関連性定義(点が通る円)
				MyCircle c = this.scannedCircle.get(j);
				if (p.isOnCircle(c)) {
					p.relatedCircle.add(c);
				}
			}
		}
	}

	/**
	 * 点との関連性に基づき、線分の補正・削除を行います。<br>
	 * 補正・削除は特に次数が1である端点を持つ線分を対象とします。
	 */
	private void modifyLine() {// ②次数が1の線分の調整(両端が1ならば線分ごと削除、片方が1ならば....)
		modifyLine1();// 両端の次数が1の場合
		modifyLine2();// 片方の次数が1の場合
	}

	/**
	 * 両方の端点の次数が1である線分を補正・削除します。<br>
	 */
	private void modifyLine1() {
		for (int i = this.scannedLine.size() - 1; i >= 0; i--) {
			MyLine l = this.scannedLine.get(i);
			boolean smallDegree1 = (l.start.relatedLine.size() == 1) && (l.start.relatedCircle.size() == 0);
			boolean smallDegree2 = (l.end.relatedLine.size() == 1) && (l.end.relatedCircle.size() == 0);

			if (smallDegree1 && smallDegree2) {// 修正したい要素 : 両端が次数1である線分(と点)
				double[] minDist = new double[2];
				Arrays.fill(minDist, Double.MAX_VALUE);

				MyPoint[] pointArray = new MyPoint[2];
				Arrays.fill(pointArray, null);

				for (int j = 0; j < this.scannedPoint.size(); j++) {// 引き寄せられる感じ
					MyPoint p = this.scannedPoint.get(j);
					if (p.calcDistToLine(l) < 10) {
						if (p != l.start && p.calcDistToPoint(l.start) < minDist[0]) {
							minDist[0] = p.calcDistToPoint(l.start);
							pointArray[0] = p;
						} else if (p != l.end && p.calcDistToPoint(l.end) < minDist[1]) {
							minDist[1] = p.calcDistToPoint(l.end);
							pointArray[1] = p;
						}
					}
				}

				boolean[] condition = new boolean[3];
				condition[0] = (pointArray[0] != null && pointArray[0] != l.end);
				condition[1] = (pointArray[1] != null && pointArray[1] != l.start);
				condition[2] = (pointArray[0] != pointArray[1]);

				if (condition[0] && condition[1] && condition[2]) {
					this.scannedPoint.remove(l.start);
					this.scannedPoint.remove(l.end);
					l.start = pointArray[0];
					l.end = pointArray[1];
				} else {
					this.scannedLine.remove(l);
					this.scannedPoint.remove(l.start);
					this.scannedPoint.remove(l.end);
				}
			}
		}
	}

	/**
	 * 片方の端点の次数が1である線分を補正します。<br>
	 */
	private void modifyLine2() {
		for (int i = this.scannedLine.size() - 1; i >= 0; i--) {
			MyLine l1 = this.scannedLine.get(i);
			boolean smallDegree1 = (l1.start.relatedLine.size() == 1) && (l1.start.relatedCircle.size() == 0);
			boolean smallDegree2 = (l1.end.relatedLine.size() == 1) && (l1.end.relatedCircle.size() == 0);

			int type = 0;
			if (smallDegree1 && !smallDegree2) {// 先に点を通るような直線を探すとよい
				type = 1;
			} else if (!smallDegree1 && smallDegree2) {
				type = 2;
			}

			if (type > 0) {
				boolean complete = false;
				MyPoint[] pointArray = new MyPoint[2];

				pointArray[0] = (type == 1) ? l1.start : l1.end;
				pointArray[1] = (type == 1) ? l1.end : l1.start;

				for (int j = 0; j < this.scannedLine.size(); j++) {
					MyLine l2 = this.scannedLine.get(j);
					if ((l1 != l2) && pointArray[0].calcDistToLine(l2) < 10) {
						double dist1 = pointArray[0].calcDistToPoint(l2.start);
						double dist2 = pointArray[0].calcDistToPoint(l2.end);
						if (dist1 < dist2) {
							l2.start = pointArray[0];
						} else {
							l2.end = pointArray[0];
						}
						complete = true;
					}
				}

				if (!complete) {
					double minDist = Double.MAX_VALUE;
					MyPoint p1 = null;

					for (int j = 0; j < this.scannedPoint.size(); j++) {
						MyPoint p2 = this.scannedPoint.get(j);
						if (p2.calcDistToLine(l1) < 10) {
							if (p2 != pointArray[0] && p2.calcDistToPoint(pointArray[0]) < minDist) {
								minDist = p2.calcDistToPoint(pointArray[0]);
								p1 = p2;
							}
						}
					}

					if (p1 != null && p1 != pointArray[1]) {
						this.scannedPoint.remove(pointArray[0]);
						if (type == 1) {
							l1.start = p1;
						} else {
							l1.end = p1;
						}
					}
				}
			}
		}
	}

	/**
	 * 線分や円との関連性に基づき、点の削除を行います。<br>
	 * 特に線分や円から発生した交点を対象とします。
	 */
	private void modifyPoint() {
		for (int i = this.scannedPoint.size() - 1; i >= 0; i--) {
			MyPoint p = this.scannedPoint.get(i);

			int relatedTotal = p.relatedPoint.size() + p.relatedLine.size() + p.relatedCircle.size();
			if (relatedTotal <= 2 && !isEndPoint(p)) {
				this.scannedPoint.remove(p);
			}
		}
	}

	/**
	 * 任意の1点が線分の端点であるか否かを判定します。<br>
	 * 
	 * @param p
	 *            任意の1点を表すMyPointクラス変数
	 * @return 任意の1点が線分の端点であるか否かを示すboolean型変数
	 */
	private boolean isEndPoint(MyPoint p) {// 点が線分の端点かどうかの判定
		for (int i = 0; i < this.scannedLine.size(); i++) {
			MyLine l = this.scannedLine.get(i);
			if (p == l.start || p == l.end) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 任意の線分上に存在する点について、関連付けを行います。<br>
	 * これらは任意の線分に対して、点との関連性が記録されます。
	 */
	private void relateToLine() {
		for (int i = 0; i < this.scannedLine.size(); i++) {// 点との関連性定義(線分上の点)
			MyLine l = this.scannedLine.get(i);
			for (int j = 0; j < this.scannedPoint.size(); j++) {
				MyPoint p = this.scannedPoint.get(j);
				if (p.isOnLine(l)) {
					l.relatedPoint.add(p);
				}
			}
		}
	}

	/**
	 * 任意の1点と線分を介して繋がる点について、関連付けを行います。<br>
	 * これらは任意の1点に対して、その他の点との関連性が記録されます。
	 */
	private void relateToPoint2() {
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			MyPoint p1 = this.scannedPoint.get(i);
			for (int j = 0; j < p1.relatedLine.size(); j++) {
				MyLine l = p1.relatedLine.get(j);
				for (int k = 0; k < l.relatedPoint.size(); k++) {
					if (p1 != l.relatedPoint.get(k)) {
						MyPoint p2 = l.relatedPoint.get(k);
						p1.relatedPoint.add(p2);
					}
				}
			}
		}
	}

	/**
	 * 補正した要素を抽出された幾何要素(点・線分・円)として確定し、図形要素(n角形・円)を分析します。<br>
	 */
	public void detectElement() {
		detectCircle();
		detectLine();
		detectPoint();
		detectPolygon();
	}

	/**
	 * 補正した円を図形問題から抽出された円として確定し、記録します。<br>
	 */
	private void detectCircle() {
		for (int i = 0; i < this.scannedCircle.size(); i++) {
			this.detectedCircle.add(this.scannedCircle.get(i));
		}
	}

	/**
	 * 補正した線分を図形問題から抽出された線分として確定し、記録します。<br>
	 */
	private void detectLine() {
		for (int i = 0; i < this.scannedLine.size(); i++) {
			this.detectedLine.add(this.scannedLine.get(i));
		}
	}

	/**
	 * 補正した点を図形問題から抽出された点として確定し、記録します。<br>
	 */
	private void detectPoint() {
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			this.detectedPoint.add(this.scannedPoint.get(i));
		}
	}

	/**
	 * 抽出が確定した点と線分に基づき、図形問題に含まれるn角形を分析します。<br>
	 */
	private void detectPolygon() {
		for (int i = 3; i <= 6; i++) {
			PolygonAnalysis.generateCombination(this.detectedPoint, new int[i], 0, 1);// n角形毎に考えられる頂点の組み合わせを列挙します。

			for (int j = 0; j < PolygonAnalysis.combination.length; j++) {
				// ①点候補セット(n個で1セット)を1つ作成する。(後の計算量を減らすために少し工夫する)
				int[] combination1 = { PolygonAnalysis.combination[j][0] };// 計算量を減らすために、最初の1つを取り出す
				int[] combination2 = new int[i - 1];
				for (int k = 0; k < PolygonAnalysis.combination[j].length - 1; k++) {
					combination2[k] = PolygonAnalysis.combination[j][k + 1];
				}

				// ②作成した1つの点候補セットに対して、(n-1)!通りの順列を列挙する。
				// int c = (myPoly2.cirPermutation(r)).intValue();// = (n-1)!
				int[][] permutation = new int[(PolygonAnalysis.calcFactorial(i - 1))][i];
				PolygonAnalysis.generatePermutation(combination2, new int[0]);// n角形の頂点の組み合わせ毎の順列を列挙します。
				for (int k = 0; k < permutation.length; k++) {
					permutation[k][0] = combination1[0];
					for (int l = 1; l < permutation[k].length; l++) {
						permutation[k][l] = PolygonAnalysis.permutation[k][l - 1];
					}
				}

				// ③列挙した順列を回転するように検査する
				for (int k = 0; k < permutation.length; k++) {
					if (PolygonAnalysis.isPolygon(this.detectedPoint, permutation[k])) {// 図形問題から抽出された点のうち、任意のn点がn角形を構成するか否かを判定します。
						MyPoint[] vertex = new MyPoint[permutation[k].length];
						for (int l = 0; l < permutation[k].length; l++) {
							vertex[l] = this.detectedPoint.get(permutation[k][l]);
						}

						MyPolygon pg = new MyPolygon(vertex);
						if (pg.hasFeature()) {
							this.detectedPolygon.add(pg);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * 和算図形問題に含まれるn角形の認識に関するクラスです。<br>
	 * このクラスでは、主にn角形の分析に利用するアルゴリズムを定義します。
	 * 
	 * @author Takuma Tsuchihashi
	 */
	private static class PolygonAnalysis {

		/**
		 * n角形毎に考えられる頂点の組み合わせを保持します。<br>
		 * 組み合わせは頂点が持つインデックスによって管理されます。
		 */
		static int[][] combination;

		/**
		 * n角形の頂点の組み合わせ毎の順列を保持します。<br>
		 * 順列は頂点が持つインデックスによって管理されます。
		 */
		static int[][] permutation;

		/**
		 * n角形の頂点の組み合わせや順列を列挙する際に利用するインデックスです。<br>
		 */
		static int index;

		/**
		 * n角形毎に考えられる頂点の組み合わせを列挙します。<br>
		 * これらの組み合わせは頂点が持つインデックスに基づいて列挙されます。
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param r
		 *            図形問題から抽出された点のうち、任意で取り出す個数を表すint型配列(N個を取り出す際は必ずnew
		 *            int[N]を代入してください。)
		 * @param start
		 *            n角形の頂点の組み合わせを列挙する際に利用するint型変数(メソッドを呼び出す際は必ず0を代入してください。)
		 * @param plus
		 *            n角形の頂点の組み合わせを列挙する際に利用するint型変数(メソッドを呼び出す際は必ず1を代入してください。)
		 */
		static void generateCombination(ArrayList<MyPoint> pointList, int[] r, int start, int plus) {
			if (plus <= 1) {
				combination = new int[calcCombination(pointList.size(), r.length)][r.length];
				for (int i = 0; i < combination.length; i++) {
					for (int j = 0; j < combination[i].length; j++) {
						combination[i][j] = -1;
					}
				}
				index = 0;
			}

			if (r.length < plus) {
				for (int i = 0; i < r.length; i++) {
					combination[index][i] = r[i];
				}
				index++;

				return;
			}
			for (int i = start; i < pointList.size() - r.length + plus; i++) {
				r[plus - 1] = i;
				generateCombination(pointList, r, i + 1, plus + 1);
			}
		}

		/**
		 * n角形の頂点の組み合わせ毎の順列を列挙します。<br>
		 * これらの順列は頂点が持つインデックスに基づいて列挙されます。
		 * 
		 * @param n
		 *            n角形の頂点の組み合わせ(頂点のインデックスの組み合わせ)を保持するint型配列
		 * @param array
		 *            n角形の頂点の組み合わせ毎の順列を列挙する際に利用するint型配列(メソッドを呼び出す際は必ずnew
		 *            int[0]を代入してください。)
		 */
		static void generatePermutation(int[] n, int[] array) {
			if (array.length == 0) {
				permutation = new int[(calcFactorial(n.length))][n.length];
				for (int i = 0; i < permutation.length; i++) {
					for (int j = 0; j < permutation[i].length; j++) {
						permutation[i][j] = -1;
					}
				}
				index = 0;
			}

			if (n.length == 0) {// 全て終えた時点で一気に入れる
				for (int i = 0; i < array.length; i++) {
					permutation[index][i] = array[i];
				}
				index++;
			} else {
				for (int i = 0; i < n.length; i++) {
					int[] value = { n[i] };
					generatePermutation(concatArray(extractArray(n, 0, i), extractArray(n, i + 1)),
							concatArray(array, value));
				}
			}
		}

		/**
		 * 2つのint型配列を結合し、新たな配列として取得します。<br>
		 * 
		 * @param array1
		 *            結合したい1つ目のint型配列
		 * @param array2
		 *            結合したい2つ目のint型配列
		 * @return 2つのint型配列を結合した、新たなint型配列
		 */
		static int[] concatArray(int[] array1, int[] array2) {
			int[] newArray = new int[array1.length + array2.length];
			for (int i = 0; i < array1.length; i++) {
				newArray[i] = array1[i];
			}
			for (int i = 0; i < array2.length; i++) {
				newArray[i + array1.length] = array2[i];
			}
			return newArray;
		}

		/**
		 * int型配列から任意の範囲の要素を取り出し、新たな配列として取得します。<br>
		 * 
		 * @param array
		 *            任意の範囲の要素を取り出したいint型配列
		 * @param index
		 *            要素を取り出す範囲を表すint型変数(引数が1つの場合はインデックス～最後、引数が2つの場合はインデックス1～インデックス2の範囲を取り出します。)
		 * @return 任意の範囲の要素を取り出した、新たなint型配列
		 */
		static int[] extractArray(int[] array, int... index) {
			int start = index[0];
			int end = (index.length > 1) ? index[1] : array.length;

			int[] newArray = new int[Math.abs(end - start)];
			for (int i = start; i < end; i++) {
				newArray[i - start] = array[i];
			}

			return newArray;
		}

		/**
		 * nの階乗の値を計算します。<br>
		 * 
		 * @param num
		 *            nの階乗の"n"を表すint型変数
		 * @return nの階乗の値を表すint型変数
		 */
		static int calcFactorial(int num) {
			if (num <= 1) {
				return 1;
			}
			return num * calcFactorial(num - 1);
		}

		/**
		 * n個の中からr個を取り出す組み合わせ総数の値を計算します。<br>
		 * 
		 * @param n
		 *            「n個の中からr個を取り出す」の"n"を表すint型変数
		 * @param r
		 *            「n個の中からr個を取り出す」の"r"を表すint型変数
		 * @return n個の中からr個を取り出す組み合わせ総数の値を表すint型変数
		 */
		static int calcCombination(int n, int r) {
			if (n < r) {
				return 0;
			} else if ((n < 0) && (r < 0)) {
				return 0;
			}

			if (r == 0 || r == n) {
				return 1;
			} else if (r == 1 || r == n - 1) {
				return n;
			}

			return calcCombination(n - 1, r - 1) + calcCombination(n - 1, r);
		}

		/**
		 * 図形問題から抽出された点のうち、任意のn点がn角形を構成するか否かを判定します。<br>
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @return 任意のn点がn角形を構成するか否かを示すboolean型変数
		 */
		static boolean isPolygon(ArrayList<MyPoint> pointList, int[] pattern) {
			boolean[] condition = new boolean[4];

			condition[0] = isCycle(pointList, pattern);
			condition[1] = isConvex(pointList, pattern);
			condition[2] = notSelfCross(pointList, pattern);
			condition[3] = notCollinear(pointList, pattern);

			return (condition[0] && condition[1] && condition[2] && condition[3]);
		}

		/**
		 * 図形問題から抽出された点のうち、任意のn点が閉路を構成するか否かを判定します。<br>
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @return 任意のn点が閉路を構成するか否かを示すboolean型変数
		 */
		static boolean isCycle(ArrayList<MyPoint> pointList, int[] pattern) {
			int pairNum = 2;// pattern 0 2 3 5 → [0, 2][2, 3][3, 5][5, 0]
			int[][] pair = generatePair(pattern, pairNum);

			int count = 0;
			for (int i = 0; i < pair.length; i++) {
				MyPoint p1 = pointList.get(pair[i][0]);
				for (int j = 0; j < p1.relatedPoint.size(); j++) {
					MyPoint p2 = pointList.get(pair[i][1]);
					if (p2 == p1.relatedPoint.get(j)) {
						count++;
						break;
					}
				}

				if (count == pattern.length) {
					return true;
				}
			}

			return false;
		}

		/**
		 * 図形問題から抽出された点のうち、任意のn点による閉路が凸性を満たすか否かを判定します。<br>
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @return 任意のn点による閉路が凸性を満たすか否かを示すboolean型変数
		 */
		static boolean isConvex(ArrayList<MyPoint> pointList, int[] pattern) {
			int pairNum = 3;// pattern 0 2 3 5 → [0, 2, 3][2, 3, 5][3, 5, 0][5,
							// 0, 2]
			int[][] pair = generatePair(pattern, pairNum);

			int count = 0;
			double[] crossProduct = new double[2];
			for (int i = 0; i < pair.length; i++) {
				MyPoint[] pointArray1 = new MyPoint[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					pointArray1[j] = pointList.get(pair[i][j]);
				}

				MyPoint[] pointArray2 = new MyPoint[pairNum];
				int nextIndex = ((i < pair.length - 1) ? i + 1 : 0);
				for (int j = 0; j < pair[nextIndex].length; j++) {
					pointArray2[j] = pointList.get(pair[nextIndex][j]);
				}

				crossProduct[0] = (pointArray1[2].x - pointArray1[1].x) * (pointArray1[0].y - pointArray1[1].y)
						- (pointArray1[2].y - pointArray1[1].y) * (pointArray1[0].x - pointArray1[1].x);
				crossProduct[1] = (pointArray2[2].x - pointArray2[1].x) * (pointArray2[0].y - pointArray2[1].y)
						- (pointArray2[2].y - pointArray2[1].y) * (pointArray2[0].x - pointArray2[1].x);

				if (crossProduct[0] * crossProduct[1] > 0) {
					count++;
				}
			}

			return (count == pattern.length);
		}

		/**
		 * 図形問題から抽出された点のうち、任意のn点による閉路が自己交差を持たないか否かを判定します。<br>
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @return 任意のn点による閉路が自己交差を持たないか否かを示すboolean型変数
		 */
		static boolean notSelfCross(ArrayList<MyPoint> pointList, int[] pattern) {// 自己交差がないか
			MyPoint centroid = new MyPoint(0, 0);
			for (int i = 0; i < pattern.length; i++) {
				centroid.x += pointList.get(pattern[i]).x;
				centroid.y += pointList.get(pattern[i]).y;
			}
			centroid.x /= pattern.length;
			centroid.y /= pattern.length;// おそらくは問題なく計算できている

			int crossNum = countCrossNum(pointList, pattern, centroid);
			int windNum = countWindNum(pointList, pattern, centroid);

			return (crossNum % 2 == 1 && windNum != 0);
		}

		/**
		 * 図形問題から抽出された点のうち、任意のn点による閉路が共線条件を満たさないか否かを判定します。<br>
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @return 任意のn点による閉路が共線条件を満たさないか否かを示すboolean型変数
		 */
		static boolean notCollinear(ArrayList<MyPoint> pointList, int[] pattern) {
			int pairNum = 3;
			int[][] pair = generatePair(pattern, pairNum);

			int count = 0;
			for (int i = 0; i < pair.length; i++) {
				MyPoint[] pointArray = new MyPoint[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					pointArray[j] = pointList.get(pair[i][j]);
				}

				MyLine[] lineArray = new MyLine[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					lineArray[j] = new MyLine(pointArray[j], pointArray[(j < pair[i].length - 1) ? (j + 1) : 0]);
				}

				double[] dist = new double[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					dist[j] = pointArray[j].calcDistToLine(lineArray[(j < pair[i].length - 1) ? (j + 1) : 0]);
				}

				int threshold = 10;
				if ((dist[0] > threshold) && (dist[1] > threshold) && (dist[2] > threshold)) {
					count++;
				}
			}

			return (count == pattern.length);
		}

		/**
		 * 任意のn点の順列(点のインデックスの順列)を定数で分割したペアを作成します。<br>
		 * 例えば、点のインデックスの順列をi1,i2,i3として2で分割した場合、[i1,i2],[i2,i3],[i3,i1]のペアが生成されます。
		 * 
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @param pairNum
		 *            分割する数を表すint型変数
		 * @return 任意のn点の順列(点のインデックスの順列)を定数で分割したペアを保持するint型配列
		 */
		static int[][] generatePair(int[] pattern, int pairNum) {
			int[][] pair = new int[pattern.length][pairNum];

			int[][] index = new int[pattern.length][pairNum];
			for (int i = 0; i < pairNum; i++) {
				for (int j = 0; j < pair.length; j++) {
					int x = (i == 0) ? j : (index[j][i - 1] + 1);
					index[j][i] = (x < pair.length) ? x : 0;
					pair[j][i] = pattern[index[j][i]];
				}
			}

			return pair;
		}

		/**
		 * 交差数に基づいて、任意のn点による閉路に対してある点が内部に含まれるか否かを判定します。<br>
		 * 交差数とは、ある点から引いた半直線が任意のn点による閉路と交差する回数を表します。
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @param p
		 *            対象となる点を表すMyPointクラス変数
		 * @return 交差数に基づいて、任意のn点による閉路に対してある点が内部に含まれるか否かを示すboolean型変数
		 */
		static int countCrossNum(ArrayList<MyPoint> pointList, int[] pattern, MyPoint p) {
			int crossNum = 0;

			for (int i = 0; i < pattern.length; i++) {
				int index1 = pattern[i];
				int index2 = pattern[(i < pattern.length - 1) ? i + 1 : 0];
				MyPoint p1 = pointList.get(index1);
				MyPoint p2 = pointList.get(index2);

				if (((p1.y <= p.y) && (p2.y > p.y)) || ((p1.y > p.y) && (p2.y <= p.y))) {
					double t = (p.y - p1.y) / (p2.y - p1.y);
					if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
						crossNum++;
					}
				}
			}

			return crossNum;
		}

		/**
		 * 回転数に基づいて、任意のn点による閉路に対してある点が内部に含まれるか否かを判定します。<br>
		 * 回転数とは、任意のn点による閉路がある点の周りを周る回数を表します。
		 * 
		 * @param pointList
		 *            図形問題から抽出された点を保持するMyPointクラスリスト
		 * @param pattern
		 *            任意のn点の順列(点のインデックスの順列)を保持するint型配列
		 * @param p
		 *            対象となる点を表すMyPointクラス変数
		 * @return 回転数に基づいて、任意のn点による閉路に対してある点が内部に含まれるか否かを示すboolean型変数
		 */
		static int countWindNum(ArrayList<MyPoint> pointList, int[] pattern, MyPoint p) {
			int windNum = 0;

			for (int i = 0; i < pattern.length; i++) {
				int index1 = pattern[i];
				int index2 = pattern[(i < pattern.length - 1) ? i + 1 : 0];
				MyPoint p1 = pointList.get(index1);
				MyPoint p2 = pointList.get(index2);

				if ((p1.y <= p.y) && (p2.y > p.y)) {
					double t = (p.y - p1.y) / (p2.y - p1.y);
					if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
						windNum++;
					}
				} else if ((p1.y > p.y) && (p2.y <= p.y)) {
					double t = (p.y - p1.y) / (p2.y - p1.y);
					if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
						windNum--;
					}
				}
			}

			return windNum;
		}
	}
}
