package com.teraim.nils;

import android.app.FragmentTransaction;
import android.os.Bundle;

public class FixPunktFragment extends GestureFragment {

	@Override
	protected void onRight() {
		final FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction(); 
		GestureFragment gs = new FixPunktFragment();
		Bundle b = new Bundle();
		b.putString("butt", "mjao");
		gs.setArguments(b);
		ft.replace(R.id.content_frame, gs);
		ft.addToBackStack(null);
		ft.commit(); 
	}

	@Override
	protected void onLeft() {
		// TODO Auto-generated method stub
		
	}

}
