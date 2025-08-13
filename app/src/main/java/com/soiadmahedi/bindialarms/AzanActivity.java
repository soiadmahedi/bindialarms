package com.soiadmahedi.bindialarms;

import android.os.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;

public class AzanActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.azan);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
	}
	
	
	private int getMaterialColor(int resourceId) {
		return MaterialColors.getColor(this, resourceId, "getMaterialColor");
	}
}
