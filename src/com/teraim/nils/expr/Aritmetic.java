package com.teraim.nils.expr;

//Variables associate values with names.
//Copyright 1996 by Darius Bacon; see the file COPYING.

import java.util.Hashtable;

import android.util.Log;

import com.teraim.nils.Constants;
import com.teraim.nils.dynamic.types.Variable;

/**
* A variable is a simple expression with a name (like "x") and a
* settable value.
*/
public class Aritmetic extends Expr implements Variable {
 
 /**
  * Return a unique variable named `name'.  There can be only one
  * variable with the same name returned by this method; that is,
  * make(s1) == make(s2) if and only if s1.equals(s2).
  * @param name the variable's name
  * @return the variable; create it initialized to 0 if it doesn't
  *         yet exist */
 

 private String name,label;
 private double val;

 /**
  * Create a new variable, with initial value NaN.
  * @param name the variable's name
  * @param label the name as presented in the ui.
  */
 public Aritmetic(String name, String label) { 
	this.name=name;
	this.label=label;
	val = Double.NaN; 
 }
 

 /** Return the name. */
 @Override
 public String toString() { return name; }
	
 @Override
 public String getName() {
		return name;
 }
	
 @Override
 public String getLabel() {
		return label;
 }
 
 
 
 /** Get the value.
  * @return the current value */
 @Override
 public double value() { 
	return val; 
 }
 /** Set the value.
  * @param value the new value */
 public void setValue(double value) { 
	val = value; 
 }


@Override
public Type getType() {
	return Type.ARITMETIC;
}


@Override
public void setValue(String value) {
	try {val=Double.parseDouble(value);}
	catch (NumberFormatException e) {
		Log.d("NILS","Numberinput in wrong format");
	}
	Log.d("NILS","Variable "+this.toString()+" set to "+val+" from string "+value);
}

}
