package wasan.problems;

import java.util.ArrayList;

/**
 * 和算図形問題に含まれる幾何要素の「点」に関するクラスです。<br>
 * 
 * @author Takuma Tsuchihashi
 */
public class MyPoint {

	// 以下、点の座標値を表すint型変数です。
	/**
	 * 点のx座標を表します。<br>
	 */
	public double x;
	/**
	 * 点のy座標を表します。<br>
	 */
	public double y;

	// 以下、点と関係性がある幾何要素(点・線分・円)を保持するリストです。
	/**
	 * 点と線分を介して隣り合う(点と関係性がある)点を保持します。<br>
	 */
	public ArrayList<MyPoint> relatedPoint = new ArrayList<MyPoint>();
	/**
	 * 点を通過する(点と関係性がある)線分を保持します。<br>
	 */
	public ArrayList<MyLine> relatedLine = new ArrayList<MyLine>();
	/**
	 * 点を通過する(点と関係性がある)円を保持します。<br>
	 */
	public ArrayList<MyCircle> relatedCircle = new ArrayList<MyCircle>();
	
	/**
	 * 座標値を指定し、点のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param _x
	 *            点のx座標を表すdouble型変数
	 * @param _y
	 *            点のy座標を表すdouble型変数
	 */
	public MyPoint(double _x, double _y) {
		this.x = _x;
		this.y = _y;
	}
	
	/**
	 * 2本の線分または直線同士の交点を取得します。<br>
	 * 線分同士の交点、直線同士の交点のどちらを取得するかを切り替えることができます。
	 * 
	 * @param l1
	 *            1本目の線分または直線を表すMyLineクラス変数
	 * @param l2
	 *            2本目の線分または直線を表すMyLineクラス変数
	 * @param type
	 *            type=1:線分同士の交点 / type=2:直線同士の交点
	 * @return 2本の線分または直線同士の交点を表すMyPointクラス変数
	 */
	public static MyPoint getIntersection1(MyLine l1, MyLine l2, int type) {// 外積から判定して線分同士が交わるかどうか
		double x = (l1.b * l2.c - l2.b * l1.c) / (l1.a * l2.b - l2.a * l1.b);
		double y = (l1.c * l2.a - l2.c * l1.a) / (l1.a * l2.b - l2.a * l1.b);

		switch (type) {
		case 1:// 線分同士で考える場合
			return l1.intersectLine(l2) ? new MyPoint(x, y) : null;
		case 2:// 直線同士で考える場合
			return new MyPoint(x, y);
		}

		return null;
	}
	
	/**
	 * 線分と円の交点を取得します。<br>
	 * 線分と円から発生する2つの交点のうち、1つを選択することができます。
	 * 
	 * @param l
	 *            線分を表すMyLineクラス変数
	 * @param c
	 *            円を表すMyCircleクラス変数
	 * @param type
	 *            type=1:線分と円の1つ目の交点 / type=2:線分と円の2つ目の交点
	 * @return 線分と円の交点を表すMyPointクラス変数
	 */
	public static MyPoint getIntersection2(MyLine l, MyCircle c, int type) {
		double radius = c.radius;// 円の半径
		double dist = c.center.calcDistToLine(l);// 交わりを考慮しない、円の中心と線分の距離

		double D = 4 * l.c * (-l.c + l.a * c.b + l.b * c.c) + (l.b * c.b - l.a * c.c) * (l.b * c.b - l.a * c.c)
				- 4 * (l.a * l.a + l.b * l.b) * c.d;

		double x = (-(2 * l.a * l.c + l.b * l.b * c.b - l.a * l.b * c.c)) / (2 * (l.a * l.a + l.b * l.b));
		double y = (-(2 * l.b * l.c + l.a * l.a * c.c - l.a * l.b * c.b)) / (2 * (l.a * l.a + l.b * l.b));

		if (D > 0 && (radius - dist) >= 10) {
			switch (type) {
			case 1:
				x = (-(2 * l.a * l.c + l.b * l.b * c.b - l.a * l.b * c.c) + l.b * Math.sqrt(D))
						/ (2 * (l.a * l.a + l.b * l.b));
				y = (-(2 * l.b * l.c + l.a * l.a * c.c - l.a * l.b * c.b) - l.a * Math.sqrt(D))
						/ (2 * (l.a * l.a + l.b * l.b));
				break;
			case 2:
				x = (-(2 * l.a * l.c + l.b * l.b * c.b - l.a * l.b * c.c) - l.b * Math.sqrt(D))
						/ (2 * (l.a * l.a + l.b * l.b));
				y = (-(2 * l.b * l.c + l.a * l.a * c.c - l.a * l.b * c.b) + l.a * Math.sqrt(D))
						/ (2 * (l.a * l.a + l.b * l.b));
				break;
			default:
				break;
			}

			MyPoint p = new MyPoint(x, y);

			if (p.withinLineRange(l)) {
				return p;
			}
			return null;
		}
		return null;
	}
	
