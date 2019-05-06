package wasan.problems;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

//��
/**
 * �a�Z�}�`���̉摜�𕪐͂���ۂɗ��p����摜�����Ɋւ���N���X�ł��B<br>
 * �}�`���摜�̓�l����N���[�W���O�����ȂǁA�摜�����̃��\�b�h�͎�ɂ��̃N���X���痘�p���܂��B
 * 
 * @author Takuma Tsuchihashi
 */
public class ImageProcessing {

	// ���ȉ��A���͂Ƃ���}�`���̉摜�t�@�C�����Ɋւ���String�^�ϐ��ł��B
	/**
	 * ���͂Ƃ���}�`���̉摜�t�@�C������\���܂��B<br>
	 * �摜�t�@�C������"001.PNG","002.PNG",...�̂悤�ɐݒ肷����̂Ƃ��܂��B
	 */
	public String imgName;
	/**
	 * ���͂Ƃ���}�`���̉摜�t�@�C�����Ɋ܂܂��ԍ���\���܂��B<br>
	 * �ԍ���"001","002",...�̂悤�Ɏ擾����܂��B
	 */
	public String imgNum;

	// ���ȉ��A�摜�����ň�����摜��\��BufferedImage�^�ϐ��ł��B
	/**
	 * ���͂Ƃ���}�`���̌��摜��\���܂��B<br>
	 */
	public BufferedImage originalImg;
	/**
	 * �}�`���Ɋ܂܂��􉽗v�f�A�}�`�v�f�̒��o�ƕ��s���ď��������摜��\���܂��B<br>
	 */
	public BufferedImage editingImg;
	/**
	 * �}�`���Ɋ܂܂��􉽗v�f�A�}�`�v�f�𒊏o�����摜��\���܂��B<br>
	 */
	public BufferedImage elementImg;
	/**
	 * �}�`���Ɋ܂܂�镶���v�f�𒊏o�����摜��\���܂��B<br>
	 */
	public BufferedImage characterImg;

	// ��
	/**
	 * �}�`���̉摜�t�@�C���p�X���w�肵�A�摜����(ImageProcessing)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param filePath
	 *            ���͂Ƃ���}�`���̉摜�t�@�C���p�X��\��String�^�ϐ�
	 */
	public ImageProcessing(String filePath) {
		imgName = filePath.split("\\\\", 0)[3];
		imgNum = imgName.substring(0, imgName.indexOf("."));

		System.out.println("< �}�`��� >");
		System.out.println(filePath);
		System.out.println();

		originalImg = loadImage(filePath);// ���͂Ƃ���}�`���̉摜�t�@�C���p�X����摜��ǂݍ��݂܂��B
		originalImg = resizeImage(500);// �ǂݍ��񂾐}�`���̌��摜�����T�C�Y���܂��B

		// �ȉ��A�}�`���ւ̎����^�O�t���O�ɉ摜�ɑ΂��鎖�O�������s���܂��B
		editingImg = preprocessImage(true);
		elementImg = preprocessImage(false);
		characterImg = preprocessImage(false);
	}

	// ��
	/**
	 * �����^�O�t�������������}�`���Ɋւ���s�v�ȃI�u�W�F�N�g�����������������܂��B<br>
	 * �����̐}�`���ɑ΂��ĘA���I�Ɏ����^�O�t�����s���ہAOutOfMemoryError��������܂��B
	 */
	public void freeResource() {
		this.originalImg.flush();
		this.editingImg.flush();
		this.elementImg.flush();
		this.characterImg.flush();
	}

	// ��
	/**
	 * ���͂Ƃ���}�`���̉摜�t�@�C���p�X����摜��ǂݍ��݂܂��B<br>
	 * 
	 * @param filePath
	 *            ���͂Ƃ���}�`���̉摜�t�@�C���p�X��\��String�^�ϐ�
	 * @return ���͂Ƃ���}�`���̌��摜��\��BufferedImage�^�ϐ�
	 */
	private BufferedImage loadImage(String filePath) {
		try {
			BufferedImage outputImg = ImageIO.read(new File(filePath));
			return outputImg;
		} catch (IOException e) {
			showException(e);
			return null;
		}
	}

