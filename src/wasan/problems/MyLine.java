package wasan.problems;

/**
 * �����E�����Ɋւ���N���X
 */

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂�����(�􉽗v�f)�Ɋւ���N���X�ł��B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyLine {
	/** �����̎n�_�ƏI�_��\���ϐ�(Point�^) */
	public MyPoint start, end;

	/** ������Hough�ϊ��̎�@�ŃƂƃς�p���ĕ\�����ۂ̕ϐ�(double�^) */
	public double theta, rho;

	/**
	 * �����̕������ɂ�����3�̌W����\���ϐ�(double�^) <br>
	 * �����̕������� ax+by+c=0 �ŕ\�����̂Ƃ���B
	 */
	public double a, b, c;

	/**
	 * ��L��3�̌W���𐳋K�������ϐ�(double�^)
	 */
	public double na, nb, nc;

	// �g���ĂȂ�? �� ���p�`���͂̍ۂɕK�v
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
		MyPoint p = c.center.getPerpendicularFoot(this);// �����̑�

		if (p.withinLineRange(this)) {// a���낵�������������ƌ����ꍇ
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

	// �O�ς��瑼�̐����Ɍ���邩
	public boolean intersectLine(MyLine l) {// �o�O?
		double v1 = (this.end.x - this.start.x) * (l.start.y - this.start.y)
				- (this.end.y - this.start.y) * (l.start.x - this.start.x);
		double v2 = (this.end.x - this.start.x) * (l.end.y - this.start.y)
				- (this.end.y - this.start.y) * (l.end.x - this.start.x);

		double m1 = (l.end.x - l.start.x) * (this.start.y - l.start.y)
				- (l.end.y - l.start.y) * (this.start.x - l.start.x);
		double m2 = (l.end.x - l.start.x) * (this.end.y - l.start.y) - (l.end.y - l.start.y) * (this.end.x - l.start.x);

		return (v1 * v2 <= 0) && (m1 * m2 <= 0);
	}

	private boolean isEqual(double a, double b) {// a��b��������
		return Math.abs(a - b) < 10;// ������threshold
	}

	private boolean isGreater(double a, double b) {// a��b���傫��
		return (a - b) >= 10;// ������threshold
	}
}

