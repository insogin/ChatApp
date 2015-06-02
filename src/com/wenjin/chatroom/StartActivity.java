package com.wenjin.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.wenjin.chatroom.R;

public class StartActivity extends Activity{

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
				setContentView(R.layout.welcome);
				
				final EditText uname = (EditText) findViewById(R.id.username_edittext); //defined in welcome.xml
		        final EditText passwd = (EditText) findViewById(R.id.password_edittext);

		        final Button loginButton = (Button) findViewById(R.id.main_login_btn);
		        loginButton.setOnClickListener(new OnClickListener() {

		            public void onClick(View v) {
		                if (checkPassword(uname.getText().toString(), passwd.getText().toString())) {

		                    // Create an explicit Intent for starting the HelloAndroid
		                    // Activity
		                    Intent helloAndroidIntent = new Intent(StartActivity.this,
		                            ChatActivity.class);

		                    // Use the Intent to start the HelloAndroid Activity
		                    startActivity(helloAndroidIntent);
		                } else {
		                    uname.setText(""); //set null text
		                    passwd.setText("");
		                }
		            }
		        });
				
	}
	

    private boolean checkPassword( String uname1, String passwd1) { //as opposed to immutable text like Strings
        // Just pretending to extract text and check password
    	
    	if(uname1.equals("wenjin")&&passwd1.equals("123456"))
    		return true;
    	return false;
    	
    }
	
	//该函数由welcome.xml文件内 id为main_login_btn的按钮触发（在 onclick事件上）
   // public void welcome_login(View v) { 
    
    
    public void welcome_exit(View view){
		this.finish();  // 关闭，无法通过返回键返回
		System.exit(0);
    }
}

