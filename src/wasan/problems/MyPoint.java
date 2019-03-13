package wasan.problems;

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��_(�􉽗v�f)�Ɋւ���N���X�ł��B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyPoint {// implements AutoCloseable
	public double x, y;// �_�̍��W�l

	public ArrayList<MyPoint> relatedPoint = new ArrayList<MyPoint>();
	public ArrayList<MyLine> relatedLine = new ArrayList<MyLine>();
	public ArrayList<MyCircle> relatedCircle = new ArrayList<MyCircle>();

	public MyPoint(double _x, double _y) {
		this.x = _x;
		this.y = _y;
	}

	public static MyPoint getIntersection1(MyLine l1, MyLine l2, int type) {// a�������m�̌�_�̎擾
		// a����1.�O�ς��画�肵�Đ������m������邩�ǂ���

		double x = (l1.b * l2.c - l2.b * l1.c) / (l1.a * l2.b - l2.a * l1.b);
		double y = (l1.c * l2.a - l2.c * l1.a) / (l1.a * l2.b - l2.a * l1.b);

		switch (type) {
		case 1:// �������m�ōl����ꍇ
			return l1.intersectLine(l2) ? new MyPoint(x, y) : null;
		case 2:// �������m�ōl����ꍇ
			return new MyPoint(x, y);
		}
		return null;
	}

	public static MyPoint getIntersection2(MyLine l, MyCircle c, int type) {// �����Ɖ~�̌�_�̎擾
		// aLine�������ł��邽�߂ɁA�ԈႦ�Čv�Z���Ă���\��������
		// a����1.
		double radius = c.radius;// �~�̔��a
		double dist = c.center.calcDistToLine(l);// a�������l�����Ȃ��A�~�̒��S�Ɛ����̋���

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

			MyPoint p = new MyPoint(x, y);// p : ���ŋ��߂���_

			if (p.withinLineRange(l)) {// p : ���ŋ��߂���_
				return p;
			}
			return null;
		}
		return null;
	}

	public boolean isOnLine(MyLine l) {
		if (l.start == this || l.end == this) {// a����͒[�_�̍l�����K�v
			return true;// ���������[�_�ł���Ȃ�΁A�����ɂ��߂��͂�������true
		}

		MyPoint p = this.getPerpendicularFoot(l);
		if (p.withinLineRange(l)) {
			double dist = this.calcDistToLine(l);

			return isEqual(dist, 0);// dist < 10
		}
		return false;
	}

	public boolean isOnCircle(MyCircle c) {
		double radius = c.radius;//
		double dist = this.calcDistToPoint(c.center);

		return isEqual(radius, dist);// Math.abs(radius - dist) < 10
	}

	public MyPoint getPerpendicularFoot(MyLine l) {// a�����̑�
		double a = l.a;
		double b = l.b;
		double c1 = l.c;
		double c2 = (a * this.y - b * this.x);

		double x = (-(a * c1 + b * c2)) / (a * a + b * b);
		double y = (-(b * c1 - a * c2)) / (a * a + b * b);

		return new MyPoint(x, y);
	}

	public boolean withinLineRange(MyLine l) {// �ύX���˂�(�x�N�g���I�ȍl����?)

		if (isEqual(this.calcDistToPoint(l.start), 0) || isEqual(this.calcDistToPoint(l.end), 0)) {
			// this.calcDistToPoint(l.start) < 10 || (this.calcDistToPoint(l.end) < 10
			return true;
		}

		double v1_x = this.x - l.start.x;
		double v1_y = this.y - l.start.y;

		double v2_x = this.x - l.end.x;
		double v2_y = this.y - l.end.y;

		return ((v1_x * v2_x + v1_y * v2_y) < 0);
	}

	public double calcDistToPoint(MyPoint p) {
		return Math.hypot((this.x - p.x), (this.y - p.y));
	}

	public double calcDistToLine(MyLine l) {
		double numerator = Math.abs(l.a * this.x + l.b * this.y + l.c);
		double denominator = Math.hypot(l.a, l.b);

		return numerator / denominator;
	}

	private boolean isEqual(double a, double b) {// a��b��������
		return Math.abs(a - b) < 10;// ������threshold
	}
}

