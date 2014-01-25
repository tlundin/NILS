package com.teraim.nils.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.util.Log;

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
}