	// ��
	/**
	 * �ǂݍ��񂾐}�`���̌��摜�����T�C�Y���܂��B<br>
	 * ���摜�̏c���Ɖ����̂����A����������Ƀ��T�C�Y���s���܂��B
	 * 
	 * @param size
	 *            ���T�C�Y��̉摜�̏c���܂��͉�����\��int�^�ϐ�
	 * @return ���T�C�Y���ꂽ�}�`���̉摜��\��BufferedImage�^�ϐ�
	 */
	private BufferedImage resizeImage(int size) {
		BufferedImage inputImg = originalImg;

		int imgWidth = inputImg.getWidth();
		int imgHeight = inputImg.getHeight();

		int newWidth, newHeight;
		if (imgWidth >= imgHeight) {
			newWidth = size;
			newHeight = newWidth * imgHeight / imgWidth;
		} else {
			newHeight = size;
			newWidth = newHeight * imgWidth / imgHeight;
		}

		BufferedImage outputImg = new BufferedImage(newWidth, newHeight, inputImg.getType());

		AffineTransform at = AffineTransform.getScaleInstance((double) newWidth / imgWidth,
				(double) newHeight / imgHeight);
		AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		ato.filter(inputImg, outputImg);

		return outputImg;
	}

	// ��
	/**
	 * �}�`���ւ̎����^�O�t���O�ɉ摜�ɑ΂��鎖�O�������s���܂��B<br>
	 * �����ōs���鏈���̓O���[�X�P�[�����E��l���E�N���[�W���O������3�ł��B
	 * 
	 * @param closing
	 *            �N���[�W���O���������s���邩�ǂ��������肷��boolean�^�ϐ�
	 * @return ���O�������{�����摜��\��BufferedImage�^�ϐ�
	 */
	private BufferedImage preprocessImage(boolean closing) {
		BufferedImage outputImg = originalImg;

		outputImg = grayscaleImage(outputImg);// �}�`���̉摜�ɑ΂���O���[�X�P�[�������s���܂��B
		outputImg = binarizeImage(outputImg);// �}�`���̉摜�ɑ΂����l�����s���܂��B
		outputImg = (closing) ? closeImage(outputImg) : outputImg;// �}�`���̉摜�ɑ΂���N���[�W���O�������s���܂��B(closing��true�����s����Afalse�����s���Ȃ�)

		return outputImg;
	}

	// ��
	/**
	 * �}�`���̉摜�ɑ΂���O���[�X�P�[�������s���܂��B<br>
	 * 
	 * @param inputImg
	 *            �O���[�X�P�[�����������摜��\��BufferedImage�^�ϐ�
	 * @return �O���[�X�P�[�������ꂽ�摜��\��BufferedImage�^�ϐ�
	 */
	private BufferedImage grayscaleImage(BufferedImage inputImg) {
		BufferedImage outputImg = new BufferedImage(inputImg.getWidth(), inputImg.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		for (int y = 0; y < outputImg.getHeight(); y++) {
			for (int x = 0; x < outputImg.getWidth(); x++) {
				outputImg.setRGB(x, y, inputImg.getRGB(x, y));
			}
		}

		return outputImg;
	}

	// ��
	/**
	 * �}�`���̉摜�ɑ΂����l�����s���܂��B<br>
	 * 
	 * @param inputImg
	 *            ��l���������摜��\��BufferedImage�^�ϐ�
	 * @return ��l�����ꂽ�摜��\��BufferedImage�^�ϐ�
	 */
	private BufferedImage binarizeImage(BufferedImage inputImg) {
		BufferedImage outputImg = new BufferedImage(inputImg.getWidth(), inputImg.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				int color = ((inputImg.getRGB(x, y) & 0xFF) > 128) ? 255 : 0;
				outputImg.setRGB(x, y, intRGB(color));
			}
		}

		return outputImg;
	}

