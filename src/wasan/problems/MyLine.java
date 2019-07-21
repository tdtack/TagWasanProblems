package wasan.problems;

/**
 * 直線・線分に関するクラス
 */

import java.util.ArrayList;

/**
 * 和算図形問題に含まれる幾何要素の「線分」に関するクラスです。<br>
 * この線分は直線として扱う場合もあります。
 * 
 * @author Takuma Tsuchihashi
 */
public class MyLine {

	// 以下、線分の端点を表すMyPointクラス変数です。
	/**
	 * 線分の始点を表します。<br>
	 */
	public MyPoint start;
	/**
	 * 線分の終点を表します。<br>
	 */
	public MyPoint end;

	// 以下、線分または直線をHough変換の定義で表現した際のパラメータを表すdouble型変数です。
	/**
	 * 線分または直線に対して、xy平面の原点から引いた法線の長さρを表します。<br>
	 */
	public double rho;
	/**
	 * 線分または直線に対して、xy平面の原点から引いた法線とx軸がなす角θを表します。<br>
	 */
	public double theta;

	// 以下、線分または直線をax+by+c=0の方程式で表現した際のパラメータを表すdouble型変数です。
	/**
	 * 線分または直線を示す方程式ax+by+c=0の係数aを表します。<br>
	 */
	public double a;
	/**
	 * 線分または直線を示す方程式ax+by+c=0の係数bを表します。<br>
	 */
	public double b;
	/**
	 * 線分または直線を示す方程式ax+by+c=0の定数cを表します。<br>
	 */
	public double c;

	// 以下、線分または直線を示す方程式ax+by+c=0のパラメータを正規化した値を表すdouble型変数です。
	/**
	 * 線分または直線を示す方程式ax+by+c=0の係数aを正規化した値です。<br>
	 */
	public double na;
	/**
	 * 線分または直線を示す方程式ax+by+c=0の係数bを正規化した値です。<br>
	 */
	public double nb;
	/**
	 * 線分または直線を示す方程式ax+by+c=0の定数cを正規化した値です。<br>
	 */
	public double nc;

	// 以下、線分または直線と関係性がある幾何要素(点・線分)を保持するリストです。
	/**
	 * 線分または直線を通過する(線分と関係性がある)点を保持します。<br>
	 */
	public ArrayList<MyPoint> relatedPoint = new ArrayList<MyPoint>();
	
	/**
	 * Hough変換におけるパラメータを指定し、線分または直線のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param _theta
	 *            線分または直線に対して、xy平面の原点から引いた法線の長さρを表すdouble型変数
	 * @param _rho
	 *            線分または直線に対して、xy平面の原点から引いた法線とx軸がなす角θを表すdouble型変数
	 */
	MyLine(double _theta, double _rho) {
		theta = _theta;
		rho = _rho;
	}
	
	/**
	 * 端点を指定し、線分のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param _start
	 *            線分の始点を表すMyPointクラス変数
	 * @param _end
	 *            線分の終点を表すMyPointクラス変数
	 */
	MyLine(MyPoint _start, MyPoint _end) {
		this.start = _start;
		this.end = _end;

		this.a = this.start.y - this.end.y;
		this.b = this.end.x - this.start.x;
		this.c = this.start.x * this.end.y - this.end.x * this.start.y;

		double scalar = Math.sqrt(this.a * this.a + this.b * this.b + this.c * this.c);
		this.na = this.a / scalar;
		this.nb = this.b / scalar;
		this.nc = this.c / scalar;
	}
	
	/**
	 * 線分が円と接するか否かを判定します。<br>
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 線分が円と接するか否かを示すboolean型変数
	 */
	public boolean contactCircle(MyCircle c) {
		MyPoint p = c.center.getPerpendicularFoot(this);

		if (p.withinLineRange(this)) {
			double dist = c.center.calcDistToLine(this);

			return (isEqual(c.radius, dist));
		}
		return false;
	}
	
	/**
	 * 線分が円と交わり、交点が1つ存在するか否かを判定します。<br>
	 * intersectCircle2との違いは存在を判定する交点の個数です。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 線分が円と交わり、交点が1つ存在するか否かを示すboolean型変数
	 */
	public boolean intersectCircle1(MyCircle c) {
		double dist = c.center.calcDistToLine(this);
		if (isGreater(c.radius, dist)) {
			MyPoint p1 = MyPoint.getIntersection2(this, c, 1);
			MyPoint p2 = MyPoint.getIntersection2(this, c, 2);

			return ((p1 != null && p2 == null) || (p1 == null && p2 != null));
		}
		return false;
	}
	
	/**
	 * 線分が円と交わり、交点が2つ存在するか否かを判定します。<br>
	 * intersectCircle1との違いは存在を判定する交点の個数です。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 線分が円と交わり、交点が2つ存在するか否かを示すboolean型変数
	 */
	public boolean intersectCircle2(MyCircle c) {
		double dist = c.center.calcDistToLine(this);
		if (isGreater(c.radius, dist)) {
			MyPoint p1 = MyPoint.getIntersection2(this, c, 1);
			MyPoint p2 = MyPoint.getIntersection2(this, c, 2);

			return (p1 != null && p2 != null);
		}
		return false;
	}
	
	/**
	 * 線分と点の距離を取得します。<br>
	 * 
	 * @param p1
	 *            対象となる点を表すMyPointクラス変数
	 * @return 線分と点の距離を表すdouble型変数
	 */
	double calcDistToPoint(MyPoint p1) {
		MyPoint p2 = p1.getPerpendicularFoot(this);

		if (p2.withinLineRange(this)) {
			double numerator = Math.abs(this.a * p1.x + this.b * p1.y + this.c);
			double denominator = Math.hypot(this.a, this.b);

			return numerator / denominator;
		}
		return Double.MAX_VALUE;
	}
	
	/**
	 * 線分の長さを取得します。<br>
	 * 
	 * @return 線分の長さを表すdouble型変数
	 */
	public double getLength() {
		return Math.hypot(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
	}
	
	/**
	 * 線分が他の線分と交わるか否かを判定します。<br>
	 * 2本の線分の端点を起点としたベクトルの外積から判定されます。
	 * 
	 * @param l
	 *            対象となる線分を表すMyLineクラス変数
	 * @return 線分が他の線分と交わるか否かを示すboolean型変数
	 */
	public boolean intersectLine(MyLine l) {
		double v1 = (this.end.x - this.start.x) * (l.start.y - this.start.y)
				- (this.end.y - this.start.y) * (l.start.x - this.start.x);
		double v2 = (this.end.x - this.start.x) * (l.end.y - this.start.y)
				- (this.end.y - this.start.y) * (l.end.x - this.start.x);

		double m1 = (l.end.x - l.start.x) * (this.start.y - l.start.y)
				- (l.end.y - l.start.y) * (this.start.x - l.start.x);
		double m2 = (l.end.x - l.start.x) * (this.end.y - l.start.y) - (l.end.y - l.start.y) * (this.end.x - l.start.x);

		return (v1 * v2 <= 0) && (m1 * m2 <= 0);
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
	
	/**
	 * 2つの値のうち、一方の値がもう一方の値より誤差を含めて大きいか否かを判定します。<br>
	 * 
	 * @param a
	 *            大きい方の値を表すdouble型変数
	 * @param b
	 *            小さい方の値を表すdouble型変数
	 * @return 一方の値がもう一方の値より誤差を含めて大きいか否かを示すboolean型変数
	 */
	private boolean isGreater(double a, double b) {
		return (a - b) >= 10;
	}
}
