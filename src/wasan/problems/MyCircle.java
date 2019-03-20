package wasan.problems;

import java.util.ArrayList;

/**
 * 和算図形問題に含まれる円(幾何要素・図形要素)に関するクラスです。
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyCircle {
	/** 円の中心を表す変数 */
	public MyPoint center;

	/** 円の半径を表す変数 */
	public double radius;

	/** 円周上の8点の情報を持つ配列 */
	public MyPoint[] circum = new MyPoint[8];// ☆

	/**
	 * 円の方程式における3つの係数を表す変数 <br>
	 * 円の方程式は x^2+bx+y^2+cy+d=0 で表すものとする。
	 */
	public double b, c, d;

	/** 上記の3つの係数を正規化した変数 */
	public double nb, nc, nd;

	/**
	 * コンストラクタ
	 * 
	 * @param _center 円の中心
	 * @param _radius 円の半径
	 */
	public MyCircle(MyPoint _center, double _radius) {// ☆
		this.center = _center;
		this.radius = _radius;

		double twoPI = 360;
		double radian = twoPI / circum.length;
		for (int i = 0; i < circum.length; i++) {
			double x = this.center.x + this.radius * Math.cos(Math.toRadians(radian * i));
			double y = this.center.y + this.radius * Math.sin(Math.toRadians(radian * i));
			circum[i] = new MyPoint(x, y);
		}

		this.b = -2 * this.center.x;
		this.c = -2 * this.center.y;
		this.d = -this.circum[0].x * (this.b + this.circum[0].x) - this.circum[0].y * (this.c + this.circum[0].y);

		double scalar = Math.sqrt(this.b * this.b + this.c * this.c + this.d * this.d);
		this.nb = this.b / scalar;
		this.nc = this.c / scalar;
		this.nd = this.d / scalar;
	}

	/**
	 * 円から見たn角形との関係性 <br>
	 * �@n角形から見た円の中心の内外 <br>
	 * �An角形から見た円周上の8点の状態(n角形の内側, n角形上, n角形の外側) <br>
	 * �B円から見たn角形の全ての辺の状態(距離が半径未満, 円に接している, 距離が半径超過) <br>
	 */

	/**
	 * 
	 * @param pg
	 * @return
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);// n角形から見た円上の8点の状態
		int[] sideState = this.classifySide(pg.side);// 円から見たn角形の辺の状態(円周とn角形の辺の接状態)

		condition[0] = pg.includePoint(this.center);// �@内側にある
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length);// �A8個の点がn角形の内側またはn角形上にある(その合計が8)
		condition[2] = (sideState[1] == pg.side.size());// �Bn本の辺が円に接している(その合計がn)

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * �@内側にある <br>
	 * �A8個の点がn角形の内側またはn角形上にある(その合計が8) <br>
	 * �Bn本未満の辺が円に接している, 1本以上の辺が距離が半径超過、内側にあるのは0本(その合計がn) <br>
	 * (�Bについては全ての辺を吟味しなければいけないか?、合計がnになるように)
	 * 
	 * @param pg
	 * @return
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);// n角形から見た円上の8点の状態
		int[] sideState = this.classifySide(pg.side);// 円から見たn角形の辺の状態(円周とn角形の辺の接状態)

		condition[0] = pg.includePoint(this.center);// n角形が円の中心を含むか
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length); // 8点について、n角形の内側or円上
		condition[2] = (sideState[0] == 0) && withinRange(sideState[1], 0, pg.side.size() - 1)
				&& withinRange(sideState[2], 1, pg.side.size());// ☆
		// 円の内側にあるのは0本、円に接しているのはn本未満、1本以上が外側にある

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * �@内外どちらの可能性もある <br>
	 * �A1個以上の点がn角形の内側にあるor接する, 8個未満(1個以上)の点がn角形の外側にある(その合計が8) <br>
	 * �B1本以上の辺が距離が半径未満, n本未満の辺が円に接しているまたは距離が半径超過(その合計がn) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean overlapPolygon(MyPolygon pg) {
		boolean[][] condition = new boolean[3][2];

		int[] circumState = pg.classifyCircum(this.circum);// n角形から見た円上の8点の状態
		int[] sideState = this.classifySide(pg.side);// 円から見たn角形の辺の状態(円周とn角形の辺の接状態)

		condition[0][0] = true;// どちらの可能性もあるのでtrueにする
		condition[1][0] = withinRange((circumState[0] + circumState[1]), 1, this.circum.length - 1)
				&& withinRange(circumState[2], 1, this.circum.length - 1);

		condition[2][0] = withinRange(sideState[0], 1, pg.side.size() - 1)
				&& withinRange((sideState[1] + sideState[2]), 1, pg.side.size() - 1);
		condition[2][1] = (sideState[0] == pg.side.size()) && (this.classifyVertex(pg.vertex)[2] > 0);// 特殊な状況の場合(n角形が円に内接すると区別)
		// (sideState[0] == pg.side.size())のみ、誤判定を除くために別の条件を付与する

		return (condition[0][0] && condition[1][0] && (condition[2][0] || condition[2][1]));
	}

	/**
	 * 2つの円同士の関係性 <br>
	 * �@円Bから見た円Aの中心の内外 + 円Aから見た円Bの中心の内外 <br>
	 * �A2円の中心間の距離で該当する条件 <br>
	 */

	/**
	 * �@内側にある + 内外どちらの可能性もある <br>
	 * �A中心間の距離=円Bの半径-円Aの半径(絶対値は考慮しない) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean inscribeCircle(MyCircle c) {// ca.inscribe(cb) caがcbの中
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;// 絶対値を考慮しない

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isEqual(centerDist, radiusDiff));// Math.abs(centerDist - radiusDiff) < 10
		// c.radius - this.radius >= 0 → falseならばreturn false;

		return (condition[0] && condition[1]);
	}

	/**
	 * �@内側にある + 内外どちらの可能性もある <br>
	 * �A中心間の距離<円Bの半径-円Aの半径(絶対値は考慮しない) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;// 絶対値を考慮しない

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isGreater(radiusDiff, centerDist));// 「重なり合う」と被らないための条件
		// condition[1] = (radiusDiff >= 0) && (Math.abs(centerDist - radiusDiff) >= 10)
		// && (centerDist < radiusDiff);// 「重なり合う」と被らないための条件

		return (condition[0] && condition[1]);
	}

	/**
	 * �@外側にある + 外側にある <br>
	 * �A中心間の距離=円Aの半径+円Bの半径 <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusSum = c.radius + this.radius;

		condition[0] = !c.includePoint(this.center) && !this.includePoint(c.center);
		condition[1] = isEqual(centerDist, radiusSum);// Math.abs(centerDist - radiusSum) < 10

		return (condition[0] && condition[1]);
	}

	/**
	 * �@内外どちらの可能性もある + 内外どちらの可能性もある <br>
	 * �A円Bの半径-円Aの半径<中心間の距離<円Aの半径+円Bの半径(絶対値は考慮しない) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean overlapCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = Math.abs(c.radius - this.radius);
		double radiusSum = c.radius + this.radius;

		condition[0] = true;
		condition[1] = (isGreater(centerDist, radiusDiff) && isGreater(radiusSum, centerDist));
		// (centerDist - radiusDiff) >= 10) && ((radiusSum - centerDist) >= 10

		return (condition[0] && condition[1]);
	}

	/////////////////////////////////////////////////////////////

	/**
	 * 円から見たn角形全ての頂点の内外
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:円の内側, 1:円上, 2:円の外側

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint p = vertex.get(i);
			double dist = this.center.calcDistToPoint(p);

			if (isEqual(this.radius, dist)) {// Math.abs(this.radius - dist) < 10
				vertexState[1]++;
			} else {
				if (this.radius > dist) {
					vertexState[0]++;
				} else {
					vertexState[2]++;
				}
			}
		}
		return vertexState;
	}

	/**
	 * 円から見たn角形全ての辺の内外
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifySide(ArrayList<MyLine> side) {
		int[] sideState = new int[3];// 0:円の内側, 1:円に接する, 2:円の外側

		for (int i = 0; i < side.size(); i++) {// a辺が収まっているかどうかは(今は)考慮しないものとする
			MyLine l = side.get(i);// a線分ではなく直線であることに注意
			double dist = this.calcDistToLine(l); // こいつを変更する?

			if (isEqual(this.radius, dist)) {// Math.abs(this.radius - dist) < 10
				sideState[1]++;// a接しているの時だけ考えればいいか
			} else {
				if (this.radius > dist) {
					sideState[0]++;
				} else {
					sideState[2]++;
				}
			}
		}

		return sideState;
	}

	/**
	 * 円から見た点の内外
	 * 
	 * @param p
	 * @return
	 */
	public boolean includePoint(MyPoint p) {
		double dist = this.center.calcDistToPoint(p);

		return isGreater(this.radius, dist);
	}

	/**
	 * 円周と点との距離
	 * 
	 * @param p
	 * @return
	 */
	double calcDistToPoint(MyPoint p) {
		return Math.abs(Math.hypot((p.x - center.x), (p.y - center.y)) - radius);
	}

	/**
	 * 円の中心と直線の距離
	 * 
	 * @param l
	 * @return
	 */
	double calcDistToLine(MyLine l) {
		MyPoint p = this.center.getPerpendicularFoot(l);

		if (p.withinLineRange(l)) {// 下ろした垂線が線分と交わる場合
			double numerator = Math.abs(l.a * this.center.x + l.b * this.center.y + l.c);
			double denominator = Math.hypot(l.a, l.b);

			return numerator / denominator;
		} else {
			if (isEqual(this.center.calcDistToPoint(l.start), 0)) {
				return 0;
			} else if (isEqual(this.center.calcDistToPoint(l.end), 0)) {
				return 0;
			}
		}
		return Double.MAX_VALUE;
	}

	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}

	private boolean isEqual(double a, double b) {// aとbが等しい
		return Math.abs(a - b) < 10;// ☆☆☆threshold
	}

	private boolean isGreater(double a, double b) {// aがbより大きい
		return (a - b) >= 10;// ☆☆☆threshold
	}
}