	/**
	 * 点が線分上にあるか否かを判定します。<br>
	 * 
	 * @param l
	 *            対象となる線分を表すMyLineクラス変数
	 * @return 点が線分上にあるか否かを示すboolean型変数
	 */
	public boolean isOnLine(MyLine l) {
		if (l.start == this || l.end == this) {
			return true;
		}

		MyPoint p = this.getPerpendicularFoot(l);
		if (p.withinLineRange(l)) {// 点から線分に垂線を下ろし、垂線の足に該当する点を取得します。
			double dist = this.calcDistToLine(l);

			return isEqual(dist, 0);
		}
		return false;
	}
	
	/**
	 * 点が円上にあるか否かを判定します。<br>
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 点が円上にあるか否かを示すboolean型変数
	 */
	public boolean isOnCircle(MyCircle c) {
		double radius = c.radius;//
		double dist = this.calcDistToPoint(c.center);

		return isEqual(radius, dist);
	}
	
	/**
	 * 点から直線に垂線を下ろし、垂線の足に該当する点を取得します。<br>
	 * 
	 * @param l
	 *            対象となる直線を表すMyLineクラス変数
	 * @return 垂線の足となる点を表すMyPointクラス変数
	 */
	public MyPoint getPerpendicularFoot(MyLine l) {
		double a = l.a;
		double b = l.b;
		double c1 = l.c;
		double c2 = (a * this.y - b * this.x);

		double x = (-(a * c1 + b * c2)) / (a * a + b * b);
		double y = (-(b * c1 - a * c2)) / (a * a + b * b);

		return new MyPoint(x, y);
	}
	
	/**
	 * 点が線分の2つの端点の間に存在するか否かを判定します。<br>
	 * 2つの端点それぞれを始点、対象の点を終点とした2つのベクトルの外積から判定されます。
	 * 
	 * @param l
	 *            対象となる線分を表すMyLineクラス変数
	 * @return 点が線分の2つの端点の間に存在するか否かを示すboolean型変数
	 */
	public boolean withinLineRange(MyLine l) {

		if (isEqual(this.calcDistToPoint(l.start), 0) || isEqual(this.calcDistToPoint(l.end), 0)) {
			return true;
		}

		double v1_x = this.x - l.start.x;
		double v1_y = this.y - l.start.y;

		double v2_x = this.x - l.end.x;
		double v2_y = this.y - l.end.y;

		return ((v1_x * v2_x + v1_y * v2_y) < 0);
	}
	
	/**
	 * 2点間の距離を取得します。<br>
	 * 
	 * @param p
	 *            対象となる点を表すMyPointクラス変数
	 * @return 2点間の距離を表すdouble型変数
	 */
	public double calcDistToPoint(MyPoint p) {
		return Math.hypot((this.x - p.x), (this.y - p.y));
	}
	
	/**
	 * 点と直線の距離を取得します。<br>
	 * 
	 * @param l
	 *            対象となる直線を表すMyLineクラス変数
	 * @return 点と直線の距離を表すdouble型変数
	 */
	public double calcDistToLine(MyLine l) {
		double numerator = Math.abs(l.a * this.x + l.b * this.y + l.c);
		double denominator = Math.hypot(l.a, l.b);

		return numerator / denominator;
	}
	
	/**
	 * 2つの値が誤差を含めて等しいか否かを判定します。<br>
	 * 
	 * @param a
	 *            1つ目の値を表すdouble型変数
	 * @param b
	 *            2つ目の値を表すdouble型変数
	 * @return 2つの値が誤差を含めて等しいか否かを示すboolean型変数
	 */
	private boolean isEqual(double a, double b) {
		return Math.abs(a - b) < 10;
	}
}
