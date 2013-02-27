package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.exceptions.IllegalCallException;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.Aritmetic;
import com.teraim.nils.expr.Expr;
import com.teraim.nils.expr.Literal;
import com.teraim.nils.expr.Parser;
import com.teraim.nils.expr.SyntaxException;


/**
 * 
 * @author Terje
 *
 * Classes defining datatypes for ruta, provyta, delyta and tåg.
 * There are two Scan() functions reading data from two input files (found under the /raw project folder).
 */
public class DataTypes  {

	static DataTypes singleton;
	public static DataTypes getSingleton(Context c) {
		if (singleton == null) {
			singleton = new DataTypes();
			singleton.scanRutData(c.getResources().openRawResource(R.raw.rutdata));
			singleton.scanDelningsData(c.getResources().openRawResource(R.raw.delningsdata));
		}
		return singleton;
	}


	private ArrayList<Ruta> rutor = new ArrayList<Ruta>();

	/*
	public abstract static class Variable {
		String uniqueName;

	}

	public static class Int_Variable extends Variable {
		int value;

		Int_Variable(String _name, int _value) {
			uniqueName = _name;
			value = _value;
		}

	}
	public static class String_Variable extends Variable {
		String value;

		String_Variable(String _name, String _value) {
			uniqueName = _name;
			value = _value;
		}

	}
	 */


	//Workflow
	public static class Workflow {
		private List<Block> blocks;
		private String name=null;

		public List<Block> getBlocks() {
			return blocks;
		}
		public void addBlocks(List<Block> _blocks) {
			blocks = _blocks;
		}

		public String getName() {
			if (name==null) {
				if (blocks!=null && blocks.size()>0)
					name = ((StartBlock)blocks.get(0)).getName();

			}
			return name;
		}

	}

	/**
	 * XML_Variable
	 * Used to describe variables defined in XML
	 */
	public static class XML_Variable {		
		String name,label,type,purpose;
	}

	/**
	 * Abstract base class Block
	 * Marker class.
	 * @author Terje
	 *
	 */
	public abstract static class Block {

	}

	/**
	 * Startblock.
	 * @author Terje
	 *
	 */
	public static class StartBlock extends Block {
		private String workflowName;

		public StartBlock(String lbl,String wfn) {
			workflowName = wfn;
		}

		public String getName() {
			return workflowName;
		}
	}

	/**
	 * buttonblock
	 * 
	 * name is ID for now..
	 * 
	 * @author Terje
	 *
	 */
	public static class ButtonBlock extends Block {
		String text,action,name;

		public ButtonBlock(String lbl,String text, String action, String name) {
			Log.d("NILS","ButtonText is set to "+text);
			this.text = text;
			this.action=action;
			this.name=name;
		}

		public String getText() {
			return text;
		}

		public Action getAction() {
			return new Action(action);
		}

		public String getName() {
			return name;
		}

		public class Action {
			public final static int VALIDATE = -1;
			public final static int WF_EXECUTE = -2;

			private int type;
			public String wfName=null;
			public Action(String t) {
				if (t.equals("validate"))
					type = VALIDATE;
				else
					type = WF_EXECUTE;
				wfName = t;
				Log.e("NILS","Workflowname in ACTION is "+t+" with length "+t.length());
			}
			public boolean isWorkflow() {
				return type==WF_EXECUTE;
			}
		}
	}

	/**
	 * CreateFieldBlock.
	 * @author Terje
	 *
	 */
	public static class CreateFieldBlock extends Block {
		//Create a fieldblock.
		//SIDEEFFECT: Creates Variable if not already created.
		XML_Variable mXvar;

		public CreateFieldBlock(XML_Variable Xvar) throws EvalException {
			mXvar = Xvar;
			Variable var = CommonVars.cv().getVariable(Xvar.name);
			//IS this a new variable? 
			if (var==null) {
				//Integer is created as a Aritmetic.
				if (Xvar.type==null||
						Xvar.type.equals(Variable.NUMERIC))
					CommonVars.cv().makeNumeric(Xvar.name,Xvar.label);
				else if (Xvar.type.equals(Variable.ARITMETIC))
					CommonVars.cv().makeAritmetic(Xvar.name,Xvar.label);
				else if (Xvar.type.equals(Variable.LITERAL))
					CommonVars.cv().makeLiteral(Xvar.name,Xvar.label);
				else if (Xvar.type.equals(Variable.BOOLEAN))
					CommonVars.cv().makeBoolean(Xvar.name,Xvar.label);

				Log.d("NILS", "Var created with name "+Xvar.name);
			} else {
				if (!Xvar.type.equals(var.getType()))
					throw new EvalException("Variable "+var.getName()+" has inconsistent type in CreateField blocks");
			}
		}
		public boolean isEditable() {
			return mXvar.equals("edit");
		}

