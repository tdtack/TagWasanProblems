package wasan.problems;

/**
 * 直線・線分に関するクラス
 */

import java.util.ArrayList;

/**
 * 和算図形問題に含まれる線分(幾何要素)に関するクラスです。
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyLine {
	/** 線分の始点と終点を表す変数(Point型) */
	public MyPoint start, end;

	/** 直線をHough変換の手法でθとρを用いて表した際の変数(double型) */
	public double theta, rho;

	/**
	 * 直線の方程式における3つの係数を表す変数(double型) <br>
	 * 直線の方程式は ax+by+c=0 で表すものとする。
	 */
	public double a, b, c;

	/**
	 * 上記の3つの係数を正規化した変数(double型)
	 */
	public double na, nb, nc;

	// 使われてない? → 多角形分析の際に必要
	public ArrayList<MyPoint> relatedPoint = new ArrayList<MyPoint>();
	public ArrayList<MyLine> relatedLine = new ArrayList<MyLine>();

	MyLine(double _theta, double _rho) {
		theta = _theta;
		rho = _rho;
	}

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

	public boolean contactCircle(MyCircle c) {
		MyPoint p = c.center.getPerpendicularFoot(this);// 垂線の足

		if (p.withinLineRange(this)) {// a下ろした垂線が線分と交わる場合
			double dist = c.center.calcDistToLine(this);

			return (isEqual(c.radius, dist));
		}
		return false;
	}

	public boolean intersectCircle1(MyCircle c) {
		double dist = c.center.calcDistToLine(this);
		if (isGreater(c.radius, dist)) {
			MyPoint p1 = MyPoint.getIntersection2(this, c, 1);
			MyPoint p2 = MyPoint.getIntersection2(this, c, 2);

			return ((p1 != null && p2 == null) || (p1 == null && p2 != null));
		}
		return false;
	}

	public boolean intersectCircle2(MyCircle c) {
		double dist = c.center.calcDistToLine(this);
		if (isGreater(c.radius, dist)) {
			MyPoint p1 = MyPoint.getIntersection2(this, c, 1);
			MyPoint p2 = MyPoint.getIntersection2(this, c, 2);

			return (p1 != null && p2 != null);
		}
		return false;
	}

	double calcDistToPoint(MyPoint p1) {
		MyPoint p2 = p1.getPerpendicularFoot(this);

		if (p2.withinLineRange(this)) {
			double numerator = Math.abs(this.a * p1.x + this.b * p1.y + this.c);
			double denominator = Math.hypot(this.a, this.b);

			return numerator / denominator;
		}
		return Double.MAX_VALUE;
	}

	public double getLength() {
		return Math.hypot(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
	}

	// 外積から他の線分に交わるか
	public boolean intersectLine(MyLine l) {// バグ?
		double v1 = (this.end.x - this.start.x) * (l.start.y - this.start.y)
				- (this.end.y - this.start.y) * (l.start.x - this.start.x);
		double v2 = (this.end.x - this.start.x) * (l.end.y - this.start.y)
				- (this.end.y - this.start.y) * (l.end.x - this.start.x);

		double m1 = (l.end.x - l.start.x) * (this.start.y - l.start.y)
				- (l.end.y - l.start.y) * (this.start.x - l.start.x);
		double m2 = (l.end.x - l.start.x) * (this.end.y - l.start.y) - (l.end.y - l.start.y) * (this.end.x - l.start.x);

		return (v1 * v2 <= 0) && (m1 * m2 <= 0);
	}

	private boolean isEqual(double a, double b) {// aとbが等しい
		return Math.abs(a - b) < 10;// ☆☆☆threshold
	}

	private boolean isGreater(double a, double b) {// aがbより大きい
		return (a - b) >= 10;// ☆☆☆threshold
	}
}