	// ��
	/**
	 * �}�`���̉摜�ɑ΂���N���[�W���O�������s���܂��B<br>
	 * �N���[�W���O�����́AdilateImage��erodeImage�𕹗p���܂��B
	 * 
	 * @param inputImg
	 *            �N���[�W���O���������s����摜��\��BufferedImage�^�ϐ�
	 * @return �N���[�W���O�������{�����摜��\��BufferedImage�^�ϐ�
	 */
	public BufferedImage closeImage(BufferedImage inputImg) {
		BufferedImage outputImg = inputImg;
		
		// https://algorithm.joho.info/image-processing/dilation-erosion-opening-closing-tophat-blackhat/
		int kernelSize = 5;
		int iteration = 2;
		outputImg = dilateImage(outputImg, kernelSize, iteration);// �}�`���̉摜�ɑ΂���N���[�W���O�������s���܂��B
		outputImg = erodeImage(outputImg, kernelSize, iteration);// �}�`���̉摜�̃N���[�W���O�����ɂ�������k���������s���܂��B

		return outputImg;
	}

	// ��
	/**
	 * �}�`���̉摜�̃N���[�W���O�����ɂ�����c�����������s���܂��B<br>
	 * �N���[�W���O�����ł́A���̃��\�b�h��erodeImage�𕹗p���܂��B
	 * 
	 * @param inputImg
	 *            �c�����������s����摜��\��BufferedImage�^�ϐ�
	 * @param kernelSize
	 *            �c�������ɂ����Ē��ڂ����f�͈�(�����s��)�̃T�C�Y��\��int�^�ϐ�
	 * @param iteration
	 *            �c�������̌J��Ԃ��񐔂�\��int�^�ϐ�
	 * @return �c���������{�����摜��\��BufferedImage�^�ϐ�
	 */
	public BufferedImage dilateImage(BufferedImage inputImg, int kernelSize, int iteration) {
		BufferedImage outputImg = new BufferedImage(inputImg.getWidth(), inputImg.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		int d = (int) ((kernelSize - 1) / 2);

		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				if (x < d || x >= inputImg.getWidth() - d) {
					outputImg.setRGB(x, y, intRGB(255));
				} else if (y < d || y >= inputImg.getHeight() - d) {
					outputImg.setRGB(x, y, intRGB(255));
				} else {
					int blackCount = 0;
					for (int dy = -d; dy <= d; dy++) {
						for (int dx = -d; dx <= d; dx++) {
							if ((inputImg.getRGB(x + dx, y + dy) & 0xFF) == 0) {
								blackCount++;
							}
						}
					}

					outputImg.setRGB(x, y, (blackCount > 0) ? intRGB(0) : intRGB(255));
				}
			}
		}

