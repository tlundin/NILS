package com.teraim.nils.dynamic.types;

import com.teraim.nils.non_generics.DelyteManager.Coord;

public class Segment {
	public Coord start,end;
	public boolean isArc;
	public Segment(Coord start, Coord end, boolean isArc) {
		super();
		this.start = start;
		this.end = end;
		this.isArc = isArc;
	}
	public boolean within(Segment s) {
		if (s.start.rikt>s.end.rikt) {
			return ((start.rikt>=s.start.rikt||start.rikt<=s.end.rikt)&&
					(end.rikt<=s.end.rikt||end.rikt>=s.start.rikt));

		} else {

			return ((start.rikt<=s.start.rikt||start.rikt>=s.end.rikt)&&
					(end.rikt>=s.end.rikt || end.rikt<=s.start.rikt));
		}



	}

}
