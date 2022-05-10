import java.util.*;
import java.io.*;
import java.net.*;

public class MsgServer{
    public static final int PORT=51001;//ポート番号
    private static final int MAX_CON=5;//最大接続数
    private static int n=0;//接続数
    private static Socket[] sk=new Socket[MAX_CON];//ソケット
    private static InputStreamReader[] isr=new InputStreamReader[MAX_CON];//入力ストリーム
    private static BufferedReader[] br=new BufferedReader[MAX_CON];//バッファリーダー
    private static PrintWriter[] pw=new PrintWriter[MAX_CON];//出力ストリーム
    private static ClientProc[] client=new ClientProc[MAX_CON];//スレッド
    private static boolean[] con=new boolean[MAX_CON];//接続状態管理

    public static void SetConnectionStatus(int n,boolean b){
	con[n]=b;
    }

    public static void broadcast(String str){
	for(int i=0;i<n;i++){
	    if(con[i]==true){
		pw[i].println(str);
		pw[i].flush();
	    }
	}
    }

    public static void main(String[] args){
	try{
	    ServerSocket ss=new ServerSocket(PORT);
	    System.out.println("クライアントの接続を待っています...");
	    while(true){
		if(n<MAX_CON){
		    try{
			sk[n]=ss.accept();
			System.out.println("クライアントの接続要求がありました: #"+n);
			con[n]=true;
			isr[n]=new InputStreamReader(sk[n].getInputStream());
			br[n]=new BufferedReader(isr[n]);
			pw[n]=new PrintWriter(sk[n].getOutputStream(),true);
			client[n]=new ClientProc(n,sk[n],isr[n],br[n],pw[n]);
			client[n].start();//スレッドの開始
			n++;
		    }catch(Exception e){
			e.printStackTrace();
		    }
		}
		else{
		    System.out.println("定員に達しました");
		    break;
		}
	    }
	    ss.close();
	}catch(Exception e){
	    System.out.println("ソケットを作成できませんでした");
	}
    }
}
//各クライアントに応じたスレッド
class ClientProc extends Thread{
    private int num;//自身のID番号
    private Socket sk;
    private InputStreamReader isr;
    private BufferedReader br;
    private PrintWriter pw;

    public ClientProc(int n,Socket s,InputStreamReader i,
		     BufferedReader b,PrintWriter p){
	num=n;
	sk=s;
	isr=i;
	br=b;
	pw=p;
    }

    public void run(){
	try{
	    while(true){//無限ループでソケットへの入力を監視する
		String str=br.readLine();
		//メッセージを受信したときに全クライアントに送信元IDをつけてメッセージを送信
		if(str!=null){
		    MsgServer.broadcast("#"+num+" > "+str+"\n");
		}
	    }
	}
	catch(Exception e){
	    System.out.println("切断します: #"+num);
	    MsgServer.SetConnectionStatus(num,false);
	}
    }
}
