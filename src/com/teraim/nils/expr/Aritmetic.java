package com.teraim.nils.expr;

//Variables associate values with names.
//Copyright 1996 by Darius Bacon; see the file COPYING.

import java.util.Hashtable;

import android.util.Log;

import com.teraim.nils.CommonVars;
import com.teraim.nils.Variable;

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
 

 private String name;
 private double val;

 /**
  * Create a new variable, with initial value 0.
  * @param name the variable's name
  */
 public Aritmetic(String name) { 
	this.name = name; val = 0; 
 }
 
 
 public static Aritmetic make(String name) {
	return CommonVars.cv().makeAritmetic(name);
 }

 /** Return the name. */
 @Override
 public String toString() { return name; }
 @Override
 public String getName() { return name; }

 /** Get the value.
  * @return the current value */
 public double value() { 
	return val; 
 }
 /** Set the value.
  * @param value the new value */
 public void setValue(double value) { 
	val = value; 
 }


@Override
public String getType() {
	return Variable.ARITMETIC;
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
