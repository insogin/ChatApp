package com.wenjin.chatroom;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.wenjin.adapter.ChatMsgViewAdapter;
import com.wenjin.adapter.ExpressionGvAdapter;
import com.wenjin.bean.Message;
import com.wenjin.bean.User;
import com.wenjin.chatroom.R;
import com.wenjin.constant.ContentFlag;
import com.wenjin.impl.IhandleMessge;
import com.wenjin.service.MessageService;
import com.wenjin.service.UserService;
import com.wenjin.tool.ExpressionUtil;
import com.wenjin.tool.SystemConstant;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	private UserService userService;
	private MessageService msgService;
	private User user;
	private TextView view_loginer;
	private ListView lvMsgLisr;
	private EditText etCtn;
	private ImageView view_image;
	private Button btn_send;
	private Button btn_operate;
	private TextView tv_login;
	private Dialog progressDialog;
	private Dialog exitDialog;
	private ChatMsgViewAdapter ctAdapter;
	private PopupWindow optionWindow;
	private PopupWindow convertWindow;
	private View parent;
	private View popuCtnView;
	private View dialog_view;
	private View convert_view;
	private View viewpager_layout;
	private RelativeLayout express_spot_layout;
	private boolean loginFlag = false; //是否已经登录
	private List<Message> msgList = new ArrayList<Message>();	//保存所有消息内容
	private ViewPager viewPager;	//实现表情的滑动翻页
	private int imageIds[] = ExpressionUtil.getExpressRcIds();	//保存所有表情资源的id
	/**
	 * 消息处理器
	 */
	private Handler handle = new Handler(){
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			int what = msg.what; //User-defined message code so that the recipient can identify what this message is about
			switch (what) {
			case 1:
				Message message = (Message) msg.obj;
				//当前用户登录
				if(user.getId() == message.getId()){
					view_loginer.setText(user.getName());
					view_image.setImageBitmap(message.getBitmap());  //位图
					if(progressDialog.isShowing()){
						progressDialog.dismiss();
					}
				}
				loginFlag = true;
				msgList.add(message);
				ctAdapter.notifyDataSetChanged();
				lvMsgLisr.setSelection(msgList.size() - 1);
				break;
			case 2:
				loginFlag = false;
				progressDialog.dismiss();
				Toast.makeText(ChatActivity.this, R.string.conn_fail,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        LayoutInflater inflater = this.getLayoutInflater();
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);	//自定义标题布局文件
        parent = findViewById(R.id.main);
        view_loginer = (TextView) findViewById(R.id.loginer);
        lvMsgLisr = (ListView) findViewById(R.id.lv_message);
        lvMsgLisr.setOnTouchListener(new MyOnTouchListener());
        view_image = (ImageView) findViewById(R.id.image);
        btn_send = (Button) findViewById(R.id.sendMsg);
        btn_operate = (Button) findViewById(R.id.btn_operate);
        etCtn = (EditText) findViewById(R.id.content);
        viewpager_layout = findViewById(R.id.viewpager_layout);
        popuCtnView = inflater.inflate(R.layout.popu_option, null);
        dialog_view = inflater.inflate(R.layout.exit_dialog, null);
        convert_view = inflater.inflate(R.layout.popu_convert, null);
        express_spot_layout = (RelativeLayout) findViewById(R.id.express_spot_layout);
        tv_login = (TextView) popuCtnView.findViewById(R.id.user_login);
        viewPager = (ViewPager) findViewById(R.id.tabpager);
        btn_send.setOnClickListener(new SendBtnClickListener());
        
        exitDialog = new Dialog(this, R.style.dialog);
        exitDialog.setContentView(dialog_view);
        
        progressDialog = new Dialog(this, R.style.dialog);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);  //
        
        etCtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(viewpager_layout.getVisibility() == View.VISIBLE){
					viewpager_layout.setVisibility(View.GONE);
				}
			}
		});
    }
    
    /**
     * 隐藏软键盘
     * @param view
     */
    public void hideSoftinput(View view){ //隐藏软键盘
    	InputMethodManager manager = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
    	if(manager.isActive()){
    		manager.hideSoftInputFromWindow(etCtn.getWindowToken(), 0);
    	}
    }
    
    public final class MyOnTouchListener implements View.OnTouchListener{
		@Override //
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				hideSoftinput(v);
			}
			return false;
		}
    }
    
	/**
     * 处理发送按钮事件
     * @author Administrator
     *
     */
	private final class SendBtnClickListener implements View.OnClickListener{
		public void onClick(View v) {
			String ctn = etCtn.getText().toString();		//待发送的消息
			if(ctn.equals("") || ctn == null){
				Toast.makeText(ChatActivity.this, R.string.tip_input, Toast.LENGTH_SHORT).show(); //Toast
				return;
			}
			try {
				etCtn.setText("");
				msgService.sendMsg(ctn); //发给服务器
			} catch (Exception e) {
				buildConnection();
			}
		}
    }
    
    /**
     * 处理回退事件
     */
	public void onBackPressed() {
		if(viewpager_layout.getVisibility() == View.VISIBLE){
			viewpager_layout.setVisibility(View.GONE);
			return;
		}
		Button btn_yes = (Button) dialog_view.findViewById(R.id.exitBtn0);
		Button btn_no = (Button) dialog_view.findViewById(R.id.exitBtn1);
		btn_yes.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				msgService.quitApp();
				ChatActivity.this.finish();
				System.exit(0);
			}
		});
		btn_no.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				exitDialog.dismiss();
			}
		});
		exitDialog.show();
	}

	@Override
	protected void onDestroy() {
		msgService.quitApp();
		Log.i(ContentFlag.TAG, "chatactivity destroy!");
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		if(null == user) buildConnection();
		ctAdapter = new ChatMsgViewAdapter(this, user, msgList);
		lvMsgLisr.setAdapter(ctAdapter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	/**
	 * 建立连接
	 * @param isLogin 是否已经登录
	 */
    private void buildConnection() {
    	userService = new UserService(this);
    	msgService = new MessageService(this);
    	user = userService.queryUser();
    	//存在当前用户，尝试连接服务器
		if (null != user) {
			
			progressDialog.show();
			new Thread(new Runnable() {
				public void run() {
					try {
						msgService.startConnect(user, new IhandleMessge() {
							@Override
							public void handleMsg(Message message) {
								android.os.Message msg = handle.obtainMessage();
								msg.what = 1;
								msg.obj = message;
								handle.sendMessage(msg);
							}
						});
					} catch (IOException e) {
						handle.sendEmptyMessage(2);
					} 
				}
			}).start();
		}
	}
    
    /**
     * 弹出popu窗口.
     * @param view
     */
    public void outOperatePopuWindow(View view){
		if(loginFlag == true){
			tv_login.setText(R.string.btn_reverse);
		}else{
			tv_login.setText(R.string.btn_login);
		}
		if(null == optionWindow){
			optionWindow = new PopupWindow(popuCtnView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			optionWindow.setFocusable(true);
			optionWindow.setBackgroundDrawable(new BitmapDrawable());
		}
		optionWindow.showAsDropDown(btn_operate, 5, 0);
    }
    
	/**
     * 处理登录/注册事件
     * @param view
     */
    public void userLoginOrRegister(View view){
    	final Intent intentLogin = new Intent(this, UserLoginActivity.class);
    	final Intent intentRegis = new Intent(this, UserRegisActivity.class);
    	final List<User> userList = userService.queryResigterUser();
    	//未登录
    	if(loginFlag == false){
    		switch (view.getId()) {
			case R.id.user_login:
	        	if(userList.size() > 0){
	        		Bundle bundle = new Bundle();
	        		bundle.putSerializable("regisUsers", (Serializable)userList);
	        		intentLogin.putExtras(bundle);
	        		startActivity(intentLogin);
	        	}else{
	        		startActivity(intentRegis);
	        	}
				break;
			case R.id.user_register:
				startActivity(intentRegis);
				break;
			}
    	}else{
    		Button btn_convert = (Button) convert_view.findViewById(R.id.btn_convert);
    		Button btn_cancel = (Button) convert_view.findViewById(R.id.btn_cancel);
    		switch (view.getId()) {
			case R.id.user_login:
	    		btn_convert.setOnClickListener(new View.OnClickListener() {
	    			public void onClick(View v) {
	    				msgService.quitApp();
	            		Bundle bundle = new Bundle();
	            		bundle.putSerializable("regisUsers", (Serializable)userList);
	            		intentLogin.putExtras(bundle);
	            		startActivity(intentLogin);
	    				convertWindow.dismiss();
	    			}
	    		});
				break;
			case R.id.user_register:
	    		btn_convert.setOnClickListener(new View.OnClickListener() {
	    			public void onClick(View v) {
	    				msgService.quitApp();
	    				startActivity(intentRegis);
	    				convertWindow.dismiss();
	    			}
	    		});
				break;
			}
    		btn_cancel.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				convertWindow.dismiss();
    			}
    		});
    		if(null == convertWindow){
    			convertWindow = new PopupWindow(convert_view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    			convertWindow.setFocusable(true);
    			convertWindow.setBackgroundDrawable(new BitmapDrawable());
    			convertWindow.setAnimationStyle(R.style.popu_animation);
        		convertWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    		}else{
    			convertWindow.setContentView(convert_view);
    			convertWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    		}
    	}
    	optionWindow.dismiss();
    }
    
    /**
     * 显示表情对话框
     * @param view
     */
    public void showExpressionWindow(View view){
    	//判断软键盘是否打开
    	this.hideSoftinput(view);
    	//显示表情对话框
    	viewpager_layout.setVisibility(View.VISIBLE);
    	//获取屏幕当前分辨率
        Display currDisplay = getWindowManager().getDefaultDisplay();
        int displayWidth = currDisplay.getWidth();
        //获得表情图片的宽度/高度
        Bitmap express = BitmapFactory.decodeResource(getResources(), R.drawable.f000);
    	int headWidth = express.getWidth();
    	int headHeight = express.getHeight();
    	Log.d(ContentFlag.TAG, displayWidth+":" + headWidth);
    	final int colmns = displayWidth/headWidth > 7 ? 7 : displayWidth/headWidth;	//每页显示的列数
    	final int rows = 170/headHeight > 3 ? 3 : 170/headHeight;	//每页显示的行数
    	final int pageItemCount = colmns * rows;		//每页显示的条目数
    	//计算总页数
		int totalPage = SystemConstant.express_counts % pageItemCount == 0 ? 
				SystemConstant.express_counts / pageItemCount : SystemConstant.express_counts / pageItemCount + 1;
		final List<View> listView = new ArrayList<View>();
		for (int index = 0; index < totalPage; index++) {
			listView.add(getViewPagerItem(index, colmns, pageItemCount));
		}
		express_spot_layout.removeAllViews();
		for (int i = 0; i < totalPage; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setId(i+1);
			if(i == 0){
				imageView.setBackgroundResource(R.drawable.d2);
			}else{
				imageView.setBackgroundResource(R.drawable.d1);
			}
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.bottomMargin = 20;
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			if(i!= 0){
				layoutParams.addRule(RelativeLayout.ALIGN_TOP, i);
				layoutParams.addRule(RelativeLayout.RIGHT_OF, i);
			}
			express_spot_layout.addView(imageView, layoutParams);
		}
		Log.d(ContentFlag.TAG, express_spot_layout.getChildCount() + "");
		//填充viewPager的适配器
		viewPager.setAdapter(new PagerAdapter() {
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			public int getCount() {
				return listView.size();
			}
			
			public void destroyItem(View container, int position, Object object) {
				((ViewPager)container).removeView(listView.get(position));
			}
			
			public Object instantiateItem(View container, int position) {
				((ViewPager)container).addView(listView.get(position));
				return listView.get(position);
			}
		});
		//注册监听器
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
    }
    
    private final class MyPageChangeListener implements OnPageChangeListener{
    	private int curIndex = 0;
		public void onPageSelected(int index) {
			express_spot_layout.getChildAt(curIndex).setBackgroundResource(R.drawable.d1);
			express_spot_layout.getChildAt(index).setBackgroundResource(R.drawable.d2);
			curIndex = index;
		}
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		public void onPageScrollStateChanged(int arg0) {
		}
    }
    
    private View getViewPagerItem(final int index, int colums, final int pageItemCount) {
    	LayoutInflater inflater = this.getLayoutInflater();
        View express_view = inflater.inflate(R.layout.express_gv, null);
    	GridView gridView = (GridView) express_view.findViewById(R.id.gv_express);
    	gridView.setNumColumns(colums);
    	gridView.setAdapter(new ExpressionGvAdapter(index, pageItemCount, imageIds, inflater));
		//注册监听事件
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int positon,
					long id) {
				Bitmap bitmap = null;
				int start = index * pageItemCount;	//起始位置?
				positon = positon + start;
				bitmap = BitmapFactory.decodeResource(getResources(), imageIds[positon]);
				ImageSpan imageSpan = new ImageSpan(bitmap);
				String str = "";
				if(positon < 10){
					str = "[f00"+positon+"]";
				}else if(positon < 100){
					str = "[f0"+positon+"]";
				}else{
					str = "[f"+positon+"]";;
				}
				SpannableString spannableString = new SpannableString(str);
				spannableString.setSpan(imageSpan, 0, str.length(),
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				etCtn.append(spannableString);
				viewpager_layout.setVisibility(View.GONE);
			}
		});
		return express_view;
	}
}
