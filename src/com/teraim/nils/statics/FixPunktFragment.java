package com.teraim.nils.statics;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;

public class FixPunktFragment extends Fragment {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("nils","in onCreateView of fixpunkt_fragment");
		View v = inflater.inflate(R.layout.template_fixpunkt_wf, container, false);	
		return v;
	}
}
