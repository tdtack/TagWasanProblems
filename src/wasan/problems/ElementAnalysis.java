package wasan.problems;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * �a�Z�}�`���Ɋ܂܂��􉽗v�f(�_�E�����E�~)�A�}�`�v�f(n�p�`�E�~)�̔F���Ɋւ���N���X�ł��B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class ElementAnalysis {

	/** ���̃N���X��HoughTransform�N���X�𗘗p����ۂɎg���܂��B */
	private HoughTransform houghTrans;

	/**
	 * �ȉ��A�}�`��肩�牼���o���ꂽ�􉽗v�f(�_�E�����E�~)��ێ����郊�X�g�ł��B
	 */
	/** �}�`��肩�牼���o���ꂽ�_��ێ����܂��B */
	public ArrayList<MyPoint> scannedPoint = new ArrayList<MyPoint>();
	/** �}�`��肩�牼���o���ꂽ������ێ����܂��B */
	public ArrayList<MyLine> scannedLine = new ArrayList<MyLine>();
	/** �}�`��肩�璊�o���ꂽ�~��ێ����܂��B */
	public ArrayList<MyCircle> scannedCircle = new ArrayList<MyCircle>();

	/**
	 * �ȉ��A�����o�̗v�f��␳���A�m�肵���􉽗v�f(�_�E����)�Ɛ}�`�v�f(n�p�`�E�~)��ێ����郊�X�g�ł��B
	 */
	/** �����o�̓_��␳���A�m�肵���_��ێ����܂��B */
	public ArrayList<MyPoint> detectedPoint = new ArrayList<MyPoint>();
	/** �����o�̐�����␳���A�m�肵��������ێ����܂��B */
	public ArrayList<MyLine> detectedLine = new ArrayList<MyLine>();
	/** �����o�̉~��␳���A�m�肵���~��ێ����܂��B */
	public ArrayList<MyCircle> detectedCircle = new ArrayList<MyCircle>();
	/** �m�肵���_�Ɛ������璊�o���ꂽn�p�`��ێ����܂��B */
	public ArrayList<MyPolygon> detectedPolygon = new ArrayList<MyPolygon>();

	/**
	 * ElementAnalysis�I�u�W�F�N�g���쐬���܂��B
	 * 
	 * @param houghTrans
	 */
	public ElementAnalysis(HoughTransform _houghTrans) {
		this.houghTrans = _houghTrans;
	}

	public void freeResource() {
		houghTrans = null;

		this.scannedPoint.clear();
		this.scannedLine.clear();
		this.scannedCircle.clear();

		this.detectedPoint.clear();
		this.detectedLine.clear();
		this.detectedCircle.clear();
		this.detectedPolygon.clear();
	}

	public void scanPoint() {// ���o���������Ɖ~�����_�𑍓�����Ō��o
		for (int i = 0; i < this.scannedLine.size(); i++) {// �������m�̌�_�̌��o
			for (int j = i + 1; j < this.scannedLine.size(); j++) {
				MyLine l1 = this.scannedLine.get(i);
				MyLine l2 = this.scannedLine.get(j);

				MyPoint p = MyPoint.getIntersection1(l1, l2, 1);
				if (p != null) {
					this.scannedPoint.add(p);
				}
			}
		}

		for (int i = 0; i < this.scannedLine.size(); i++) {// �����Ɖ~�̌�_�̌��o
			for (int j = 0; j < this.scannedCircle.size(); j++) {
				MyLine l = this.scannedLine.get(i);
				MyCircle c = this.scannedCircle.get(j);

				MyPoint p1 = MyPoint.getIntersection2(l, c, 1);
				if (p1 != null) {
					this.scannedPoint.add(p1);
				}

				MyPoint p2 = MyPoint.getIntersection2(l, c, 2);
				if (p2 != null) {
					this.scannedPoint.add(p2);
				}
			}
		}

		for (int i = 0; i < this.scannedLine.size(); i++) {// �����̒[�_�̌��o
			MyPoint p1 = this.scannedLine.get(i).start;
			if (p1 != null) {
				this.scannedPoint.add(p1);
			}

			MyPoint p2 = this.scannedLine.get(i).end;
			if (p2 != null) {
				this.scannedPoint.add(p2);
			}
		}

		// ��肪����_�̔r��
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			this.scannedPoint = organizePoint(i, i + 1);// �d�����Ȃ���
		}
	}

	private ArrayList<MyPoint> organizePoint(int index1, int index2) {
		for (int i = index2; i < this.scannedPoint.size(); i++) {
			MyPoint p1 = this.scannedPoint.get(i);
			MyPoint p2 = this.scannedPoint.get(index1);

			if (p1.calcDistToPoint(p2) < 20) {
				for (int j = 0; j < this.scannedLine.size(); j++) {
					MyLine l = this.scannedLine.get(j);
					if (l.start == p1) {
						l.start = p2;
						p2.x = (p1.x + p2.x) / 2;
						p2.y = (p1.y + p2.y) / 2;
					} else if (l.end == p1) {
						l.end = p2;
						p2.x = (p1.x + p2.x) / 2;
						p2.y = (p1.y + p2.y) / 2;
					}
				}
				this.scannedPoint.remove(i);
				return organizePoint(index1, i);
			}
		}
		return this.scannedPoint;
	}

	public void scanLine(int num) {// �����̌��o
		for (int i = 0; i < num; i++) {
			MyLine l1 = this.houghTrans.getFieldLine();
			MyLine l2 = this.houghTrans.restoreLine(l1.theta, l1.rho);

			if ((l1.theta != 0 || l1.rho != 0) && l2.getLength() > 0) {
				this.scannedLine.add(l2);
			} else {
				break;
			}
		}
		this.scannedLine = organizeLine();
	}

	private ArrayList<MyLine> organizeLine() {
		for (int i = 0; i < this.scannedLine.size(); i++) {
			MyLine l1 = this.scannedLine.get(i);
			
			// ������̓_�ɋ߂��ꍇ�͒u������
			for (int j = i + 1; j < this.scannedLine.size(); j++) {
				MyLine l2 = this.scannedLine.get(j);

				MyPoint p = MyPoint.getIntersection1(l1, l2, 2);
				if (l1.start.isOnLine(l2)) {
					l1.start.x = p.x;
					l1.start.y = p.y;
				} else if (l1.end.isOnLine(l2)) {
					l1.end.x = p.x;
					l1.end.y = p.y;
				}
			}

			// �~��̓_�ɋ߂��ꍇ�͒u������
			for (int j = 0; j < this.scannedCircle.size(); j++) {
				MyCircle c = this.scannedCircle.get(j);

				MyPoint p1 = MyPoint.getIntersection2(l1, c, 1);
				MyPoint p2 = MyPoint.getIntersection2(l1, c, 2);
				MyPoint p3 = null;

				if (p1 != null && p2 == null) {
					p3 = p1;
				} else if (p1 == null && p2 != null) {
					p3 = p2;
				}

				if (l1.start.isOnCircle(c)) {
					if (p1 != null && p2 != null) {
						p3 = (p1.calcDistToPoint(l1.start) < p2.calcDistToPoint(l1.start)) ? p1 : p2;
					}
					l1.start.x = (p3 != null) ? p3.x : l1.start.x;
					l1.start.y = (p3 != null) ? p3.y : l1.start.y;
				} else if (l1.end.isOnCircle(c)) {
					if (p1 != null && p2 != null) {
						p3 = (p1.calcDistToPoint(l1.end) < p2.calcDistToPoint(l1.end)) ? p1 : p2;
					}
					l1.end.x = (p3 != null) ? p3.x : l1.end.x;
					l1.end.y = (p3 != null) ? p3.y : l1.end.y;
				}
			}
		}
		return this.scannedLine;
	}

	public void scanCircle(int num) {// �~�̌��o
		for (int i = 0; i < num; i++) {
			MyCircle c1 = this.houghTrans.getFieldCircle();
			MyCircle c2 = this.houghTrans.restoreCircle(c1.center, c1.radius);

			if (c1.center.x != 0 || c1.center.y != 0 || c1.radius != 0) {
				this.scannedCircle.add(c2);
			} else {
				break;
			}
		}
	}

	public void relateElement() {
		relateToPoint1();// �@�_���猩���֌W��(����)�̒�`
		modifyLine();// �A������1�̐����̒���(���[��1�Ȃ�ΐ������ƍ폜�A�Е���1�Ȃ��....)

		relateToPoint1();
		modifyPoint();// �B�s�v�ȓ_(�����̌�_)�̍폜

		relateToPoint1();
		relateToLine();// �C�������猩��(�_�Ƃ�)�֌W��
		relateToPoint2();// �D�_�Ɠ_�Ƃ̊֌W��
	}

	private void relateToPoint1() {// �@�_���猩���֌W��(����)�̒�`
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			MyPoint p = this.scannedPoint.get(i);
			p.relatedLine.clear();
			p.relatedCircle.clear();

			for (int j = 0; j < this.scannedLine.size(); j++) {// �����Ƃ̊֘A����`(�_���ʂ����)
				MyLine l = this.scannedLine.get(j);
				if (p.isOnLine(l)) {
					p.relatedLine.add(l);
				}
			}

			for (int j = 0; j < this.scannedCircle.size(); j++) {// �~�Ƃ̊֘A����`(�_���ʂ�~)
				MyCircle c = this.scannedCircle.get(j);
				if (p.isOnCircle(c)) {
					p.relatedCircle.add(c);
				}
			}
		}
	}

	private void modifyLine() {// �A������1�̐����̒���(���[��1�Ȃ�ΐ������ƍ폜�A�Е���1�Ȃ��....)
		modifyLine1();// ���[�̎�����1�̏ꍇ
		modifyLine2();// �Е��̎�����1�̏ꍇ
	}

	private void modifyLine1() {
		for (int i = this.scannedLine.size() - 1; i >= 0; i--) {
			MyLine l = this.scannedLine.get(i);
			boolean smallDegree1 = (l.start.relatedLine.size() == 1) && (l.start.relatedCircle.size() == 0);
			boolean smallDegree2 = (l.end.relatedLine.size() == 1) && (l.end.relatedCircle.size() == 0);

			if (smallDegree1 && smallDegree2) {// �C���������v�f : ���[������1�ł������(�Ɠ_)
				double[] minDist = new double[2];
				Arrays.fill(minDist, Double.MAX_VALUE);

				MyPoint[] pointArray = new MyPoint[2];
				Arrays.fill(pointArray, null);

				for (int j = 0; j < this.scannedPoint.size(); j++) {// �����񂹂��銴��
					MyPoint p = this.scannedPoint.get(j);
					if (p.calcDistToLine(l) < 10) {
						if (p != l.start && p.calcDistToPoint(l.start) < minDist[0]) {
							minDist[0] = p.calcDistToPoint(l.start);
							pointArray[0] = p;
						} else if (p != l.end && p.calcDistToPoint(l.end) < minDist[1]) {
							minDist[1] = p.calcDistToPoint(l.end);
							pointArray[1] = p;
						}
					}
				}

				boolean[] condition = new boolean[3];
				condition[0] = (pointArray[0] != null && pointArray[0] != l.end);
				condition[1] = (pointArray[1] != null && pointArray[1] != l.start);
				condition[2] = (pointArray[0] != pointArray[1]);

				if (condition[0] && condition[1] && condition[2]) {
					this.scannedPoint.remove(l.start);
					this.scannedPoint.remove(l.end);
					l.start = pointArray[0];
					l.end = pointArray[1];
				} else {
					this.scannedLine.remove(l);
					this.scannedPoint.remove(l.start);
					this.scannedPoint.remove(l.end);
				}
			}
		}
	}

	private void modifyLine2() {
		for (int i = this.scannedLine.size() - 1; i >= 0; i--) {
			MyLine l1 = this.scannedLine.get(i);
			boolean smallDegree1 = (l1.start.relatedLine.size() == 1) && (l1.start.relatedCircle.size() == 0);
			boolean smallDegree2 = (l1.end.relatedLine.size() == 1) && (l1.end.relatedCircle.size() == 0);

			int type = 0;
			if (smallDegree1 && !smallDegree2) {// ��ɓ_��ʂ�悤�Ȓ�����T���Ƃ悢
				type = 1;
			} else if (!smallDegree1 && smallDegree2) {
				type = 2;
			}

			if (type > 0) {
				boolean complete = false;
				MyPoint[] pointArray = new MyPoint[2];

				pointArray[0] = (type == 1) ? l1.start : l1.end;
				pointArray[1] = (type == 1) ? l1.end : l1.start;

				for (int j = 0; j < this.scannedLine.size(); j++) {// �����񂹂銴��
					MyLine l2 = this.scannedLine.get(j);
					if ((l1 != l2) && pointArray[0].calcDistToLine(l2) < 10) {
						double dist1 = pointArray[0].calcDistToPoint(l2.start);
						double dist2 = pointArray[0].calcDistToPoint(l2.end);
						if (dist1 < dist2) {
							// this.scannedPoint.remove(l2.start);
							l2.start = pointArray[0];
						} else {
							// this.scannedPoint.remove(l2.end);
							l2.end = pointArray[0];
						}
						complete = true;
					}
				}

				if (!complete) {
					double minDist = Double.MAX_VALUE;
					MyPoint p1 = null;

					for (int j = 0; j < this.scannedPoint.size(); j++) {// �����񂹂��銴��
						MyPoint p2 = this.scannedPoint.get(j);
						if (p2.calcDistToLine(l1) < 10) {
							if (p2 != pointArray[0] && p2.calcDistToPoint(pointArray[0]) < minDist) {
								minDist = p2.calcDistToPoint(pointArray[0]);
								p1 = p2;
							}
						}
					}

					if (p1 != null && p1 != pointArray[1]) {
						this.scannedPoint.remove(pointArray[0]);
						if (type == 1) {
							l1.start = p1;
						} else {
							l1.end = p1;
						}
					}
				}
			}
		}
	}

	private void modifyPoint() {
		for (int i = this.scannedPoint.size() - 1; i >= 0; i--) {
			MyPoint p = this.scannedPoint.get(i);

			int relatedTotal = p.relatedPoint.size() + p.relatedLine.size() + p.relatedCircle.size();
			if (relatedTotal <= 2 && !isEndPoint(p)) {
				this.scannedPoint.remove(p);
			}
		}
	}

	private boolean isEndPoint(MyPoint p) {// �_�������̒[�_���ǂ����̔���
		for (int i = 0; i < this.scannedLine.size(); i++) {
			MyLine l = this.scannedLine.get(i);
			if (p == l.start || p == l.end) {
				return true;
			}
		}
		return false;
	}

	private void relateToLine() {
		for (int i = 0; i < this.scannedLine.size(); i++) {// �_�Ƃ̊֘A����`(������̓_)
			MyLine l = this.scannedLine.get(i);
			for (int j = 0; j < this.scannedPoint.size(); j++) {
				MyPoint p = this.scannedPoint.get(j);
				if (p.isOnLine(l)) {
					l.relatedPoint.add(p);
				}
			}
		}
	}

	private void relateToPoint2() {
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			MyPoint p1 = this.scannedPoint.get(i);
			for (int j = 0; j < p1.relatedLine.size(); j++) {
				MyLine l = p1.relatedLine.get(j);
				for (int k = 0; k < l.relatedPoint.size(); k++) {
					if (p1 != l.relatedPoint.get(k)) {
						MyPoint p2 = l.relatedPoint.get(k);
						p1.relatedPoint.add(p2);
					}
				}
			}
		}
	}

	public void detectElement() {// circle��line��point�̏��ł�������ł�?
		detectCircle();
		detectLine();
		detectPoint();
		detectPolygon();
	}

	private void detectCircle() {
		for (int i = 0; i < this.scannedCircle.size(); i++) {
			this.detectedCircle.add(this.scannedCircle.get(i));
		}
	}

	private void detectLine() {
		for (int i = 0; i < this.scannedLine.size(); i++) {
			this.detectedLine.add(this.scannedLine.get(i));
		}
	}

	private void detectPoint() {
		for (int i = 0; i < this.scannedPoint.size(); i++) {
			this.detectedPoint.add(this.scannedPoint.get(i));
		}
	}

	private void detectPolygon() {
		for (int i = 3; i <= 6; i++) {
			PolygonAnalysis.generateCombination(this.detectedPoint, new int[i], 0, 1);// 3�p�`�̓_���,4�p�`�̓_���...

			for (int j = 0; j < PolygonAnalysis.combination.length; j++) {
				// �@�_���Z�b�g(n��1�Z�b�g)��1�쐬����B(��̌v�Z�ʂ����炷���߂ɏ����H�v����)
				int[] combination1 = { PolygonAnalysis.combination[j][0] };// �v�Z�ʂ����炷���߂ɁA�ŏ���1�����o��
				int[] combination2 = new int[i - 1];
				for (int k = 0; k < PolygonAnalysis.combination[j].length - 1; k++) {
					combination2[k] = PolygonAnalysis.combination[j][k + 1];
				}

				// �A�쐬����1�̓_���Z�b�g�ɑ΂��āA(n-1)!�ʂ�̏����񋓂���B
				// int c = (myPoly2.cirPermutation(r)).intValue();// = (n-1)!
				int[][] permutation = new int[(PolygonAnalysis.calcFactorial(i - 1))][i];// candidate1�������������̒����ɂ�����
				PolygonAnalysis.generatePermutation(combination2, new int[0]);
				for (int k = 0; k < permutation.length; k++) {
					permutation[k][0] = combination1[0];
					for (int l = 1; l < permutation[k].length; l++) {
						permutation[k][l] = PolygonAnalysis.permutation[k][l - 1];
					}
				}

				// �B�񋓂����������]����悤�Ɍ�������
				for (int k = 0; k < permutation.length; k++) {
					if (PolygonAnalysis.isPolygon(this.detectedPoint, permutation[k])) {
						MyPoint[] vertex = new MyPoint[permutation[k].length];
						for (int l = 0; l < permutation[k].length; l++) {
							vertex[l] = this.detectedPoint.get(permutation[k][l]);
						}

						MyPolygon pg = new MyPolygon(vertex);
						if (pg.hasFeature()) {
							this.detectedPolygon.add(pg);
							break;
						}
					}
				}
			}
		}
	}

	private static class PolygonAnalysis {

		/** n�p�`���ƂȂ�_�̑g�ݍ��킹 */
		static int[][] combination;// ��

		/** n�p�`���ƂȂ�_�̏��� */
		static int[][] permutation;// ��

		static int index;

		/**
		 * �g�ݍ��킹��񋓂���֐�
		 * 
		 * @param n
		 * @param r
		 * @param start
		 * @param plus
		 */
		static void generateCombination(ArrayList<MyPoint> pointList, int[] r, int start, int plus) {// r�����o��
			if (plus <= 1) {
				combination = new int[calcCombination(pointList.size(), r.length)][r.length];
				for (int i = 0; i < combination.length; i++) {
					for (int j = 0; j < combination[i].length; j++) {
						combination[i][j] = -1;
					}
				}
				index = 0;
			}

			if (r.length < plus) {
				for (int i = 0; i < r.length; i++) {
					combination[index][i] = r[i];// minReSt[s] = n.get(r[s]).getIndex(n);
				}
				index++;

				return;
			}
			for (int i = start; i < pointList.size() - r.length + plus; i++) {
				r[plus - 1] = i;
				generateCombination(pointList, r, i + 1, plus + 1);
			}
		}

		static void generatePermutation(int[] n, int[] array) {// n�̊K�悾�����������
			if (array.length == 0) {
				permutation = new int[(calcFactorial(n.length))][n.length];
				for (int i = 0; i < permutation.length; i++) {
					for (int j = 0; j < permutation[i].length; j++) {
						permutation[i][j] = -1;
					}
				}
				index = 0;
			}

			if (n.length == 0) {// �S�ďI�������_�ň�C�ɓ����
				for (int i = 0; i < array.length; i++) {
					permutation[index][i] = array[i];
				}
				index++;
			} else {
				for (int i = 0; i < n.length; i++) {
					int[] value = { n[i] };// value
					generatePermutation(concatArray(extractArray(n, 0, i), extractArray(n, i + 1)),
							concatArray(array, value));
				}
			}
		}

		static int[] concatArray(int[] array1, int[] array2) {
			int[] newArray = new int[array1.length + array2.length];
			for (int i = 0; i < array1.length; i++) {
				newArray[i] = array1[i];
			}
			for (int i = 0; i < array2.length; i++) {
				newArray[i + array1.length] = array2[i];
			}
			return newArray;
		}

		static int[] extractArray(int[] array, int... index) {
			int start = index[0];
			int end = (index.length > 1) ? index[1] : array.length;

			int[] newArray = new int[Math.abs(end - start)];
			for (int i = start; i < end; i++) {
				newArray[i - start] = array[i];
			}

			return newArray;
		}

		/**
		 * n�̊K����v�Z����
		 * 
		 * @param n
		 * @return
		 */
		static int calcFactorial(int num) {
			if (num <= 1) {
				return 1;
			}
			return num * calcFactorial(num - 1);
		}

		/**
		 * n�̒�����r�����o���g�ݍ��킹���v�Z����
		 * 
		 * @param n
		 * @param r
		 * @return
		 */
		static int calcCombination(int n, int r) {
			if (n < r) {
				return 0;
			} else if ((n < 0) && (r < 0)) {
				return 0;
			}

			if (r == 0 || r == n) {
				return 1;
			} else if (r == 1 || r == n - 1) {
				return n;
			}

			return calcCombination(n - 1, r - 1) + calcCombination(n - 1, r);
		}

		static boolean isPolygon(ArrayList<MyPoint> pointList, int[] pattern) {
			boolean[] condition = new boolean[4];

			condition[0] = isCycle(pointList, pattern);// �~����łȂ����Ă��邩
			condition[1] = isConvex(pointList, pattern);// �ʐ��𖞂�����
			condition[2] = notSelfCross(pointList, pattern);// ���Ȍ����͂Ȃ���
			condition[3] = notCollinear(pointList, pattern);// 1�������Ȃ�3�_�͂Ȃ���

			return (condition[0] && condition[1] && condition[2] && condition[3]);
		}

		static boolean isCycle(ArrayList<MyPoint> pointList, int[] pattern) {
			int pairNum = 2;// pattern 0 2 3 5 �� [0, 2][2, 3][3, 5][5, 0]
			int[][] pair = generatePair(pattern, pairNum);

			int count = 0;
			for (int i = 0; i < pair.length; i++) {
				MyPoint p1 = pointList.get(pair[i][0]);
				for (int j = 0; j < p1.relatedPoint.size(); j++) {
					MyPoint p2 = pointList.get(pair[i][1]);
					if (p2 == p1.relatedPoint.get(j)) {
						count++;
						break;
					}
				}

				if (count == pattern.length) {
					return true;
				}
			}

			return false;
		}

		static boolean isConvex(ArrayList<MyPoint> pointList, int[] pattern) {
			int pairNum = 3;// pattern 0 2 3 5 �� [0, 2, 3][2, 3, 5][3, 5, 0][5, 0, 2]
			int[][] pair = generatePair(pattern, pairNum);

			int count = 0;
			double[] crossProduct = new double[2];
			for (int i = 0; i < pair.length; i++) {
				MyPoint[] pointArray1 = new MyPoint[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					pointArray1[j] = pointList.get(pair[i][j]);
				}

				MyPoint[] pointArray2 = new MyPoint[pairNum];
				int nextIndex = ((i < pair.length - 1) ? i + 1 : 0);
				for (int j = 0; j < pair[nextIndex].length; j++) {
					pointArray2[j] = pointList.get(pair[nextIndex][j]);
				}

				crossProduct[0] = (pointArray1[2].x - pointArray1[1].x) * (pointArray1[0].y - pointArray1[1].y)
						- (pointArray1[2].y - pointArray1[1].y) * (pointArray1[0].x - pointArray1[1].x);
				crossProduct[1] = (pointArray2[2].x - pointArray2[1].x) * (pointArray2[0].y - pointArray2[1].y)
						- (pointArray2[2].y - pointArray2[1].y) * (pointArray2[0].x - pointArray2[1].x);

				if (crossProduct[0] * crossProduct[1] > 0) {
					count++;
				}
			}

			return (count == pattern.length);
		}

		static boolean notSelfCross(ArrayList<MyPoint> pointList, int[] pattern) {// ���Ȍ������Ȃ���
			MyPoint centroid = new MyPoint(0, 0);
			for (int i = 0; i < pattern.length; i++) {
				centroid.x += pointList.get(pattern[i]).x;
				centroid.y += pointList.get(pattern[i]).y;
			}
			centroid.x /= pattern.length;
			centroid.y /= pattern.length;// �����炭�͖��Ȃ��v�Z�ł��Ă���

			int crossNum = countCrossNum(pointList, pattern, centroid);
			int windNum = countWindNum(pointList, pattern, centroid);

			return (crossNum % 2 == 1 && windNum != 0);
		}

		static boolean notCollinear(ArrayList<MyPoint> pointList, int[] pattern) {
			int pairNum = 3;
			int[][] pair = generatePair(pattern, pairNum);

			int count = 0;
			for (int i = 0; i < pair.length; i++) {
				MyPoint[] pointArray = new MyPoint[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					pointArray[j] = pointList.get(pair[i][j]);
				}

				MyLine[] lineArray = new MyLine[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					lineArray[j] = new MyLine(pointArray[j], pointArray[(j < pair[i].length - 1) ? (j + 1) : 0]);
				}

				double[] dist = new double[pairNum];
				for (int j = 0; j < pair[i].length; j++) {
					dist[j] = pointArray[j].calcDistToLine(lineArray[(j < pair[i].length - 1) ? (j + 1) : 0]);
				}

				int threshold = 10;
				if ((dist[0] > threshold) && (dist[1] > threshold) && (dist[2] > threshold)) {
					count++;
				}
			}

			return (count == pattern.length);
		}

		static int[][] generatePair(int[] pattern, int pairNum) {
			int[][] pair = new int[pattern.length][pairNum];

			int[][] index = new int[pattern.length][pairNum];
			for (int i = 0; i < pairNum; i++) {
				for (int j = 0; j < pair.length; j++) {
					int x = (i == 0) ? j : (index[j][i - 1] + 1);
					index[j][i] = (x < pair.length) ? x : 0;
					pair[j][i] = pattern[index[j][i]];
				}
			}

			return pair;
		}

		static int countCrossNum(ArrayList<MyPoint> pointList, int[] pattern, MyPoint p) {
			int crossNum = 0;

			for (int i = 0; i < pattern.length; i++) {
				int index1 = pattern[i];
				int index2 = pattern[(i < pattern.length - 1) ? i + 1 : 0];
				MyPoint p1 = pointList.get(index1);
				MyPoint p2 = pointList.get(index2);

				if (((p1.y <= p.y) && (p2.y > p.y)) || ((p1.y > p.y) && (p2.y <= p.y))) {
					double t = (p.y - p1.y) / (p2.y - p1.y);
					if (p.x < (p1.x + (t * (p2.x - p1.x)))) {
						crossNum++;
					}
				}
			}

			return crossNum;
		}

		static int countWindNum(ArrayList<MyPoint> pointList, int[] pattern, MyPoint p) {
			int windNum = 0;

			for (int i = 0; i < pattern.length; i++) {
				int index1 = pattern[i];
				int index2 = pattern[(i < pattern.length - 1) ? i + 1 : 0];
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
	}
}

