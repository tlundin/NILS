package com.teraim.nils.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Numerable.Type;
import com.teraim.nils.dynamic.types.Ruta;
import com.teraim.nils.dynamic.types.SpinnerDefinition;
import com.teraim.nils.dynamic.types.SpinnerDefinition.SpinnerElement;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.log.DummyLogger;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.non_generics.Constants;
import com.teraim.nils.utils.DbHelper.Selection;

public class Tools {



	/** Read the object from Base64 string. */
	public static byte[] serialize(Serializable s) { 
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		ObjectOutputStream oos = null; 
		try { 
			oos = new ObjectOutputStream(baos); 
			oos.writeObject(s); 
		} catch (IOException e) { 
			Log.e("nils", e.getMessage(), e); 
			return null; 
		} finally { 
			try { 
				oos.close(); 
			} catch (IOException e) {} 
		} 
		byte[] result = baos.toByteArray(); 
		Log.d("nils", "Object " + s.getClass().getSimpleName() + "written to byte[]: " + result.length); 
		return result; 
	} 

	public static Object deSerialize(byte[] in) { 
		Object result = null; 
		ByteArrayInputStream bais = new ByteArrayInputStream(in); 
		ObjectInputStream ois = null; 
		try { 
			ois = new ObjectInputStream(bais); 
			result = ois.readObject(); 
		} catch (Exception e) { 
			result = null; 
		} finally { 
			try { 
				ois.close(); 
			} catch (Throwable e) { 
			} 
		} 
		return result; 
	}

	public static boolean writeToFile(String filename,String text) {
		PrintWriter out;
		try {
			out = new PrintWriter(filename);
			out.println(text);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean witeObjectToFile(Context context, Object object, String filename) {

		Log.d("nils","Writing frozen object to file "+filename);
		ObjectOutputStream objectOut = null;
		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(object);
			fileOut.getFD().sync();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objectOut != null) {
				try {
					objectOut.close();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}


	/**
	 * 
	 * @param context
	 * @param filename
	 * @return
	 */
	public static Object readObjectFromFile(Context context, String filename) {

		Object object = null;
		ObjectInputStream objectIn = null;
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			objectIn = new ObjectInputStream(fileIn);
			object = objectIn.readObject();

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (objectIn != null) {
				try {
					objectIn.close();
				} catch (IOException e) {
					// do nowt
				}
			}
		}

		return object;
	}



	//This cannot be part of Variable, since Variable is an interface.

	public static Type convertToType(String text) {
		Type[] types = Type.values();	
		//Special cases
		if (text.equals("number"))
			return Type.NUMERIC;
		for (int i =0;i<types.length;i++) {
			if (text.equalsIgnoreCase(types[i].name()))
				return types[i];

		}
		return null;
	}

	public static Unit convertToUnit(String unit) {
		Log.d("unit","unit is "+unit+" with length "+unit.length());
		if (unit == null ||unit.length()==0) {
			Log.d("unit","translates to undefined");
			return Unit.nd;
		}
		Unit[] units = Unit.values();
		if (unit.equals("%"))
			return Unit.percentage;
		for (int i =0;i<units.length;i++) {
			if (unit.equalsIgnoreCase(units[i].name()))
				return units[i];
		}
		return Unit.nd;				
	}

	public static void createFoldersIfMissing(File file) {
		final File parent_directory = file.getParentFile();

		if (null != parent_directory)
		{
			parent_directory.mkdirs();
		}
	}


	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}


	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}




