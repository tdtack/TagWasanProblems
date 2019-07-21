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

//��
/**
 * �a�Z�}�`���Ɋ܂܂�镶���v�f�̔F���Ɋւ���N���X�ł��B<br>
 * �����v�f�̊w�K��Python��TensorFlow�𕹗p���čs���A�w�K�f�[�^�Ƃ���pb�t�@�C���𐶐����܂��B
 * �����v�f�̔F����Java��TensorFlow�𕹗p���čs���A�w�K�f�[�^�ł���pb�t�@�C���𗘗p���܂��B
 * 
 * @author Takuma Tsuchihashi
 */
public class CharacterRecognition {

	// ��
	/**
	 * �摜�����𗘗p���邽�߂�ImageProcessing�N���X�ϐ��ł��B<br>
	 * ���̕ϐ���p���邱�Ƃ�ImageProcessing�N���X���̃��\�b�h�Ȃǂ��Ăяo�����Ƃ��ł��܂��B
	 */
	private ImageProcessing imgProc;

	// ��
	/**
	 * �}�`���Ɋ܂܂�镶���v�f�݂̂𒊏o�����摜��\���܂��B<br>
	 */
	private BufferedImage characterImg;

	// ��
	/**
	 * �}�`��肩��؂�o�����e�X�̕����v�f�̉摜��ێ����܂��B<br>
	 * �����v�f��characterImg����؂�o���܂��B
	 */
	private ArrayList<BufferedImage> characterList;

	// ��
	/**
	 * �}�`��肩��؂�o�����e�X�̕����v�f����]�������摜��ێ����܂��B<br>
	 * characterList���̕����v�f1�ɑ΂��A��]�Ő��������摜�̖����͉�]�p�x�ɂ�茈�肵�܂��B
	 */
	private BufferedImage[][] characterRotation;

	// ��
	/**
	 * �摜����(ImageProcessing)���w�肵�A�����F��(CharacterRecognition)�̃C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
	 * 
	 * @param _imgProc
	 *            �摜�����𗘗p���邽�߂�ImageProcessing�N���X�ϐ�
	 */
	public CharacterRecognition(ImageProcessing _imgProc) {
		this.imgProc = _imgProc;

		this.characterImg = imgProc.dilateImage(imgProc.characterImg, 5, 3);// �}�`���Ɋ܂܂�镶���v�f�𒊏o�����摜�̖c�����������s���܂��B
		this.characterList = imgProc.labelImage(this.characterImg);// �}�`���Ɋ܂܂�镶���v�f�𒊏o�����摜�Ƀ��x�����O���s���A�����v�f��؂�o���܂��B
		this.characterRotation = imgProc.rotateImage(characterList);// �}�`��肩��؂�o���������v�f����]�������摜�𐶐����܂��B
	}

	// ��
	/**
	 * �}�`��肩��؂�o���������v�f��F�����A�^�O��t�^���܂��B<br>
	 */
	public void printResult() {
		printBestResult(characterRotation);// �}�`��肩��؂�o���������v�f����]�������摜��F�����A�^�O��t�^���܂��B
	}

