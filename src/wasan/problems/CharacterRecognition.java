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
 * �a�Z�}�`���Ɋ܂܂�镶���v�f�̔F���Ɋւ���N���X�ł��B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class CharacterRecognition {// �ϐ��A�֐��� �ۗ�

	/** CharacterRecognition�N���X���ŉ摜�����𗘗p����B */
	private ImageProcessing imgProc;

	/** �}�`��蒆�̕����v�f�݂̂��c�����摜��\���B */
	private BufferedImage characterImg;

	/** �}�`��肩��؂�o���������v�f�̉摜��ێ�����B */
	private ArrayList<BufferedImage> characterList;

	/** �}�`��肩��؂�o���������v�f�̉�]�摜��ێ�����B */
	private BufferedImage[][] characterRotation;

	/**
	 * �R���X�g���N�^
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

	private void printBestResult(BufferedImage[][] imageFiles) {// result//�x�X�g
		String dir = "dat/input/ocr";

		byte[] graphDef = readAllBytesOrExit(Paths.get(dir, "wasan_model_09091142.pb")); // pb�t�@�C���ǂݍ���
		List<String> labels = readAllLinesOrExit(Paths.get(dir, "label.txt")); // ����(���x��)�ǂݍ���

		for (int i = 0; i < imageFiles.length; i++) {
			String[] result = new String[imageFiles[i].length];
			float[] accuracy = new float[imageFiles[i].length];

			for (int j = 0; j < imageFiles[i].length; j++) {
				byte[] imageBytes = readAllBytesfromImage(imageFiles[i][j]); // �摜�f�[�^�ǂݍ���

				try (Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {// ��U�ۗ�
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

			System.out.println("�E ����[" + i + "] > " + result[index] + "(" + accuracy[index] + "%)");
		}

	}

	private static Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {// �摜�𐳋K�����邽�߂ɃO���t���쐬�A���s����
		try (Graph g = new Graph()) {
			GraphBuilder b = new GraphBuilder(g);

			final Output<String> input = b.constant("input", imageBytes, String.class);
			final Output<Integer> make_batch = b.constant("make_batch", 0, Integer.class);
			final Output<Integer> size = b.constant("size", new int[] { 64, 64 }, Integer.class);
			final Output<Float> mean = b.constant("mean", 0f, Float.class);
			final Output<Float> scale = b.constant("scale", 255f, Float.class);

			final Output<Float> cast = b.cast(b.decodeJpeg(input, 1), Float.class);
			final Output<Float> expandDims = b.expandDims(cast, make_batch);// ��������
			final Output<Float> resize = b.resizeBilinear(expandDims, size);// �����܂ł͉摜�T�C�Y�̒���?
			final Output<Float> sub = b.sub(resize, mean);// �Ȃ��Ă����܂�ς��Ȃ�����
			final Output<Float> output = b.div(sub, scale);// Python�Ńf�[�^�Z�b�g�ϊ������Ƃ��̂��̎��ł�?

			try (Session s = new Session(g)) {
				return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
			}
		}
	}

	private static float[] executeInceptionGraph(byte[] graphDef, Tensor<Float> image) {// �O���t�Ɠ��͉摜���琳���𓾂�
		try (Graph g = new Graph()) {
			g.importGraphDef(graphDef);

			float[][] keep_prob = { { 1.0f } };
			Tensor<Float> dropout = Tensor.create(keep_prob, Float.class);// �h���b�v�A�E�g�̐ݒ�
			try (Session s = new Session(g); // �Z�b�V�����J�n
					Tensor<Float> result = s.runner().feed("input", image).feed("dropout", dropout).fetch("output")
							.run().get(0).expect(Float.class)) {// �摜�f�[�^�ƃh���b�v�A�E�g�ɑ΂��Đ����𓾂�
				final long[] rshape = result.shape();
				if (result.numDimensions() != 2 || rshape[0] != 1) { // �����ɕs�����������ꍇ�̓��b�Z�[�W������
					throw new RuntimeException(
							"Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape "
									+ Arrays.toString(rshape));
				}
				int nlabels = (int) rshape[1];
				return result.copyTo(new float[1][nlabels])[0];
			}
		}
	}

	private static int maxIndex(float[] probabilities) {// ����Ƃ��Đ�����index��I������
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
			ImageIO.write(input, "png", bos); // . png �^
		} catch (IOException e) {
			showException(e);
		}

		return baos.toByteArray();
	}

	private static byte[] readAllBytesOrExit(Path path) {// pb�t�@�C����摜�t�@�C����ǂݍ��߂邩�ǂ���
		try {// ��������΁A�t�@�C�����o�C�g�Ƃ��ēǂݍ��݊J�n
			return Files.readAllBytes(path);
		} catch (IOException e) {// ���s����΁A���b�Z�[�W��\�����ďI��
			showException(e);
		}
		return null;
	}

	private static List<String> readAllLinesOrExit(Path path) {// ����(���x��)��ǂݍ��߂邩�ǂ���
		try {// ��������΁A�t�@�C������S�Ă̍s��ǂݎ��
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {// ���s����΁A���b�Z�[�W��\�����ďI��
			showException(e);
		}
		return null;
	}

	private static class GraphBuilder {
		GraphBuilder(Graph g) {
			this.g = g;
		}

		Output<Float> div(Output<Float> x, Output<Float> y) { // ����Z?
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
		System.err.println("��O���� : " + e.getClass().getName());
		System.err.println("��O���e : " + e.getMessage());
		System.err.println("�����ꏊ : " + ste[ste.length - 1]);
		System.exit(0);
	}
}