		public String getVariableReference() {
			return mXvar.name;
		}

		public String getType() {
			return mXvar.type;
		}

	}

	/**
	 * CreateFieldBlock.
	 * @author Terje
	 *
	 */
	public static class CreateListEntryBlock extends Block {
		//Create a fieldblock.
		//SIDEEFFECT: Creates Variable if not already created.
		ArrayList<XML_Variable> mXvars;
		String myName;

		public CreateListEntryBlock(ArrayList<XML_Variable> Xvars, String name) throws EvalException {
			mXvars = Xvars;
			myName = name;
			for (XML_Variable Xvar:Xvars) {
				Variable var = CommonVars.cv().getVariable(Xvar.name);
				//IS this a new variable? 
				if (var==null) {
					//Integer is created as a Aritmetic.
					if (Xvar.type==null||
							Xvar.type.equals(Variable.NUMERIC))
						CommonVars.cv().makeNumeric(Xvar.name,Xvar.label);
					else if (Xvar.type.equals(Variable.ARITMETIC))
						CommonVars.cv().makeAritmetic(Xvar.name,Xvar.label);
					else if (Xvar.type.equals(Variable.LITERAL))
						CommonVars.cv().makeLiteral(Xvar.name,Xvar.label);
					else if (Xvar.type.equals(Variable.BOOLEAN))
						CommonVars.cv().makeBoolean(Xvar.name,Xvar.label);

					Log.d("NILS", "Var created with name "+Xvar.name);
				} else {
					if (!Xvar.type.equals(var.getType()))
						throw new EvalException("Variable "+var.getName()+" has inconsistent type in CreateField blocks");
				}
			}
		}

		public String getName() {
			return myName;
		}
		public ArrayList<XML_Variable> getVariables() {
			return mXvars;
		}
	}



	/**
	 * setvalueblock.
	 * @author Terje
	 *
	 */
	public static class SetValueBlock extends Block {
		private String varRef;
		private String expr;

		//Save references.
		public SetValueBlock(String lbl,String _varReference,String _expr) {
			varRef = _varReference;
			expr = _expr;
		}

		//Assign value of Expr to Variable.
		public void run() throws EvalException, SyntaxException {
			//Var Ref must refer to an existing variable.		
			Variable var = CommonVars.cv().getVariable(varRef);
			if (var==null)
				throw new EvalException("Variable does not exist");
			//Should expression be evaluated?
			if (var.getType().equals(Variable.ARITMETIC)||
					var.getType().equals(Variable.NUMERIC)) {
				double val =-1;
				val = Parser.parse(expr).value();
				((Aritmetic)var).setValue(val);
				Log.d("NILS","Expr: "+expr+" evaluated to: "+val);
			} else {
				((Literal)var).setValue(expr);

			}
		}



	}

	/**
	 * Layoutblock
	 * @author Terje
	 *
	 */
	public static class LayoutBlock extends Block {

		private String layoutDirection="", alignment="";

		public String getLayoutDirection() {
			return layoutDirection;
		}
		public String getAlignment() {
			return alignment;
		}
		public LayoutBlock(String lbl, String layoutDirection, String alignment) {
			this.layoutDirection = layoutDirection;
			this.alignment = alignment;
		}
	}


	/**
	 * AddRuleBlock
	 * @author Terje
	 *
	 */
	public static class Rule {

		public String targetName, condition, action, errorMsg,name;

		public Rule(String ruleName, String target, String condition,
				String action, String errorMsg) {
			this.name=ruleName;
			this.targetName=target;
			this.condition=condition;
			this.action=action;
			this.errorMsg=errorMsg;
			Log.e("NILS","Create Rule with name "+ruleName+" and target "+target+" and cond "+ condition);
			
		}
		public Variable getTarget() throws RuleException {
			Variable var = CommonVars.cv().getVariable(targetName);
			if (var==null)
				throw new RuleException("Variable "+targetName+" must exist");
			
			return var;
		}	
		//Execute Rule. Target will be colored accordingly.
		public boolean execute() throws SyntaxException {
			Expr result=null;
			result = Parser.parse(condition);
			Log.d("NILS","Result of eval was: "+result.value());
			return (result.value()==1.0);
		}

		public String getErrorMessage() {
			return errorMsg;
		}
		public String getName() {
			return name;
		}	
	}

	public static class AddRuleBlock extends Block {

		private Rule r;

		public AddRuleBlock(String lbl, String ruleName,String target, String condition, String action, String errorMsg) {
			this.r = new Rule(ruleName,target,condition,action,errorMsg);

		}

		public Rule getRule() {
			return r;
		}


	}

	///ValuePair

