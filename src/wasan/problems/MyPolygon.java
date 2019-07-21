package wasan.problems;

import java.util.ArrayList;

/**
 * 和算図形問題に含まれる図形要素の「n角形」に関するクラスです。<br>
 * 
 * @author Takuma Tsuchihashi
 */
public class MyPolygon {

	// 以下、n角形を構成する要素を表すリスト・変数です。
	/**
	 * n角形の頂点を保持します。<br>
	 */
	public ArrayList<MyPoint> vertex = new ArrayList<MyPoint>();
	/**
	 * n角形の辺を保持します。<br>
	 */
	public ArrayList<MyLine> side = new ArrayList<MyLine>();
	/**
	 * n角形の重心を表します。<br>
	 */
	public MyPoint centroid;
	
	/**
	 * n角形の全ての頂点を指定し、n角形のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param _p
	 *            n角形の頂点を表すMyPointクラス変数(全ての頂点を順に引数とします。)
	 */
	public MyPolygon(MyPoint... _p) {
		for (int i = 0; i < _p.length; i++) {
			this.vertex.add(_p[i]);
		}

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint start = vertex.get(i);
			MyPoint end = vertex.get((i < vertex.size() - 1) ? i + 1 : 0);

			side.add(new MyLine(start, end));
		}

		centroid = new MyPoint(0, 0);
		for (int i = 0; i < vertex.size(); i++) {
			centroid.x += vertex.get(i).x;
			centroid.y += vertex.get(i).y;
		}
		centroid.x /= vertex.size();
		centroid.y /= vertex.size();
	}
	
	/**
	 * 「n角形が円に内接する」を満たすか否かを判定します。 <br>
	 * 「n角形が円に内接する」はn角形から見た円との関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「n角形が円に内接する」を満たすか否かを示すboolean型変数
	 */
	public boolean inscribeCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);
		int[] sideState = c.classifySide(this.side);

		condition[0] = c.includePoint(this.centroid);
		condition[1] = (vertexState[1] == this.vertex.size());
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}
	
	/**
	 * 「n角形が円の内部に存在する」を満たすか否かを判定します。 <br>
	 * 「n角形が円の内部に存在する」はn角形から見た円との関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「n角形が円の内部に存在する」を満たすか否かを示すboolean型変数
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);
		int[] sideState = c.classifySide(this.side);

		condition[0] = c.includePoint(this.centroid);
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}
	
	/**
	 * 「n角形と円が互いに隣接する」を満たすか否かを判定します。 <br>
	 * 「n角形と円が互いに隣接する」はn角形から見た円との関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「n角形と円が互いに隣接する」を満たすか否かを示すboolean型変数
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[][] condition = new boolean[3][2];

		int[] vertexState = c.classifyVertex(this.vertex);
		int[] sideState = c.classifySide(this.side);

		condition[0][0] = !c.includePoint(this.centroid) && !this.includePoint(c.center);

		condition[1][0] = (vertexState[1] == 0) && (vertexState[2] == this.vertex.size());
		condition[2][0] = (sideState[1] == 1) && (sideState[2] == this.side.size() - 1);

		condition[1][1] = (vertexState[1] == 1) && (vertexState[2] == this.vertex.size() - 1);
		condition[2][1] = (sideState[1] == 0) && (sideState[2] == this.side.size());

		return (condition[0][0] && ((condition[1][0] && condition[2][0]) || (condition[1][1] && condition[2][1])));
	}
	
	/**
	 * 「n(A)角形がn(B)角形に内接する」を満たすか否かを判定します。 <br>
	 * 「n(A)角形がn(B)角形に内接する」は二つのn角形同士の関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「n(A)角形がn(B)角形に内接する」を満たすか否かを示すboolean型変数
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);

		condition[0] = pg.includePoint(this.centroid);
		condition[1] = (vertexState[1] == this.vertex.size());

		return (condition[0] && condition[1]);
	}
	
	/**
	 * 「n(A)角形がn(B)角形の内部に存在する」を満たすか否かを判定します。 <br>
	 * 「n(A)角形がn(B)角形の内部に存在する」は二つのn角形同士の関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「n(A)角形がn(B)角形の内部に存在する」を満たすか否かを示すboolean型変数
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);

		condition[0] = pg.includePoint(this.centroid);
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);

		return (condition[0] && condition[1]);
	}
	
	/**
	 * 「n(A)角形とn(B)角形が互いに隣接する」を満たすか否かを判定します。 <br>
	 * 「n(A)角形とn(B)角形が互いに隣接する」は二つのn角形同士の関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「n(A)角形とn(B)角形が互いに隣接する」を満たすか否かを示すboolean型変数
	 */
	public boolean adjoinPolygon(MyPolygon pg) {//
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = !pg.includePoint(this.centroid) && !this.includePoint(pg.centroid);
		condition[1][0] = (vertexState1[0] == 0) && withinRange(vertexState1[1], 1, 2)
				&& withinRange(vertexState1[2], this.vertex.size() - 2, this.vertex.size() - 1);
		condition[1][1] = (vertexState2[0] == 0) && withinRange(vertexState2[1], 1, 2)
				&& withinRange(vertexState2[2], pg.vertex.size() - 2, pg.vertex.size() - 1);

		return (condition[0][0] && (condition[1][0] || condition[1][1]));
	}
	
	/**
	 * 「n(A)角形とn(B)角形が互いに重なり合う」を満たすか否かを判定します。 <br>
	 * 「n(A)角形とn(B)角形が互いに重なり合う」は二つのn角形同士の関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「n(A)角形とn(B)角形が互いに重なり合う」を満たすか否かを示すboolean型変数
	 */
	public boolean overlapPolygon(MyPolygon pg) {
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = true;
		condition[1][0] = withinRange((vertexState1[0] + vertexState1[1]), 1, this.vertex.size() - 1)
				&& withinRange(vertexState1[2], 1, this.vertex.size() - 1);
		condition[1][1] = withinRange((vertexState2[0] + vertexState2[1]), 1, pg.vertex.size() - 1)
				&& withinRange(vertexState2[2], 1, pg.vertex.size() - 1);

		return (condition[0][0] && (condition[1][0] && condition[1][1]));
	}
	
	/**
	 * n角形に対する円周上の8点の状態を記録します。<br>
	 * 記録される状態は「円周上の点がn角形の内部にある」「円周上の点がn角形上にある」「円周上の点がn角形の外部にある」です。
	 * 
	 * @param circum
	 *            円周上の8点を表すMyPointクラス配列
	 * @return n角形に対する円周上の8点の状態を保持するint型配列
	 */
	public int[] classifyCircum(MyPoint[] circum) {
		int[] circumState = new int[3];// 0:内側, 1:頂点に一致or辺に接する, 2:外側

		for (int i = 0; i < circum.length; i++) {
			MyPoint p = circum[i];

			if (matchVertex(p) || isOnSide(p)) {
				circumState[1]++;
			} else {
				if (includePoint(p)) {
					circumState[0]++;
				} else {
					circumState[2]++;
				}
			}
		}
		return circumState;
	}
	
	/**
	 * n(A)角形に対するn(B)角形の全ての頂点の状態を記録します。<br>
	 * 記録される状態は「頂点がn(A)角形の内部にある」「頂点がn(A)角形上にある」「頂点がn(A)角形の外部にある」です。
	 * 
	 * @param vertex
	 *            n(B)角形の頂点を保持するMyPointクラスリスト
	 * @return 頂点の状態を保持するint型配列
	 */
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:内側, 1:頂点に一致or辺に接する, 2:外側

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint p = vertex.get(i);

			if (matchVertex(p) || isOnSide(p)) {
				vertexState[1]++;
			} else {
				if (includePoint(p)) {
					vertexState[0]++;
				} else {
					vertexState[2]++;
				}
			}
		}
		return vertexState;
	}
	
	/**
	 * n角形に対して点が頂点と合致するか否かを判定します。<br>
	 * 
	 * @param p
	 *            対象となる点を表すMyPointクラス変数
	 * @return n角形に対して点が頂点と合致するか否かを示すboolean型変数
	 */
	private boolean matchVertex(MyPoint p) {
		for (int i = 0; i < this.vertex.size(); i++) {
			if (p == this.vertex.get(i)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * n角形に対して点が辺上に存在するか否かを判定します。<br>
	 * 
	 * @param p
	 *            対象となる点を表すMyPointクラス変数
	 * @return n角形に対して点が辺上に存在するか否かを示すboolean型変数
	 */
	private boolean isOnSide(MyPoint p) {
		for (int i = 0; i < this.side.size(); i++) {
			if ((p.calcDistToLine(this.side.get(i)) < 10) && p.withinLineRange(this.side.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * n角形に対して点が内部に含まれるか否かを判定します。<br>
	 * 
	 * @param p
	 *            対象となる点を表すMyPointクラス変数
	 * @return n角形に対して点が内部に含まれるか否かを示すboolean型変数
	 */
	public boolean includePoint(MyPoint p) {
		int crossNum = countCrossNum(this.vertex, p);
		int windNum = countWindNum(this.vertex, p);

		return (crossNum % 2 == 1) && (windNum != 0);
	}
	
	/**
	 * 交差数に基づいて、n角形に対して点が内部に含まれるか否かを判定します。<br>
	 * 交差数とは、任意の点から引いた半直線がn角形の辺と交差する回数を表します。
	 * 
	 * @param pointList
	 *            n角形の頂点を保持するMyPointクラスリスト
	 * @param p
	 *            対象となる点を表すMyPointクラス変数
	 * @return 交差数に基づいて、n角形に対して点が内部に含まれるか否かを示すboolean型変数
	 */
	private int countCrossNum(ArrayList<MyPoint> pointList, MyPoint p) {
		int crossNum = 0;

		for (int i = 0; i < pointList.size(); i++) {
			int index1 = i;
			int index2 = (i < pointList.size() - 1) ? i + 1 : 0;
			MyPoint p1 = pointList.get(index1);
			MyPoint p2 = pointList.get(index2);

			if ((p1.y <= p.y) && (p2.y > p.y) || (p1.y > p.y) && (p2.y <= p.y)) {
				double t = (p.y - p1.y) / (p2.y - p1.y);
				if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
					crossNum++;
				}
			}
		}

		return crossNum;
	}
	
	/**
	 * 回転数に基づいて、n角形に対して点が内部に含まれるか否かを判定します。<br>
	 * 回転数とは、n角形によって構成される閉路が任意の点の周りを周る回数を表します。
	 * 
	 * @param pointList
	 *            n角形の頂点を保持するMyPointクラスリスト
	 * @param p
	 *            対象となる点を表すMyPointクラス変数
	 * @return 回転数に基づいて、n角形に対して点が内部に含まれるか否かを示すboolean型変数
	 */
	private int countWindNum(ArrayList<MyPoint> pointList, MyPoint p) {
		int windNum = 0;

		for (int i = 0; i < pointList.size(); i++) {
			int index1 = i;
			int index2 = (i < pointList.size() - 1) ? i + 1 : 0;
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
	
	/**
	 * ある値が指定した範囲内の値であるか否かを判定します。<br>
	 * 
	 * @param value
	 *            対象となる値を表すint型変数
	 * @param min
	 *            指定する範囲の最小値を表すint型変数
	 * @param max
	 *            指定する範囲の最大値を表すint型変数
	 * @return ある値が指定した範囲内の値であるか否かを示すboolean型変数
	 */
	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}
	
	/**
	 * n角形の形状を分析し、特徴的なn角形の名称を取得します。<br>
	 * 特徴的なn角形は「正三角形」や「正方形」などを指します。
	 * 
	 * @return 特徴的なn角形の名称を表すString型変数
	 */
	public String getPolygonName() {
		switch (this.vertex.size()) {
		case 3:
			return getTriangleName();
		case 4:
			return getQuadrangleName();
		default:
			return getGonName();
		}
	}
	
	/**
	 * 三角形の形状を分析し、特徴的な三角形の名称を取得します。<br>
	 * 特徴的な三角形は「正三角形」「二等辺三角形」「直角三角形」を指します。
	 * 
	 * @return 特徴的な三角形の名称を表すString型変数
	 */
	public String getTriangleName() {
		boolean[][] condition = new boolean[2][3];

		condition[0][0] = isSameLength(side.get(0), side.get(1));
		condition[0][1] = isSameLength(side.get(1), side.get(2));
		condition[0][2] = isSameLength(side.get(2), side.get(0));

		condition[1][0] = isPerpendicular(side.get(0), side.get(1));
		condition[1][1] = isPerpendicular(side.get(1), side.get(2));
		condition[1][2] = isPerpendicular(side.get(2), side.get(0));

		if (isSameLength(side.get(0), side.get(1), side.get(2))) {
			return "正三角形";
		} else {
			if (condition[0][0] || condition[0][1] || condition[0][2]) {
				return "二等辺三角形";
			} else if (condition[1][0] || condition[1][1] || condition[1][2]) {
				return "直角三角形";
			}
		}

		return "三角形";
	}
	
	/**
	 * 四角形の形状を分析し、特徴的な四角形の名称を取得します。<br>
	 * 特徴的な四角形は「正方形」「長方形」「菱形」「等脚台形」を指します。
	 * 
	 * @return 特徴的な四角形の名称を表すString型変数
	 */
	public String getQuadrangleName() {
		boolean[][] condition = new boolean[3][4];

		condition[0][0] = isSameLength(side.get(0), side.get(2));
		condition[0][1] = isSameLength(side.get(1), side.get(3));

		condition[1][0] = isPerpendicular(side.get(0), side.get(1));
		condition[1][1] = isPerpendicular(side.get(1), side.get(2));
		condition[1][2] = isPerpendicular(side.get(2), side.get(3));
		condition[1][3] = isPerpendicular(side.get(3), side.get(0));

		condition[2][0] = isParallel(side.get(0), side.get(2));
		condition[2][1] = isParallel(side.get(1), side.get(3));

		if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3))) {
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "正方形";
			} else if (condition[2][0] && condition[2][1]) {
				return "菱形";
			}
		} else {
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "長方形";
			} else if ((condition[0][0] && condition[2][1]) || (condition[0][1] && condition[2][0])) {
				return "等脚台形";
			}
		}

		return "四角形";
	}
	
	/**
	 * 五角形や六角形の形状を分析し、特徴的な五角形や六角形の名称を取得します。<br>
	 * 特徴的な五角形は「正五角形」、特徴的な六角形は「正六角形」を指します。
	 * 
	 * @return 特徴的な五角形や六角形の名称を表すString型変数
	 */
	public String getGonName() {
		if (this.vertex.size() == 5) {
			if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3), side.get(4))) {
				return "正五角形";
			}
			return "五角形";
		} else {
			if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3), side.get(4), side.get(5))) {
				return "正六角形";
			}
			return "六角形";
		}
	}
	
	/**
	 * n角形が特徴的であるか否かを判定します。 <br>
	 * 特徴的であるとは、n角形が「正三角形」や「正方形」などを条件を満たすことを指します。
	 * 
	 * @return n角形が特徴的であるか否かを示すboolean型変数
	 */
	public boolean hasFeature() {
		String polygonName = getPolygonName();// n角形の形状を分析し、特徴的なn角形の名称を取得します。
		String[] polygon = { "三角形", "四角形", "五角形", "六角形" };

		return !(polygonName.equals(polygon[vertex.size() - 3]));
	}
	
	/**
	 * 複数の線分の長さが誤差を含めて等しいか否かを判定します。<br>
	 * 
	 * @param l
	 *            複数の線分を表すMyLineクラス変数(比較する全ての線分を順に引数とします。)
	 * @return 複数の線分の長さが誤差を含めて等しいか否かを示すboolean型変数
	 */
	private boolean isSameLength(MyLine... l) {
		double[] lengthRatio = new double[l.length];

		double totalLength = 0;
		for (int i = 0; i < l.length; i++) {
			totalLength += l[i].getLength();
		}
		double avgLength = totalLength / l.length;

		for (int i = 0; i < l.length; i++) {
			lengthRatio[i] = l[i].getLength() / avgLength;
		}

		int count = 0;
		double threshold = l.length * (2.5e-2) - (4e-2);
		for (int i = 0; i < lengthRatio.length; i++) {
			if (Math.abs(1 - lengthRatio[i]) < threshold) {
				count++;
			}
		}

		return (count == l.length);
	}
	
	/**
	 * 2本の線分が誤差を含めて平行であるか否かを判定します。<br>
	 * 
	 * @param l1
	 *            1本目の線分を表すMyLineクラス変数
	 * @param l2
	 *            2本目の線分を表すMyLineクラス変数
	 * @return 2本の線分が誤差を含めて平行であるか否かを示すboolean型変数
	 */
	private boolean isParallel(MyLine l1, MyLine l2) {
		if (!l1.intersectLine(l2)) {
			double crossProduct = l1.na * l2.nb - l2.na * l1.nb;

			return Math.abs(crossProduct) < 5e-5;
		}
		return false;
	}
	
	/**
	 * 2本の線分が誤差を含めて垂直であるか否かを判定します。<br>
	 * 
	 * @param l1
	 *            1本目の線分を表すMyLineクラス変数
	 * @param l2
	 *            2本目の線分を表すMyLineクラス変数
	 * @return 2本の線分が誤差を含めて垂直であるか否かを示すboolean型変数
	 */
	private boolean isPerpendicular(MyLine l1, MyLine l2) {
		if (l1.intersectLine(l2)) {
			double dotProduct = l1.na * l2.na + l1.nb * l2.nb;

			return Math.abs(dotProduct) < 5e-6;
		}
		return false;
	}
}
