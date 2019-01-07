package wasan.problems;

import java.util.ArrayList;

/**
 * 和算図形問題に含まれるn角形(図形要素)に関するクラスである。
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyPolygon {
	/** n角形の頂点の情報を持つリスト */
	public ArrayList<MyPoint> vertex = new ArrayList<MyPoint>();

	/** n角形の辺の情報を持つリスト */
	public ArrayList<MyLine> side = new ArrayList<MyLine>();

	/** n角形の重心を表す変数 */
	public MyPoint centroid;

	/**
	 * コンストラクタ
	 * 
	 * @param _p
	 */
	public MyPolygon(MyPoint... _p) {// 辺情報も加えたい
		for (int i = 0; i < _p.length; i++) {
			this.vertex.add(_p[i]);
		}

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint start = vertex.get(i);
			MyPoint end = vertex.get((i < vertex.size() - 1) ? i + 1 : 0);

			side.add(new MyLine(start, end));// 仮想的な辺であることに注意
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
	 * n角形から見た円との関係性 <br>
	 * ①円から見たn角形の重心の内外 + n角形から見た円の中心の内外 <br>
	 * ②円から見たn角形の全ての点の状態(円の内側にある, 円上にある, 円の外側にある) <br>
	 * ③円から見たn角形の全ての辺の状態(距離が半径未満, 円に接している, 距離が半径超過) <br>
	 */

	/**
	 * ①内側にある + 内外どちらの可能性もある <br>
	 * ②n個の点が円上にある(その合計がn) <br>
	 * ③n本の辺が距離が半径未満(その合計がn) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean inscribeCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);// 円から見たn角形の頂点の状態
		int[] sideState = c.classifySide(this.side);// 円から見たn角形の辺の状態(円周とn角形の辺の接状態)

		condition[0] = c.includePoint(this.centroid);
		condition[1] = (vertexState[1] == this.vertex.size());
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * ①内側にある + 内外どちらの可能性もある <br>
	 * ②1個以上の点が円の内側にある, n個未満の点が円上にある <br>
	 * ③n本の辺が距離が半径未満(その合計がn) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);// 円から見たn角形の頂点の状態
		int[] sideState = c.classifySide(this.side);// 円から見たn角形の辺の状態(円周とn角形の辺の接状態)

		condition[0] = c.includePoint(this.centroid);
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * ①外側にある + 外側にある <br>
	 * ②1個以下の点が円上にある, n-1個以上の点が円の外部にある(その合計がn) <br>
	 * ③1本以下の辺が円に接している, n-1本以上の辺が距離が半径超過(その合計がn) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[][] condition = new boolean[3][2];

		int[] vertexState = c.classifyVertex(this.vertex);// 円から見たn角形の頂点の状態
		int[] sideState = c.classifySide(this.side);// 円から見たn角形の辺の状態(円周とn角形の辺の接状態)

		condition[0][0] = !c.includePoint(this.centroid) && !this.includePoint(c.center);

		condition[1][0] = (vertexState[1] == 0) && (vertexState[2] == this.vertex.size());// vertexState[1]+vertexState[2]=this.vertex.size()は必然
		condition[2][0] = (sideState[1] == 1) && (sideState[2] == this.side.size() - 1);// sideState[1]+sideState[2]=this.side.size()は必然

		condition[1][1] = (vertexState[1] == 1) && (vertexState[2] == this.vertex.size() - 1);// vertexState[1]+vertexState[2]=this.vertex.size()は必然
		condition[2][1] = (sideState[1] == 0) && (sideState[2] == this.side.size());// sideState[1]+sideState[2]=this.side.size()は必然

//		condition[1] = (vertexState[0] == 0) && withinRange(vertexState[1], 0, 1)
//				&& withinRange(vertexState[2], this.vertex.size() - 1, this.vertex.size());
//		condition[2] = (sideState[0] == 0) && withinRange(sideState[1], 0, 1)
//				&& withinRange(sideState[2], this.side.size() - 1, this.side.size());
		// これでやりたいところだが、離れた同士のn角形と円まで「隣接する」と判断してしまう

		return (condition[0][0] && ((condition[1][0] && condition[2][0]) || (condition[1][1] && condition[2][1])));// a排他的論理和に近い
	}

	/**
	 * 2つのn角形同士の関係性 <br>
	 * ①n角形Bから見たn角形Aの重心の内外 + n角形Aから見たn角形Bの重心の内外 <br>
	 * ②n角形Bから見たn角形Aの全ての点の状態 <br>
	 */

	/**
	 * ①内側にある + 内外どちらの可能性もある <br>
	 * ②n個の点がn角形Bの点に一致or辺上にある(その合計がn) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);

		condition[0] = pg.includePoint(this.centroid);// ☆
		condition[1] = (vertexState[1] == this.vertex.size());// ☆

		return (condition[0] && condition[1]);
	}

	/**
	 * ①内側にある + 内外どちらの可能性もある <br>
	 * ②n個未満の点がn角形Bの点に一致or辺上にある, 1個以上の点がn角形Bの内側にある <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);// Yから見た、Xの各頂点の接し具合

		condition[0] = pg.includePoint(this.centroid);// ☆
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);// ☆

		return (condition[0] && condition[1]);
	}

	/**
	 * ①外側にある + 外側にある <br>
	 * ②1個以上2個以下の点がn角形Bの点に一致or辺上にある, n-2個以上の点がn角形Bの外側にある(その合計がn) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean adjoinPolygon(MyPolygon pg) {//
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);// Yから見た、Xの各頂点の接し具合
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = !pg.includePoint(this.centroid) && !this.includePoint(pg.centroid);// ☆
		condition[1][0] = (vertexState1[0] == 0) && withinRange(vertexState1[1], 1, 2)
				&& withinRange(vertexState1[2], this.vertex.size() - 2, this.vertex.size() - 1);// ☆
		condition[1][1] = (vertexState2[0] == 0) && withinRange(vertexState2[1], 1, 2)
				&& withinRange(vertexState2[2], pg.vertex.size() - 2, pg.vertex.size() - 1);// ☆

		return (condition[0][0] && (condition[1][0] || condition[1][1]));
	}

	/**
	 * ①内外どちらの可能性もある + 内外どちらの可能性もある <br>
	 * ②1個以上の点がn角形Bの内側にある, n個未満の点がn角形Bの外側にある(その合計がn) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean overlapPolygon(MyPolygon pg) {// どうするか
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);// Yから見た、Xの各頂点の接し具合
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = true;
		condition[1][0] = withinRange((vertexState1[0] + vertexState1[1]), 1, this.vertex.size() - 1)
				&& withinRange(vertexState1[2], 1, this.vertex.size() - 1);// ☆
		condition[1][1] = withinRange((vertexState2[0] + vertexState2[1]), 1, pg.vertex.size() - 1)
				&& withinRange(vertexState2[2], 1, pg.vertex.size() - 1);// ☆
		// もし、vertexState1[2]=this.vertex.size() && vertexState2[2]=pg.vertex.size() →

//		condition2[0][0] = withinRange(sideState[0], 1, pg.side.size() - 1)
//				&& withinRange((sideState[1] + sideState[2]), 1, pg.side.size() - 1);// ☆
//		condition2[0][1] = (sideState[0] == pg.side.size()) && (this.classifyVertex(pg.vertex)[2] > 0);

		return (condition[0][0] && (condition[1][0] && condition[1][1]));
	}

	///////////////////////////////////////////////////////////// a関係性判定関数

	// n角形から見た円の分割点の内外(boolean型, 引数はPolygon, int(閾値))
	public int[] classifyCircum(MyPoint[] circum) {
		int[] circumState = new int[3];// 0:内側, 1:頂点に一致or辺に接する, 2:外側

		for (int i = 0; i < circum.length; i++) {
			MyPoint p = circum[i];

			if (matchVertex(p) || isOnSide(p)) {// match_side(p)
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

	// n角形Xから見たn角形Yの全ての頂点の(一致,辺に接する数)または内外
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:内側, 1:頂点に一致or辺に接する, 2:外側

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint p = vertex.get(i);// n角形Yの頂点

			// 1:頂点に一致or辺に接する
			// aもしthisの頂点いずれかに一致している、もしくは辺上にあると確認できたらbreakして[1]++
			// aこれが確認できなければ[0]と[2]の判断

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

	// 点がn角形のいずれかの点に合致するか
	private boolean matchVertex(MyPoint p) {
		for (int i = 0; i < this.vertex.size(); i++) {
			if (p == this.vertex.get(i)) {
				return true;
			}
		}
		return false;
	}

	// 点がn角形のいずれかの辺の上にあるか
	private boolean isOnSide(MyPoint p) {
		for (int i = 0; i < this.side.size(); i++) {
			if ((p.calcDistToLine(this.side.get(i)) < 10) && p.withinLineRange(this.side.get(i))) {// ☆☆☆threshold
				return true;
			}
		}
		return false;
	}

	// 点の内外
	public boolean includePoint(MyPoint p) {
		int crossNum = countCrossNum(this.vertex, p);
		int windNum = countWindNum(this.vertex, p);

		return (crossNum % 2 == 1) && (windNum != 0);
	}

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

	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}

	public String getPolygonName() {// Triangle > ...のようにreturnされる
		switch (this.vertex.size()) {
		case 3:
			return getTriangleName();
		case 4:
			return getQuadrangleName();
		default:
			return getGonName();
		}
	}

	public String getTriangleName() {// 辺情報はl[]に入っている → 点の情報で計算しないとダメ(端点ではなく、線分上にあるケース)
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
			if (condition[0][0] || condition[0][1] || condition[0][2]) {// a二等辺三角形
				return "二等辺三角形";
			} else if (condition[1][0] || condition[1][1] || condition[1][2]) {// a直角三角形
				return "直角三角形";//
			}
		}

		return "三角形";
	}

	public String getQuadrangleName() {// aこんなもんかな
		boolean[][] condition = new boolean[3][4];

		condition[0][0] = isSameLength(side.get(0), side.get(2));
		condition[0][1] = isSameLength(side.get(1), side.get(3));

		condition[1][0] = isPerpendicular(side.get(0), side.get(1));
		condition[1][1] = isPerpendicular(side.get(1), side.get(2));
		condition[1][2] = isPerpendicular(side.get(2), side.get(3));
		condition[1][3] = isPerpendicular(side.get(3), side.get(0));

		condition[2][0] = isParallel(side.get(0), side.get(2));
		condition[2][1] = isParallel(side.get(1), side.get(3));

		if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3))) {// a正方形、菱形
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "正方形";//
			} else if (condition[2][0] && condition[2][1]) {
				return "菱形";
			}
		} else {// a長方形、等脚台形
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "長方形";//
			} else if ((condition[0][0] && condition[2][1]) || (condition[0][1] && condition[2][0])) {
				return "等脚台形";
			}

//			if (condition[0][0] && condition[0][1]) {
//				if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
//					return "長方形";//
//				}
//			} else {
//				if ((condition[0][0] && condition[2][1]) || (condition[0][1] && condition[2][0])) {
//					return "等脚台形";
//				}
//			}
		}

		return "四角形";
	}

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

	public boolean hasFeature() {
		String polygonName = getPolygonName();
		String[] polygon = { "三角形", "四角形", "五角形", "六角形" };

		return !(polygonName.equals(polygon[vertex.size() - 3]));
	}

	private boolean isSameLength(MyLine... l) {//
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
		double threshold = l.length * (2.5e-2) - (4e-2);// 一応OK
		for (int i = 0; i < lengthRatio.length; i++) {
			if (Math.abs(1 - lengthRatio[i]) < threshold) {// 1を基準に判定する
				count++;
			}
		}

		return (count == l.length);
	}

	private boolean isParallel(MyLine l1, MyLine l2) {//
		if (!l1.intersectLine(l2)) {
			double crossProduct = l1.na * l2.nb - l2.na * l1.nb;

			return Math.abs(crossProduct) < 5e-5;// 1e-4
		}
		return false;
	}

	private boolean isPerpendicular(MyLine l1, MyLine l2) {//
		if (l1.intersectLine(l2)) {
			double dotProduct = l1.na * l2.na + l1.nb * l2.nb;

			return Math.abs(dotProduct) < 5e-6;// もしくはもっと大きいか
		}
		return false;
	}
}