	public class ValuePair {
		public String mkey,mval;
		public ValuePair(String key, String val) {
			mkey=key;
			mval=val;
		}
	}


	//Train class stores the "TÅG" in swedish, i.e. the dividing lines crossing the Provyta (TestArea).
	//Train defined by points in a circle. Each point is described as an angle (rikt) and a distance (dist).
	//There can be up to 8 points per Train but there must be an equal number of Avst/Rikt, so
	//setAvst and setRikt needs be called equal number of times. 


	public class Train  {
		static final int Max_Points = 10;
		final int[] avst;
		final int[] rikt;
		private int current;


		boolean nick;
		boolean carter;

		public Train() {
			nick = carter = false;
			avst=new int[Max_Points];
			rikt=new int[Max_Points];
			current=0;
		}
		public void setAvst(int avs) throws IllegalCallException {
			if(!nick) {
				avst[current]=avs;
				nick = true;
				checkIfNext();
			} else
				throw new IllegalCallException();

		}
		public void setRikt(int rik) throws IllegalCallException {
			if(!carter) {
				rikt[current]=rik;
				carter = true;
				checkIfNext();
			} else
				throw new IllegalCallException();

		}
		private void checkIfNext() {
			if (nick&carter) {
				current++;
				nick = carter = false;
			}
		}

		public int getSize() {
			return current;
		}

		public int[][] getTag() {
			if (current==0)
				return null;
			int ret[][]= new int[current][2];
			for(int i=0;i<current;i++) {
				ret[i][0]=avst[i];
				ret[i][1]=rikt[i];
			}
			return ret;
		}
	}
	public class Delyta extends ParameterCache {
		final int Max_Points = 10;
		private Train tr=null; 
		private final String myId;


