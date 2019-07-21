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

//☆
/**
 * 和算図形問題に含まれる文字要素の認識に関するクラスです。<br>
 * 文字要素の学習はPythonとTensorFlowを併用して行い、学習データとしてpbファイルを生成します。
 * 文字要素の認識はJavaとTensorFlowを併用して行い、学習データであるpbファイルを利用します。
 * 
 * @author Takuma Tsuchihashi
 */
public class CharacterRecognition {

	// ☆
	/**
	 * 画像処理を利用するためのImageProcessingクラス変数です。<br>
	 * この変数を用いることでImageProcessingクラス内のメソッドなどを呼び出すことができます。
	 */
	private ImageProcessing imgProc;

	// ☆
	/**
	 * 図形問題に含まれる文字要素のみを抽出した画像を表します。<br>
	 */
	private BufferedImage characterImg;

	// ☆
	/**
	 * 図形問題から切り出した各々の文字要素の画像を保持します。<br>
	 * 文字要素はcharacterImgから切り出します。
	 */
	private ArrayList<BufferedImage> characterList;

	// ☆
	/**
	 * 図形問題から切り出した各々の文字要素を回転させた画像を保持します。<br>
	 * characterList内の文字要素1つに対し、回転で生成される画像の枚数は回転角度により決定します。
	 */
	private BufferedImage[][] characterRotation;

	// ☆
	/**
	 * 画像処理(ImageProcessing)を指定し、文字認識(CharacterRecognition)のインスタンスを生成するコンストラクタです。<br>
	 * 
	 * @param _imgProc
	 *            画像処理を利用するためのImageProcessingクラス変数
	 */
	public CharacterRecognition(ImageProcessing _imgProc) {
		this.imgProc = _imgProc;

		this.characterImg = imgProc.dilateImage(imgProc.characterImg, 5, 3);// 図形問題に含まれる文字要素を抽出した画像の膨張処理を実行します。
		this.characterList = imgProc.labelImage(this.characterImg);// 図形問題に含まれる文字要素を抽出した画像にラベリングを行い、文字要素を切り出します。
		this.characterRotation = imgProc.rotateImage(characterList);// 図形問題から切り出した文字要素を回転させた画像を生成します。
	}

	// ☆
	/**
	 * 図形問題から切り出した文字要素を認識し、タグを付与します。<br>
	 */
	public void printResult() {
		printBestResult(characterRotation);// 図形問題から切り出した文字要素を回転させた画像を認識し、タグを付与します。
	}

