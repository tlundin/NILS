package com.teraim.nils;

import android.util.Log;



public class Geomatte {
	final static double d2r = (Math.PI / 180.0);
	//calculate haversine distance for linear distance

	
	static double dist(double lat1, double long1, double lat2, double long2)
	{
	    double dlong = (long2 - long1) * d2r;
	    double dlat = (lat2 - lat1) * d2r;
	    double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(lat1*d2r) * 
	    		Math.cos(lat2*d2r) * Math.pow(Math.sin(dlong/2.0), 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = 6367 * c;

	    return d;
	}
	
	static double sweDist(double myY,double myX,double destY, double destX) {	
		Log.d("NILS","diffX: diffY: "+(myX-destX)+" "+(myY-destY));
		Log.d("NILS","Values  x1 y1 x2 y2: "+myX+" "+myY+" "+destX+" "+destY);
		return Math.sqrt(Math.pow((myX-destX),2)+Math.pow(myY-destY, 2));
		
	}
	
	
		
	
	static double getRikt(double dest, double centerY, double centerX, double destY, double destX) {
		//dest is Opposite side length.
		//We still 
		double b = destX-centerX;
		//double a = Math.abs(destY-centerY); **not needed.
		double c = dest;
		
		double  beta = Math.acos(b/c);
		Log.d("NILS","b,c,beta: "+b+" "+c+" "+beta);
		//Gamma is the top angle in a 90 deg. triangle.
		double gamma = Math.PI/2-beta; // 90 grader - beta i radianer = 90*pi/180 = 1*pi/2.
		//alfa is PI+gamma if destx - x is negative.
		double alfa =  Math.PI+gamma;
		Log.d("NILS","gamma: "+gamma);
		
		//Alfa should also be equal to Atan2(y,x).
		double alfa2 = Math.atan2(destY, destX);
		Log.d("NILS","ALFA: "+alfa+" ALFA (tan2): "+alfa2);
		return alfa;
		
		
	}
	
	
	private static double math_atanh(double value) {
		return 0.5 * Math.log((1.0 + value) / (1.0 - value));
	}
	
	
	
	public static double[] convertToSweRef(double lat, double lon) {
	
	double a = 6378137.0000;
	double f = 1.0/298.257222101;
	double e2 = f*(2.0-f);
	double cent_m = 15.0;
	double k0 = 0.9996;
	double FN = 0;
	double FE = 500000;
	double n = f/(2.0-f);
	double ap = a/(1.0+n)*(1.0+(Math.pow(n,2)/4.0)+(Math.pow(n,4)/64.0));

	double latr = lat*Math.PI/180.0;
	double lambda = lon*Math.PI/180.0;
	double lambda0 = cent_m*Math.PI/180.0;
	double deltalambda = lambda-lambda0;
	double A = e2;
	double B = (1.0/6.0)*(5.0*Math.pow(e2, 2)-Math.pow(e2, 3));	
	double C = (1.0/120.0)*(104.0*Math.pow(e2, 3)-45.0*Math.pow(e2, 4));
	double D = (1.0/1260.0)*(1237.0*Math.pow(e2, 4));
	double latStar = latr-Math.sin(latr)*Math.cos(latr)*(A+B*Math.pow(Math.sin(latr),2)+
			C*Math.pow(Math.sin(latr), 4)+D*Math.pow(Math.sin(latr),6));
	double oui = Math.atan(Math.tan(latStar)/Math.cos(deltalambda));
	double nui = math_atanh(Math.cos(latStar)*Math.sin(deltalambda));
	
	double b1 = n/2.0-(2.0*n*n)/3.0+(5.0/16.0)*Math.pow(n,3)+(41.0/180.0)*Math.pow(n, 4);
	double b2 = (13.0/48.0)*n*n-(3.0/5.0)*Math.pow(n,3)+(557.0/1440.0)*Math.pow(n, 4);
	double b3 = (61.0/240.0)*Math.pow(n, 3)-(103.0/140.0)*Math.pow(n, 4);
	double b4 = (49561.0/161280.0)*Math.pow(n, 4);

			
	double x = k0*ap*(oui+
			b1*Math.sin(2.0*oui)*Math.cosh(2.0*nui)+
			b2*Math.sin(4.0*oui)*Math.cosh(4.0*nui)+
			b3*Math.sin(6.0*oui)*Math.cosh(6.0*nui)+
			b4*Math.sin(8.0*oui)*Math.cosh(8.0*nui))+FN;
	double y = k0*ap*(nui+
			b1*Math.cos(2.0*oui)*Math.sinh(2.0*nui)+
			b2*Math.cos(4.0*oui)*Math.sinh(4.0*nui)+
			b3*Math.cos(6.0*oui)*Math.sinh(6.0*nui)+
			b4*Math.cos(8.0*oui)*Math.sinh(8.0*nui))+FE;
	x = Math.round(x * 1000.0) / 1000.0;
	y = Math.round(y * 1000.0) / 1000.0;

	Log.d("NILS"," lat long "+x+" "+y);
	double[] ret = new double[2];
	ret[0]=x;
	ret[1]=y;
	return ret;
	}

	
}