		public Delyta(String id, String[] raw) {

			myId = id;

			setPoints(raw);

		}
		public int[][] getPoints() {
			if(tr!=null)
				return tr.getTag();
			else
				return null;
		}
		public String getId() {
			return myId;
		}
		public boolean setPoints(String[] tag) {
			int val = -1;
			boolean avst = true;

			//Put -999 to signal null value.
			if (tag!=null) {
				tr = new Train();
				for (String s:tag) {

					try {
						val = Integer.parseInt(s);
					} catch(NumberFormatException e) {
						//If error, break! 
						if (!s.equals("NA"))
							Log.e("NILS", "Not a number in delytedata: "+s);
						return false;
					}
					if (val<0) {
						return false;
					}

					//If avst is true, the AVSTÅND will be set and the arraypointer moved forward.
					if (avst) {

						avst = false;
						try {
							tr.setAvst(val);
						} catch (IllegalCallException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
					} else {
						try {
							tr.setRikt(val);
						} catch (IllegalCallException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
						avst = true;
					}

				}
			}
			return true;
		}


	}

	protected class Provyta {

		private String id;
		double N=0;
		double E=0;
		double lat=0;
		double longh=0;

		private ArrayList<Delyta>dy = new ArrayList<Delyta>();


		public Provyta(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public double[] getLatLong() {
			double[] ret = new double[2];
			ret[0]=lat;
			ret[1]=longh;
			return ret;
		}
		public void setSweRef(double n, double e) {
			N = n;
			E = e;
		}
		public void setGPS(double lat, double longh) {
			this.lat = lat;
			this.longh = longh;
		}

		//ADD will add the delyta if new. Otherwise it will update the current value.
		public void addDelyta(String delyteId, String[] raw) {

			dy.add(new Delyta(delyteId,raw));
		}

		public Delyta findDelyta(String delyteId) {
			for(Delyta d:dy)
				if(d.getId().equals(delyteId))
					return d;
			return null;
		}

		public ArrayList<Delyta>getDelytor() {
			return dy;
		}

		public void updateDelyta(int index, String[] tag) {
			Delyta d = dy.get(index);
			d.setPoints(tag);
		}
	}


	protected class Ruta {
		private String myId;

		private ArrayList<Provyta> provytor = new ArrayList<Provyta>();

		public Ruta(String id) {
			myId = id;
		}

		public String getId() {
			return myId;
		}

		private void addDelYta(String provYteId,String delyteId,String[] raw) {

			if (provYteId != null) {
				Provyta _py = findProvYta(provYteId);
				if (_py==null) {
					Log.e("NILS","Provyta with id "+provYteId+" not  found in rutdata but found in delningsdata");
					//_py = new ProvYta(provYteId);
					//py.add(_py);

				} else
					_py.addDelyta(delyteId, raw);
			}
		}

		public Provyta addProvYta_rutdata(String ytId, String north, String east, String lat, String longh) {
			Provyta yta = new Provyta(ytId);
			try {

				yta.setSweRef(Double.parseDouble(north),Double.parseDouble(east));
				Log.d("NILS","Adding Yta ID:  N E:"+ytId+" "+ Double.parseDouble(north)+" "+Double.parseDouble(east));
				yta.setGPS(Double.parseDouble(lat),Double.parseDouble(longh));
			} catch (NumberFormatException e) {
				Log.d("NILS","The center coordinates for yta "+ytId+" are not recognized as proper doubles");
				return null;
			}
			provytor.add(yta);
			//Add default 0 delyta.
			yta.addDelyta("0", null);
			return yta;
		}
		public ArrayList<Provyta> getAllProvYtor() {
			return provytor;
		}

		public Sorted sort() {
			Sorted s = new Sorted();
			return s;
		}

		public LatLng[] getCorners() {
			//North south
			double[] lat = new double[provytor.size()];
			//East west
			double[] lon = new double[provytor.size()];
			int i = 0;

			for(Provyta y:provytor) {
				lat[i]= y.lat;
				lon[i]= y.longh;
				Log.d("NILS","SN: "+y.N+" SE: "+y.E);
				i++;
			}
			Arrays.sort(lat);
			Arrays.sort(lon);
			LatLng[] ret = new LatLng[2];
			//sw
			ret[0] = new LatLng(lat[0],lon[0]);
			//ne
			ret[1]= new LatLng(lat[lat.length-1],  lon[lon.length-1]);
			return ret;
		}

		public class Sorted {
			double[] N = new double[provytor.size()];
			double[] E = new double[provytor.size()];
			public Sorted() {
				int i = 0;
				for(Provyta y:provytor) {
					N[i]= y.N;
					E[i]= y.E;
					Log.d("NILS","SN: "+y.N+" SE: "+y.E);
					i++;
				}
				Arrays.sort(N);
				Arrays.sort(E);
			}
			//return minx,miny,maxx,maxy
			public double getMax_N_sweref_99() {
				return N[N.length-1];
			}
			public double getMax_E_sweref_99() {
				return E[E.length-1];
			}
			public double getMin_N_sweref_99() {
				return N[0];
			}
			public double getMin_E_sweref_99() {
				return E[0];
			}
		}



		public Provyta findProvYta(String ytId) {
			for(Provyta y:provytor) {
				if(y.id.equals(ytId)) {
					return y;
				}

			}
			return null;
		}


	}

	public Ruta findRuta(String id) {
		if (id == null)
			return null;
		for (Ruta r:rutor) 
			if (r.getId().equals(id))
				return r;
		return null;
	}

	public String[] getRutIds() {
		if (rutor != null) {
			String[] contents = new String[rutor.size()];		
			int i=0;
			for (Ruta r:rutor)
				contents[i++]=r.getId();
			return contents;
		}
		return null;
	}

	public ArrayList<Delyta> getDelytor(String rutId, String provyteId) {
		Ruta r = findRuta(rutId);
		if (r!=null) {
			Log.d("NILS","found ruta "+ rutId);
			Provyta p = r.findProvYta(provyteId);
			if (p!=null) {
				Log.d("NILS","Found provyta"+ provyteId);			
				return (p.getDelytor());
			} else {
				Log.e("NILS","DID NOT FIND Provyta for id "+provyteId);
				//TODO: Files must contains same provytor!
				//Fix for now: Generate default if missing.
				//p.addDelyta("1", null);
				//r.addProvYta(provyteId, "1", null);
				//return getDelytor(rutId,provyteId);
			}
		} else
			Log.e("NILS","DID NOT FIND RUTA "+ rutId);
		return null;
	}
	//scan csv file for Rutor. Create if needed.
	private void scanRutData(InputStream csvFile) {
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
					Log.d("NILS",r[0]);
					Ruta ruta=findRuta(r[0]);
					if (ruta ==null) {
						ruta = new Ruta(r[0]);
						rutor.add(ruta);
					}
					int id = Integer.parseInt(r[1]);
					//Skip IDs belonging to inner ytor.
					if (id>12&&id<17)
						continue;
					if (ruta.addProvYta_rutdata(r[1],r[2],r[3],r[7],r[8])!=null)
						Log.d("NILS","added provyta with ID "+r[1]);
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
		for (Ruta r:rutor) {
			Ruta.Sorted s = r.sort();
			Log.d("NILS","Ruta with id "+r.getId()+" has minxy: "+s.getMin_E_sweref_99()+" "+s.getMin_N_sweref_99()+
					" and maxXy: "+s.getMax_E_sweref_99()+" "+s.getMax_N_sweref_99());
		}
	}

	//scan csv file for Rutor. Create if needed.
	private void scanDelningsData(InputStream csvFile) {
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
					Ruta ruta = findRuta(r[2]);
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
		//Calculate the distance between smallest and biggest x,y values
		//This is done to be able to calculate the grid position.

	}





}
