package wasan.problems;

import java.awt.Frame;

/**
 * 和算図形問題へのタグ付け結果を出力する画面フレームのクラスです。 <br>
 * Javaの標準クラスであるFrameクラスをサブクラス化して利用します。
 * 
 * @author Takuma Tsuchihashi
 */
public class NewFrame extends Frame {
	
	/**
	 * 出力する画面フレーム(NewFrame)のインスタンスを生成するコンストラクタです。<br>
	 */
	NewFrame() {
		setTitle("TagWasanProblems");// 画面フレームのタイトルを設定します。
	}
}