		return (iteration > 1) ? dilateImage(outputImg, kernelSize, iteration - 1) : outputImg;
	}

	// ��
	/**
	 * �}�`���̉摜�̃N���[�W���O�����ɂ�������k���������s���܂��B<br>
	 * �N���[�W���O�����ł́A���̃��\�b�h��dilateImage�𕹗p���܂��B
	 * 
	 * @param inputImg
	 *            ���k���������s����摜��\��BufferedImage�^�ϐ�
	 * @param kernelSize
	 *            ���k�����ɂ����Ē��ڂ����f�͈�(�����s��)�̃T�C�Y��\��int�^�ϐ�
	 * @param iteration
	 *            ���k�����̌J��Ԃ��񐔂�\��int�^�ϐ�
	 * @return ���k�������{�����摜��\��BufferedImage�^�ϐ�
	 */
	public BufferedImage erodeImage(BufferedImage inputImg, int kernelSize, int iteration) {
		BufferedImage outputImg = new BufferedImage(inputImg.getWidth(), inputImg.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		int d = (int) ((kernelSize - 1) / 2);

		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				if (x < d || x >= inputImg.getWidth() - d) {
					outputImg.setRGB(x, y, intRGB(255));
				} else if (y < d || y >= inputImg.getHeight() - d) {
					outputImg.setRGB(x, y, intRGB(255));
				} else {
					int whiteCount = 0;
					for (int dy = -d; dy <= d; dy++) {
						for (int dx = -d; dx <= d; dx++) {
							if ((inputImg.getRGB(x + dx, y + dy) & 0xFF) == 255) {
								whiteCount++;
							}
						}
					}

					outputImg.setRGB(x, y, (whiteCount > 0) ? intRGB(255) : intRGB(0));
				}
			}
		}

		return (iteration > 1) ? erodeImage(outputImg, kernelSize, iteration - 1) : outputImg;
	}

	// ��
	/**
	 * �}�`���̉摜���璊�o���ꂽ�������������܂��B<br>
	 * ���̏����͑��̊􉽗v�f�̌댟�o���y�����邱�Ƃ�ړI�Ƃ��Ă��܂��B
	 * 
	 * @param lineList
	 *            ���o���ꂽ������ێ�����MyLine�N���X���X�g
	 * @return ���o���ꂽ���������������}�`���̉摜��\��BufferedImage�^�ϐ�
	 */
	public BufferedImage removeLine(ArrayList<MyLine> lineList) {// �����v�f�̏���
		for (int y = 0; y < editingImg.getHeight(); y++) {
			for (int x = 0; x < editingImg.getWidth(); x++) {
				if ((editingImg.getRGB(x, y) & 0xFF) == 0) {
					for (int i = 0; i < lineList.size(); i++) {
						if (lineList.get(i).calcDistToPoint(new MyPoint(x, y)) < 3) {
							editingImg.setRGB(x, y, intRGB(255));
						}
					}
				}
			}
		}

		return editingImg;
	}

	// ��
	/**
	 * �}�`���̉摜���璊�o�����~���������܂��B<br>
	 * ���̏����͑��̊􉽗v�f�̌댟�o���y�����邱�Ƃ�ړI�Ƃ��Ă��܂��B
	 * 
	 * @param circleList
	 *            ���o���ꂽ�~��ێ�����MyCircle�N���X���X�g
	 * @return ���o���ꂽ�~�����������}�`���̉摜��\��BufferedImage�^�ϐ�
	 */
	public BufferedImage removeCircle(ArrayList<MyCircle> circleList) {// �~�v�f�̏���
		for (int y = 0; y < editingImg.getHeight(); y++) {
			for (int x = 0; x < editingImg.getWidth(); x++) {
				if ((editingImg.getRGB(x, y) & 0xFF) == 0) {
					for (int i = 0; i < circleList.size(); i++) {
						if (circleList.get(i).calcDistToPoint(new MyPoint(x, y)) < 3) {
							editingImg.setRGB(x, y, intRGB(255));
						}
					}
				}
			}
		}

		return editingImg;
	}

	// ��
	/**
	 * �}�`���Ɋ܂܂��􉽗v�f�𒊏o�����摜����ѕ����v�f�𒊏o�����摜�𐶐����܂��B<br>
	 * ������2�̉摜�̘a�͐}�`���̌��摜�ɓ������ł��B
	 * 
	 * @param lineList
	 *            ���o���ꂽ������ێ�����MyLine�N���X���X�g
	 * @param circleList
	 *            ���o���ꂽ�~��ێ�����MyCircle�N���X���X�g
	 * @return �􉽗v�f�𒊏o�����摜����ѕ����v�f�𒊏o�����摜��2�̉摜��ێ�����BufferedImage�^�z��
	 */
	public BufferedImage[] clipImage(ArrayList<MyLine> lineList, ArrayList<MyCircle> circleList) {
		BufferedImage[] imgArray = new BufferedImage[2];

		for (int y = 0; y < characterImg.getHeight(); y++) {
			for (int x = 0; x < characterImg.getWidth(); x++) {
				if ((characterImg.getRGB(x, y) & 0xFF) == 0) {
					for (int i = 0; i < circleList.size(); i++) {
						if (circleList.get(i).calcDistToPoint(new MyPoint(x, y)) < 6) {
							characterImg.setRGB(x, y, intRGB(255));
						}
					}
				}
			}
		}

		for (int y = 0; y < characterImg.getHeight(); y++) {
			for (int x = 0; x < characterImg.getWidth(); x++) {
				if ((characterImg.getRGB(x, y) & 0xFF) == 0) {
					for (int i = 0; i < lineList.size(); i++) {
						if (lineList.get(i).calcDistToPoint(new MyPoint(x, y)) < 6) {
							characterImg.setRGB(x, y, intRGB(255));
						}
					}
				}
			}
		}

		for (int y = 0; y < elementImg.getHeight(); y++) {
			for (int x = 0; x < elementImg.getWidth(); x++) {
				if ((characterImg.getRGB(x, y) & 0xFF) == 0) {
					elementImg.setRGB(x, y, intRGB(255));
				}
			}
		}

		imgArray[0] = characterImg;
		imgArray[1] = elementImg;

		return imgArray;
	}

	// ��
	/**
	 * �}�`���Ɋ܂܂�镶���v�f�𒊏o�����摜�Ƀ��x�����O���s���A�����v�f��؂�o���܂��B<br>
	 * 
	 * @param inputImg
	 *            �����v�f�𒊏o�����摜��\��BufferedImage�^�ϐ�
	 * @return �؂�o���������v�f�̉摜��ێ�����BufferedImage�^���X�g
	 */
	public ArrayList<BufferedImage> labelImage(BufferedImage inputImg) {
		ArrayList<BufferedImage> characterList = new ArrayList<BufferedImage>();

		// ���x�����O
		int label = 0;
		int[][] labelNum = new int[inputImg.getWidth()][inputImg.getHeight()];

		boolean[][] blackCheck = new boolean[inputImg.getWidth()][inputImg.getHeight()];
		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				blackCheck[x][y] = ((inputImg.getRGB(x, y) & 0xFF) == 0);
			}
		}

		int[] LUT = new int[1000];

		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				if (y == 0) {
					if (x == 0) {
						labelNum[x][y] = label;
					} else {
						if (blackCheck[x][y]) { // �^�[�Q�b�g�F�̎�
							if (!blackCheck[x - 1][y]) { // ���ׂ肪���̎�
								label++;
								labelNum[x][y] = label; // �V�������x����t����
							} else {
								labelNum[x][y] = labelNum[x - 1][y]; // ���x���̃R�s�[
							}
						}
					}
				} else {
					if (blackCheck[x][y]) { // ���̎�
						if (!blackCheck[x - 1][y] && !blackCheck[x - 1][y - 1] && !blackCheck[x][y - 1]
								&& !blackCheck[x + 1][y - 1]) {// �����Ȃ����
							label++;
							labelNum[x][y] = label;
							LUT[label] = label;
						} else {
							boolean labelSet = false; // ��荞�ނ悤�Ɋm�F���邱�Ƃōŏ��l�ɏ���������
							if (blackCheck[x - 1][y]) {
								labelNum[x][y] = labelNum[x - 1][y];
								labelSet = true;
							}

							for (int dx = -1; dx <= 1; dx++) {
								int mx = x + dx;
								int my = y - 1;
								if (blackCheck[mx][my]) {
									if (!labelSet) {
										labelNum[x][y] = labelNum[mx][my]; // ����
										labelSet = true;
									} else {
										int minLabel = Math.min(labelNum[x][y], labelNum[mx][my]);
										int maxLabel = Math.max(labelNum[x][y], labelNum[mx][my]);
										labelNum[x][y] = minLabel;
										LUT[maxLabel] = minLabel;
									}
								}
							}
						}
					}
				}
			}
		}

		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				if (blackCheck[x][y]) {
					labelNum[x][y] = LUT[labelNum[x][y]];
				}
			}
		}

		// �ʒu�̓���
		int[][] labelEdge = new int[label + 1][4];
		boolean[] edgeCheck = new boolean[label + 1];
		for (int y = 0; y < inputImg.getHeight(); y++) {
			for (int x = 0; x < inputImg.getWidth(); x++) {
				if (blackCheck[x][y]) {// labelNum[x][y] > 0
					int l = labelNum[x][y];
					if (!edgeCheck[l]) {
						edgeCheck[l] = true;
						labelEdge[l][0] = x;
						labelEdge[l][1] = x;
						labelEdge[l][2] = y;
						labelEdge[l][3] = y;
					} else {
						labelEdge[l][0] = Math.min(x, labelEdge[l][0]);
						labelEdge[l][1] = Math.max(x, labelEdge[l][1]);
						labelEdge[l][2] = Math.min(y, labelEdge[l][2]);
						labelEdge[l][3] = Math.max(y, labelEdge[l][3]);
					}
				}
			}
		}

		int margin = 5;
		int minSize = 30;
		characterList.clear();

		for (int i = 1; i <= label; i++) {
			int centerX = Math.abs(labelEdge[i][0] + labelEdge[i][1]) / 2;
			int centerY = Math.abs(labelEdge[i][2] + labelEdge[i][3]) / 2;

			int halfWidth = Math.abs(labelEdge[i][0] - labelEdge[i][1]) / 2;
			int halfHeight = Math.abs(labelEdge[i][2] - labelEdge[i][3]) / 2;

			int rectX = (centerX - halfWidth) - margin;// label_edge[i][0] -
														// margin;
			int rectY = (centerY - halfHeight) - margin;// label_edge[i][2] -
														// margin;

			int rectWidth = 2 * (halfWidth + margin);
			int rectHeight = 2 * (halfHeight + margin);

			if (rectWidth >= minSize && rectHeight >= minSize) {
				int rectSize = Math.max(rectWidth, rectHeight);
				int gapSize = Math.abs(rectWidth - rectHeight);
				if (rectWidth > rectHeight) {
					rectY -= gapSize / 2;
				} else {
					rectX -= gapSize / 2;
				}

				// �؂���ʒu�̒�������

				boolean inLeft = (0 <= rectX);
				boolean inRight = ((rectX + rectSize) < characterImg.getWidth());
				boolean inTop = (0 <= rectY);
				boolean inBottom = ((rectY + rectSize) < characterImg.getHeight());

				if (!(inLeft && inRight) || !(inTop && inBottom)) {
					int d = 0;
					if (!inLeft) {
						d = Math.abs(centerX);
					} else if (!inRight) {
						d = Math.abs(centerX - characterImg.getWidth());
					} else if (!inTop) {
						d = Math.abs(centerY);
					} else if (!inBottom) {
						d = Math.abs(centerY - characterImg.getHeight());
					}

					rectX = centerX - d;
					rectY = centerY - d;
					rectSize = 2 * d;
				}

				try {
					BufferedImage character = characterImg.getSubimage(rectX, rectY, rectSize, rectSize);
					// �s�N�Z�����ɂ��Ċm�F
					int blackCount = 0;
					for (int y = 0; y < character.getHeight(); y++) {
						for (int x = 0; x < character.getWidth(); x++) {
							if ((character.getRGB(x, y) & 0xFF) < 10) {
								blackCount++;
							}
						}
					}

					if (blackCount > 0.03 * (character.getWidth() * character.getHeight())) {
						characterList.add(character);
					}
				} catch (Exception e) {
					showException(e);
				}
			}
		}

		return characterList;
	}

	// ��
	/**
	 * �}�`��肩��؂�o���������v�f����]�������摜�𐶐����܂��B<br>
	 * �����v�f1�ɑ΂��A��]�Ő��������摜�̖����͉�]�p�x�ɂ�茈�肵�܂��B
	 * 
	 * @param characterList
	 *            �؂�o���������v�f�̉摜��ێ�����BufferedImage�^���X�g
	 * @return �����v�f����]�������摜��ێ�����BufferedImage�^�z��
	 */
	public BufferedImage[][] rotateImage(ArrayList<BufferedImage> characterList) {
		int twoPI = 360;
		int radian = 45;
		int rotateNum = twoPI / radian;

		BufferedImage[][] characterRotation = new BufferedImage[characterList.size()][rotateNum];

		for (int i = 0; i < characterList.size(); i++) {
			String imgNumPath = "dat/output/image/character_rotate/" + imgNum;
			File imgNumFolder = new File(imgNumPath);
			if (!imgNumFolder.exists()) {
				imgNumFolder.mkdir();
			}

			String characterNumPath = imgNumPath + "/" + i;
			File characterNumFolder = new File(characterNumPath);
			if (characterNumFolder.exists()) {
				File[] characterFile = characterNumFolder.listFiles();
				for (int k = 0; k < characterFile.length; k++) {
					characterFile[k].delete();
				}
			} else {
				characterNumFolder.mkdir();
			}

			for (int j = 0; j < rotateNum; j++) {
				int characterWidth = characterList.get(i).getWidth();
				int characterHeight = characterList.get(i).getHeight();
				BufferedImage character = new BufferedImage(characterWidth, characterHeight,
						BufferedImage.TYPE_BYTE_GRAY);
				for (int y = 0; y < character.getHeight(); y++) {
					for (int x = 0; x < character.getWidth(); x++) {
						character.setRGB(x, y, intRGB(255));
					}
				}

				AffineTransform at = new AffineTransform();

				double angle = Math.toRadians(radian * j);

				// ��]��̒��S���W(cx, cy)
				double cx = (characterWidth * Math.cos(angle) - characterHeight * Math.sin(angle)) / 2.0;
				double cy = (characterWidth * Math.sin(angle) + characterHeight * Math.cos(angle)) / 2.0;
				// �^�񒆂Ɉ����߂��̂ɕK�v�Ȉړ���(dx, dy)
				double dx = characterWidth / 2.0 - cx;
				double dy = characterHeight / 2.0 - cy;

				at.setToTranslation(dx, dy);
				at.rotate(angle);

				AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
				ato.filter(characterList.get(i), character);

				try {
					characterRotation[i][j] = character;

					String fileName = j + ".png";
					ImageIO.write(character, "png", new File(characterNumFolder.getPath() + "/" + fileName));
				} catch (IOException e) {
					showException(e);
				}
			}
		}

		return characterRotation;
	}

	/**
	 * 
	 * @param color
	 * @return
	 */
	public int intRGB(int... color) {
		if (color.length == 1) {
			return 0xff000000 | color[0] << 16 | color[0] << 8 | color[0];
		} else if (color.length == 3) {
			return 0xff000000 | color[0] << 16 | color[1] << 8 | color[2];
		}
		return 0xff000000 | 128 << 16 | 128 << 8 | 128;
	}

	/**
	 * 
	 * @param e
	 */
	private void showException(Exception e) {
		StackTraceElement[] ste = e.getStackTrace();
		System.err.println("��O���� : " + e.getClass().getName());
		System.err.println("��O���e : " + e.getMessage());
		System.err.println("�����ꏊ : " + ste[ste.length - 1]);
		System.exit(0);
	}
}
