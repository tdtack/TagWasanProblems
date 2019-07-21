package wasan.problems;

import java.util.ArrayList;

/**
 * �a�Z�}�`���Ɋ܂܂��}�`�v�f�́un�p�`�v�Ɋւ���N���X�ł��B<br>
 * 
 * @author Takuma Tsuchihashi
 */
public class MyPolygon {

	// �ȉ��An�p�`���\������v�f��\�����X�g�E�ϐ��ł��B
	/**
	 * n�p�`�̒��_��ێ����܂��B<br>
	 */
	public ArrayList<MyPoint> vertex = new ArrayList<MyPoint>();
	/**
	 * n�p�`�̕ӂ�ێ����܂��B<br>
	 */
	public ArrayList<MyLine> side = new ArrayList<MyLine>();
	/**
	 * n�p�`�̏d�S��\���܂��B<br>
	 */
	public MyPoint centroid;
	
	/**
	 * n�p�`�̑S�Ă̒��_���w�肵�An�p�`�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param _p
	 *            n�p�`�̒��_��\��MyPoint�N���X�ϐ�(�S�Ă̒��_�����Ɉ����Ƃ��܂��B)
	 */
	public MyPolygon(MyPoint... _p) {
		for (int i = 0; i < _p.length; i++) {
			this.vertex.add(_p[i]);
		}

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint start = vertex.get(i);
			MyPoint end = vertex.get((i < vertex.size() - 1) ? i + 1 : 0);

			side.add(new MyLine(start, end));
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
	 * �un�p�`���~�ɓ��ڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un�p�`���~�ɓ��ڂ���v��n�p�`���猩���~�Ƃ̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �un�p�`���~�ɓ��ڂ���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean inscribeCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);
		int[] sideState = c.classifySide(this.side);

