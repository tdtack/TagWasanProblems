package wasan.problems;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

//☆
/**
 * 和算図形問題の画像を分析する際に利用する画像処理に関するクラスです。<br>
 * 図形問題画像の二値化やクロージング処理など、画像処理のメソッドは主にこのクラスから利用します。
 * 
 * @author Takuma Tsuchihashi
 */
public class ImageProcessing {

	// ☆以下、入力とする図形問題の画像ファイル名に関するString型変数です。
	/**
	 * 入力とする図形問題の画像ファイル名を表します。<br>
	 * 画像ファイル名は"001.PNG","002.PNG",...のように設定するものとします。
	 */
	public String imgName;
	/**
	 * 入力とする図形問題の画像ファイル名に含まれる番号を表します。<br>
	 * 番号は"001","002",...のように取得されます。
	 */
	public String imgNum;

	// ☆以下、画像処理で扱われる画像を表すBufferedImage型変数です。
	/**
	 * 入力とする図形問題の元画像を表します。<br>
	 */
	public BufferedImage originalImg;
	/**
	 * 図形問題に含まれる幾何要素、図形要素の抽出と並行して処理される画像を表します。<br>
	 */
	public BufferedImage editingImg;
	/**
	 * 図形問題に含まれる幾何要素、図形要素を抽出した画像を表します。<br>
	 */
	public BufferedImage elementImg;
	/**
	 * 図形問題に含まれる文字要素を抽出した画像を表します。<br>
	 */
	public BufferedImage characterImg;

	// ☆
	/**
	 * 図形問題の画像ファイルパスを指定し、画像処理(ImageProcessing)のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param filePath
	 *            入力とする図形問題の画像ファイルパスを表すString型変数
	 */
	public ImageProcessing(String filePath) {
		imgName = filePath.split("\\\\", 0)[3];
		imgNum = imgName.substring(0, imgName.indexOf("."));

		System.out.println("< 図形問題 >");
		System.out.println(filePath);
		System.out.println();

		originalImg = loadImage(filePath);// 入力とする図形問題の画像ファイルパスから画像を読み込みます。
		originalImg = resizeImage(500);// 読み込んだ図形問題の元画像をリサイズします。

		// 以下、図形問題への自動タグ付け前に画像に対する事前処理を行います。
		editingImg = preprocessImage(true);
		elementImg = preprocessImage(false);
		characterImg = preprocessImage(false);
	}

	// ☆
	/**
	 * 自動タグ付けが完了した図形問題に関する不要なオブジェクトをメモリから解放します。<br>
	 * 複数の図形問題に対して連続的に自動タグ付けを行う際、OutOfMemoryErrorを回避します。
	 */
	public void freeResource() {
		this.originalImg.flush();
		this.editingImg.flush();
		this.elementImg.flush();
		this.characterImg.flush();
	}

	// ☆
	/**
	 * 入力とする図形問題の画像ファイルパスから画像を読み込みます。<br>
	 * 
	 * @param filePath
	 *            入力とする図形問題の画像ファイルパスを表すString型変数
	 * @return 入力とする図形問題の元画像を表すBufferedImage型変数
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

	// ☆
	/**
	 * 読み込んだ図形問題の元画像をリサイズします。<br>
	 * 元画像の縦幅と横幅のうち、長い方を基準にリサイズを行います。
	 * 
	 * @param size
	 *            リサイズ後の画像の縦幅または横幅を表すint型変数
	 * @return リサイズされた図形問題の画像を表すBufferedImage型変数
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

	// ☆
	/**
	 * 図形問題への自動タグ付け前に画像に対する事前処理を行います。<br>
	 * ここで行われる処理はグレースケール化・二値化・クロージング処理の3つです。
	 * 
	 * @param closing
	 *            クロージング処理を実行するかどうかを決定するboolean型変数
	 * @return 事前処理を施した画像を表すBufferedImage型変数
	 */
	private BufferedImage preprocessImage(boolean closing) {
		BufferedImage outputImg = originalImg;

		outputImg = grayscaleImage(outputImg);// 図形問題の画像に対するグレースケール化を行います。
		outputImg = binarizeImage(outputImg);// 図形問題の画像に対する二値化を行います。
		outputImg = (closing) ? closeImage(outputImg) : outputImg;// 図形問題の画像に対するクロージング処理を行います。(closingがtrue→実行する、false→実行しない)

		return outputImg;
	}

	// ☆
	/**
	 * 図形問題の画像に対するグレースケール化を行います。<br>
	 * 
	 * @param inputImg
	 *            グレースケール化したい画像を表すBufferedImage型変数
	 * @return グレースケール化された画像を表すBufferedImage型変数
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

	// ☆
	/**
	 * 図形問題の画像に対する二値化を行います。<br>
	 * 
	 * @param inputImg
	 *            二値化したい画像を表すBufferedImage型変数
	 * @return 二値化された画像を表すBufferedImage型変数
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

	// ☆
	/**
	 * 図形問題の画像に対するクロージング処理を行います。<br>
	 * クロージング処理は、dilateImageとerodeImageを併用します。
	 * 
	 * @param inputImg
	 *            クロージング処理を実行する画像を表すBufferedImage型変数
	 * @return クロージング処理を施した画像を表すBufferedImage型変数
	 */
	public BufferedImage closeImage(BufferedImage inputImg) {
		BufferedImage outputImg = inputImg;
		
		// https://algorithm.joho.info/image-processing/dilation-erosion-opening-closing-tophat-blackhat/
		int kernelSize = 5;
		int iteration = 2;
		outputImg = dilateImage(outputImg, kernelSize, iteration);// 図形問題の画像に対するクロージング処理を行います。
		outputImg = erodeImage(outputImg, kernelSize, iteration);// 図形問題の画像のクロージング処理における収縮処理を実行します。

		return outputImg;
	}

