package wasan.problems;

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��􉽗v�f�́u�_�v�Ɋւ���N���X�ł��B<br>
 * 
 * @author Takuma Tsuchihashi
 */
public class MyPoint {

	// �ȉ��A�_�̍��W�l��\��int�^�ϐ��ł��B
	/**
	 * �_��x���W��\���܂��B<br>
	 */
	public double x;
	/**
	 * �_��y���W��\���܂��B<br>
	 */
	public double y;

	// �ȉ��A�_�Ɗ֌W��������􉽗v�f(�_�E�����E�~)��ێ����郊�X�g�ł��B
	/**
	 * �_�Ɛ�������ėׂ荇��(�_�Ɗ֌W��������)�_��ێ����܂��B<br>
	 */
	public ArrayList<MyPoint> relatedPoint = new ArrayList<MyPoint>();
	/**
	 * �_��ʉ߂���(�_�Ɗ֌W��������)������ێ����܂��B<br>
	 */
	public ArrayList<MyLine> relatedLine = new ArrayList<MyLine>();
	/**
	 * �_��ʉ߂���(�_�Ɗ֌W��������)�~��ێ����܂��B<br>
	 */
	public ArrayList<MyCircle> relatedCircle = new ArrayList<MyCircle>();
	
	/**
	 * ���W�l���w�肵�A�_�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param _x
	 *            �_��x���W��\��double�^�ϐ�
	 * @param _y
	 *            �_��y���W��\��double�^�ϐ�
	 */
	public MyPoint(double _x, double _y) {
		this.x = _x;
		this.y = _y;
	}
	
	/**
	 * 2�{�̐����܂��͒������m�̌�_���擾���܂��B<br>
	 * �������m�̌�_�A�������m�̌�_�̂ǂ�����擾���邩��؂�ւ��邱�Ƃ��ł��܂��B
	 * 
	 * @param l1
	 *            1�{�ڂ̐����܂��͒�����\��MyLine�N���X�ϐ�
	 * @param l2
	 *            2�{�ڂ̐����܂��͒�����\��MyLine�N���X�ϐ�
	 * @param type
	 *            type=1:�������m�̌�_ / type=2:�������m�̌�_
	 * @return 2�{�̐����܂��͒������m�̌�_��\��MyPoint�N���X�ϐ�
	 */
	public static MyPoint getIntersection1(MyLine l1, MyLine l2, int type) {// �O�ς��画�肵�Đ������m������邩�ǂ���
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
	
	/**
	 * �����Ɖ~�̌�_���擾���܂��B<br>
	 * �����Ɖ~���甭������2�̌�_�̂����A1��I�����邱�Ƃ��ł��܂��B
	 * 
	 * @param l
	 *            ������\��MyLine�N���X�ϐ�
	 * @param c
	 *            �~��\��MyCircle�N���X�ϐ�
	 * @param type
	 *            type=1:�����Ɖ~��1�ڂ̌�_ / type=2:�����Ɖ~��2�ڂ̌�_
	 * @return �����Ɖ~�̌�_��\��MyPoint�N���X�ϐ�
	 */
	public static MyPoint getIntersection2(MyLine l, MyCircle c, int type) {
		double radius = c.radius;// �~�̔��a
		double dist = c.center.calcDistToLine(l);// �������l�����Ȃ��A�~�̒��S�Ɛ����̋���

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
	 * �_��������ɂ��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param l
	 *            �ΏۂƂȂ������\��MyLine�N���X�ϐ�
	 * @return �_��������ɂ��邩�ۂ�������boolean�^�ϐ�
	 */
	public boolean isOnLine(MyLine l) {
		if (l.start == this || l.end == this) {
			return true;
		}

		MyPoint p = this.getPerpendicularFoot(l);
		if (p.withinLineRange(l)) {// �_��������ɐ��������낵�A�����̑��ɊY������_���擾���܂��B
			double dist = this.calcDistToLine(l);

			return isEqual(dist, 0);
		}
		return false;
	}
	
	/**
	 * �_���~��ɂ��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �_���~��ɂ��邩�ۂ�������boolean�^�ϐ�
	 */
	public boolean isOnCircle(MyCircle c) {
		double radius = c.radius;//
		double dist = this.calcDistToPoint(c.center);

		return isEqual(radius, dist);
	}
	
	/**
	 * �_���璼���ɐ��������낵�A�����̑��ɊY������_���擾���܂��B<br>
	 * 
	 * @param l
	 *            �ΏۂƂȂ钼����\��MyLine�N���X�ϐ�
	 * @return �����̑��ƂȂ�_��\��MyPoint�N���X�ϐ�
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
	 * �_��������2�̒[�_�̊Ԃɑ��݂��邩�ۂ��𔻒肵�܂��B<br>
	 * 2�̒[�_���ꂼ����n�_�A�Ώۂ̓_���I�_�Ƃ���2�̃x�N�g���̊O�ς��画�肳��܂��B
	 * 
	 * @param l
	 *            �ΏۂƂȂ������\��MyLine�N���X�ϐ�
	 * @return �_��������2�̒[�_�̊Ԃɑ��݂��邩�ۂ�������boolean�^�ϐ�
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
	 * 2�_�Ԃ̋������擾���܂��B<br>
	 * 
	 * @param p
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return 2�_�Ԃ̋�����\��double�^�ϐ�
	 */
	public double calcDistToPoint(MyPoint p) {
		return Math.hypot((this.x - p.x), (this.y - p.y));
	}
	
	/**
	 * �_�ƒ����̋������擾���܂��B<br>
	 * 
	 * @param l
	 *            �ΏۂƂȂ钼����\��MyLine�N���X�ϐ�
	 * @return �_�ƒ����̋�����\��double�^�ϐ�
	 */
	public double calcDistToLine(MyLine l) {
		double numerator = Math.abs(l.a * this.x + l.b * this.y + l.c);
		double denominator = Math.hypot(l.a, l.b);

		return numerator / denominator;
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
}
