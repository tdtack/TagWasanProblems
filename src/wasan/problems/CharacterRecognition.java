package wasan.problems;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

/**
 * 和算図形問題に含まれる文字要素の認識に関するクラスです。
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class CharacterRecognition {// 変数、関数名 保留

	/** CharacterRecognitionクラス内で画像処理を利用する。 */
	private ImageProcessing imgProc;

	/** 図形問題中の文字要素のみを残した画像を表す。 */
	private BufferedImage characterImg;

	/** 図形問題から切り出した文字要素の画像を保持する。 */
	private ArrayList<BufferedImage> characterList;

	/** 図形問題から切り出した文字要素の回転画像を保持する。 */
	private BufferedImage[][] characterRotation;

	/**
	 * コンストラクタ
	 * 
	 * @param _imgProc
	 */
	public CharacterRecognition(ImageProcessing _imgProc) {
		this.imgProc = _imgProc;

		this.characterImg = imgProc.dilateImage(imgProc.characterImg, 5, 3);
		this.characterList = imgProc.labelImage(this.characterImg);
		this.characterRotation = imgProc.rotateImage(characterList);
	}

	public void printResult() {
		printBestResult(characterRotation);
	}

	private void printBestResult(BufferedImage[][] imageFiles) {// result//ベスト
		String dir = "dat/input/ocr";

		byte[] graphDef = readAllBytesOrExit(Paths.get(dir, "wasan_model_09091142.pb")); // pbファイル読み込み
		List<String> labels = readAllLinesOrExit(Paths.get(dir, "label.txt")); // 正解(ラベル)読み込み

		for (int i = 0; i < imageFiles.length; i++) {
			String[] result = new String[imageFiles[i].length];
			float[] accuracy = new float[imageFiles[i].length];

			for (int j = 0; j < imageFiles[i].length; j++) {
				byte[] imageBytes = readAllBytesfromImage(imageFiles[i][j]); // 画像データ読み込み

				try (Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {// 一旦保留
					float[] labelProbabilities = executeInceptionGraph(graphDef, image);
					int bestLabelIdx = maxIndex(labelProbabilities);

					result[j] = labels.get(bestLabelIdx);
					accuracy[j] = labelProbabilities[bestLabelIdx] * 100f;
				}
			}

			int index = -1;
			float accuracy_max = 0;
			for (int j = 0; j < imageFiles[i].length; j++) {
				accuracy_max = Math.max(accuracy[j], accuracy_max);
				if (accuracy[j] == accuracy_max) {
					index = j;
				}
			}

			System.out.println("・ 文字[" + i + "] > " + result[index] + "(" + accuracy[index] + "%)");
		}

	}

	private static Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {// 画像を正規化するためにグラフを作成、実行する
		try (Graph g = new Graph()) {
			GraphBuilder b = new GraphBuilder(g);

			final Output<String> input = b.constant("input", imageBytes, String.class);
			final Output<Integer> make_batch = b.constant("make_batch", 0, Integer.class);
			final Output<Integer> size = b.constant("size", new int[] { 64, 64 }, Integer.class);
			final Output<Float> mean = b.constant("mean", 0f, Float.class);
			final Output<Float> scale = b.constant("scale", 255f, Float.class);

			final Output<Float> cast = b.cast(b.decodeJpeg(input, 1), Float.class);
			final Output<Float> expandDims = b.expandDims(cast, make_batch);// 次元調整
			final Output<Float> resize = b.resizeBilinear(expandDims, size);// ここまでは画像サイズの調整?
			final Output<Float> sub = b.sub(resize, mean);// なくてもあまり変わらなかった
			final Output<Float> output = b.div(sub, scale);// Pythonでデータセット変換したときのあの式では?

			try (Session s = new Session(g)) {
				return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
			}
		}
	}

	private static float[] executeInceptionGraph(byte[] graphDef, Tensor<Float> image) {// グラフと入力画像から正解を得る
		try (Graph g = new Graph()) {
			g.importGraphDef(graphDef);

			float[][] keep_prob = { { 1.0f } };
			Tensor<Float> dropout = Tensor.create(keep_prob, Float.class);// ドロップアウトの設定
			try (Session s = new Session(g); // セッション開始
					Tensor<Float> result = s.runner().feed("input", image).feed("dropout", dropout).fetch("output")
							.run().get(0).expect(Float.class)) {// 画像データとドロップアウトに対して正解を得る
				final long[] rshape = result.shape();
				if (result.numDimensions() != 2 || rshape[0] != 1) { // 正解に不備があった場合はメッセージを示す
					throw new RuntimeException(
							"Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape "
									+ Arrays.toString(rshape));
				}
				int nlabels = (int) rshape[1];
				return result.copyTo(new float[1][nlabels])[0];
			}
		}
	}

	private static int maxIndex(float[] probabilities) {// 推定として正しいindexを選択する
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

	private static byte[] readAllBytesfromImage(BufferedImage input) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);

		input.flush();
		try {
			ImageIO.write(input, "png", bos); // . png 型
		} catch (IOException e) {
			showException(e);
		}

		return baos.toByteArray();
	}

	private static byte[] readAllBytesOrExit(Path path) {// pbファイルや画像ファイルを読み込めるかどうか
		try {// 成功すれば、ファイルをバイトとして読み込み開始
			return Files.readAllBytes(path);
		} catch (IOException e) {// 失敗すれば、メッセージを表示して終了
			showException(e);
		}
		return null;
	}

	private static List<String> readAllLinesOrExit(Path path) {// 正解(ラベル)を読み込めるかどうか
		try {// 成功すれば、ファイルから全ての行を読み取る
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {// 失敗すれば、メッセージを表示して終了
			showException(e);
		}
		return null;
	}

	private static class GraphBuilder {
		GraphBuilder(Graph g) {
			this.g = g;
		}

		Output<Float> div(Output<Float> x, Output<Float> y) { // 割り算?
			return binaryOp("Div", x, y);
		}

		<T> Output<T> sub(Output<T> x, Output<T> y) {
			return binaryOp("Sub", x, y);
		}

		<T> Output<Float> resizeBilinear(Output<T> images, Output<Integer> size) {
			return binaryOp3("ResizeBilinear", images, size);
		}

		<T> Output<T> expandDims(Output<T> input, Output<Integer> dim) {
			return binaryOp3("ExpandDims", input, dim);
		}

		<T, U> Output<U> cast(Output<T> value, Class<U> type) {
			DataType dtype = DataType.fromClass(type);
			return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().<U>output(0);
		}

		Output<UInt8> decodeJpeg(Output<String> contents, long channels) {
			return g.opBuilder("DecodeJpeg", "DecodeJpeg").addInput(contents).setAttr("channels", channels).build()
					.<UInt8>output(0);
		}

		<T> Output<T> constant(String name, Object value, Class<T> type) {
			try (Tensor<T> t = Tensor.<T>create(value, type)) {
				return g.opBuilder("Const", name).setAttr("dtype", DataType.fromClass(type)).setAttr("value", t).build()
						.<T>output(0);
			}
		}

		private <T> Output<T> binaryOp(String type, Output<T> in1, Output<T> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}

		private <T, U, V> Output<T> binaryOp3(String type, Output<U> in1, Output<V> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}

		private Graph g;
	}
	
	private static void showException(Exception e) {
		StackTraceElement[] ste = e.getStackTrace();
		System.err.println("例外発生 : " + e.getClass().getName());
		System.err.println("例外内容 : " + e.getMessage());
		System.err.println("発生場所 : " + ste[ste.length - 1]);
		System.exit(0);
	}
}

