package com.soiadmahedi.bindialarms;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.google.android.material.button.*;
import com.google.android.material.color.MaterialColors;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private ArrayList<HashMap<String, Object>> listmap_alarms = new ArrayList<>();
	
	private NestedScrollView vscroll_bg;
	private LinearLayout linear_bg;
	private CardView cardview_alarm_display;
	private LinearLayout linear1;
	private RecyclerView recyclerview;
	private TimePicker timepicker;
	private EditText edittext_title;
	private EditText edittext_description;
	private EditText edittext_code;
	private Switch switch_isRepeating;
	private MaterialButton materialbutton_set_alarms;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		vscroll_bg = findViewById(R.id.vscroll_bg);
		linear_bg = findViewById(R.id.linear_bg);
		cardview_alarm_display = findViewById(R.id.cardview_alarm_display);
		linear1 = findViewById(R.id.linear1);
		recyclerview = findViewById(R.id.recyclerview);
		timepicker = findViewById(R.id.timepicker);
		edittext_title = findViewById(R.id.edittext_title);
		edittext_description = findViewById(R.id.edittext_description);
		edittext_code = findViewById(R.id.edittext_code);
		switch_isRepeating = findViewById(R.id.switch_isRepeating);
		materialbutton_set_alarms = findViewById(R.id.materialbutton_set_alarms);
		
		materialbutton_set_alarms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				String title = edittext_title.getText().toString();
				String description = edittext_description.getText().toString();
				int requestCode = Integer.parseInt(edittext_code.getText().toString());
				if (!PermissionUtils.isIgnoringBatteryOptimizations(MainActivity.this)) {
					PermissionUtils.requestIgnoreBatteryOptimizationsIfNeeded(MainActivity.this);
				} else {
					if (!PermissionUtils.hasExactAlarmPermission(MainActivity.this)) {
						PermissionUtils.requestExactAlarmPermission(MainActivity.this);
					} else {
						AzanAlarmUtils.scheduleAppAlarm(MainActivity.this, timepicker.getHour(), timepicker.getMinute(), title, description, switch_isRepeating.isChecked(), requestCode);
					}
				}
				_REFRESH_ALARMS_LIST();
			}
		});

		View decorView = getWindow().getDecorView();
		decorView.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
			@NonNull
			@Override
			public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                return insets;
			}
		});

	}
	
	private void initializeLogic() {
		_REFRESH_ALARMS_LIST();
	}
	
	public void _REFRESH_ALARMS_LIST() {
		ArrayList<HashMap<String, Object>> list_this = AzanAlarmUtils.getActiveAlarms(this);
		recyclerview.setAdapter(new RecyclerviewAdapter(list_this));
		recyclerview.setLayoutManager(new LinearLayoutManager(this));
	}
	
	public class RecyclerviewAdapter extends Adapter<RecyclerviewAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public RecyclerviewAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.alarms_items, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, @SuppressLint("RecyclerView") final int _position) {
			View _view = _holder.itemView;
			
			final com.google.android.material.card.MaterialCardView cardview_alarm_bg = _view.findViewById(R.id.cardview_alarm_bg);
			final LinearLayout linear_xbg = _view.findViewById(R.id.linear_xbg);
			final TextView textview_icon_emoji = _view.findViewById(R.id.textview_icon_emoji);
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final ImageView imageview_btn_dlt = _view.findViewById(R.id.imageview_btn_dlt);
			final TextView textview_title = _view.findViewById(R.id.textview_title);
			final TextView textview_info = _view.findViewById(R.id.textview_info);
			
			textview_title.setText(_data.get((int)_position).get("title").toString());
			textview_info.setText(_data.get((int)_position).get("description").toString().concat(" • ".concat(_data.get((int)_position).get("hour").toString().concat(":".concat(_data.get((int)_position).get("minute").toString())))));
			imageview_btn_dlt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					AzanAlarmUtils.cancelAppAlarm(MainActivity.this, (int) _data.get((int)_position).get("requestCode"));
					_REFRESH_ALARMS_LIST();
				}
			});
		}
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
	
	private int getMaterialColor(int resourceId) {
		return MaterialColors.getColor(this, resourceId, "getMaterialColor");
	}
}
