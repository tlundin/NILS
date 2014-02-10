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
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.dynamic.types.Ruta;
import com.teraim.nils.dynamic.types.Variable.Type;
import com.teraim.nils.dynamic.types.Workflow.Unit;

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
	
	
	   public static boolean witeObjectToFile(Context context, Object object, String filename) {

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
	                    // do nowt
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
	            // Do nothing
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } finally {
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
	
	
	public static void printDatabase(DbHelper db) {
		Log.d("NILS","Printing all database values in init");
    	ArrayList<StoredVariable> sds = db.getAllVariables();
    	int i=0;
    	for(StoredVariable s:sds) 
    		Log.d("nils",(i++) + ": "+s.getRutId()+","+s.getProvytaId()+","+s.getDelytaId()+","+s.getVarId()+":  "+s.getValue());
		
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
						if (ruta.addProvYta_rutdata(r[1],r[2],r[3],r[7],r[8])!=null)
			;				//Log.d("NILS","added provyta with ID "+r[1]);
						else
							Log.d("NILS","discarded provyta with ID "+r[1]);

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Calculate the distance between smallest and biggest x,y values
			//This is done to be able to calculate the grid position.
			Log.d("NILS","checking minmax...");
			for (Ruta r:gs.getRutor()) {
				Ruta.Sorted s = r.sort();
				Log.d("NILS","Ruta with id "+r.getId()+" has minxy: "+s.getMin_E_sweref_99()+" "+s.getMin_N_sweref_99()+
						" and maxXy: "+s.getMax_E_sweref_99()+" "+s.getMax_N_sweref_99());
			}
		}


	
	//scan csv file for Rutor. Create if needed.
	public static void scanDelningsData(InputStream csvFile, GlobalState gs) {
		InputStreamReader is = new InputStreamReader(csvFile);
		BufferedReader br = new BufferedReader(is);
		final int noPo = 16;
		try {
			String row;
			String header = br.readLine();
			Log.d("NILS",header);
			//Find rutId etc
			while((row = br.readLine())!=null) {
				String  r[] = row.split("\t");
				if (r!=null) {	
					if (r[2]==null)
						continue;
					Ruta ruta = gs.findRuta(r[2]);
					//if this is a new ruta, add it to the array
					if (ruta!=null) {
						//Extract the delningståg out from the data.
						String[] points = new String[noPo];
						System.arraycopy(r, 6, points, 0, noPo);
						ruta.addDelYta(r[4],r[5],points);
					}
					//TODO: Add this as ELSE when the files match. 
					//Currently only Rutor from Rutdata will matter.
					/* ruta = new Ruta(r[2]);
						rutor.add(ruta);
					 */

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
