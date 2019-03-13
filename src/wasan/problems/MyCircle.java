package wasan.problems;

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��~(�􉽗v�f�E�}�`�v�f)�Ɋւ���N���X�ł��B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyCircle {
	/** �~�̒��S��\���ϐ� */
	public MyPoint center;// ��

	/** �~�̔��a��\���ϐ� */
	public double radius;// ��

	/** �~�����8�_�̏������z�� */
	public MyPoint[] circum = new MyPoint[8];// ��

	/**
	 * �~�̕������ɂ�����3�̌W����\���ϐ� <br>
	 * �~�̕������� x^2+bx+y^2+cy+d=0 �ŕ\�����̂Ƃ���B
	 */
	public double b, c, d;// ��

	/** ��L��3�̌W���𐳋K�������ϐ� */
	public double nb, nc, nd;// ��

	/**
	 * �R���X�g���N�^
	 * 
	 * @param _center �~�̒��S
	 * @param _radius �~�̔��a
	 */
	public MyCircle(MyPoint _center, double _radius) {// ��
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
	 * �~���猩��n�p�`�Ƃ̊֌W�� <br>
	 * �@n�p�`���猩���~�̒��S�̓��O <br>
	 * �An�p�`���猩���~�����8�_�̏��(n�p�`�̓���, n�p�`��, n�p�`�̊O��) <br>
	 * �B�~���猩��n�p�`�̑S�Ă̕ӂ̏��(���������a����, �~�ɐڂ��Ă���, ���������a����) <br>
	 */

	/**
	 * 
	 * @param pg
	 * @return
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);// n�p�`���猩���~���8�_�̏��
		int[] sideState = this.classifySide(pg.side);// �~���猩��n�p�`�̕ӂ̏��(�~����n�p�`�̕ӂ̐ڏ��)

		condition[0] = pg.includePoint(this.center);// �@�����ɂ���
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length);// �A8�̓_��n�p�`�̓����܂���n�p�`��ɂ���(���̍��v��8)
		condition[2] = (sideState[1] == pg.side.size());// �Bn�{�̕ӂ��~�ɐڂ��Ă���(���̍��v��n)

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * �@�����ɂ��� <br>
	 * �A8�̓_��n�p�`�̓����܂���n�p�`��ɂ���(���̍��v��8) <br>
	 * �Bn�{�����̕ӂ��~�ɐڂ��Ă���, 1�{�ȏ�̕ӂ����������a���߁A�����ɂ���̂�0�{(���̍��v��n) <br>
	 * (�B�ɂ��Ă͑S�Ă̕ӂ��ᖡ���Ȃ���΂����Ȃ���?�A���v��n�ɂȂ�悤��)
	 * 
	 * @param pg
	 * @return
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);// n�p�`���猩���~���8�_�̏��
		int[] sideState = this.classifySide(pg.side);// �~���猩��n�p�`�̕ӂ̏��(�~����n�p�`�̕ӂ̐ڏ��)

		condition[0] = pg.includePoint(this.center);// n�p�`���~�̒��S���܂ނ�
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length); // 8�_�ɂ��āAn�p�`�̓���or�~��
		condition[2] = (sideState[0] == 0) && withinRange(sideState[1], 0, pg.side.size() - 1)
				&& withinRange(sideState[2], 1, pg.side.size());// ��
		// �~�̓����ɂ���̂�0�{�A�~�ɐڂ��Ă���̂�n�{�����A1�{�ȏオ�O���ɂ���

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * �@���O�ǂ���̉\�������� <br>
	 * �A1�ȏ�̓_��n�p�`�̓����ɂ���or�ڂ���, 8����(1�ȏ�)�̓_��n�p�`�̊O���ɂ���(���̍��v��8) <br>
	 * �B1�{�ȏ�̕ӂ����������a����, n�{�����̕ӂ��~�ɐڂ��Ă���܂��͋��������a����(���̍��v��n) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean overlapPolygon(MyPolygon pg) {
		boolean[][] condition = new boolean[3][2];

		int[] circumState = pg.classifyCircum(this.circum);// n�p�`���猩���~���8�_�̏��
		int[] sideState = this.classifySide(pg.side);// �~���猩��n�p�`�̕ӂ̏��(�~����n�p�`�̕ӂ̐ڏ��)

		condition[0][0] = true;// �ǂ���̉\��������̂�true�ɂ���
		condition[1][0] = withinRange((circumState[0] + circumState[1]), 1, this.circum.length - 1)
				&& withinRange(circumState[2], 1, this.circum.length - 1);

		condition[2][0] = withinRange(sideState[0], 1, pg.side.size() - 1)
				&& withinRange((sideState[1] + sideState[2]), 1, pg.side.size() - 1);
		condition[2][1] = (sideState[0] == pg.side.size()) && (this.classifyVertex(pg.vertex)[2] > 0);// ����ȏ󋵂̏ꍇ(n�p�`���~�ɓ��ڂ���Ƌ��)
		// (sideState[0] == pg.side.size())�̂݁A�딻����������߂ɕʂ̏�����t�^����

		return (condition[0][0] && condition[1][0] && (condition[2][0] || condition[2][1]));
	}

	/**
	 * 2�̉~���m�̊֌W�� <br>
	 * �@�~B���猩���~A�̒��S�̓��O + �~A���猩���~B�̒��S�̓��O <br>
	 * �A2�~�̒��S�Ԃ̋����ŊY��������� <br>
	 */

	/**
	 * �@�����ɂ��� + ���O�ǂ���̉\�������� <br>
	 * �A���S�Ԃ̋���=�~B�̔��a-�~A�̔��a(��Βl�͍l�����Ȃ�) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean inscribeCircle(MyCircle c) {// ca.inscribe(cb) ca��cb�̒�
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;// ��Βl���l�����Ȃ�

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isEqual(centerDist, radiusDiff));// Math.abs(centerDist - radiusDiff) < 10
		// c.radius - this.radius >= 0 �� false�Ȃ��return false;

		return (condition[0] && condition[1]);
	}

	/**
	 * �@�����ɂ��� + ���O�ǂ���̉\�������� <br>
	 * �A���S�Ԃ̋���<�~B�̔��a-�~A�̔��a(��Βl�͍l�����Ȃ�) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;// ��Βl���l�����Ȃ�

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isGreater(radiusDiff, centerDist));// �u�d�Ȃ荇���v�Ɣ��Ȃ����߂̏���
		// condition[1] = (radiusDiff >= 0) && (Math.abs(centerDist - radiusDiff) >= 10)
		// && (centerDist < radiusDiff);// �u�d�Ȃ荇���v�Ɣ��Ȃ����߂̏���

		return (condition[0] && condition[1]);
	}

	/**
	 * �@�O���ɂ��� + �O���ɂ��� <br>
	 * �A���S�Ԃ̋���=�~A�̔��a+�~B�̔��a <br>
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
	 * �@���O�ǂ���̉\�������� + ���O�ǂ���̉\�������� <br>
	 * �A�~B�̔��a-�~A�̔��a<���S�Ԃ̋���<�~A�̔��a+�~B�̔��a(��Βl�͍l�����Ȃ�) <br>
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
	 * �~���猩��n�p�`�S�Ă̒��_�̓��O
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:�~�̓���, 1:�~��, 2:�~�̊O��

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
	 * �~���猩��n�p�`�S�Ă̕ӂ̓��O
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifySide(ArrayList<MyLine> side) {
		int[] sideState = new int[3];// 0:�~�̓���, 1:�~�ɐڂ���, 2:�~�̊O��

		for (int i = 0; i < side.size(); i++) {// a�ӂ����܂��Ă��邩�ǂ�����(����)�l�����Ȃ����̂Ƃ���
			MyLine l = side.get(i);// a�����ł͂Ȃ������ł��邱�Ƃɒ���
			double dist = this.calcDistToLine(l); // ������ύX����?

			if (isEqual(this.radius, dist)) {// Math.abs(this.radius - dist) < 10
				sideState[1]++;// a�ڂ��Ă���̎������l����΂�����
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
	 * �~���猩���_�̓��O
	 * 
	 * @param p
	 * @return
	 */
	public boolean includePoint(MyPoint p) {
		double dist = this.center.calcDistToPoint(p);

		return isGreater(this.radius, dist);
	}

	/**
	 * �~���Ɠ_�Ƃ̋���
	 * 
	 * @param p
	 * @return
	 */
	double calcDistToPoint(MyPoint p) {
		return Math.abs(Math.hypot((p.x - center.x), (p.y - center.y)) - radius);
	}

	/**
	 * �~�̒��S�ƒ����̋���
	 * 
	 * @param l
	 * @return
	 */
	double calcDistToLine(MyLine l) {
		MyPoint p = this.center.getPerpendicularFoot(l);

		if (p.withinLineRange(l)) {// ���낵�������������ƌ����ꍇ
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

	private boolean isEqual(double a, double b) {// a��b��������
		return Math.abs(a - b) < 10;// ������threshold
	}

	private boolean isGreater(double a, double b) {// a��b���傫��
		return (a - b) >= 10;// ������threshold
	}
}