	/*********************************************************
	 * 
	 * File Data Parsers.
	 */
	//scan csv file for Rutor. Create if needed.
	public static void scanRutData(InputStream csvFile,GlobalState gs) {
		InputStreamReader is = new InputStreamReader(csvFile);
		BufferedReader br = new BufferedReader(is);
		String header;
		try {
			String row;
			header = br.readLine();
			Log.d("nils",header);
			//Find all RutIDs from csv. Create Ruta Class for each.
			while((row = br.readLine())!=null) {
				String  r[] = row.split(",");
				if (r!=null&&r.length>3) {
					//						Log.d("NILS",r[0]);
					Ruta ruta=gs.findRuta(r[0]);
					if (ruta ==null) {
						ruta = new Ruta(gs,r[0]);
						gs.getRutor().add(ruta);
					}
					int id = Integer.parseInt(r[1]);
					//Skip IDs belonging to inner ytor.
					if (id>12&&id<17)
						continue;
					//					if (ruta.addProvYta_rutdata(r[1],r[2],r[3],r[7],r[8])!=null)
					;				//Log.d("NILS","added provyta with ID "+r[1]);
					//					else
					//					Log.d("NILS","discarded provyta with ID "+r[1]);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Calculate the distance between smallest and biggest x,y values
		//This is done to be able to calculate the grid position.
		Log.d("NILS","checking minmax...");
		//TODO: HOW TO DO THIS ONE
		/*
			for (Ruta r:gs.getRutor()) {
				Ruta.Sorted s = r.sort();
				Log.d("NILS","Ruta with id "+r.getId()+" has minxy: "+s.getMin_E_sweref_99()+" "+s.getMin_N_sweref_99()+
						" and maxXy: "+s.getMax_E_sweref_99()+" "+s.getMax_N_sweref_99());
			}
		 */
	}



	//scan csv file for Rutor. Create if needed.


	public static Map<String,String> createKeyMap(String ...parameters) {
		Map<String,String> ret = new HashMap<String,String>();
		boolean colName = true;
		String column=null;
		if ((parameters.length & 1) != 0 ) {
			Log.e("nils","createKeyMap needs an even number of arguments");
			return null;
		}

		for (String p:parameters) {
			if (colName) {
				colName = false;
				column = p;
			} else {
				colName = true;
				ret.put(column,p);
			}
		}
		return ret;
	}

	public static String getPrintedUnit(Unit unit) {
		if (unit == Unit.percentage)
			return "%";
		if (unit == Unit.nd || unit == null)
			return "";
		else
			return unit.name();
	}

	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}

	
	//Create a map of references to variables. 

	public static SpinnerDefinition scanSpinerDef(Context myC, LoggerI o) {

		int noOfC=-1;
		int noOfRequiredColumns=3;
		SpinnerDefinition sd=null;
		// Create dummylogger if o set to null.
		if (o==null)
			o = new DummyLogger();
		String fileUrl = Constants.SPINNER_DEF_URL;
		if (isNetworkAvailable(myC)) {
		
			URL url;
			try {
				url = new URL(fileUrl);
			o.addRow("Fetching spinner configuration from: "+fileUrl);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			InputStream in = ucon.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String header = br.readLine();
			int rowC=0;
			if (header == null)  {
				o.addRow("");
				o.addRedText("Spinner header corrupt. Spinnerfile likely missing");
				Log.e("nils","Spinner header corrupt");
				return null;
			} else
				noOfC = header.split(",").length;

			String row,spinnerID,value,opt,varMap,descr;
			sd = new SpinnerDefinition();
			List<SpinnerElement>sl = null;
			Log.d("nils","spinnerheader: "+header);
			String curId=null;
			while((row = br.readLine())!=null) {
				//Log.d("nils","SPINNING ROW: "+row);
				String[]  r = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1);
				if (r.length<noOfRequiredColumns) {
					Log.d("nils","TOO SHORT ROW on line in spinnerdef file."+rowC);
					o.addRow("");
					o.addRow("Line "+rowC+" of SpinnerDef file too short: "+row);
				} else {
					String id = r[0];
					if (curId==null || !id.equals(curId)) {
						sl = new ArrayList<SpinnerElement>();
						sd.add(id, sl);
						curId = id;
					}
					sl.add(sd.new SpinnerElement(r[1],r[2],r[3],r[4]));
				}
				rowC++;
			}
			Log.d("nils","loaded "+sd.size()+" spinner objects");
			//Write Spinner Def to file.
			boolean ok= Tools.witeObjectToFile(myC, sd, Constants.CONFIG_FILES_DIR+Constants.WF_FROZEN_SPINNER_ID);
			if (!ok) {
				Log.e("nils","Could not freeze spinner definition. I/O Error!");
			} else {
				Log.d("nils","Spinner def frozen");
				o.addRow("Spinner definition loaded: ");o.addGreenText("[OK]");
			}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  catch (FileNotFoundException e) {
				e.printStackTrace();
				o.addRow("");
				o.addRedText("Could not find the file at the specified location");
				return null;			
			}  catch (IOException e) {
				e.printStackTrace();
				o.addRow("IO ERROR!");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);		
				o.addRedText(sw.toString());
			}

		} else {		
			sd = (SpinnerDefinition)Tools.readObjectFromFile(myC,Constants.CONFIG_FILES_DIR+Constants.WF_FROZEN_SPINNER_ID);		
			if (sd==null) 
				Log.d("NILS","No frozen Spinner definition");

		}
		
		
		return sd;
	}
	
	public static Bitmap drawableToBitmap (Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static String[] generateList(GlobalState gs, Variable variable) {
		String[] opt=null;
		VariableConfiguration al = gs.getArtLista();
		LoggerI o = gs.getLogger();
		List<String >listValues = al.getListElements(variable.getBackingDataSet());
		Log.d("nils","Found dynamic list definition..parsing");
		
		if (listValues!=null&&listValues.size()>0) {
			String [] columnSelector = listValues.get(0).split("=");
			String[] column=null;
			boolean error = false;
			if (columnSelector[0].equalsIgnoreCase("@col")) {
				Log.d("nils","found column selector");
				//Column to select.
				String dbColName = gs.getDb().getColumnName(columnSelector[1]);
				if (dbColName!=null) {
					Log.d("nils","Real Column name for "+columnSelector[1]+" is "+dbColName);
					column = new String[1];
					column[0]=dbColName;
				} else {
					Log.d("nils","Column referenced in List definition for variable "+variable.getLabel()+" not found: "+columnSelector[1]);
					o.addRow("");
					o.addRedText("Column referenced in List definition for variable "+variable.getLabel()+" not found: "+columnSelector[1]);
					error = true;
				}
				if (!error) {
					//Any other columns part of key?
					Map<String,String>keySet = new HashMap<String,String>();
					if (listValues.size()>1) {
						//yes..include these in search
						Log.d("nils","found additional keys...");
						String[] keyPair;							
						for (int i=1;i<listValues.size();i++) {
							keyPair = listValues.get(i).split("=");
							if (keyPair!=null && keyPair.length==2) {
								String valx=al.getVariableValue(null,keyPair[1]);
								if (valx!=null) 										
									keySet.put(keyPair[0], valx);
								else {
									Log.e("nils","The variable used for dynamic list "+variable.getLabel()+" is not returning a value");
									o.addRow("");
									o.addRedText("The variable used for dynamic list "+variable.getLabel()+" is not returning a value");
								}
							} else {
								Log.d("nils","Keypair error: "+keyPair);
								o.addRow("");
								o.addRedText("Keypair referenced in List definition for variable "+variable.getLabel()+" cannot be read: "+keyPair);
							}
						}

					} else 
						Log.d("nils","no additional keys..only column");
					Selection s = gs.getDb().createCoulmnSelection(keySet);
					List<String[]> values = gs.getDb().getValues(column, s);
					if (values !=null) {
						Log.d("nils","Got "+values.size()+" results");
						//Remove duplicates and sort.
						SortedSet<String> ss = new TreeSet<String>(new Comparator<String>(){
							public int compare(String a, String b){
								return Integer.parseInt(a)-Integer.parseInt(b);
							}}						                         
								);
						for (int i = 0; i<values.size();i++) 
							ss.add(values.get(i)[0]);
						opt = new String[ss.size()];
						int i = 0; 
						Iterator<String> it = ss.iterator();
						while (it.hasNext()) {
							opt[i++]=it.next();
						}
					}
				} else
					opt=new String[]{"Config Error...please check your list definitions for variable "+variable.getLabel()};


			} else
				Log.e("nils","List "+variable.getId()+" has too few parameters: "+listValues.toString());
		} else
			Log.e("nils","List "+variable.getId()+" has strange parameters: "+listValues.toString());
		return opt;
		}
	
}