	// ☆
	/**
	 * 図形問題の画像のクロージング処理における膨張処理を実行します。<br>
	 * クロージング処理では、このメソッドとerodeImageを併用します。
	 * 
	 * @param inputImg
	 *            膨張処理を実行する画像を表すBufferedImage型変数
	 * @param kernelSize
	 *            膨張処理において注目する画素範囲(正方行列)のサイズを表すint型変数
	 * @param iteration
	 *            膨張処理の繰り返し回数を表すint型変数
	 * @return 膨張処理を施した画像を表すBufferedImage型変数
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

	// ☆
	/**
	 * 図形問題の画像のクロージング処理における収縮処理を実行します。<br>
	 * クロージング処理では、このメソッドとdilateImageを併用します。
	 * 
	 * @param inputImg
	 *            収縮処理を実行する画像を表すBufferedImage型変数
	 * @param kernelSize
	 *            収縮処理において注目する画素範囲(正方行列)のサイズを表すint型変数
	 * @param iteration
	 *            収縮処理の繰り返し回数を表すint型変数
	 * @return 収縮処理を施した画像を表すBufferedImage型変数
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

	// ☆
	/**
	 * 図形問題の画像から抽出された線分を除去します。<br>
	 * この除去は他の幾何要素の誤検出を軽減することを目的としています。
	 * 
	 * @param lineList
	 *            抽出された線分を保持するMyLineクラスリスト
	 * @return 抽出された線分を除去した図形問題の画像を表すBufferedImage型変数
	 */
	public BufferedImage removeLine(ArrayList<MyLine> lineList) {// 線分要素の除去
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

	// ☆
	/**
	 * 図形問題の画像から抽出した円を除去します。<br>
	 * この除去は他の幾何要素の誤検出を軽減することを目的としています。
	 * 
	 * @param circleList
	 *            抽出された円を保持するMyCircleクラスリスト
	 * @return 抽出された円を除去した図形問題の画像を表すBufferedImage型変数
	 */
	public BufferedImage removeCircle(ArrayList<MyCircle> circleList) {// 円要素の除去
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

	// ☆
	/**
	 * 図形問題に含まれる幾何要素を抽出した画像および文字要素を抽出した画像を生成します。<br>
	 * これらの2つの画像の和は図形問題の元画像に等しいです。
	 * 
	 * @param lineList
	 *            抽出された線分を保持するMyLineクラスリスト
	 * @param circleList
	 *            抽出された円を保持するMyCircleクラスリスト
	 * @return 幾何要素を抽出した画像および文字要素を抽出した画像の2つの画像を保持するBufferedImage型配列
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

	// ☆
	/**
	 * 図形問題に含まれる文字要素を抽出した画像にラベリングを行い、文字要素を切り出します。<br>
	 * 
	 * @param inputImg
	 *            文字要素を抽出した画像を表すBufferedImage型変数
	 * @return 切り出した文字要素の画像を保持するBufferedImage型リスト
	 */
	public ArrayList<BufferedImage> labelImage(BufferedImage inputImg) {
		ArrayList<BufferedImage> characterList = new ArrayList<BufferedImage>();

		// ラベリング
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
						if (blackCheck[x][y]) { // ターゲット色の時
							if (!blackCheck[x - 1][y]) { // 左隣りが黒の時
								label++;
								labelNum[x][y] = label; // 新しいラベルを付ける
							} else {
								labelNum[x][y] = labelNum[x - 1][y]; // ラベルのコピー
							}
						}
					}
				} else {
					if (blackCheck[x][y]) { // 黒の時
						if (!blackCheck[x - 1][y] && !blackCheck[x - 1][y - 1] && !blackCheck[x][y - 1]
								&& !blackCheck[x + 1][y - 1]) {// 黒がなければ
							label++;
							labelNum[x][y] = label;
							LUT[label] = label;
						} else {
							boolean labelSet = false; // 回り込むように確認することで最小値に書き換える
							if (blackCheck[x - 1][y]) {
								labelNum[x][y] = labelNum[x - 1][y];
								labelSet = true;
							}

							for (int dx = -1; dx <= 1; dx++) {
								int mx = x + dx;
								int my = y - 1;
								if (blackCheck[mx][my]) {
									if (!labelSet) {
										labelNum[x][y] = labelNum[mx][my]; // 左上
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

		// 位置の特定
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

				// 切り取り位置の調整完了

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
					// ピクセル数について確認
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

	// ☆
	/**
	 * 図形問題から切り出した文字要素を回転させた画像を生成します。<br>
	 * 文字要素1つに対し、回転で生成される画像の枚数は回転角度により決定します。
	 * 
	 * @param characterList
	 *            切り出した文字要素の画像を保持するBufferedImage型リスト
	 * @return 文字要素を回転させた画像を保持するBufferedImage型配列
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

				// 回転後の中心座標(cx, cy)
				double cx = (characterWidth * Math.cos(angle) - characterHeight * Math.sin(angle)) / 2.0;
				double cy = (characterWidth * Math.sin(angle) + characterHeight * Math.cos(angle)) / 2.0;
				// 真ん中に引き戻すのに必要な移動量(dx, dy)
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
		System.err.println("例外発生 : " + e.getClass().getName());
		System.err.println("例外内容 : " + e.getMessage());
		System.err.println("発生場所 : " + ste[ste.length - 1]);
		System.exit(0);
	}
}
