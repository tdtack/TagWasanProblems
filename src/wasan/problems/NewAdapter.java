package wasan.problems;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * �a�Z�}�`���ւ̃^�O�t�����ʂ��o�͂����E�B���h�E�Ɋւ���N���X�ł��B <br>
 * Java�̕W���N���X�ł���WindowAdapter�N���X���T�u�N���X�����ė��p���܂��B
 * 
 * @author Takuma Tsuchihashi
 */
public class NewAdapter extends WindowAdapter {
	
	/**
	 * �}�`���ւ̃^�O�t�����ʂ��o�͂����E�B���h�E�����ۂɗ��p���܂��B<br>
	 */
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}

