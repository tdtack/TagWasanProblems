package wasan.problems;

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��􉽗v�f�́u�~�v�Ɋւ���N���X�ł��B <br>
 * ���̉~�͐}�`�v�f�Ƃ��Ĉ����ꍇ������܂��B
 * 
 * @author Takuma Tsuchihashi
 */
public class MyCircle {

	// ���ȉ��A�~���\������v�f��\���ϐ��E�z��ł��B
	/**
	 * �~�̒��S��\���܂��B<br>
	 */
	public MyPoint center;
	/**
	 * �~�̔��a��\���܂��B<br>
	 */
	public double radius;
	/**
	 * �~�����8�_��\���܂��B<br>
	 */
	public MyPoint[] circum = new MyPoint[8];

	// ���ȉ��A�~��x^2+bx+y^2+cy+d=0�̕������ŕ\�������ۂ̃p�����[�^��\��double�^�ϐ��ł��B
	/**
	 * �~������������x^2+bx+y^2+cy+d=0�̌W��b��\���܂��B<br>
	 */
	public double b;
	/**
	 * �~������������x^2+bx+y^2+cy+d=0�̌W��c��\���܂��B<br>
	 */
	public double c;
	/**
	 * �~������������x^2+bx+y^2+cy+d=0�̒萔d��\���܂��B<br>
	 */
	public double d;

	// ���ȉ��A�~������������x^2+bx+y^2+cy+d=0�̃p�����[�^�𐳋K�������l��\��double�^�ϐ��ł��B
	/**
	 * �~������������x^2+bx+y^2+cy+d=0�̌W��b�𐳋K�������l�ł��B<br>
	 */
	public double nb;
	/**
	 * �~������������x^2+bx+y^2+cy+d=0�̌W��c�𐳋K�������l�ł��B<br>
	 */
	public double nc;
	/**
	 * �~������������x^2+bx+y^2+cy+d=0�̒萔d�𐳋K�������l�ł��B<br>
	 */
	public double nd;

	// ��
	/**
	 * �~�̒��S�Ɣ��a���w�肵�A�~�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param _center
	 *            �~�̒��S��\��MyPoint�N���X�ϐ�
	 * @param _radius
	 *            �~�̔��a��\��double�^�ϐ�
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

	// ��
	/**
	 * �u�~��n�p�`�ɓ��ڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~��n�p�`�ɓ��ڂ���v�͉~���猩��n�p�`�Ƃ̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �u�~��n�p�`�ɓ��ڂ���v�𖞂������ۂ�������boolean�^�ϐ�
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

	// ��
	/**
	 * �u�~��n�p�`�̓����ɑ��݂���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~��n�p�`�̓����ɑ��݂���v�͉~���猩��n�p�`�Ƃ̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �u�~��n�p�`�̓����ɑ��݂���v�𖞂������ۂ�������boolean�^�ϐ�
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

	// ��
	/**
	 * �u�~��n�p�`���݂��ɏd�Ȃ荇���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~��n�p�`���݂��ɏd�Ȃ荇���v�͉~���猩��n�p�`�Ƃ̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �u�~��n�p�`���݂��ɏd�Ȃ荇���v�𖞂������ۂ�������boolean�^�ϐ�
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

	// ��
	/**
	 * �u�~A���~B�̓����Őڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~A���~B�̓����Őڂ���v�͓�̉~���m�̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �u�~A���~B�̓����Őڂ���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean inscribeCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isEqual(centerDist, radiusDiff));

		return (condition[0] && condition[1]);
	}

	// ��
	/**
	 * �u�~A���~B�̓����ɑ��݂���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~A���~B�̓����ɑ��݂���v�͓�̉~���m�̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �u�~A���~B�̓����ɑ��݂���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isGreater(radiusDiff, centerDist));

		return (condition[0] && condition[1]);
	}

	// ��
	/**
	 * �u�~A�Ɖ~B���݂��ɊO�ڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~A�Ɖ~B���݂��ɊO�ڂ���v�͓�̉~���m�̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �u�~A�Ɖ~B���݂��ɊO�ڂ���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusSum = c.radius + this.radius;

		condition[0] = !c.includePoint(this.center) && !this.includePoint(c.center);
		condition[1] = isEqual(centerDist, radiusSum);

		return (condition[0] && condition[1]);
	}

	// ��
	/**
	 * �u�~A�Ɖ~B���݂��ɏd�Ȃ荇���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �u�~A�Ɖ~B���݂��ɏd�Ȃ荇���v�͓�̉~���m�̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �u�~A�Ɖ~B���݂��ɏd�Ȃ荇���v�𖞂������ۂ�������boolean�^�ϐ�
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
	 * �~���猩��n�p�`�S�Ă̕ӂ̓��O
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifySide(ArrayList<MyLine> side) {
		int[] sideState = new int[3];// 0:�~�̓���, 1:�~�ɐڂ���, 2:�~�̊O��

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

	// ��
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

	// ��
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
