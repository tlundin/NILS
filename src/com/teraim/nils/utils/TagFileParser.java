package com.teraim.nils.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.GlobalState;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;

public class TagFileParser extends AsyncTask<GlobalState ,Integer,ErrorCode>{



	public static final int approxLines = 704;
	ProgressBar pb;
	TextView tv;
	FileLoadedCb cb;
	public TagFileParser(ProgressBar pb, TextView tv,FileLoadedCb cb) {
		this.pb=pb;
		this.tv=tv;
		this.cb=cb;
		pb.setMax(approxLines);
	}



	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(ErrorCode result) {
		cb.onFileLoaded(result);
	}



	@Override
	protected ErrorCode doInBackground(GlobalState... params) {
		int lineC=0;
		Log.d("nils","In scanDelningsData");
		GlobalState gs = params[0];

		try {
			InputStreamReader is = new InputStreamReader(gs.getContext().getResources().openRawResource(R.raw.delningspunkter_short));
			BufferedReader br = new BufferedReader(is);
			String row;
			String header = br.readLine();
			Log.d("NILS",header);
			//Find rutId etc
			while((row = br.readLine())!=null) {
				lineC++;
				String  r[] = row.split(",");
				String rutaID,provytaID,delytaID,year;
				if (r!=null&&r.length>5) {	
					year = r[0];
					rutaID = r[1];
					provytaID = r[3];
					delytaID = r[4];
					String tag="";
					for (int i=5;i<r.length;i++) {

						if (r[i]!=null && !r[i].equalsIgnoreCase("NULL") && !r[i].startsWith("-")) {
							tag+=r[i];
							if (i<r.length-1)
								tag+="|";
						}
					}
					//Insert to database.
					if(tag.length()>0) {

						String varId = "TAG";

						Variable v=null; 
						Map<String,String>keys = Tools.createKeyMap(VariableConfiguration.KEY_YEAR,year,"ruta",rutaID,"provyta",provytaID,"delyta",delytaID);
						if (keys!=null  && provytaID !=null && rutaID !=null && delytaID != null) {
							gs.setKeyHash(keys);
							v = gs.getArtLista().getVariableInstance(varId);
							if (v!=null)
								v.setValue(tag);
							else
								Log.e("nils","Variable null in TagfileParser");
							Log.d("nils","Tåg:"+tag+"[R:"+rutaID+" P:"+provytaID+" D:"+delytaID+"]");
							publishProgress(lineC);
						} else {
							Log.e("nils","Null error on line "+lineC+" rutaID: "+
									rutaID+" pyID: "+provytaID+" delytaID: "+delytaID+" Variable: "+v+" keys: "+keys.toString());
							br.close();
							return ErrorCode.configurationError;
						}

					}

				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return ErrorCode.ioError;
		}
		return ErrorCode.tagLoaded;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		tv.setText("Tåg loaded: "+values[0]+"/"+approxLines);
		pb.setProgress(values[0]);
	}

}


