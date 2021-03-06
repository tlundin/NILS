package com.teraim.nils.expr;

import android.util.Log;

//A lexical token from an input string.

class Token {
 public static final int TT_ERROR  = -1;
 public static final int TT_EOF    = -2;
 public static final int TT_NUMBER = -3;
 public static final int TT_WORD   = -4;
 public static final int TT_LE     = -5;
 public static final int TT_NE     = -6;
 public static final int TT_GE     = -7;
 public static final int TT_LT     = -8;
 public static final int TT_GT     = -9;
 public static final int TT_EQ     = -10;
 


 public Token(int ttype, double nval, String input, int start, int end) {
     this.ttype = ttype;
     this.sval = input.substring(start, end);
	this.nval = nval;
	this.location = start;
	
	int count = 0;
	
	Log.d("PARSER","Found token: "+sval);
	if (sval.length()==1) {
		if (Character.isLetter(sval.charAt(0)))
			Log.d("PARSER","...and this is a letter");
	}
	for (int i = start-1; 0 <= i; --i) {
	    if (!Character.isWhitespace(input.charAt(i)))
		break;
	    ++count;
	}
	this.leadingWhitespace = count;

	count = 0;
	for (int i = end; i < input.length(); ++i) {
	    if (!Character.isWhitespace(input.charAt(i)))
		break;
	    ++count;
	}
	this.trailingWhitespace = count;
 }

 Token(int ttype, double nval, String sval, Token token) {
	this.ttype = ttype;
	this.sval = sval;
	this.nval = nval;
	this.location = token.location;
	this.leadingWhitespace = token.leadingWhitespace;
	this.trailingWhitespace = token.trailingWhitespace;
 }

 public final int ttype;
 public final String sval;
 public final double nval;

 public final int location;

 public final int leadingWhitespace, trailingWhitespace;
}