		condition[0] = c.includePoint(this.centroid);
		condition[1] = (vertexState[1] == this.vertex.size());
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}
	
	/**
	 * �un�p�`���~�̓����ɑ��݂���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un�p�`���~�̓����ɑ��݂���v��n�p�`���猩���~�Ƃ̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �un�p�`���~�̓����ɑ��݂���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[3];

		int[] vertexState = c.classifyVertex(this.vertex);
		int[] sideState = c.classifySide(this.side);

		condition[0] = c.includePoint(this.centroid);
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);
		condition[2] = (sideState[0] == this.side.size());

		return (condition[0] && condition[1] && condition[2]);
	}
	
	/**
	 * �un�p�`�Ɖ~���݂��ɗאڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un�p�`�Ɖ~���݂��ɗאڂ���v��n�p�`���猩���~�Ƃ̊֌W����1�ł��B
	 * 
	 * @param c
	 *            �ΏۂƂȂ�~��\��MyCircle�N���X�ϐ�
	 * @return �un�p�`�Ɖ~���݂��ɗאڂ���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[][] condition = new boolean[3][2];

		int[] vertexState = c.classifyVertex(this.vertex);
		int[] sideState = c.classifySide(this.side);

		condition[0][0] = !c.includePoint(this.centroid) && !this.includePoint(c.center);

		condition[1][0] = (vertexState[1] == 0) && (vertexState[2] == this.vertex.size());
		condition[2][0] = (sideState[1] == 1) && (sideState[2] == this.side.size() - 1);

		condition[1][1] = (vertexState[1] == 1) && (vertexState[2] == this.vertex.size() - 1);
		condition[2][1] = (sideState[1] == 0) && (sideState[2] == this.side.size());

		return (condition[0][0] && ((condition[1][0] && condition[2][0]) || (condition[1][1] && condition[2][1])));
	}
	
	/**
	 * �un(A)�p�`��n(B)�p�`�ɓ��ڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un(A)�p�`��n(B)�p�`�ɓ��ڂ���v�͓��n�p�`���m�̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �un(A)�p�`��n(B)�p�`�ɓ��ڂ���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);

		condition[0] = pg.includePoint(this.centroid);
		condition[1] = (vertexState[1] == this.vertex.size());

		return (condition[0] && condition[1]);
	}
	
	/**
	 * �un(A)�p�`��n(B)�p�`�̓����ɑ��݂���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un(A)�p�`��n(B)�p�`�̓����ɑ��݂���v�͓��n�p�`���m�̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �un(A)�p�`��n(B)�p�`�̓����ɑ��݂���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[2];

		int[] vertexState = pg.classifyVertex(this.vertex);

		condition[0] = pg.includePoint(this.centroid);
		condition[1] = withinRange(vertexState[0], 1, this.vertex.size())
				&& withinRange(vertexState[1], 0, this.vertex.size() - 1) && (vertexState[2] == 0);

		return (condition[0] && condition[1]);
	}
	
	/**
	 * �un(A)�p�`��n(B)�p�`���݂��ɗאڂ���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un(A)�p�`��n(B)�p�`���݂��ɗאڂ���v�͓��n�p�`���m�̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �un(A)�p�`��n(B)�p�`���݂��ɗאڂ���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean adjoinPolygon(MyPolygon pg) {//
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = !pg.includePoint(this.centroid) && !this.includePoint(pg.centroid);
		condition[1][0] = (vertexState1[0] == 0) && withinRange(vertexState1[1], 1, 2)
				&& withinRange(vertexState1[2], this.vertex.size() - 2, this.vertex.size() - 1);
		condition[1][1] = (vertexState2[0] == 0) && withinRange(vertexState2[1], 1, 2)
				&& withinRange(vertexState2[2], pg.vertex.size() - 2, pg.vertex.size() - 1);

		return (condition[0][0] && (condition[1][0] || condition[1][1]));
	}
	
	/**
	 * �un(A)�p�`��n(B)�p�`���݂��ɏd�Ȃ荇���v�𖞂������ۂ��𔻒肵�܂��B <br>
	 * �un(A)�p�`��n(B)�p�`���݂��ɏd�Ȃ荇���v�͓��n�p�`���m�̊֌W����1�ł��B
	 * 
	 * @param pg
	 *            �ΏۂƂȂ�n�p�`��\��MyPolygon�N���X�ϐ�
	 * @return �un(A)�p�`��n(B)�p�`���݂��ɏd�Ȃ荇���v�𖞂������ۂ�������boolean�^�ϐ�
	 */
	public boolean overlapPolygon(MyPolygon pg) {
		boolean[][] condition = new boolean[2][2];

		int[] vertexState1 = pg.classifyVertex(this.vertex);
		int[] vertexState2 = this.classifyVertex(pg.vertex);

		condition[0][0] = true;
		condition[1][0] = withinRange((vertexState1[0] + vertexState1[1]), 1, this.vertex.size() - 1)
				&& withinRange(vertexState1[2], 1, this.vertex.size() - 1);
		condition[1][1] = withinRange((vertexState2[0] + vertexState2[1]), 1, pg.vertex.size() - 1)
				&& withinRange(vertexState2[2], 1, pg.vertex.size() - 1);

		return (condition[0][0] && (condition[1][0] && condition[1][1]));
	}
	
	/**
	 * n�p�`�ɑ΂���~�����8�_�̏�Ԃ��L�^���܂��B<br>
	 * �L�^������Ԃ́u�~����̓_��n�p�`�̓����ɂ���v�u�~����̓_��n�p�`��ɂ���v�u�~����̓_��n�p�`�̊O���ɂ���v�ł��B
	 * 
	 * @param circum
	 *            �~�����8�_��\��MyPoint�N���X�z��
	 * @return n�p�`�ɑ΂���~�����8�_�̏�Ԃ�ێ�����int�^�z��
	 */
	public int[] classifyCircum(MyPoint[] circum) {
		int[] circumState = new int[3];// 0:����, 1:���_�Ɉ�vor�ӂɐڂ���, 2:�O��

		for (int i = 0; i < circum.length; i++) {
			MyPoint p = circum[i];

			if (matchVertex(p) || isOnSide(p)) {
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
	
	/**
	 * n(A)�p�`�ɑ΂���n(B)�p�`�̑S�Ă̒��_�̏�Ԃ��L�^���܂��B<br>
	 * �L�^������Ԃ́u���_��n(A)�p�`�̓����ɂ���v�u���_��n(A)�p�`��ɂ���v�u���_��n(A)�p�`�̊O���ɂ���v�ł��B
	 * 
	 * @param vertex
	 *            n(B)�p�`�̒��_��ێ�����MyPoint�N���X���X�g
	 * @return ���_�̏�Ԃ�ێ�����int�^�z��
	 */
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:����, 1:���_�Ɉ�vor�ӂɐڂ���, 2:�O��

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint p = vertex.get(i);

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
	
	/**
	 * n�p�`�ɑ΂��ē_�����_�ƍ��v���邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param p
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return n�p�`�ɑ΂��ē_�����_�ƍ��v���邩�ۂ�������boolean�^�ϐ�
	 */
	private boolean matchVertex(MyPoint p) {
		for (int i = 0; i < this.vertex.size(); i++) {
			if (p == this.vertex.get(i)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * n�p�`�ɑ΂��ē_���ӏ�ɑ��݂��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param p
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return n�p�`�ɑ΂��ē_���ӏ�ɑ��݂��邩�ۂ�������boolean�^�ϐ�
	 */
	private boolean isOnSide(MyPoint p) {
		for (int i = 0; i < this.side.size(); i++) {
			if ((p.calcDistToLine(this.side.get(i)) < 10) && p.withinLineRange(this.side.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * n�p�`�ɑ΂��ē_�������Ɋ܂܂�邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param p
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return n�p�`�ɑ΂��ē_�������Ɋ܂܂�邩�ۂ�������boolean�^�ϐ�
	 */
	public boolean includePoint(MyPoint p) {
		int crossNum = countCrossNum(this.vertex, p);
		int windNum = countWindNum(this.vertex, p);

		return (crossNum % 2 == 1) && (windNum != 0);
	}
	
	/**
	 * �������Ɋ�Â��āAn�p�`�ɑ΂��ē_�������Ɋ܂܂�邩�ۂ��𔻒肵�܂��B<br>
	 * �������Ƃ́A�C�ӂ̓_�����������������n�p�`�̕ӂƌ�������񐔂�\���܂��B
	 * 
	 * @param pointList
	 *            n�p�`�̒��_��ێ�����MyPoint�N���X���X�g
	 * @param p
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return �������Ɋ�Â��āAn�p�`�ɑ΂��ē_�������Ɋ܂܂�邩�ۂ�������boolean�^�ϐ�
	 */
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
	
	/**
	 * ��]���Ɋ�Â��āAn�p�`�ɑ΂��ē_�������Ɋ܂܂�邩�ۂ��𔻒肵�܂��B<br>
	 * ��]���Ƃ́An�p�`�ɂ���č\�������H���C�ӂ̓_�̎��������񐔂�\���܂��B
	 * 
	 * @param pointList
	 *            n�p�`�̒��_��ێ�����MyPoint�N���X���X�g
	 * @param p
	 *            �ΏۂƂȂ�_��\��MyPoint�N���X�ϐ�
	 * @return ��]���Ɋ�Â��āAn�p�`�ɑ΂��ē_�������Ɋ܂܂�邩�ۂ�������boolean�^�ϐ�
	 */
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
	
	/**
	 * ����l���w�肵���͈͓��̒l�ł��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param value
	 *            �ΏۂƂȂ�l��\��int�^�ϐ�
	 * @param min
	 *            �w�肷��͈͂̍ŏ��l��\��int�^�ϐ�
	 * @param max
	 *            �w�肷��͈͂̍ő�l��\��int�^�ϐ�
	 * @return ����l���w�肵���͈͓��̒l�ł��邩�ۂ�������boolean�^�ϐ�
	 */
	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}
	
	/**
	 * n�p�`�̌`��𕪐͂��A�����I��n�p�`�̖��̂��擾���܂��B<br>
	 * �����I��n�p�`�́u���O�p�`�v��u�����`�v�Ȃǂ��w���܂��B
	 * 
	 * @return �����I��n�p�`�̖��̂�\��String�^�ϐ�
	 */
	public String getPolygonName() {
		switch (this.vertex.size()) {
		case 3:
			return getTriangleName();
		case 4:
			return getQuadrangleName();
		default:
			return getGonName();
		}
	}
	
	/**
	 * �O�p�`�̌`��𕪐͂��A�����I�ȎO�p�`�̖��̂��擾���܂��B<br>
	 * �����I�ȎO�p�`�́u���O�p�`�v�u�񓙕ӎO�p�`�v�u���p�O�p�`�v���w���܂��B
	 * 
	 * @return �����I�ȎO�p�`�̖��̂�\��String�^�ϐ�
	 */
	public String getTriangleName() {
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
			if (condition[0][0] || condition[0][1] || condition[0][2]) {
				return "�񓙕ӎO�p�`";
			} else if (condition[1][0] || condition[1][1] || condition[1][2]) {
				return "���p�O�p�`";
			}
		}

		return "�O�p�`";
	}
	
	/**
	 * �l�p�`�̌`��𕪐͂��A�����I�Ȏl�p�`�̖��̂��擾���܂��B<br>
	 * �����I�Ȏl�p�`�́u�����`�v�u�����`�v�u�H�`�v�u���r��`�v���w���܂��B
	 * 
	 * @return �����I�Ȏl�p�`�̖��̂�\��String�^�ϐ�
	 */
	public String getQuadrangleName() {
		boolean[][] condition = new boolean[3][4];

		condition[0][0] = isSameLength(side.get(0), side.get(2));
		condition[0][1] = isSameLength(side.get(1), side.get(3));

		condition[1][0] = isPerpendicular(side.get(0), side.get(1));
		condition[1][1] = isPerpendicular(side.get(1), side.get(2));
		condition[1][2] = isPerpendicular(side.get(2), side.get(3));
		condition[1][3] = isPerpendicular(side.get(3), side.get(0));

		condition[2][0] = isParallel(side.get(0), side.get(2));
		condition[2][1] = isParallel(side.get(1), side.get(3));

		if (isSameLength(side.get(0), side.get(1), side.get(2), side.get(3))) {
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "�����`";
			} else if (condition[2][0] && condition[2][1]) {
				return "�H�`";
			}
		} else {
			if (condition[1][0] && condition[1][1] && condition[1][2] && condition[1][3]) {
				return "�����`";
			} else if ((condition[0][0] && condition[2][1]) || (condition[0][1] && condition[2][0])) {
				return "���r��`";
			}
		}

		return "�l�p�`";
	}
	
	/**
	 * �܊p�`��Z�p�`�̌`��𕪐͂��A�����I�Ȍ܊p�`��Z�p�`�̖��̂��擾���܂��B<br>
	 * �����I�Ȍ܊p�`�́u���܊p�`�v�A�����I�ȘZ�p�`�́u���Z�p�`�v���w���܂��B
	 * 
	 * @return �����I�Ȍ܊p�`��Z�p�`�̖��̂�\��String�^�ϐ�
	 */
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
	
	/**
	 * n�p�`�������I�ł��邩�ۂ��𔻒肵�܂��B <br>
	 * �����I�ł���Ƃ́An�p�`���u���O�p�`�v��u�����`�v�Ȃǂ������𖞂������Ƃ��w���܂��B
	 * 
	 * @return n�p�`�������I�ł��邩�ۂ�������boolean�^�ϐ�
	 */
	public boolean hasFeature() {
		String polygonName = getPolygonName();// n�p�`�̌`��𕪐͂��A�����I��n�p�`�̖��̂��擾���܂��B
		String[] polygon = { "�O�p�`", "�l�p�`", "�܊p�`", "�Z�p�`" };

		return !(polygonName.equals(polygon[vertex.size() - 3]));
	}
	
	/**
	 * �����̐����̒������덷���܂߂ē��������ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param l
	 *            �����̐�����\��MyLine�N���X�ϐ�(��r����S�Ă̐��������Ɉ����Ƃ��܂��B)
	 * @return �����̐����̒������덷���܂߂ē��������ۂ�������boolean�^�ϐ�
	 */
	private boolean isSameLength(MyLine... l) {
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
		double threshold = l.length * (2.5e-2) - (4e-2);
		for (int i = 0; i < lengthRatio.length; i++) {
			if (Math.abs(1 - lengthRatio[i]) < threshold) {
				count++;
			}
		}

		return (count == l.length);
	}
	
	/**
	 * 2�{�̐������덷���܂߂ĕ��s�ł��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param l1
	 *            1�{�ڂ̐�����\��MyLine�N���X�ϐ�
	 * @param l2
	 *            2�{�ڂ̐�����\��MyLine�N���X�ϐ�
	 * @return 2�{�̐������덷���܂߂ĕ��s�ł��邩�ۂ�������boolean�^�ϐ�
	 */
	private boolean isParallel(MyLine l1, MyLine l2) {
		if (!l1.intersectLine(l2)) {
			double crossProduct = l1.na * l2.nb - l2.na * l1.nb;

			return Math.abs(crossProduct) < 5e-5;
		}
		return false;
	}
	
	/**
	 * 2�{�̐������덷���܂߂Đ����ł��邩�ۂ��𔻒肵�܂��B<br>
	 * 
	 * @param l1
	 *            1�{�ڂ̐�����\��MyLine�N���X�ϐ�
	 * @param l2
	 *            2�{�ڂ̐�����\��MyLine�N���X�ϐ�
	 * @return 2�{�̐������덷���܂߂Đ����ł��邩�ۂ�������boolean�^�ϐ�
	 */
	private boolean isPerpendicular(MyLine l1, MyLine l2) {
		if (l1.intersectLine(l2)) {
			double dotProduct = l1.na * l2.na + l1.nb * l2.nb;

			return Math.abs(dotProduct) < 5e-6;
		}
		return false;
	}
}
