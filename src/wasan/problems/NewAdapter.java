package wasan.problems;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 和算図形問題へのタグ付け結果を出力したウィンドウに関するクラスです。 <br>
 * Javaの標準クラスであるWindowAdapterクラスをサブクラス化して利用します。
 * 
 * @author Takuma Tsuchihashi
 */
public class NewAdapter extends WindowAdapter {
	
	/**
	 * 図形問題へのタグ付け結果を出力したウィンドウを閉じる際に利用します。<br>
	 */
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}

