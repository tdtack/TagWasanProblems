package wasan.problems;

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��n�p�`(�}�`�v�f)�Ɋւ���N���X�ł���B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyPolygon {
	/** n�p�`�̒��_�̏��������X�g */
	public ArrayList<MyPoint> vertex = new ArrayList<MyPoint>();

	/** n�p�`�̕ӂ̏��������X�g */
	public ArrayList<MyLine> side = new ArrayList<MyLine>();

	/** n�p�`�̏d�S��\���ϐ� */
	public MyPoint centroid;

	/**
	 * �R���X�g���N�^
	 * 
	 * @param _p
	 */
	public MyPolygon(MyPoint... _p) {// �ӏ�����������
		for (int i = 0; i < _p.length; i++) {
			this.vertex.add(_p[i]);
		}

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint start = vertex.get(i);
			MyPoint end = vertex.get((i < vertex.size() - 1) ? i + 1 : 0);

			side.add(new MyLine(start, end));// ���z�I�ȕӂł��邱�Ƃɒ���
		}

		centroid = new MyPoint(0, 0);
		for (int i = 0; i < vertex.size(); i++) {
			centroid.x += vertex.get(i).x;
			centroid.y += vertex.get(i).y;
		}
		centroid.x /= vertex.size();
		centroid.y /= vertex.size();
	}

	/**
	 * n�p�`���猩���~�Ƃ̊֌W�� <br>
	 * �@�~���猩��n�p�`�̏d�S�̓��O + n�p�`���猩���~�̒��S�̓��O <br>
	 * �A�~���猩��n�p�`�̑S�Ă̓_�̏��(�~�̓����ɂ���, �~��ɂ���, �~�̊O���ɂ���) <br>
	 * �B�~���猩��n�p�`�̑S�Ă̕ӂ̏��(���������a����, �~�ɐڂ��Ă���, ���������a����) <br>
	 */

	/**
	 * �@�����ɂ��� + ���O�ǂ���̉\�������� <br>
	 * �An�̓_���~��ɂ���(���̍��v��n) <br>
	 * �Bn�{�̕ӂ����������a����(���̍��v��n) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean inscribeCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);// �~���猩��n�p�`�̒��_�̏��
		int[] sideState = c.classifySide(this.side);// �~���猩��n�p�`�̕ӂ̏��(�~����n�p�`�̕ӂ̐ڏ��)

		condition[0] = c.includePoint(this.centroid);
		condition[1] = (vertexState[1] == this.vertex.size());
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * �@�����ɂ��� + ���O�ǂ���̉\�������� <br>
	 * �A1�ȏ�̓_���~�̓����ɂ���, n�����̓_���~��ɂ��� <br>
	 * �Bn�{�̕ӂ����������a����(���̍��v��n) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);// �~���猩��n�p�`�̒��_�̏��
		int[] sideState = c.classifySide(this.side);// �~���猩��n�p�`�̕ӂ̏��(�~����n�p�`�̕ӂ̐ڏ��)

		condition[0] = c.includePoint(this.centroid);
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * �@�O���ɂ��� + �O���ɂ��� <br>
	 * �A1�ȉ��̓_���~��ɂ���, n-1�ȏ�̓_���~�̊O���ɂ���(���̍��v��n) <br>
	 * �B1�{�ȉ��̕ӂ��~�ɐڂ��Ă���, n-1�{�ȏ�̕ӂ����������a����(���̍��v��n) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[][] condition = new boolean[3][2];

		int[] vertexState = c.classifyVertex(this.vertex);// �~���猩��n�p�`�̒��_�̏��
		int[] sideState = c.classifySide(this.side);// �~���猩��n�p�`�̕ӂ̏��(�~����n�p�`�̕ӂ̐ڏ��)

		condition[0][0] = !c.includePoint(this.centroid) && !this.includePoint(c.center);

		condition[1][0] = (vertexState[1] == 0) && (vertexState[2] == this.vertex.size());// vertexState[1]+vertexState[2]=this.vertex.size()�͕K�R
		condition[2][0] = (sideState[1] == 1) && (sideState[2] == this.side.size() - 1);// sideState[1]+sideState[2]=this.side.size()�͕K�R

		condition[1][1] = (vertexState[1] == 1) && (vertexState[2] == this.vertex.size() - 1);// vertexState[1]+vertexState[2]=this.vertex.size()�͕K�R
		condition[2][1] = (sideState[1] == 0) && (sideState[2] == this.side.size());// sideState[1]+sideState[2]=this.side.size()�͕K�R

//		condition[1] = (vertexState[0] == 0) && withinRange(vertexState[1], 0, 1)
//				&& withinRange(vertexState[2], this.vertex.size() - 1, this.vertex.size());
//		condition[2] = (sideState[0] == 0) && withinRange(sideState[1], 0, 1)
//				&& withinRange(sideState[2], this.side.size() - 1, this.side.size());
		// ����ł�肽���Ƃ��낾���A���ꂽ���m��n�p�`�Ɖ~�܂Łu�אڂ���v�Ɣ��f���Ă��܂�

		return (condition[0][0] && ((condition[1][0] && condition[2][0]) || (condition[1][1] && condition[2][1])));// a�r���I�_���a�ɋ߂�
	}

	/**
	 * 2��n�p�`���m�̊֌W�� <br>
	 * �@n�p�`B���猩��n�p�`A�̏d�S�̓��O + n�p�`A���猩��n�p�`B�̏d�S�̓��O <br>
	 * �An�p�`B���猩��n�p�`A�̑S�Ă̓_�̏�� <br>
	 */

	/**
	 * �@�����ɂ��� + ���O�ǂ���̉\�������� <br>
	 * �An�̓_��n�p�`B�̓_�Ɉ�vor�ӏ�ɂ���(���̍��v��n) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);

		condition[0] = pg.includePoint(this.centroid);// ��
		condition[1] = (vertexState[1] == this.vertex.size());// ��

		return (condition[0] && condition[1]);
	}

	/**
	 * �@�����ɂ��� + ���O�ǂ���̉\�������� <br>
	 * �An�����̓_��n�p�`B�̓_�Ɉ�vor�ӏ�ɂ���, 1�ȏ�̓_��n�p�`B�̓����ɂ��� <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);// Y���猩���AX�̊e���_�̐ڂ��

		condition[0] = pg.includePoint(this.centroid);// ��
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);// ��

		return (condition[0] && condition[1]);
	}

	/**
	 * �@�O���ɂ��� + �O���ɂ��� <br>
	 * �A1�ȏ�2�ȉ��̓_��n�p�`B�̓_�Ɉ�vor�ӏ�ɂ���, n-2�ȏ�̓_��n�p�`B�̊O���ɂ���(���̍��v��n) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean adjoinPolygon(MyPolygon pg) {//
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);// Y���猩���AX�̊e���_�̐ڂ��
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = !pg.includePoint(this.centroid) && !this.includePoint(pg.centroid);// ��
		condition[1][0] = (vertexState1[0] == 0) && withinRange(vertexState1[1], 1, 2)
				&& withinRange(vertexState1[2], this.vertex.size() - 2, this.vertex.size() - 1);// ��
		condition[1][1] = (vertexState2[0] == 0) && withinRange(vertexState2[1], 1, 2)
				&& withinRange(vertexState2[2], pg.vertex.size() - 2, pg.vertex.size() - 1);// ��

		return (condition[0][0] && (condition[1][0] || condition[1][1]));
	}

	/**
	 * �@���O�ǂ���̉\�������� + ���O�ǂ���̉\�������� <br>
	 * �A1�ȏ�̓_��n�p�`B�̓����ɂ���, n�����̓_��n�p�`B�̊O���ɂ���(���̍��v��n) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean overlapPolygon(MyPolygon pg) {// �ǂ����邩
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);// Y���猩���AX�̊e���_�̐ڂ��
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = true;
		condition[1][0] = withinRange((vertexState1[0] + vertexState1[1]), 1, this.vertex.size() - 1)
				&& withinRange(vertexState1[2], 1, this.vertex.size() - 1);// ��
		condition[1][1] = withinRange((vertexState2[0] + vertexState2[1]), 1, pg.vertex.size() - 1)
				&& withinRange(vertexState2[2], 1, pg.vertex.size() - 1);// ��
		// �����AvertexState1[2]=this.vertex.size() && vertexState2[2]=pg.vertex.size() ��

//		condition2[0][0] = withinRange(sideState[0], 1, pg.side.size() - 1)
//				&& withinRange((sideState[1] + sideState[2]), 1, pg.side.size() - 1);// ��
//		condition2[0][1] = (sideState[0] == pg.side.size()) && (this.classifyVertex(pg.vertex)[2] > 0);

		return (condition[0][0] && (condition[1][0] && condition[1][1]));
	}

	///////////////////////////////////////////////////////////// a�֌W������֐�

	// n�p�`���猩���~�̕����_�̓��O(boolean�^, ������Polygon, int(臒l))
	public int[] classifyCircum(MyPoint[] circum) {
		int[] circumState = new int[3];// 0:����, 1:���_�Ɉ�vor�ӂɐڂ���, 2:�O��

		for (int i = 0; i < circum.length; i++) {
			MyPoint p = circum[i];

			if (matchVertex(p) || isOnSide(p)) {// match_side(p)
				circumState[1]++;
			} else {
				if (includePoint(p)) {
					circumState[0]++;
				} else {
					circumState[2]++;
				}
			}
		}
		return circumState;
	}

	// n�p�`X���猩��n�p�`Y�̑S�Ă̒��_��(��v,�ӂɐڂ��鐔)�܂��͓��O
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:����, 1:���_�Ɉ�vor�ӂɐڂ���, 2:�O��

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint p = vertex.get(i);// n�p�`Y�̒��_

			// 1:���_�Ɉ�vor�ӂɐڂ���
			// a����this�̒��_�����ꂩ�Ɉ�v���Ă���A�������͕ӏ�ɂ���Ɗm�F�ł�����break����[1]++
			// a���ꂪ�m�F�ł��Ȃ����[0]��[2]�̔��f

			if (matchVertex(p) || isOnSide(p)) {
				vertexState[1]++;
			} else {
				if (includePoint(p)) {
					vertexState[0]++;
				} else {
					vertexState[2]++;
				}
			}
		}
		return vertexState;
	}

	// �_��n�p�`�̂����ꂩ�̓_�ɍ��v���邩
	private boolean matchVertex(MyPoint p) {
		for (int i = 0; i < this.vertex.size(); i++) {
			if (p == this.vertex.get(i)) {
				return true;
			}
		}
		return false;
	}

	// �_��n�p�`�̂����ꂩ�̕ӂ̏�ɂ��邩
	private boolean isOnSide(MyPoint p) {
		for (int i = 0; i < this.side.size(); i++) {
			if ((p.calcDistToLine(this.side.get(i)) < 10) && p.withinLineRange(this.side.get(i))) {// ������threshold
				return true;
			}
		}
		return false;
	}

	// �_�̓��O
	public boolean includePoint(MyPoint p) {
		int crossNum = countCrossNum(this.vertex, p);
		int windNum = countWindNum(this.vertex, p);

		return (crossNum % 2 == 1) && (windNum != 0);
	}

	private int countCrossNum(ArrayList<MyPoint> pointList, MyPoint p) {
		int crossNum = 0;

		for (int i = 0; i < pointList.size(); i++) {
			int index1 = i;
			int index2 = (i < pointList.size() - 1) ? i + 1 : 0;
			MyPoint p1 = pointList.get(index1);
			MyPoint p2 = pointList.get(index2);

			if ((p1.y <= p.y) && (p2.y > p.y) || (p1.y > p.y) && (p2.y <= p.y)) {
				double t = (p.y - p1.y) / (p2.y - p1.y);
				if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
					crossNum++;
				}
			}
		}

		return crossNum;
	}

	private int countWindNum(ArrayList<MyPoint> pointList, MyPoint p) {
		int windNum = 0;

		for (int i = 0; i < pointList.size(); i++) {
			int index1 = i;
			int index2 = (i < pointList.size() - 1) ? i + 1 : 0;
			MyPoint p1 = pointList.get(index1);
			MyPoint p2 = pointList.get(index2);

			if ((p1.y <= p.y) && (p2.y > p.y)) {
				double t = (p.y - p1.y) / (p2.y - p1.y);
				if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
					windNum++;
				}
			} else if ((p1.y > p.y) && (p2.y <= p.y)) {
				double t = (p.y - p1.y) / (p2.y - p1.y);
				if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
					windNum--;
				}
			}
		}

		return windNum;
	}

	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}

	public String getPolygonName() {// Triangle > ...�̂悤��return�����
		switch (this.vertex.size()) {
		case 3:
			return getTriangleName();
		case 4:
			return getQuadrangleName();
		default:
			return getGonName();
		}
	}

	public String getTriangleName() {// �ӏ���l[]�ɓ����Ă��� �� �_�̏��Ōv�Z���Ȃ��ƃ_��(�[�_�ł͂Ȃ��A������ɂ���P�[�X)
		boolean[][] condition = new boolean[2][3];

		condition[0][0] = isSameLength(side.get(0), side.get(1));
		condition[0][1] = isSameLength(side.get(1), side.get(2));
		condition[0][2] = isSameLength(side.get(2), side.get(0));

		condition[1][0] = isPerpendicular(side.get(0), side.get(1));
		condition[1][1] = isPerpendicular(side.get(1), side.get(2));
		condition[1][2] = isPerpendicular(side.get(2), side.get(0));

		if (isSameLength(side.get(0), side.get(1), side.get(2))) {
			return "���O�p�`";
		} else {
			if (condition[0][0] || condition[0][1] || condition[0][2]) {// a�񓙕ӎO�p�`
				return "�񓙕ӎO�p�`";
			} else if (condition[1][0] || condition[1][1] || condition[1][2]) {// a���p�O�p�`
				return "���p�O�p�`";//
			}
		}

		return "�O�p�`";
	}

	public String getQuadrangleName() {// a����Ȃ��񂩂�
		boolean[][] condition = new boolean[3][4];

		condition[0][0] = isSameLength(side.get(0), side.get(2));
		condition[0][1] = isSameLength(side.get(1), side.get(3));

		condition[1][0] = isPerpendicular(side.get(0), side.get(1));
		condition[1][1] = isPerpendicular(side.get(1), side.get(2));
		condition[1][2] = isPerpendicular(side.get(2), side.get(3));
		condition[1][3] = isPerpendicular(side.get(3), side.get(0));

		condition[2][0] = isParallel(side.get(0), side.get(2));
		condition[2][1] = isParallel(side.get(1), side.get(3));

		if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3))) {// a�����`�A�H�`
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "�����`";//
			} else if (condition[2][0] && condition[2][1]) {
				return "�H�`";
			}
		} else {// a�����`�A���r��`
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "�����`";//
			} else if ((condition[0][0] && condition[2][1]) || (condition[0][1] && condition[2][0])) {
				return "���r��`";
			}

//			if (condition[0][0] && condition[0][1]) {
//				if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
//					return "�����`";//
//				}
//			} else {
//				if ((condition[0][0] && condition[2][1]) || (condition[0][1] && condition[2][0])) {
//					return "���r��`";
//				}
//			}
		}

		return "�l�p�`";
	}

	public String getGonName() {
		if (this.vertex.size() == 5) {
			if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3), side.get(4))) {
				return "���܊p�`";
			}
			return "�܊p�`";
		} else {
			if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3), side.get(4), side.get(5))) {
				return "���Z�p�`";
			}
			return "�Z�p�`";
		}
	}

	public boolean hasFeature() {
		String polygonName = getPolygonName();
		String[] polygon = { "�O�p�`", "�l�p�`", "�܊p�`", "�Z�p�`" };

		return !(polygonName.equals(polygon[vertex.size() - 3]));
	}

	private boolean isSameLength(MyLine... l) {//
		double[] lengthRatio = new double[l.length];

		double totalLength = 0;
		for (int i = 0; i < l.length; i++) {
			totalLength += l[i].getLength();
		}
		double avgLength = totalLength / l.length;

		for (int i = 0; i < l.length; i++) {
			lengthRatio[i] = l[i].getLength() / avgLength;
		}

		int count = 0;
		double threshold = l.length * (2.5e-2) - (4e-2);// �ꉞOK
		for (int i = 0; i < lengthRatio.length; i++) {
			if (Math.abs(1 - lengthRatio[i]) < threshold) {// 1����ɔ��肷��
				count++;
			}
		}

		return (count == l.length);
	}

	private boolean isParallel(MyLine l1, MyLine l2) {//
		if (!l1.intersectLine(l2)) {
			double crossProduct = l1.na * l2.nb - l2.na * l1.nb;

			return Math.abs(crossProduct) < 5e-5;// 1e-4
		}
		return false;
	}

	private boolean isPerpendicular(MyLine l1, MyLine l2) {//
		if (l1.intersectLine(l2)) {
			double dotProduct = l1.na * l2.na + l1.nb * l2.nb;

			return Math.abs(dotProduct) < 5e-6;// �������͂����Ƒ傫����
		}
		return false;
	}
}

