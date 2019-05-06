package wasan.problems;

import java.util.ArrayList;

/**
 * 和算図形問題に含まれる幾何要素の「円」に関するクラスです。 <br>
 * この円は図形要素として扱う場合もあります。
 * 
 * @author Takuma Tsuchihashi
 */
public class MyCircle {

	// ☆以下、円を構成する要素を表す変数・配列です。
	/**
	 * 円の中心を表します。<br>
	 */
	public MyPoint center;
	/**
	 * 円の半径を表します。<br>
	 */
	public double radius;
	/**
	 * 円周上の8点を表します。<br>
	 */
	public MyPoint[] circum = new MyPoint[8];

	// ☆以下、円をx^2+bx+y^2+cy+d=0の方程式で表現した際のパラメータを表すdouble型変数です。
	/**
	 * 円を示す方程式x^2+bx+y^2+cy+d=0の係数bを表します。<br>
	 */
	public double b;
	/**
	 * 円を示す方程式x^2+bx+y^2+cy+d=0の係数cを表します。<br>
	 */
	public double c;
	/**
	 * 円を示す方程式x^2+bx+y^2+cy+d=0の定数dを表します。<br>
	 */
	public double d;

	// ☆以下、円を示す方程式x^2+bx+y^2+cy+d=0のパラメータを正規化した値を表すdouble型変数です。
	/**
	 * 円を示す方程式x^2+bx+y^2+cy+d=0の係数bを正規化した値です。<br>
	 */
	public double nb;
	/**
	 * 円を示す方程式x^2+bx+y^2+cy+d=0の係数cを正規化した値です。<br>
	 */
	public double nc;
	/**
	 * 円を示す方程式x^2+bx+y^2+cy+d=0の定数dを正規化した値です。<br>
	 */
	public double nd;

	// ☆
	/**
	 * 円の中心と半径を指定し、円のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param _center
	 *            円の中心を表すMyPointクラス変数
	 * @param _radius
	 *            円の半径を表すdouble型変数
	 */
	public MyCircle(MyPoint _center, double _radius) {
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

	// ☆
	/**
	 * 「円がn角形に内接する」を満たすか否かを判定します。 <br>
	 * 「円がn角形に内接する」は円から見たn角形との関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「円がn角形に内接する」を満たすか否かを示すboolean型変数
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);
		int[] sideState = this.classifySide(pg.side);

		condition[0] = pg.includePoint(this.center);
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length);
		condition[2] = (sideState[1] == pg.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}

	// ☆
	/**
	 * 「円がn角形の内部に存在する」を満たすか否かを判定します。 <br>
	 * 「円がn角形の内部に存在する」は円から見たn角形との関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「円がn角形の内部に存在する」を満たすか否かを示すboolean型変数
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);
		int[] sideState = this.classifySide(pg.side);

		condition[0] = pg.includePoint(this.center);
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length);
		condition[2] = (sideState[0] == 0) && withinRange(sideState[1], 0, pg.side.size() - 1)
				&& withinRange(sideState[2], 1, pg.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}

	// ☆
	/**
	 * 「円とn角形が互いに重なり合う」を満たすか否かを判定します。 <br>
	 * 「円とn角形が互いに重なり合う」は円から見たn角形との関係性の1つです。
	 * 
	 * @param pg
	 *            対象となるn角形を表すMyPolygonクラス変数
	 * @return 「円とn角形が互いに重なり合う」を満たすか否かを示すboolean型変数
	 */
	public boolean overlapPolygon(MyPolygon pg) {
		boolean[][] condition = new boolean[3][2];

		int[] circumState = pg.classifyCircum(this.circum);
		int[] sideState = this.classifySide(pg.side);

		condition[0][0] = true;
		condition[1][0] = withinRange((circumState[0] + circumState[1]), 1, this.circum.length - 1)
				&& withinRange(circumState[2], 1, this.circum.length - 1);

		condition[2][0] = withinRange(sideState[0], 1, pg.side.size() - 1)
				&& withinRange((sideState[1] + sideState[2]), 1, pg.side.size() - 1);
		condition[2][1] = (sideState[0] == pg.side.size()) && (this.classifyVertex(pg.vertex)[2] > 0);

		return (condition[0][0] && condition[1][0] && (condition[2][0] || condition[2][1]));
	}

	// ☆
	/**
	 * 「円Aが円Bの内側で接する」を満たすか否かを判定します。 <br>
	 * 「円Aが円Bの内側で接する」は二つの円同士の関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「円Aが円Bの内側で接する」を満たすか否かを示すboolean型変数
	 */
	public boolean inscribeCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isEqual(centerDist, radiusDiff));

		return (condition[0] && condition[1]);
	}

	// ☆
	/**
	 * 「円Aが円Bの内部に存在する」を満たすか否かを判定します。 <br>
	 * 「円Aが円Bの内部に存在する」は二つの円同士の関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「円Aが円Bの内部に存在する」を満たすか否かを示すboolean型変数
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isGreater(radiusDiff, centerDist));

		return (condition[0] && condition[1]);
	}

	// ☆
	/**
	 * 「円Aと円Bが互いに外接する」を満たすか否かを判定します。 <br>
	 * 「円Aと円Bが互いに外接する」は二つの円同士の関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「円Aと円Bが互いに外接する」を満たすか否かを示すboolean型変数
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusSum = c.radius + this.radius;

		condition[0] = !c.includePoint(this.center) && !this.includePoint(c.center);
		condition[1] = isEqual(centerDist, radiusSum);

		return (condition[0] && condition[1]);
	}

	// ☆
	/**
	 * 「円Aと円Bが互いに重なり合う」を満たすか否かを判定します。 <br>
	 * 「円Aと円Bが互いに重なり合う」は二つの円同士の関係性の1つです。
	 * 
	 * @param c
	 *            対象となる円を表すMyCircleクラス変数
	 * @return 「円Aと円Bが互いに重なり合う」を満たすか否かを示すboolean型変数
	 */
	public boolean overlapCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = Math.abs(c.radius - this.radius);
		double radiusSum = c.radius + this.radius;

		condition[0] = true;
		condition[1] = (isGreater(centerDist, radiusDiff) && isGreater(radiusSum, centerDist));

		return (condition[0] && condition[1]);
	}

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

			if (isEqual(this.radius, dist)) {
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

		for (int i = 0; i < side.size(); i++) {
			MyLine l = side.get(i);
			double dist = this.calcDistToLine(l);

			if (isEqual(this.radius, dist)) {
				sideState[1]++;
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

		if (p.withinLineRange(l)) {
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

	/**
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}

	// ☆
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

	// ☆
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
