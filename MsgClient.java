import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MsgClient extends JFrame implements ActionListener{
    public static final String HOST="localhost";
    public static final int PORT=51001;//ポート番号

    //コントロール
    JTextField tf=new JTextField("",40);//メッセージ入力用テキストフィールド
    JTextArea ta=new JTextArea(20,50);//テキストエリア
    JButton bs=new JButton("送信");//メッセージ送信用ボタン
    private Container c;//コンテナ
    PrintWriter pw;//出力用のライター

    public MsgClient(){
	//ウィンドウを作成する
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setTitle("メッセンジャー");
	setSize(600,400);
	c=getContentPane();
	c.setLayout(new FlowLayout());//レイアウトの設定
	c.add(tf);//テキストフィールドをコンテナに追加
	c.add(bs);//ボタンをコンテナに追加
	bs.addActionListener(this);//ボタンにイベントハンドラを追加
	c.add(ta);//テキストエリアをコンテナに追加
	ta.setEditable(false);//テキストエリアを編集不可にする

	//サーバに接続する
	Socket socket=null;
	try{
	    socket=new Socket(HOST,PORT);
	}catch(UnknownHostException e){
	    System.err.println("ホストのIPアドレスが判定できません: "+e);
	}catch(IOException e){
	    System.err.println("エラーが発生しました: "+e);
	    System.exit(0);
	}

	MsgRecvThread mrt=new MsgRecvThread(socket);
	mrt.start();
    }

    //メッセージ受信のためのスレッド
    public class MsgRecvThread extends Thread{
	    Socket socket;

	    public MsgRecvThread(Socket s){
		socket=s;
	    }
	    //通信状況を監視し，受信データによって動作する
	    public void run(){
		try{
		    InputStreamReader isr=new InputStreamReader(socket.getInputStream());
		    BufferedReader br=new BufferedReader(isr);
		    pw=new PrintWriter(socket.getOutputStream(),true);
		    while(true){
			String inputLine=br.readLine();
			if(inputLine!=null){
			    ta.append(inputLine+"\n");//メッセージをテキストエリアに追加する
			}
			else{
			    break;
			}
		    }
		    socket.close();
		}catch(IOException e){
		    System.err.println("エラーが発生しました: "+e);
		    System.exit(0);
		}
	    }
	}

    //送信ボタンが押されたときの処理
    public void actionPerformed(ActionEvent ae){
	if(ae.getActionCommand()=="送信"){
	    String msg=tf.getText();//テキストフィールドに入力された文字列を取得
	    tf.setText("");//テキストフィールドをクリア
	    if(msg.length()>0){
		pw.println(msg);//メッセージをサーバに送信
		pw.flush();
	    }
	}
    }
    public static void main(String[] args){
	MsgClient mc=new MsgClient();
	mc.setVisible(true);
    }
}
