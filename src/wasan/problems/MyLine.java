package wasan.problems;

/**
 * �����E�����Ɋւ���N���X
 */

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��􉽗v�f�́u�����v�Ɋւ���N���X�ł��B<br>
 * ���̐����͒����Ƃ��Ĉ����ꍇ������܂��B
 * 
 * @author Takuma Tsuchihashi
 */
public class MyLine {

	// �ȉ��A�����̒[�_��\��MyPoint�N���X�ϐ��ł��B
	/**
	 * �����̎n�_��\���܂��B<br>
	 */
	public MyPoint start;
	/**
	 * �����̏I�_��\���܂��B<br>
	 */
	public MyPoint end;

	// �ȉ��A�����܂��͒�����Hough�ϊ��̒�`�ŕ\�������ۂ̃p�����[�^��\��double�^�ϐ��ł��B
	/**
	 * �����܂��͒����ɑ΂��āAxy���ʂ̌��_����������@���̒����ς�\���܂��B<br>
	 */
	public double rho;
	/**
	 * �����܂��͒����ɑ΂��āAxy���ʂ̌��_����������@����x�����Ȃ��p�Ƃ�\���܂��B<br>
	 */
	public double theta;

	// �ȉ��A�����܂��͒�����ax+by+c=0�̕������ŕ\�������ۂ̃p�����[�^��\��double�^�ϐ��ł��B
	/**
	 * �����܂��͒���������������ax+by+c=0�̌W��a��\���܂��B<br>
	 */
	public double a;
	/**
	 * �����܂��͒���������������ax+by+c=0�̌W��b��\���܂��B<br>
	 */
	public double b;
	/**
	 * �����܂��͒���������������ax+by+c=0�̒萔c��\���܂��B<br>
	 */
	public double c;

	// �ȉ��A�����܂��͒���������������ax+by+c=0�̃p�����[�^�𐳋K�������l��\��double�^�ϐ��ł��B
	/**
	 * �����܂��͒���������������ax+by+c=0�̌W��a�𐳋K�������l�ł��B<br>
	 */
	public double na;
	/**
	 * �����܂��͒���������������ax+by+c=0�̌W��b�𐳋K�������l�ł��B<br>
	 */
	public double nb;
	/**
	 * �����܂��͒���������������ax+by+c=0�̒萔c�𐳋K�������l�ł��B<br>
	 */
	public double nc;

	// �ȉ��A�����܂��͒����Ɗ֌W��������􉽗v�f(�_�E����)��ێ����郊�X�g�ł��B
	/**
	 * �����܂��͒�����ʉ߂���(�����Ɗ֌W��������)�_��ێ����܂��B<br>
	 */
	public ArrayList<MyPoint> relatedPoint = new ArrayList<MyPoint>();
	
	/**
	 * Hough�ϊ��ɂ�����p�����[�^���w�肵�A�����܂��͒����̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param _theta
	 *            �����܂��͒����ɑ΂��āAxy���ʂ̌��_����������@���̒����ς�\��double�^�ϐ�
	 * @param _rho
	 *            �����܂��͒����ɑ΂��āAxy���ʂ̌��_����������@����x�����Ȃ��p�Ƃ�\��double�^�ϐ�
	 */
	MyLine(double _theta, double _rho) {
		theta = _theta;
		rho = _rho;
	}
	
	/**
	 * �[�_���w�肵�A�����̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param _start
	 *            �����̎n�_��\��MyPoint�N���X�ϐ�
	 * @param _end
	 *            �����̏I�_��\��MyPoint�N���X�ϐ�
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
	 * �������~�Ɛڂ��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �������~�Ɛڂ��邩�ۂ�������boolean�^�ϐ�
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
	 * �������~�ƌ����A��_��1���݂��邩�ۂ��𔻒肵�܂��B<br>
	 * intersectCircle2�Ƃ̈Ⴂ�͑��݂𔻒肷���_�̌��ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �������~�ƌ����A��_��1���݂��邩�ۂ�������boolean�^�ϐ�
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
	 * �������~�ƌ����A��_��2���݂��邩�ۂ��𔻒肵�܂��B<br>
	 * intersectCircle1�Ƃ̈Ⴂ�͑��݂𔻒肷���_�̌��ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �������~�ƌ����A��_��2���݂��邩�ۂ�������boolean�^�ϐ�
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
	 * �����Ɠ_�̋������擾���܂��B<br>
	 * 
	 * @param p1
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return �����Ɠ_�̋�����\��double�^�ϐ�
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
	 * �����̒������擾���܂��B<br>
	 * 
	 * @return �����̒�����\��double�^�ϐ�
	 */
	public double getLength() {
		return Math.hypot(Math.abs(start.x - end.x), Math.abs(start.y - end.y));
	}
	
	/**
	 * ���������̐����ƌ���邩�ۂ��𔻒肵�܂��B<br>
	 * 2�{�̐����̒[�_���N�_�Ƃ����x�N�g���̊O�ς��画�肳��܂��B
	 * 
	 * @param l
	 *            �ΏۂƂȂ������\��MyLine�N���X�ϐ�
	 * @return ���������̐����ƌ���邩�ۂ�������boolean�^�ϐ�
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
	 * 2�̒l���덷���܂߂ē��������ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param a
	 *            1�ڂ̒l��\��double�^�ϐ�
	 * @param b
	 *            2�ڂ̒l��\��double�^�ϐ�
	 * @return 2�̒l���덷���܂߂ē��������ۂ�������boolean�^�ϐ�
	 */
	private boolean isEqual(double a, double b) {
		return Math.abs(a - b) < 10;
	}
	
	/**
	 * 2�̒l�̂����A����̒l����������̒l���덷���܂߂đ傫�����ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param a
	 *            �傫�����̒l��\��double�^�ϐ�
	 * @param b
	 *            ���������̒l��\��double�^�ϐ�
	 * @return ����̒l����������̒l���덷���܂߂đ傫�����ۂ�������boolean�^�ϐ�
	 */
	private boolean isGreater(double a, double b) {
		return (a - b) >= 10;
	}
}
