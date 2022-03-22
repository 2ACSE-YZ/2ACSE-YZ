package com.stripe1.xmouse;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyInterface, NavigationView.OnNavigationItemSelectedListener,
		CoolDragAndDropGridView.DragAndDropListener,
		SpanVariableGridView.OnItemClickListener,
		SpanVariableGridView.OnItemLongClickListener,
		myDialogFragment.NoticeDialogListener,
		SensorEventListener{

	MenuItem conDiscButton = null;
	MenuItem KeyLockButton = null;
	MenuItem InvScrollButton= null;
	FragmentManager fm = getSupportFragmentManager();

	public static DatabaseHandler db;
	public static MyConnectionHandler conn;

	private int pointing_device=-1;
	private int gyro_function=-1;

	private int mouseButtonsLayout=0;

//	boolean s=true;

	LinearLayout canvasLayout;
	LinearLayout keyboardLayout;


	public static ArrayList<ArrayList<String>> hostDBKeys;
	public static String setting_host="";
	public static String setting_user="";
	public static int setting_port=22;
	public static String setting_pass="";
	public static String setting_shell = "";

	public static int setting_pointing_device=1;
	public static int setting_gyro_function = 1;

	public static float setting_mouse_sensitivity =1.5f;
	public static int setting_mouse_delay =350;
	public static int setting_mouse_mdelay =0;

	public static float setting_hscroll_sensitivity=1.0f;
	public static float setting_vscroll_sensitivity=1.0f;


	public static float setting_gyro_z_sensitivity=1.5f;
	public static float setting_gyro_y_sensitivity=1.5f;
	public static float setting_gyro_x_sensitivity=1.5f;
	public static float setting_gyro_z_threshold=1.5f;
	public static float setting_gyro_y_threshold=1.5f;
	public static float setting_gyro_x_threshold=1.5f;


	public static float setting_js_sensitivity=1.0f;
	public static float setting_js_size=0.5f;
	public static float setting_js_dead_zone=0.0f;



	boolean setting_autoconnect=false;
	private static boolean setting_keyboard_locked=false;
	public static String setting_xdotool_initial="";
	//static boolean setting_mouse_background=false;
	private static boolean setting_keyboard_batch=true;
	private boolean setting_keyboard_autoclear=true;
	public static String setting_key_passphrase="";
	public static boolean setting_use_keys = false;
	public static boolean setting_invert_scroll = false;
	public static String setting_key_filename = "";
	//public static boolean setting_keyboard_show_details;
	//static boolean setting_command_overlay=true;
	private String setting_host_all;

	private EditText ET;
	private CustomKeyboardButtonAdapter mItemAdapter;
	private CoolDragAndDropGridView mCoolDragAndDropGridView;
	private List<CustomKeyboardButton> mItems = new LinkedList<CustomKeyboardButton>();
	private String KEYLOAYOUTFILENAME = "keyLayoutFile.csv";
	//static ImageView DeleteView;
	private RelativeLayout EditKeyboardButtonsLayout;
	private LinearLayout ETLayout;
	private int potentialDeletePosition;
	private int startPosition;
	//static ListView scriptList;
	///\\\private FloatingActionButton fab;
	private Button button_bottom1, button_bottom2, button_bottom3, button_bottom4;
	private Button button_top00, button_top01, button_top02, button_top03, button_top10, button_top11, button_top12, button_top13, button_top14;
	private ToggleButton gyroToggle;
	private int Mblayer=0;
	private boolean Mbmod=false;
	private boolean button_top00down =false, button_top01down =false, button_top02down =false, button_top03down =false, button_top10down =false, button_top11down =false, button_top12down =false, button_top13down =false, button_top14down =false;
	private ToggleButton canvasToggleButton;
	private static TextView recentCmdTextView;


	private Sensor gyroscope;
	
	float regyroscopeX=0;
	float regyroscopeY=0;
	float regyroscopeZ=0;

	///\\\private Button jsSwitch;


	//private ArrayList<ArrayList<String>> scriptItems = new ArrayList<ArrayList<String>>();

	static {
		Log.d("MainActivity", "Configure spongy castle security");
		Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	@Override
	public void onDialogPositiveClickValid(
			com.stripe1.xmouse.myDialogFragment dialog, String layoutText) {
		//Log.d("onDialogPositiveClickValid",layoutText);

		LinkedList<CustomKeyboardButton> temp = loadKeyboardLayout(MainActivity.this,layoutText);
		if(temp!=null){
			mItems = temp;

			mItemAdapter = new CustomKeyboardButtonAdapter(MainActivity.this, mItems);
			mCoolDragAndDropGridView.setAdapter(mItemAdapter);
			mItemAdapter.notifyDataSetChanged();
		}
	}
	@Override
	public void onDialogNegativeClick(com.stripe1.xmouse.myDialogFragment dialog) {


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		conDiscButton = menu.findItem(R.id.action_conn_disc);//getItem(0);
		KeyLockButton = menu.findItem(R.id.action_lock_keys);
		InvScrollButton = menu.findItem(R.id.action_invert_scroll);




		return true;
		//super.onCreateOptionsMenu(menu);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		gyroscope= sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		sensorManager.registerListener((SensorEventListener) this,gyroscope,SensorManager.SENSOR_DELAY_FASTEST);



		/*/fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(keyboardLayout.getVisibility() == View.VISIBLE){
					if (s) {
						mouseLayout.setVisibility(View.VISIBLE);
						jsLayout.setVisibility(View.INVISIBLE);
					}
					else {
						mouseLayout.setVisibility(View.INVISIBLE);
						jsLayout.setVisibility(View.VISIBLE);
					}
					keyboardLayout.setVisibility(View.INVISIBLE);
					//Log.d("MainActivity", "Show Mouse");
					fab.setImageResource(R.drawable.ic_action_hardware_keyboard);
				}else {
					keyboardLayout.setVisibility(View.VISIBLE);
					mouseLayout.setVisibility(View.INVISIBLE);
					jsLayout.setVisibility(View.INVISIBLE);
					//Log.d("MainActivity", "Show Keyboard");
					fab.setImageResource(R.drawable.ic_action_hardware_mouse);
				}
				//Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

				//Hides soft keyboard if open
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


			}
		});

		fab.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if(s){
					s=false;
					mouseLayout.setVisibility(View.INVISIBLE);
					keyboardLayout.setVisibility(View.INVISIBLE);
					jsLayout.setVisibility(View.VISIBLE);
					//Log.d("MainActivity", "Show Mouse");
					fab.setImageResource(R.drawable.ic_action_hardware_keyboard);
				}else {
					s=true;
					keyboardLayout.setVisibility(View.INVISIBLE);
					mouseLayout.setVisibility(View.VISIBLE);
					jsLayout.setVisibility(View.INVISIBLE);
					//Log.d("MainActivity", "Show Keyboard");
					fab.setImageResource(R.drawable.ic_action_hardware_mouse);
				}
				//Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

				//Hides soft keyboard if open
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


				return true;
			}
		});/*/

		button_bottom2 =findViewById(R.id.button_bottom2);

		button_bottom2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Tab";
				conn.executeShellCommand(cmd);
			}
		});


		button_bottom1 =findViewById(R.id.button_bottom1);

		button_bottom1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Shift_L+Tab";
				conn.executeShellCommand(cmd);
			}
		});


		button_bottom3 =findViewById(R.id.button_bottom3);

		button_bottom3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Menu";
				conn.executeShellCommand(cmd);
			}
		});

		button_bottom3.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Multi_key";
				conn.executeShellCommand(cmd);
				return true;
			}
		});


		button_bottom4 =findViewById(R.id.button_bottom4);

		button_bottom4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Return";
				conn.executeShellCommand(cmd);
			}
		});


		button_top00 =findViewById(R.id.button_top00);

		button_top14 =findViewById(R.id.button_top14);

		button_top01 =findViewById(R.id.button_top01);
		button_top02 =findViewById(R.id.button_top02);
		button_top03 =findViewById(R.id.button_top03);
		button_top10 =findViewById(R.id.button_top10);
		button_top11 =findViewById(R.id.button_top11);
		button_top12 =findViewById(R.id.button_top12);
		button_top13 =findViewById(R.id.button_top13);

		setlayerMb();

		gyroToggle =findViewById(R.id.gyroToggleButton);
		///TODO

		gyroToggle.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Mblayer++;
				Mblayer%=3;
				switch (Mblayer) {
					case 0:
						setlayerMb();
						break;
					case 1:
						if (Mbmod) {
							setlayerArr();
						}
						else {
							setlayerArrS();
						}
						break;
					case 2:
						setlayerMedia();
						break;
				}
				return true;
			}
		});
			

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		///\\\mouseLayout = (LinearLayout) findViewById(R.id.mouse);
		keyboardLayout = findViewById(R.id.keyboard);

		///\\\jsLayout = (LinearLayout) findViewById(R.id.js);

		canvasLayout=findViewById(R.id.canvas);

		keyboardLayout.setVisibility(View.INVISIBLE);
		canvasLayout.setVisibility(View.VISIBLE);
		canvasLayout.addView(new MyCanvasView(getBaseContext()));
		canvasToggleButton =findViewById(R.id.canvasToggleButton);
		canvasToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					keyboardLayout.setVisibility(View.INVISIBLE);
					canvasLayout.setVisibility(View.VISIBLE);
				}
				else {
					keyboardLayout.setVisibility(View.VISIBLE);
					canvasLayout.setVisibility(View.INVISIBLE);
				}
				/*InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(keyboardLayout.getWindowToken(), 0);*/
			}
		});



		ScrollView scrollView = (ScrollView) findViewById(R.id.key_drag_scrollView);
		mCoolDragAndDropGridView = (CoolDragAndDropGridView) findViewById(R.id.key_drag_DragAndDropGridView);


		mItems = loadKeyboardLayout(this,false);//new LinkedList<Item>();


		mItemAdapter = new CustomKeyboardButtonAdapter(this, mItems);
		mCoolDragAndDropGridView.setAdapter(mItemAdapter);
		mCoolDragAndDropGridView.setScrollingStrategy(new SimpleScrollingStrategy(scrollView));
		mCoolDragAndDropGridView.setDragAndDropListener(this);
		mCoolDragAndDropGridView.setOnItemLongClickListener(this);
		//RelativeLayout k = (RelativeLayout) rootView.findViewById(R.id.keyboard_full_layout);
		//FrameLayout sh = (FrameLayout) rootView.findViewById(R.id.content_frame_keyboard);
		//sh.addView(keyView);
		ET = (EditText) findViewById(R.id.keyboard_input);

		ET.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(!MainActivity.setting_keyboard_batch){
					useKeyboardSendText();
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start,  int before, int count) {}
		});

		EditKeyboardButtonsLayout = (RelativeLayout) findViewById(R.id.editKeyboardButtonsLayout);
		EditKeyboardButtonsLayout.setVisibility(View.GONE);
		//DeleteView = (ImageView) findViewById(R.id.deleteButton);
		//DeleteView.setVisibility(View.GONE);
		ETLayout = (LinearLayout) findViewById(R.id.keyboard_send_layout);
		//SlidingPane SH = (SlidingPane) rootView.findViewById(R.id.slideHolder_keyboard);
		////SH.setParallaxDistance(100);
		////SH.setEnabled(false);
		//SH.setCoveredFadeColor(Color.TRANSPARENT);
		////SH.setShadowResource(R.drawable.shadow);
        /*ListView drawerList = (ListView) rootView.findViewById(R.id.drawerList);

        ButtonListAdapter adapter = new ButtonListAdapter(getActivity());
        drawerList.setAdapter(adapter);*/
		recentCmdTextView = (TextView) findViewById(R.id.recentCmd);

		initDb();
		initMenu();

		conn = new MyConnectionHandler(MainActivity.this, MainActivity.this);
	}


	private void unsetlayer() {
		button_top01down =false;
		button_top02down =false;
		button_top03down =false;
		button_top10down =false;
		button_top11down =false;
		button_top12down =false;
		button_top13down =false;

		button_top00.setText("");
		button_top00.setOnClickListener(null);
		button_top00.setOnLongClickListener(null);
		button_top00.setOnTouchListener(null);

		button_top01.setText("");
		button_top01.setOnClickListener(null);
		button_top01.setOnLongClickListener(null);
		button_top01.setOnTouchListener(null);

		button_top02.setText("");
		button_top02.setOnClickListener(null);
		button_top02.setOnLongClickListener(null);
		button_top02.setOnTouchListener(null);

		button_top03.setText("");
		button_top03.setOnClickListener(null);
		button_top03.setOnLongClickListener(null);
		button_top03.setOnTouchListener(null);

		button_top10.setText("");
		button_top10.setOnClickListener(null);
		button_top10.setOnLongClickListener(null);
		button_top10.setOnTouchListener(null);

		button_top11.setText("");
		button_top11.setOnClickListener(null);
		button_top11.setOnLongClickListener(null);
		button_top11.setOnTouchListener(null);

		button_top12.setText("");
		button_top12.setOnClickListener(null);
		button_top12.setOnLongClickListener(null);
		button_top12.setOnTouchListener(null);

		button_top13.setText("");
		button_top13.setOnClickListener(null);
		button_top13.setOnLongClickListener(null);
		button_top13.setOnTouchListener(null);

		button_top14.setText("");
		button_top14.setOnClickListener(null);
		button_top14.setOnLongClickListener(null);
		button_top14.setOnTouchListener(null);


	}



	private void setlayerMb() {

		unsetlayer();

		button_top00.setText(getString(R.string.escape));

		button_top00.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Escape";
				conn.executeShellCommand(cmd);
			}
		});


		button_top01.setText(getString(R.string._1));


		button_top01.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					String cmd="";
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if (!button_top01down) {
								cmd="xdotool mousedown 1";
							}
							button_top01down =true;
							break;
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							if (button_top01down) {
								cmd="xdotool mouseup 1";
							}
							button_top01down =false;
					}
					if (cmd!="") {
						conn.executeShellCommand(cmd);
						return true;
					}
					return false;
				}
		});


		button_top02.setText(getString(R.string._2));

		button_top02.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top02down) {
							cmd="xdotool mousedown 2";
						}
						button_top02down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top02down) {
							cmd="xdotool mouseup 2";
						}
						button_top02down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top03.setText(getString(R.string._3));

		button_top03.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top03down) {
							cmd="xdotool mousedown 3";
						}
						button_top03down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top03down) {
							cmd="xdotool mouseup 3";
						}
						button_top03down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top10.setText(getString(R.string._4));

		button_top10.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top10down) {
							cmd="xdotool mousedown 4";
						}
						button_top10down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top10down) {
							cmd="xdotool mouseup 4";
						}
						button_top10down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});



		button_top11.setText(getString(R.string._5));


		button_top11.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top11down) {
							cmd="xdotool mousedown 5";
						}
						button_top11down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top11down) {
							cmd="xdotool mouseup 5";
						}
						button_top11down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});



		button_top12.setText(getString(R.string._6));


		button_top12.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top12down) {
							cmd="xdotool mousedown 6";
						}
						button_top12down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top12down) {
							cmd="xdotool mouseup 6";
						}
						button_top12down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});



		button_top13.setText(getString(R.string._7));

		button_top13.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top13down) {
							cmd="xdotool mousedown 7";
						}
						button_top13down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top13down) {
							cmd="xdotool mouseup 7";
						}
						button_top13down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});

		button_top14.setText(getString(R.string.backspace));

		button_top14.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key BackSpace";
				conn.executeShellCommand(cmd);
			}
			});

		button_top14.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Control_L+BackSpace";
				conn.executeShellCommand(cmd);
				return true;
			}
		});

	}

	private void setlayerArr() {

		unsetlayer();


		button_top00.setText(getString(R.string.page_up));

		button_top00.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Page_Up";
				conn.executeShellCommand(cmd);
			}
		});

		button_top00.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Control_L+Left";
				conn.executeShellCommand(cmd);
				return true;
			}
		});


		button_top01.setText(getString(R.string.home));

		button_top01.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Home";
				conn.executeShellCommand(cmd);
			}
		});

		button_top01.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Shift_L+Left";
				conn.executeShellCommand(cmd);
				return true;
			}
		});


		button_top02.setText(getString(R.string.arr_up));

		button_top02.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top02down) {
							cmd="xdotool keydown Up";
						}
						button_top02down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top02down) {
							cmd="xdotool keyup Up";
						}
						button_top02down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top03.setText(getString(R.string.end));

		button_top03.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key End";
				conn.executeShellCommand(cmd);
			}
		});

		button_top03.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Shift_L+Right";
				conn.executeShellCommand(cmd);
				return true;
			}
		});

		button_top10.setText(getString(R.string.page_down));

		button_top10.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Page_Down";
				conn.executeShellCommand(cmd);
			}
		});

		button_top10.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Control_L+Right";
				conn.executeShellCommand(cmd);
				return true;
			}
		});

		button_top11.setText(getString(R.string.arr_left));

		button_top11.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top11down) {
							cmd="xdotool keydown Left";
						}
						button_top11down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top11down) {
							cmd="xdotool keyup Left";
						}
						button_top11down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top12.setText(getString(R.string.arr_down));

		button_top12.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top12down) {
							cmd="xdotool keydown Down";
						}
						button_top12down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top12down) {
							cmd="xdotool keyup Down";
						}
						button_top12down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top13.setText(getString(R.string.arr_right));

		button_top13.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top13down) {
							cmd="xdotool keydown Right";
						}
						button_top13down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top13down) {
							cmd="xdotool keyup Right";
						}
						button_top13down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top14.setText(getString(R.string.scroll_lock));

		button_top14.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Scroll_Lock";
				conn.executeShellCommand(cmd);
			}
		});

		button_top14.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Mbmod=!Mbmod;
				if (Mbmod) {
					setlayerArr();
				}
				else {
					setlayerArrS();
				}
				return true;
			}
		});
	}



	private void setlayerArrS() {
		unsetlayer();



		button_top00.setText(getString(R.string.shift)+getString(R.string.arr_up)+getString(R.string.sep)+getString(R.string.alt)+getString(R.string.arr_left));
		button_top00.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Shift_L+Up";
				conn.executeShellCommand(cmd);
			}
		});

		button_top00.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Alt_L+Left";
				conn.executeShellCommand(cmd);
				return true;
			}
		});



		button_top01.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Control_L+Shift_L+Left";
				conn.executeShellCommand(cmd);
			}
		});

		button_top01.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Shift_L+Home";
				conn.executeShellCommand(cmd);
				return true;
			}
		});


		button_top02.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top02down) {
							cmd="xdotool keydown Up";
						}
						button_top02down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top02down) {
							cmd="xdotool keyup Up";
						}
						button_top02down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});




		button_top03.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Control_L+Shift_L+Right";
				conn.executeShellCommand(cmd);
			}
		});

		button_top03.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Shift_L+End";
				conn.executeShellCommand(cmd);
				return true;
			}
		});


		button_top10.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Shift_L+Down";
				conn.executeShellCommand(cmd);
			}
		});

		button_top10.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Alt_L+Right";
				conn.executeShellCommand(cmd);
				return true;
			}
		});

		button_top11.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top11down) {
							cmd="xdotool keydown Left";
						}
						button_top11down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top11down) {
							cmd="xdotool keyup Left";
						}
						button_top11down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top12.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top12down) {
							cmd="xdotool keydown Down";
						}
						button_top12down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top12down) {
							cmd="xdotool keyup Down";
						}
						button_top12down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});



		button_top13.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top13down) {
							cmd="xdotool keydown Right";
						}
						button_top13down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top13down) {
							cmd="xdotool keyup Right";
						}
						button_top13down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});

		button_top14.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Scroll_Lock";
				conn.executeShellCommand(cmd);
			}
		});

		button_top14.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Mbmod=!Mbmod;
				if (Mbmod) {
					setlayerArr();
				}
				else {
					setlayerArrS();
				}
				return true;
			}
		});
	}

	private void setlayerMedia() {
		unsetlayer();

		button_top00.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top00down) {
							cmd="xdotool keydown XF86MonBrightnessUp";
						}
						button_top00down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top00down) {
							cmd="xdotool keyup XF86MonBrightnessUp";
						}
						button_top00down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top14.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Alt_L+Tab";
				conn.executeShellCommand(cmd);
			}
		});


		button_top14.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Control_L+Tab";
				conn.executeShellCommand(cmd);
				return true;
			}
		});


		button_top01.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					String cmd="";
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if (!button_top01down) {
								cmd="xdotool keydown XF86AudioRaiseVolume";
							}
							button_top01down =true;
							break;
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							if (button_top01down) {
								cmd="xdotool keyup XF86AudioRaiseVolume";
							}
							button_top01down =false;
					}
					if (cmd!="") {
						conn.executeShellCommand(cmd);
						return true;
					}
					return false;
				}
			});


		button_top02.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key XF86AudioPlay";
				conn.executeShellCommand(cmd);
			}
		});

		button_top02.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key XF86AudioPause";
				conn.executeShellCommand(cmd);
				return true;
			}
		});

		button_top03.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key XF86Search";
				conn.executeShellCommand(cmd);
			}
		});

		button_top03.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key XF86HomePage";
				return true;
			}
		});

		button_top10.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top10down) {
							cmd="xdotool keydown XF86MonBrightnessDown";
						}
						button_top10down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top10down) {
							cmd="xdotool keyup XF86MonBrightnessDown";
						}
						button_top10down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top11.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String cmd="";
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!button_top11down) {
							cmd="xdotool keydown XF86AudioLowerVolume";
						}
						button_top11down =true;
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if (button_top11down) {
							cmd="xdotool keyup XF86AudioLowerVolume";
						}
						button_top11down =false;
				}
				if (cmd!="") {
					conn.executeShellCommand(cmd);
					return true;
				}
				return false;
			}
		});


		button_top12.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key XF86AudioMute";
				conn.executeShellCommand(cmd);
			}
		});


		button_top13.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String cmd="xdotool key Alt_L+Shift_L+Tab";
				conn.executeShellCommand(cmd);
			}
		});

		button_top13.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String cmd="xdotool key Control_L+Shift_L+Tab";
				conn.executeShellCommand(cmd);
				return true;
			}
		});
	}

	public void initDb(){

		hostDBKeys = new ArrayList<ArrayList<String>>();
		ArrayList<String> dataKey = new ArrayList<String>();
		dataKey.add("Alias");
		dataKey.add("TEXT");
		hostDBKeys.add(dataKey);
		dataKey = new ArrayList<String>();
		dataKey.add("Host");
		dataKey.add("TEXT");
		hostDBKeys.add(dataKey);
		dataKey = new ArrayList<String>();
		dataKey.add("Username");
		dataKey.add("TEXT");
		hostDBKeys.add(dataKey);
		dataKey = new ArrayList<String>();
		dataKey.add("Port");
		dataKey.add("TEXT");
		hostDBKeys.add(dataKey);
		dataKey = new ArrayList<String>();
		dataKey.add("Password");
		dataKey.add("TEXT");
		hostDBKeys.add(dataKey);


		ArrayList<ArrayList<String>> scriptDBKeys = new ArrayList<ArrayList<String>>();
		dataKey = new ArrayList<String>();
		dataKey.add("Alias");
		dataKey.add("TEXT");
		scriptDBKeys.add(dataKey);
		dataKey = new ArrayList<String>();
		dataKey.add("Script");
		dataKey.add("TEXT");
		scriptDBKeys.add(dataKey);

		db = new DatabaseHandler(this, hostDBKeys,scriptDBKeys);
	}

	public void initMenu(){



		//SETUP NAV PANE
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		Menu m = (Menu) navigationView.getMenu();//.findViewById(R.id.drawer_menu_hosts);
		m.clear(); //clear in case we added or removed scripts or hosts

		MenuItem title1 = m.add(0, 999, Menu.NONE, "Host Computers");
		ArrayList<ArrayList<String>> hosts = db.listAll(DatabaseHandler.HOST_TABLE_NAME, new String[]{"Alias", "Host", "Username", "Port", "Password", "id"});

		if(hosts.size()>0) {
			for (int i = hosts.size()-1;i>=0; i--) {

				String desc = hosts.get(i).get(0);// + " [" + hosts.get(i).get(2) + "@" + hosts.get(i).get(1) + ":" + hosts.get(i).get(3) + "] id=" + hosts.get(i).get(5);
				Integer id = Integer.valueOf(hosts.get(i).get(5));

				MenuItem itemAdd1 = m.add(1, id, Menu.NONE, desc);
				itemAdd1.setIcon(R.drawable.ic_action_hardware_desktop_windows);
				itemAdd1.setCheckable(true);


			}
		}
		MenuItem itemAdd0 = m.add(1, 999999999, Menu.NONE, "Manage Hosts");
		itemAdd0.setIcon(android.R.drawable.ic_menu_manage);



		MenuItem title3 = m.add(2, 60, Menu.NONE, "Keyboard Layouts");

		MenuItem itemAdd7 = m.add(3, 61, Menu.NONE, "Layout 1");
		itemAdd7.setIcon(R.drawable.ic_action_av_subtitles);
		MenuItem itemAdd8 = m.add(3, 62, Menu.NONE, "Layout 2");
		itemAdd8.setIcon(R.drawable.ic_action_av_subtitles);
		MenuItem itemAdd = m.add(3, 63, Menu.NONE, "Layout 3");
		itemAdd.setIcon(R.drawable.ic_action_av_subtitles);
		MenuItem itemAdd9 = m.add(3, 64, Menu.NONE, "Add Button");
		itemAdd9.setIcon(android.R.drawable.ic_menu_add);

		MenuItem title2 = m.add(4, 1234, Menu.NONE, "Saved Scripts");

		ArrayList<ArrayList<String>> scriptItems = db.listAll(DatabaseHandler.SCRIPT_TABLE_NAME, new String[]{"Alias", "Script", "id"});
		if(scriptItems.size()>0){
			for(int i=scriptItems.size()-1;i>=0; i--) {
				MenuItem thisItem = m.add(5, Integer.parseInt(scriptItems.get(i).get(2)), Menu.NONE, scriptItems.get(i).get(0));
				thisItem.setIcon(R.drawable.ic_action_action_assignment);
			}
		}

		MenuItem itemAdd6 = m.add(5, 19999999, Menu.NONE, "Manage Scripts");
		itemAdd6.setIcon(android.R.drawable.ic_menu_manage);

	}

	public void getPreferences(){

		try{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			setting_host= prefs.getString("setting_host", "");
			setting_user= prefs.getString("setting_user", "");
			setting_port= Integer.valueOf(prefs.getString("setting_port", "22"));
			setting_pass= prefs.getString("setting_pass", "");

			//Log.d("prefTest",setting_host+" "+setting_user+" "+setting_pass+" "+setting_port);

			setting_host_all = prefs.getString("hostPreferenceList", "0");//id of host in db

			ArrayList<ArrayList<String>> host = db.getRowWithId(DatabaseHandler.HOST_TABLE_NAME,new String[] {"Host","Username","Port","Password"},setting_host_all);

			if(host.size()>0){

				setting_host= host.get(0).get(0);
				setting_user= host.get(0).get(1);
				setting_port= Integer.valueOf(host.get(0).get(2));
				setting_pass= host.get(0).get(3);
			}/*else{

                //Log.d("getPreferences", "Zero host records, will default to last used settings");
                Toast.makeText(getBaseContext(), "No Saved Host selected", Toast.LENGTH_LONG).show();
            }*/

			//Log.d("prefTest",setting_host+" "+setting_user+" "+setting_pass+" "+setting_port);

			setting_pointing_device = Integer.valueOf(prefs.getString("pointing_device","1"));
			setting_gyro_function = Integer.valueOf(prefs.getString("gyro_function","1"));

			setting_mouse_sensitivity = Float.valueOf(prefs.getString("sensitivity_mouse_list", "1.0f"));
			setting_mouse_delay = Integer.valueOf(prefs.getString("delay_mouse_list", "350"));
			setting_mouse_mdelay = Integer.valueOf(prefs.getString("mdelay_mouse_list", "0"));

			setting_hscroll_sensitivity = Float.valueOf(prefs.getString("sensitivity_mouse_list", "1.0f"));
			setting_vscroll_sensitivity = Float.valueOf(prefs.getString("sensitivity_mouse_list", "1.0f"));

			setting_gyro_z_sensitivity = Float.valueOf(prefs.getString("gyro_z_sensitivity_list", "1.5f"));
			setting_gyro_y_sensitivity = Float.valueOf(prefs.getString("gyro_y_sensitivity_list", "1.5f"));
			setting_gyro_x_sensitivity = Float.valueOf(prefs.getString("gyro_x_sensitivity_list", "1.5f"));
			setting_gyro_z_threshold = Float.valueOf(prefs.getString("gyro_z_threshold_list", "1.5f"));
			setting_gyro_y_threshold = Float.valueOf(prefs.getString("gyro_y_threshold_list", "1.5f"));
			setting_gyro_x_threshold = Float.valueOf(prefs.getString("gyro_x_threshold_list", "1.5f"));


			setting_js_sensitivity = Float.valueOf(prefs.getString("js_sensitivity_list", "1.0f"));
			setting_js_size = Float.valueOf(prefs.getString("js_size_list", "0.5f"));
			setting_js_dead_zone = Float.valueOf(prefs.getString("js_dead_zone_list", "0.0f"));

			setting_autoconnect=prefs.getBoolean("autologin_checkbox", false);
			setting_xdotool_initial=prefs.getString("setting_xdotool_initial", "export DISPLAY=':0' && unset HISTFILE");
			setting_shell = prefs.getString("setting_shell", "");
			setting_keyboard_autoclear=prefs.getBoolean("keyboard_autoclear", true);
			//setting_keyboard_show_details=prefs.getBoolean("show_commands_on_buttons",false);
			setting_use_keys = prefs.getBoolean("pref_usekeyauth",false);
			setting_key_filename=prefs.getString("pref_addkeybutton", "");
			setting_key_passphrase=prefs.getString("pref_key_passphrase", "");
			setting_keyboard_batch=prefs.getBoolean("setting_keyboard_batch",true);
			//setting_mouse_background=prefs.getBoolean("setting_mouse_background",false);
			setting_keyboard_locked=prefs.getBoolean("keyboard_layout_locked",false);
			//setting_log_staydown=prefs.getBoolean("log_staydown", true);
			//setting_command_overlay=prefs.getBoolean("command_overlay", false);
			setting_invert_scroll=prefs.getBoolean("mouse_invert_scroll",false);
			Log.d("getPref",""+setting_invert_scroll);
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "There was a problem retrieving your settings: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	public void bindValueToPref(String key,String val){

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key,val);
		editor.commit();
	}
	public void xMouseKeyboardSend(View v){

		useKeyboardSendText();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// The activity is about to become visible.
		Log.d("onStart","onStart");

	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("onResume","onResume");
		// The activity has become visible (it is now "resumed").
		getPreferences();



		//Toast.makeText(getBaseContext(), "setting_autoconnect "+String.valueOf(setting_autoconnect), Toast.LENGTH_SHORT).show();
		if(setting_autoconnect){
			conn.xMouseTryConnect();
		}

		if(mCoolDragAndDropGridView!=null){
			mItemAdapter = new CustomKeyboardButtonAdapter(MainActivity.this, mItems);
			mCoolDragAndDropGridView.setAdapter(mItemAdapter);
			mItemAdapter.notifyDataSetChanged();
		}
		initMenu();
		showConnectionStat();
	}
	@Override
	protected void onPause() {
		KEYLOAYOUTFILENAME = "keyLayoutFile.csv";
		saveKeyboardLayout();
		super.onPause();
		// Another activity is taking focus (this activity is about to be "paused").
	}
	@Override
	protected void onStop() {

		super.onStop();  // Always call the superclass method first
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
		conn.xMouseDisconnect();
	}

	public void showConnectionStat(){
		if(conDiscButton != null){
			if(conn.session==null || !conn.session.isConnected()){
				conDiscButton.setIcon(R.drawable.state_disc);
			}else{
				conDiscButton.setIcon(R.drawable.state_conn);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
	 	/*case KeyEvent.KEYCODE_MENU:
	   case KeyEvent.KEYCODE_SEARCH:
	   case KeyEvent.KEYCODE_BACK:*/
			case KeyEvent.KEYCODE_VOLUME_UP:
				event.startTracking();
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				event.startTracking();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(keyCode){
       /*case KeyEvent.KEYCODE_MENU:
       case KeyEvent.KEYCODE_SEARCH:*/
			case KeyEvent.KEYCODE_VOLUME_UP:
				if(event.isTracking() && !event.isCanceled()){
					//Toast.makeText(this, "Volumen Up released", Toast.LENGTH_SHORT).show();
					conn.executeShellCommand("xdotool key XF86AudioRaiseVolume");

				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if(event.isTracking() && !event.isCanceled()){
					//Toast.makeText(this, "Volumen Down released", Toast.LENGTH_SHORT).show();case
					conn.executeShellCommand("xdotool key XF86AudioLowerVolume");
				}
				return true;

		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}
	public void confirmLayoutReload(String title, String msg,final boolean def){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mItems = loadKeyboardLayout(MainActivity.this,def);
				mItemAdapter = new CustomKeyboardButtonAdapter(MainActivity.this, mItems);
				mCoolDragAndDropGridView.setAdapter(mItemAdapter);
				mItemAdapter.notifyDataSetChanged();

			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	public void confirmSaveLayout(String title, String msg,final boolean def){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveKeyboardLayout();

			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	@Override

	public boolean onPrepareOptionsMenu(Menu menu) {
		if(conDiscButton!=null) {
			conDiscButton.setChecked(setting_keyboard_locked);
		}
		if(KeyLockButton!=null) {
			KeyLockButton.setChecked(setting_keyboard_locked);
		}
		if(InvScrollButton!=null){
			InvScrollButton.setChecked(setting_invert_scroll);
		}
		//Log.d("prepMenu","menu prepared");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.action_conn_disc:

				//Log.d("prefTest",setting_host+" "+setting_user+" "+setting_pass+" "+setting_port);

				if(conn.session != null && conn.session.isConnected()){
					conn.xMouseDisconnect();
				}else {
					conn.xMouseTryConnect();
				}
				break;
			case R.id.action_restore_default_keys:
				KEYLOAYOUTFILENAME = "keyLayoutFile.csv";
				confirmLayoutReload("Restore default Keyboard?","Any unsaved buttons will be lost",true);
				break;
			case R.id.action_save_preset_one:
				KEYLOAYOUTFILENAME = "keyFileOne.csv";
				confirmSaveLayout("Save as Keyboard Layout 1", "Any previous layout data will be overwritten", false);

				break;
			case R.id.action_save_preset_two:
				KEYLOAYOUTFILENAME = "keyFileTwo.csv";
				confirmSaveLayout("Save as Keyboard Layout 2", "Any previous layout data will be overwritten", false);
				break;
			case R.id.action_save_preset_three:
				KEYLOAYOUTFILENAME = "keyFileThree.csv";
				confirmSaveLayout("Save as Keyboard Layout 3", "Any previous layout data will be overwritten", false);
				break;

			case R.id.action_lock_keys:
				setting_keyboard_locked=!setting_keyboard_locked;
				item.setChecked(setting_keyboard_locked);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("keyboard_layout_locked", setting_keyboard_locked);
				editor.commit();
				break;
			case R.id.action_invert_scroll:
				setting_invert_scroll=!setting_invert_scroll;
				item.setChecked(setting_invert_scroll);
				SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor2 = prefs2.edit();
				editor2.putBoolean("mouse_invert_scroll", setting_invert_scroll);
				editor2.commit();
				break;
			case R.id.action_edit_layout:
				showNoticeDialog(getBaseContext());

				break;
			case R.id.action_exit:

				//close all connections and finish.
				conn.xMouseDisconnect();

				this.finish();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	public void showNoticeDialog(Context mCont) {
		// Create an instance of the dialog fragment and show it
		myDialogFragment dialog = new myDialogFragment();
		dialog.setmCont(mCont);
		dialog.setText(getKeyboardTextValue());

		dialog.show(fm, "NoticeDialogFragment");
	}
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view keyboard_button clicks here.
		int id = item.getItemId();
		int group = item.getGroupId();
		//Toast.makeText(getBaseContext(),String.valueOf(id),Toast.LENGTH_LONG).show();
		switch(group) {
			case 1: //hosts

				if(id==999999999){
					//manage sripts ID

					Intent intent = new Intent(this, ListEditActivity.class);
					intent.putExtra("intVariableType", 0); //0=hosts
					startActivity(intent);

				}else {

					ArrayList<ArrayList<String>> host = db.getRowWithId(DatabaseHandler.HOST_TABLE_NAME,
							new String[]{"Host", "Username", "Port", "Password"}, String.valueOf(id));

					if (host.size() > 0) {

						setting_host = host.get(0).get(0);
						setting_user = host.get(0).get(1);
						setting_port = Integer.valueOf(host.get(0).get(2));
						setting_pass = host.get(0).get(3);

						bindValueToPref("setting_host",setting_host);
						bindValueToPref("setting_user",setting_user);
						bindValueToPref("setting_port",String.valueOf(setting_port));
						bindValueToPref("setting_pass",setting_pass);

						conn.xMouseDisconnect();
						conn.xMouseTryConnect();
					} else {

						Toast.makeText(getBaseContext(), "Error finding Host settings with id=" + id, Toast.LENGTH_LONG).show();
					}
				}

				break;
			case 3: //keyboard layout files group
				switch (id) {

					case 61:
						KEYLOAYOUTFILENAME = "keyFileOne.csv";
						confirmLayoutReload("Load Keyboard Layout 1", "Any unsaved buttons will be lost", false);
						break;
					case 62:
						KEYLOAYOUTFILENAME = "keyFileTwo.csv";
						confirmLayoutReload("Load Keyboard Layout 2", "Any unsaved buttons will be lost", false);
						break;
					case 63:
						KEYLOAYOUTFILENAME = "keyFileThree.csv";
						confirmLayoutReload("Load Keyboard Layout 3", "Any unsaved buttons will be lost", false);
						break;
					case 64:
						xMouseNewCustomButton(null);
						break;
					default:
						break;
				}
				break;

			case 5: //scripts;

				if(id==19999999){
					//manage sripts ID

					Intent intent = new Intent(this, ListEditActivity.class);
					intent.putExtra("intVariableType", 1); //1=scripts
					startActivity(intent);

				}else {

					ArrayList<ArrayList<String>> script = db.getRowWithId(DatabaseHandler.SCRIPT_TABLE_NAME,
							new String[]{"Alias", "Script", "id"}, String.valueOf(id));

					if (script.size() > 0) {
						String cmd = script.get(0).get(1);
						conn.executeExecCommand(cmd);
					} else {

						Toast.makeText(getBaseContext(), "Error finding Script command with id=" + id, Toast.LENGTH_LONG).show();
					}
				}
				break;
			default:
				break;
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}


	@SuppressLint("RestrictedApi")
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(!MainActivity.setting_keyboard_locked){
			mCoolDragAndDropGridView.startDragAndDrop();
			EditKeyboardButtonsLayout.setVisibility(View.VISIBLE);
			ETLayout.setVisibility(View.GONE);
			//fab.setVisibility(View.GONE);
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

	}

	@Override
	public void onDragItem(int from) {

	}

	@Override
	public void onDraggingItem(int from, int to) {

	}

	@SuppressLint("RestrictedApi")
	@Override
	public void onDropItem(int from, int to) {
		if (from != to) {

			mItems.add(to, mItems.remove(from));
			mItemAdapter.notifyDataSetChanged();
		}

		potentialDeletePosition = to;
		startPosition = from;
		EditKeyboardButtonsLayout.setVisibility(View.GONE);
		//fab.setVisibility(View.VISIBLE);
		ETLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean isDragAndDropEnabled(int position) {
		return true;
	}
	@Override
	public void onEditItem(int mDragPosition) {
		//Log.d("onDeleteItem", ""+mDragPosition);
		//mItems.remove(mDragPosition);
		//mItemAdapter.notifyDataSetChanged();

		onDropItem(potentialDeletePosition,startPosition);
		//Toast.makeText(getBaseContext(),"Edit item "+potentialDeletePosition,Toast.LENGTH_LONG).show();
		xMouseNewCustomButton(potentialDeletePosition);

	}
	@Override
	public void onDeleteItem(int mDragPosition) {
		//Log.d("onDeleteItem", ""+mDragPosition);

		try {
			mItems.remove(potentialDeletePosition);
			mItemAdapter.notifyDataSetChanged();
		}catch (Exception e){
			e.printStackTrace();
			Toast.makeText(getBaseContext(),"Error during delete",Toast.LENGTH_LONG).show();
			mItemAdapter.notifyDataSetChanged();
		}
		Toast.makeText(getBaseContext(),"Remove item "+potentialDeletePosition,Toast.LENGTH_LONG).show();
	}
	public LinkedList<CustomKeyboardButton> loadKeyboardLayout(Context mCont,String stream){
		LinkedList<CustomKeyboardButton> views = new LinkedList<CustomKeyboardButton>();
		try{
			//BufferedReader reader = new BufferedReader(stream);
			String[] lines = stream.split("\n");
			String line=null;
			for(int i=0;i<lines.length;i++){
				line = lines[i];
				//sb.append(line).append("\n");
				if(line.contains("<xmousesep>")){
					//parse line in to customItems
					//Log.d("loadKeyboardLayout",line);
					String[] temp =line.split("<xmousesep>");
					//iconId,span int,title string, script

					if(Integer.valueOf(temp[2])<1 || Integer.valueOf(temp[2])>10){
						Toast.makeText(mCont, "You have included an invalid span integer, try again", Toast.LENGTH_SHORT).show();
						return null;

					}

					if(temp.length>3){
						views.add(new CustomKeyboardButton( 0,Integer.valueOf(temp[2]), temp[0], temp[1],temp[3]));
					}else{
						views.add(new CustomKeyboardButton( 0,Integer.valueOf(temp[2]), temp[0], temp[1],"#ffffff"));

					}
					//Log.d("loadKeyboardLayout","using new <xmousesep> separator");
				}else{
					//Log.d("loadKeyboardLayout","using old comma separator");
					String[] temp =line.split(",");
					views.add(new CustomKeyboardButton( 0,Integer.valueOf(temp[2]), temp[0], temp[1],"#ffffff"));

				}
			}
			//fis.close();
		} catch(OutOfMemoryError om){
			om.printStackTrace();
			Toast.makeText(mCont, om.toString(), Toast.LENGTH_LONG).show();
			return null;
		} catch(Exception ex){
			ex.printStackTrace();
			Toast.makeText(mCont, ex.toString(), Toast.LENGTH_LONG).show();
			return null;
		}

		if(views.size()<1){
			views = loadDefaultKeyboardLayout(mCont);
		}

		return views;
	}
	public LinkedList<CustomKeyboardButton> loadKeyboardLayout(Context mCont,boolean def){

		if(def){
			return loadDefaultKeyboardLayout(mCont);
		}

		LinkedList<CustomKeyboardButton> views = new LinkedList<CustomKeyboardButton>();
		FileInputStream fis;
		Log.d("loadKeyboardLayout", KEYLOAYOUTFILENAME);
		try {
			fis = mCont.openFileInput(KEYLOAYOUTFILENAME);

			//StringBuilder sb = new StringBuilder();
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					//sb.append(line).append("\n");
					if(line.contains("<xmousesep>")){
						//parse line in to customItems
						String[] temp =line.split("<xmousesep>");
						if(temp.length>3){
							views.add(new CustomKeyboardButton( 0,Integer.valueOf(temp[2]), temp[0], temp[1],temp[3]));
						}else{
							views.add(new CustomKeyboardButton( 0,Integer.valueOf(temp[2]), temp[0], temp[1],"#FFFFFF"));

						}
						//Log.d("loadKeyboardLayout","using new <xmousesep> separator");
					}else{
						//Log.d("loadKeyboardLayout","using old comma separator");
						String[] temp =line.split(",");
						views.add(new CustomKeyboardButton( 0,Integer.valueOf(temp[2]), temp[0], temp[1],"#FFFFFF"));

					}
				}
				fis.close();
			} catch(OutOfMemoryError om){
				om.printStackTrace();
			} catch(Exception ex){
				ex.printStackTrace();
			}
			//String result = sb.toString();
			//Toast.makeText(mCont, result, Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			//Toast.makeText(mCont, e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		if(views.size()<1){
			views = loadDefaultKeyboardLayout(mCont);
		}

		return views;
	}
	public LinkedList<CustomKeyboardButton> loadDefaultKeyboardLayout(Context mCont){
		LinkedList<CustomKeyboardButton> views = new LinkedList<CustomKeyboardButton>();
		//10 cols per row
        /*views.add(new CustomKeyboardButton( 0,2, "F1", "xdotool key F1","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,2, "F2", "xdotool key F2","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,2, "F3", "xdotool key F3","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,2, "F4", "xdotool key F4","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,2, "F5", "xdotool key F5","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,2, "[Prev]", "xdotool key XF86AudioPrev","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "[Pause]", "xdotool key XF86AudioPause","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "[Play]", "xdotool key XF86AudioPlay","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,2, "[Next]", "xdotool key XF86AudioNext","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "Esc", "xdotool key Escape","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "Alt|F4", "xdotool key alt+F4","red"));
        views.add(new CustomKeyboardButton( 0,4, "Backspace", "xdotool key BackSpace","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,5, "<Back", "xdotool key XF86Back","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,5, "Forward>", "xdotool key XF86Forward","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "-Mute-", "xdotool key XF86AudioMute","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,4, "Up", "xdotool key Up","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "Enter", "xdotool key Return","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "Left", "xdotool key Left","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,4, "Down", "xdotool key Down","#FFFFFF"));
        views.add(new CustomKeyboardButton( 0,3, "Right", "xdotool key Right","#FFFFFF"));
        */

		//new default design by Xalalau Xubilozo, Brazil
		views.add(new CustomKeyboardButton( 0, 2,"Esc","xdotool key Escape","#FFe6e6"));
		views.add(new CustomKeyboardButton( 0, 3,"Minimize","xdotool windowminimize $(xdotool getactivewindow)","#9FFF80"));
		views.add(new CustomKeyboardButton( 0, 3,"Maximize","xdotool windowsize $(xdotool getactivewindow) 100% 100%","#FFCC66"));
		views.add(new CustomKeyboardButton( 0, 2,"X","xdotool key alt+F4","#FF9999"));
		views.add(new CustomKeyboardButton( 0, 2,"F1","xdotool key F1","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F2","xdotool key F2","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F3","xdotool key F3","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F4","xdotool key F4","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F5","xdotool key F5","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F6","xdotool key F6","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F7","xdotool key F7","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F8","xdotool key F8","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F9","xdotool key F9","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F10","xdotool key F10","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F11","xdotool key F11","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"F12","xdotool key F12","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 3,"+Brightness","xdotool key XF86MonBrightnessUp","#e6f7ff"));
		views.add(new CustomKeyboardButton( 0, 3,"-Brightness","xdotool key XF86MonBrightnessDown","#e6f7ff"));
		views.add(new CustomKeyboardButton( 0, 2,"Super","xdotool key super","#80d4ff"));
		views.add(new CustomKeyboardButton( 0, 3,"+Volume","xdotool key XF86AudioRaiseVolume","#B3E6FF"));
		views.add(new CustomKeyboardButton( 0, 3,"-Volume","xdotool key XF86AudioLowerVolume","#B3E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Mute","xdotool key XF86AudioMute","#B3E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Alt|Tab","xdotool key alt+Tab","#80d4ff"));
		views.add(new CustomKeyboardButton( 0, 3,"[Prev]","xdotool key XF86AudioPrev","#B3E6FF"));
		views.add(new CustomKeyboardButton( 0, 3,"[Next]","xdotool key XF86AudioNext","#B3E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"[Play]","xdotool key XF86AudioPlay","#B3E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"New","xdotool key ctrl+n","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Open","xdotool key ctrl+o","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Save","xdotool key ctrl+s","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Find","xdotool key ctrl+f","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Print","xdotool key ctrl+p","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 4,"Select all","xdotool key ctrl+a","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 3,"+Zoom","xdotool key ctrl+plus","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 3,"-Zoom","xdotool key ctrl+minus","#E6B3FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Undo","xdotool key ctrl+z","#F7E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Redo","xdotool key ctrl+y","#F7E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Copy","xdotool key ctrl+c","#F7E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Crop","xdotool key ctrl+x","#F7E6FF"));
		views.add(new CustomKeyboardButton( 0, 2,"Paste","xdotool key ctrl+v","#F7E6FF"));
		views.add(new CustomKeyboardButton( 0, 3,"PgUp","xdotool key Prior","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 3,"PgDown","xdotool key Next","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 4,"Delete","xdotool key Delete","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"Home","xdotool key Home","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"Up","xdotool key Up","#FFFFB3"));
		views.add(new CustomKeyboardButton( 0, 2,"End","xdotool key End","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 4,"Backspace","xdotool key BackSpace","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 2,"Left","xdotool key Left","#FFFFB3"));
		views.add(new CustomKeyboardButton( 0, 2,"Down","xdotool key Down","#FFFFB3"));
		views.add(new CustomKeyboardButton( 0, 2,"Right","xdotool key Right","#FFFFB3"));
		views.add(new CustomKeyboardButton( 0, 4,"Enter","xdotool key Return","#FFFFFF"));
		views.add(new CustomKeyboardButton( 0, 5,"<Back","xdotool key XF86Back","#f2e5d9"));
		views.add(new CustomKeyboardButton( 0, 5,"Forward>","xdotool key XF86Forward","#f2e5d9"));
		views.add(new CustomKeyboardButton( 0, 1,"+","xdotool key ctrl+t","#e6ccb3"));
		views.add(new CustomKeyboardButton( 0, 1,"-","xdotool key ctrl+w","#e6ccb3"));
		views.add(new CustomKeyboardButton( 0, 4,"Reopen Last Tab","xdotool key ctrl+shift+t","#e6ccb3"));
		views.add(new CustomKeyboardButton( 0, 2,"<-","xdotool key ctrl+shift+Tab","#e6ccb3"));
		views.add(new CustomKeyboardButton( 0, 2,"->","xdotool key ctrl+Tab","#e6ccb3"));

		return views;
	}
	public String getKeyboardTextValue(){

		StringBuilder sb = new StringBuilder();
		for(int x=0;x<mItems.size();x++){
			sb.append(mItems.get(x).getmTitle())
					.append("<xmousesep>").append(mItems.get(x).getmCommand())
					.append("<xmousesep>").append(mItems.get(x).getmSpans())
					.append("<xmousesep>").append(mItems.get(x).getmColor());

			if(x!=mItems.size()-1){
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	public void saveKeyboardLayout(){

		//Toast.makeText(getBaseContext(), sb.toString(), Toast.LENGTH_LONG).show();

		try{
			Log.d("saveKeyboardLayout", KEYLOAYOUTFILENAME);
			FileOutputStream fos = openFileOutput(KEYLOAYOUTFILENAME, Context.MODE_PRIVATE);
			fos.write(getKeyboardTextValue().getBytes());
			fos.close();
			//Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	public void xMouseNewCustomButton(final Integer id){
		//open dialog for new command

		//Preparing views
		LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
		View layout = inflater.inflate(R.layout.new_button_dialog, null);
		//layout_root should be the name of the "top-level" layout node in the dialog_layout.xml file.
		final EditText nameBox = (EditText) layout.findViewById(R.id.new_command_name);
		final EditText script = (EditText) layout.findViewById(R.id.new_command_command);
		final EditText spans = (EditText) layout.findViewById(R.id.new_command_width);
		final EditText color = (EditText) layout.findViewById(R.id.new_command_hex);
		//Building dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		if(id != null) {
			builder.setTitle("Edit Button");

			nameBox.setText(mItems.get(id).getmTitle());
			script.setText(mItems.get(id).getmCommand());
			spans.setText(String.valueOf(mItems.get(id).getmSpans()));
			color.setText(mItems.get(id).getmColor());

		}else{
			builder.setTitle("New Button");
		}
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String name = nameBox.getText().toString();
				String content = script.getText().toString();
				String spanStr = spans.getText().toString();
				String colorStr = color.getText().toString();

				if (!spanStr.isEmpty()) {

					if (name.length() > 0 && content.length() > 0) {
						int span = Integer.valueOf(spanStr);

						if (span > 0 && span < 11) {


							if(id!=null){

								mItems.get(id).setName(name);
								mItems.get(id).setSpans(span);
								mItems.get(id).setmCommand(content);
								mItems.get(id).setColor(colorStr);

							}else {
								mItems.add(new CustomKeyboardButton(0, span, name, content, colorStr));
							}

							mItemAdapter = new CustomKeyboardButtonAdapter(getBaseContext(), mItems);
							mCoolDragAndDropGridView.setAdapter(mItemAdapter);
							mItemAdapter.notifyDataSetChanged();

							Toast.makeText(getBaseContext(), "Button \"" + name + "\" saved to main layout", Toast.LENGTH_LONG).show();

							dialog.dismiss();
						} else {
							Toast.makeText(getBaseContext(), "Width must be between 1 and 10", Toast.LENGTH_LONG).show();

						}
					} else {

						Toast.makeText(getBaseContext(), "A required value was empty, button not saved", Toast.LENGTH_LONG).show();

					}
				} else {
					Toast.makeText(getBaseContext(), "A required value was empty, button not saved", Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();


	}
	private void useKeyboardSendText() {
		CharSequence contents = ET.getText();
		String t = contents.toString();
		Log.d("type",String.valueOf(t));
		if(!t.isEmpty()){
			t = t.replaceAll("'", "'\\\"'\\\"'"); // -> xdotool type ''"'"''
			t = t.replaceAll("\"", "\\\"");       // -> xdotool type '"'

			conn.executeShellCommand("xdotool type '" + t + "'");
			if(setting_keyboard_autoclear){
				ET.setText("");
			}
		}
	}


	static void setRecentCmdTextViewText(String s) {
		recentCmdTextView.setText(s);
	}


	@Override
	public void performCallback() {
		Log.d("tagCallback","doCallback");
		showConnectionStat();
	}

	@Override
	public void performCallback2(Integer i) {

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {///TODO
		if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE)
		{
			float gyroscopeX=sensorEvent.values[0];
			float gyroscopeY=sensorEvent.values[1];
			float gyroscopeZ=sensorEvent.values[2];

			gyroscopeX*=setting_gyro_x_sensitivity;
			gyroscopeY*=setting_gyro_y_sensitivity;
			gyroscopeZ*=setting_gyro_z_sensitivity;
			
			
			gyroscopeX += regyroscopeX;
			gyroscopeY += regyroscopeY;
			gyroscopeZ += regyroscopeZ;
			regyroscopeX = gyroscopeX - Math.round(gyroscopeX);
			regyroscopeY = gyroscopeY - Math.round(gyroscopeY);
			regyroscopeZ = gyroscopeZ - Math.round(gyroscopeZ);
			gyroscopeX -= regyroscopeX;
			gyroscopeY -= regyroscopeY;
			gyroscopeZ -= regyroscopeZ;

			String cmd;

			if (gyroToggle.isChecked()) {
				if (Math.abs(gyroscopeZ) >= setting_gyro_z_threshold || Math.abs(gyroscopeX) >= setting_gyro_x_threshold) {
					/*if (gyroscopeZ < 0 || gyroscopeX < 0) {
						cmd = "xdotool mousemove_relative -- " + (gyroscopeZ) * -15 + " " + (gyroscopeX) * -15;
					} else {

						cmd = "xdotool mousemove_relative " + (gyroscopeZ) * -15 + " " + (gyroscopeX) * -15;
					}*/
					cmd = "xdotool mousemove_relative -- " + (gyroscopeZ) * -15 + " " + (gyroscopeX) * -15;
					conn.executeShellCommand(cmd);
				}
			}
		}
		
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}
