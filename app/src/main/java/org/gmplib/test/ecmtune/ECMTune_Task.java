package org.gmplib.test.ecmtune;

import android.os.AsyncTask;
import android.util.Log;

import org.gmplib.gmpjni.ECMTune;

public class ECMTune_Task extends AsyncTask<Integer, Integer, Integer> {

    private static final String TAG = "ECMTune_Task";
    private UI uinterface;
    private String failmsg;
    private RandomNumberFile rng;
    private long elapsedTime;
    private StringBuffer result;

    public ECMTune_Task(UI ui, RandomNumberFile rng)
    {
	this.uinterface = ui;
	this.rng = rng;
	this.failmsg = null;
	this.elapsedTime = 0;
	this.result = new StringBuffer();
    }

    /**
     */
    protected Integer doInBackground(Integer... params)
    {
	int rc = -1;
	long st;
	if (params.length < 1) {
	    return Integer.valueOf(-1);
	}
	st = System.currentTimeMillis();
	try {
	    int action = MainActivity.BENCH;
	    long seed;
	    seed = this.rng.nextInt();
	    if (seed < 0) {
		seed = 0x100000000L + seed;
	    }
	    String s = "seed=" + seed;
	    Log.d(TAG, s);
	    this.result.append(s);
	    this.result.append("\n");
	    action = params[0];
	    if (action == MainActivity.BENCH) {
		this.result.append("bench_mulredc...");
	        ECMTune.bench_mulredc(1);
	        rc = 0;
	    } else if (action == MainActivity.TUNE) {
		this.result.append("tune...");
		ECMTune.tune(1, seed);
		rc = 0;
	    } else {
	        this.result.append("unknown action");
	    }
	}
	catch (Exception e) {
	    Log.d(TAG, e.toString());
	    this.failmsg = e.getMessage();
	    rc = -1;
	}
	this.elapsedTime = System.currentTimeMillis() - st;
	return Integer.valueOf(rc);
    }
    protected void onPostExecute(Integer result)
    {
	if (result < 0) {
	    if (this.failmsg != null) {
		uinterface.display("ERROR: " + this.failmsg);
	    } else {
		uinterface.display("UNKNOWN ERROR");
	    }
	    return;
	}
	uinterface.display(this.result.toString());
	uinterface.display("done.");
	uinterface.display("random digits consumed so far: " + this.rng.consumed());
	uinterface.display("elapsed time: " + this.elapsedTime + " milliseconds");
    }

    protected void onPreExecute()
    {
	uinterface.display(TAG);
    }

    protected void onProgressUpdate(Integer... progress)
    {
    }
}