	// ��
	/**
	 * �}�`��肩��؂�o���������v�f����]�������摜��F�����A�^�O��t�^���܂��B<br>
	 * �����v�f1�ɂ���]�摜8�������ꂼ��F�����A���𗦂��ł��������ʂ��^�O�t�����܂��B
	 * 
	 * @param imageFiles
	 *            �}�`��肩��؂�o�����e�X�̕����v�f�̉�]�摜��ێ�����BufferedImage�^�z��
	 */
	private void printBestResult(BufferedImage[][] imageFiles) {
		String dir = "dat/input/ocr";

		byte[] graphDef = readAllBytesOrExit(Paths.get(dir, "wasan_model_09091142.pb"));
		List<String> labels = readAllLinesOrExit(Paths.get(dir, "label.txt"));

		for (int i = 0; i < imageFiles.length; i++) {
			String[] result = new String[imageFiles[i].length];
			float[] accuracy = new float[imageFiles[i].length];

			for (int j = 0; j < imageFiles[i].length; j++) {
				byte[] imageBytes = readAllBytesfromImage(imageFiles[i][j]); // �摜�f�[�^�ǂݍ���

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

			System.out.println("�E ����[" + i + "] > " + result[index] + "(" + accuracy[index] + "%)");
		}
	}

	// ��
	/**
	 * �����v�f�̊w�K�f�[�^�ł���pb�t�@�C���̓��e���o�C�g�f�[�^�Ƃ��ēǂݍ��݂܂��B<br>
	 * 
	 * @param path
	 *            pb�t�@�C���̃p�X��\��Path�^�ϐ�
	 * @return �ǂݍ���pb�t�@�C���̓��e��\��byte�^�z��
	 * 
	 */
	private static byte[] readAllBytesOrExit(Path path) {// pb�t�@�C����摜�t�@�C����ǂݍ��߂邩�ǂ���
		try {// ��������΁A�t�@�C�����o�C�g�Ƃ��ēǂݍ��݊J�n
			return Files.readAllBytes(path);
		} catch (IOException e) {// ���s����΁A���b�Z�[�W��\�����ďI��
			showException(e);
		}
		return null;
	}

	// ��
	/**
	 * �����v�f�̐������x�����L�^���ꂽtxt�t�@�C���̑S�s��ǂݍ��݂܂��B<br>
	 * 
	 * @param path
	 *            txt�t�@�C���̃p�X��\��Path�^�ϐ�
	 * @return �ǂݍ���txt�t�@�C���̑S�s��ێ�����String�^���X�g
	 */
	private static List<String> readAllLinesOrExit(Path path) {// ����(���x��)��ǂݍ��߂邩�ǂ���
		try {// ��������΁A�t�@�C������S�Ă̍s��ǂݎ��
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {// ���s����΁A���b�Z�[�W��\�����ďI��
			showException(e);
		}
		return null;
	}

	// ��
	/**
	 * �}�`��肩��؂�o���������v�f�̉摜���o�C�g�f�[�^�Ƃ��ēǂݍ��݂܂��B<br>
	 * 
	 * @param input
	 *            �����v�f�̉摜��\��BufferedImage�^�ϐ�
	 * @return �ǂݍ��񂾕����v�f�̉摜��\��byte�^�z��
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

	/**
	 * <br>
	 * 
	 * @param probabilities
	 * 
	 * @return
	 */
	private static int maxIndex(float[] probabilities) {// ����Ƃ��Đ�����index��I������
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

	/**
	 * Tensorflow�𗘗p���������v�f�̔F�����T�|�[�g����N���X�ł��B<br>
	 * 
	 * @author Takuma Tsuchihashi
	 */
	private static class GraphBuilder {

		// ��
		/**
		 * Tensorflow�ɂ����鉉�Z�O���t��\���܂��B<br>
		 * �����ŃO���t�Ƃ̓e���\��(Tensor)�I�u�W�F�N�g���m�[�h�Ƃ���L���O���t���w���A���̃m�[�h���I�y���[�V�����ƌĂт܂��B
		 */
		private Graph g;

		// ��
		/**
		 * Tensorflow�̉��Z�O���t(Graph)���w�肵�A�O���t�̍\�z(GraphBuilder)�Ɋւ���C���X�^���X�𐶐�����R���X�g���N�^�ł��B<br>
		 * 
		 * @param g
		 *            Tensorflow�̉��Z�O���t��\��Graph�^�ϐ�
		 */
		GraphBuilder(Graph g) {
			this.g = g;
		}

		// ��
		/**
		 * �萔����͂Ƃ��A���Z�O���t�ɒǉ�����V���ȃI�y���[�V�����𐶐����܂��B<br>
		 * 
		 * @param name
		 *            �������ꂽ�V���ȃI�y���[�V�������Q�Ƃ���ۂ̎��ʎq��\��String�^�ϐ�
		 * @param value
		 *            ���͂���萔��\���ϐ�(�ϐ��̌^�ɐ����͂Ȃ�)
		 * @param type
		 *            ��������I�y���[�V�����ɐݒ肵�����f�[�^�^��\��Class�^�ϐ�
		 * @return �������ꂽ�V���ȃI�y���[�V������\��Output�^�ϐ�
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

		// ��
		/**
		 * �����̃I�y���[�V�����ɐݒ肳�ꂽ�f�[�^�^�̕ϊ����s���܂��B<br>
		 * 
		 * @param value
		 *            �����̃I�y���[�V������\��Output�^�ϐ�
		 * @param type
		 *            �����̃I�y���[�V�����ɐV���ɐݒ肵�����f�[�^�^��\��Class�^�ϐ�
		 * @return �V���ȃf�[�^�^��ݒ肵���I�y���[�V������\��Output�^�ϐ�
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

		// ��
		/**
		 * ���Z�O���t�̃m�[�h�ɑ�������I�y���[�V�������m�̏�Z�����s���܂��B<br>
		 * 
		 * @param x
		 *            ��Z�ɂ�����搔��\���I�y���[�V�����Ɋւ���Output�^�ϐ�
		 * @param y
		 *            ��Z�ɂ�����搔��\���I�y���[�V�����Ɋւ���Output�^�ϐ�
		 * @return ��Z�̌��ʂ�\���I�y���[�V�����Ɋւ���Output�^�ϐ�
		 */
		<T> Output<T> sub(Output<T> x, Output<T> y) {
			return binaryOp("Sub", x, y);
		}

		// ��
		/**
		 * ���Z�O���t�̃m�[�h�ɑ�������I�y���[�V�������m�̏��Z�����s���܂��B<br>
		 * 
		 * @param x
		 *            ���Z�ɂ�����폜����\���I�y���[�V�����Ɋւ���Output�^�ϐ�
		 * @param y
		 *            ���Z�ɂ����鏜����\���I�y���[�V�����Ɋւ���Output�^�ϐ�
		 * @return ���Z�̌��ʂ�\���I�y���[�V�����Ɋւ���Output�^�ϐ�
		 */
		Output<Float> div(Output<Float> x, Output<Float> y) {
			return binaryOp("Div", x, y);
		}

		// ��
		/**
		 * �����̃I�y���[�V��������͂Ƃ��A���Z�O���t�ɒǉ�����V���ȃI�y���[�V�����𐶐����܂��B<br>
		 * 
		 * @param type
		 *            �������ꂽ�V���ȃI�y���[�V�������Q�Ƃ���ۂ̎��ʎq��\��String�^�ϐ�
		 * @param in1
		 *            ���͂�������̃I�y���[�V������\��Output�^�ϐ�
		 * @param in2
		 *            ���͂�������̃I�y���[�V������\��Output�^�ϐ�
		 * @return �������ꂽ�V���ȃI�y���[�V������\��Output�^�ϐ�
		 */
		private <T> Output<T> binaryOp(String type, Output<T> in1, Output<T> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}

		// ��
		/**
		 * �����̃I�y���[�V��������͂Ƃ��A���Z�O���t�ɒǉ�����V���ȃI�y���[�V�����𐶐����܂��B<br>
		 * 
		 * @param type
		 *            �������ꂽ�V���ȃI�y���[�V�������Q�Ƃ���ۂ̎��ʎq��\��String�^�ϐ�
		 * @param in1
		 *            ���͂�������̃I�y���[�V������\��Output�^�ϐ�
		 * @param in2
		 *            ���͂�������̃I�y���[�V������\��Output�^�ϐ�
		 * @return �������ꂽ�V���ȃI�y���[�V������\��Output�^�ϐ�
		 */
		private <T, U, V> Output<T> binaryOp3(String type, Output<U> in1, Output<V> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
		}
	}

	// ��
	/**
	 * �������ɉ��炩�̗�O�����������ہA���̏ڍׂ�\�����܂��B<br>
	 * 
	 * @param e
	 *            ��O�̓��e��\��Exception�^�ϐ�
	 */
	private static void showException(Exception e) {
		StackTraceElement[] ste = e.getStackTrace();
		System.err.println("��O���� : " + e.getClass().getName());
		System.err.println("��O���e : " + e.getMessage());
		System.err.println("�����ꏊ : " + ste[ste.length - 1]);
		System.exit(0);
	}
}