	// ☆
	/**
	 * 図形問題から切り出した文字要素を回転させた画像を認識し、タグを付与します。<br>
	 * 文字要素1つにつき回転画像8枚をそれぞれ認識し、正解率が最も高い結果をタグ付けします。
	 * 
	 * @param imageFiles
	 *            図形問題から切り出した各々の文字要素の回転画像を保持するBufferedImage型配列
	 */
	private void printBestResult(BufferedImage[][] imageFiles) {
		String dir = "dat/input/ocr";

		byte[] graphDef = readAllBytesOrExit(Paths.get(dir, "wasan_model_09091142.pb"));
		List<String> labels = readAllLinesOrExit(Paths.get(dir, "label.txt"));

		for (int i = 0; i < imageFiles.length; i++) {
			String[] result = new String[imageFiles[i].length];
			float[] accuracy = new float[imageFiles[i].length];

			for (int j = 0; j < imageFiles[i].length; j++) {
				byte[] imageBytes = readAllBytesfromImage(imageFiles[i][j]); // 画像データ読み込み

				try (Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {
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

	// ☆
	/**
	 * 文字要素の学習データであるpbファイルの内容をバイトデータとして読み込みます。<br>
	 * 
	 * @param path
	 *            pbファイルのパスを表すPath型変数
	 * @return 読み込んだpbファイルの内容を表すbyte型配列
	 * 
	 */
	private static byte[] readAllBytesOrExit(Path path) {// pbファイルや画像ファイルを読み込めるかどうか
		try {// 成功すれば、ファイルをバイトとして読み込み開始
			return Files.readAllBytes(path);
		} catch (IOException e) {// 失敗すれば、メッセージを表示して終了
			showException(e);
		}
		return null;
	}

	// ☆
	/**
	 * 文字要素の正解ラベルが記録されたtxtファイルの全行を読み込みます。<br>
	 * 
	 * @param path
	 *            txtファイルのパスを表すPath型変数
	 * @return 読み込んだtxtファイルの全行を保持するString型リスト
	 */
	private static List<String> readAllLinesOrExit(Path path) {// 正解(ラベル)を読み込めるかどうか
		try {// 成功すれば、ファイルから全ての行を読み取る
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {// 失敗すれば、メッセージを表示して終了
			showException(e);
		}
		return null;
	}

	// ☆
	/**
	 * 図形問題から切り出した文字要素の画像をバイトデータとして読み込みます。<br>
	 * 
	 * @param input
	 *            文字要素の画像を表すBufferedImage型変数
	 * @return 読み込んだ文字要素の画像を表すbyte型配列
	 */
	private static byte[] readAllBytesfromImage(BufferedImage input) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);

		input.flush();
		try {
			ImageIO.write(input, "png", bos);
		} catch (IOException e) {
			showException(e);
		}

		return baos.toByteArray();
	}

	/**
	 * <br>
	 * 
	 * @param imageBytes
	 * 
	 * @return
	 */
	private static Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
		try (Graph g = new Graph()) {
			GraphBuilder b = new GraphBuilder(g);

			final Output<String> input = b.constant("input", imageBytes, String.class);
			final Output<Integer> make_batch = b.constant("make_batch", 0, Integer.class);
			final Output<Integer> size = b.constant("size", new int[] { 64, 64 }, Integer.class);
			final Output<Float> mean = b.constant("mean", 0f, Float.class);
			final Output<Float> scale = b.constant("scale", 255f, Float.class);

			final Output<Float> cast = b.cast(b.decodeJpeg(input, 1), Float.class);
			final Output<Float> expandDims = b.expandDims(cast, make_batch);
			final Output<Float> resize = b.resizeBilinear(expandDims, size);
			final Output<Float> sub = b.sub(resize, mean);
			final Output<Float> output = b.div(sub, scale);

			try (Session s = new Session(g)) {
				return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
			}
		}
	}

	/**
	 * <br>
	 * 
	 * @param graphDef
	 * 
	 * @param image
	 * 
	 * @return
	 */
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

	/**
	 * <br>
	 * 
	 * @param probabilities
	 * 
	 * @return
	 */
	private static int maxIndex(float[] probabilities) {// 推定として正しいindexを選択する
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

	/**
	 * Tensorflowを利用した文字要素の認識をサポートするクラスです。<br>
	 * 
	 * @author Takuma Tsuchihashi
	 */
	private static class GraphBuilder {

		// ☆
		/**
		 * Tensorflowにおける演算グラフを表します。<br>
		 * ここでグラフとはテンソル(Tensor)オブジェクトをノードとする有向グラフを指し、このノードをオペレーションと呼びます。
		 */
		private Graph g;

		// ☆
		/**
		 * Tensorflowの演算グラフ(Graph)を指定し、グラフの構築(GraphBuilder)に関するインスタンスを生成するコンストラクタです。<br>
		 * 
		 * @param g
		 *            Tensorflowの演算グラフを表すGraph型変数
		 */
		GraphBuilder(Graph g) {
			this.g = g;
		}

		// ☆
		/**
		 * 定数を入力とし、演算グラフに追加する新たなオペレーションを生成します。<br>
		 * 
		 * @param name
		 *            生成された新たなオペレーションを参照する際の識別子を表すString型変数
		 * @param value
		 *            入力する定数を表す変数(変数の型に制限はない)
		 * @param type
		 *            生成するオペレーションに設定したいデータ型を表すClass型変数
		 * @return 生成された新たなオペレーションを表すOutput型変数
		 */
		<T> Output<T> constant(String name, Object value, Class<T> type) {
			try (Tensor<T> t = Tensor.<T>create(value, type)) {
				return g.opBuilder("Const", name).setAttr("dtype", DataType.fromClass(type)).setAttr("value", t).build()
						.<T>output(0);
			}
		}

		/**
		 * <br>
		 * 
		 * @param contents
		 * 
		 * @param channels
		 * 
		 * @return
		 */
		Output<UInt8> decodeJpeg(Output<String> contents, long channels) {
			return g.opBuilder("DecodeJpeg", "DecodeJpeg").addInput(contents).setAttr("channels", channels).build()
					.<UInt8>output(0);
		}

		// ☆
		/**
		 * 既存のオペレーションに設定されたデータ型の変換を行います。<br>
		 * 
		 * @param value
		 *            既存のオペレーションを表すOutput型変数
		 * @param type
		 *            既存のオペレーションに新たに設定したいデータ型を表すClass型変数
		 * @return 新たなデータ型を設定したオペレーションを表すOutput型変数
		 */
		<T, U> Output<U> cast(Output<T> value, Class<U> type) {
			DataType dtype = DataType.fromClass(type);
			return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().<U>output(0);
		}

		/**
		 * <br>
		 * 
		 * @param input
		 * 
		 * @param dim
		 * 
		 * @return
		 */
		<T> Output<T> expandDims(Output<T> input, Output<Integer> dim) {
			return binaryOp3("ExpandDims", input, dim);
		}

		/**
		 * <br>
		 * 
		 * @param images
		 * 
		 * @param size
		 * 
		 * @return
		 */
		<T> Output<Float> resizeBilinear(Output<T> images, Output<Integer> size) {
			return binaryOp3("ResizeBilinear", images, size);
		}

		// ☆
		/**
		 * 演算グラフのノードに相当するオペレーション同士の乗算を実行します。<br>
		 * 
		 * @param x
		 *            乗算における乗数を表すオペレーションに関するOutput型変数
		 * @param y
		 *            乗算における乗数を表すオペレーションに関するOutput型変数
		 * @return 乗算の結果を表すオペレーションに関するOutput型変数
		 */
		<T> Output<T> sub(Output<T> x, Output<T> y) {
			return binaryOp("Sub", x, y);
		}

		// ☆
		/**
		 * 演算グラフのノードに相当するオペレーション同士の除算を実行します。<br>
		 * 
		 * @param x
		 *            除算における被除数を表すオペレーションに関するOutput型変数
		 * @param y
		 *            除算における除数を表すオペレーションに関するOutput型変数
		 * @return 除算の結果を表すオペレーションに関するOutput型変数
		 */
		Output<Float> div(Output<Float> x, Output<Float> y) {
			return binaryOp("Div", x, y);
		}

		// ☆
		/**
		 * 既存のオペレーションを入力とし、演算グラフに追加する新たなオペレーションを生成します。<br>
		 * 
		 * @param type
		 *            生成された新たなオペレーションを参照する際の識別子を表すString型変数
		 * @param in1
		 *            入力する既存のオペレーションを表すOutput型変数
		 * @param in2
		 *            入力する既存のオペレーションを表すOutput型変数
		 * @return 生成された新たなオペレーションを表すOutput型変数
		 */
		private <T> Output<T> binaryOp(String type, Output<T> in1, Output<T> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}

		// ☆
		/**
		 * 既存のオペレーションを入力とし、演算グラフに追加する新たなオペレーションを生成します。<br>
		 * 
		 * @param type
		 *            生成された新たなオペレーションを参照する際の識別子を表すString型変数
		 * @param in1
		 *            入力する既存のオペレーションを表すOutput型変数
		 * @param in2
		 *            入力する既存のオペレーションを表すOutput型変数
		 * @return 生成された新たなオペレーションを表すOutput型変数
		 */
		private <T, U, V> Output<T> binaryOp3(String type, Output<U> in1, Output<V> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}
	}

	// ☆
	/**
	 * 処理中に何らかの例外が発生した際、その詳細を表示します。<br>
	 * 
	 * @param e
	 *            例外の内容を表すException型変数
	 */
	private static void showException(Exception e) {
		StackTraceElement[] ste = e.getStackTrace();
		System.err.println("例外発生 : " + e.getClass().getName());
		System.err.println("例外内容 : " + e.getMessage());
		System.err.println("発生場所 : " + ste[ste.length - 1]);
		System.exit(0);
	}
}
