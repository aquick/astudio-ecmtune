package org.gmplib.test.ecmtune;

import android.app.Activity;
//import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.StringTokenizer;

import org.gmplib.gmpjni.ECMTune;

public class MainActivity extends Activity implements UI {

    private TextView mView;
    private Button mBench;
    private Button mTune;
    private RandomNumberFile rng = null;
    private String randfname = "2010-03-02.hex.txt";
    private int base = 16;
    AsyncTask<Integer, Integer, Integer> task = null;
    private MyHandler mHandler;
    private static final String TAG = "MainActivity";
    public static final int BENCH = 0;
    public static final int TUNE = 1;

    private class MyHandler extends Handler {

	public static final int DISPLAY_INFO = 0;

	public MyHandler(Looper looper)
	{
	    super(looper);
	}

	@Override
	public void handleMessage(Message inputMessage)
	{
	    int code = inputMessage.what;
	    switch (code) {
		case DISPLAY_INFO:
		    StringBuffer sb = new StringBuffer();
		    String msg = (String) inputMessage.obj;
		    sb.append(msg);
		    sb.append("\n");
		    MainActivity.this.mView.append(sb.toString());
		    break;
		default:
		    break;
	    }
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	mView = (TextView) findViewById(R.id.TextView01);
	mBench = (Button) findViewById(R.id.Button01);
	mBench.setOnClickListener(
		new View.OnClickListener() {
		    public void onClick(View v) {
			if (MainActivity.this.rng == null) {
			    init();
			}
			task = new ECMTune_Task(MainActivity.this, MainActivity.this.rng);
			task.execute(BENCH);
		    }
		});
	mTune = (Button) findViewById(R.id.Button02);
	mTune.setOnClickListener(
		new View.OnClickListener() {
		    public void onClick(View v)
		    {
		        if (MainActivity.this.rng == null) {
		            init();
			}
			task = new ECMTune_Task(MainActivity.this, MainActivity.this.rng);
			task.execute(TUNE);
		    }
		});
	mHandler = new MyHandler(Looper.getMainLooper());
	try {
	    String root = MainActivity.this.getExternalFilesDir(null).getPath();
	    ECMTune.init(root);
	} catch (Exception e) {
	    Log.d(TAG, "EXCEPTION: " + e.toString());
	    display("EXCEPTION: " + e.toString());
	    StackTraceElement[] st = e.getStackTrace();
	    for (int m = 0; m < st.length; m++) {
		display(st[m].toString());
	    }
	}
    }

    protected void init()
    {
	try {
	    initRandom();
	} catch (Exception e) {
	    Log.d(TAG, "EXCEPTION: " + e.toString());
	    display("EXCEPTION: " + e.toString());
	}
    }

    @Override
    protected void onDestroy()
    {
	Log.d(TAG, "onDestroy");
	try {
	    finiRandom();
	} catch (IOException e) {
	    Log.d(TAG, "EXCEPTION: " + e.toString());
	}
	super.onDestroy();
    }

    @Override
    protected void onPause()
    {
	Log.d(TAG, "onPause");
	try {
	    finiRandom();
	} catch (IOException e) {
	    Log.d(TAG, "EXCEPTION: " + e.toString());
	}
	super.onPause();
    }

    @Override
    protected void onResume()
    {
	super.onResume();
	Log.d(TAG, "onResume");
    }

    protected void initRandom() throws IOException
    {
	int n = 0;
	String root = this.getExternalFilesDir(null).getPath();
	String fname = root + "/.randseed2";
	BufferedReader fin = new BufferedReader(new FileReader(fname));
	String line = fin.readLine();
	fin.close();
	if (line.length() > 0) {
	    StringTokenizer st = new StringTokenizer(line);
	    if (st.hasMoreTokens()) {
		randfname = st.nextToken();
		if (st.hasMoreTokens()) {
		    base = Integer.parseInt(st.nextToken());
		    if (st.hasMoreTokens()) {
			n = Integer.parseInt(st.nextToken());
		    }
		}
	    }
	}
	rng = new RandomNumberFile(root + "/" + randfname, base);
	rng.skip(n);
    }

    protected void finiRandom() throws IOException
    {
	if (rng != null) {
	    long consumed = rng.consumed();
	    rng.close();
	    String root = this.getExternalFilesDir(null).getPath();
	    String fname = root + "/.randseed2";
	    BufferedWriter fout = new BufferedWriter(new FileWriter(fname));
	    fout.write(randfname + " " + base + " " + consumed);
	    fout.close();
	    rng = null;
	}
    }

    public void display(String line)
    {
	Message msg = mHandler.obtainMessage(
		MyHandler.DISPLAY_INFO,
		line);
	mHandler.sendMessage(msg);
    }

}
