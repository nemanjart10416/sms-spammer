package com.sms.proba;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import com.sms.proba.R;

class Constants{
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final int PICK_CONTACT = 1;
}

public class MainActivity extends AppCompatActivity {
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                TextView t = (TextView) findViewById(R.id.Text);
                t.setText(Html.fromHtml("<font color='red'>Permission to send sms has been denied or request cancelled<font>"));
            }
        }
    }

    public TextView addTextView(int id){
        TableLayout Table = findViewById(R.id.TableId);

        TableRow row = new TableRow(MainActivity.this);
        TextView textVw = new TextView(MainActivity.this);
        textVw.setId(id);

        textVw.setText("sending");

        row.addView(textVw);
        Table.addView(row);

        return  textVw;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Button send = (Button) findViewById(R.id.sendBtn);

        if(!checkSmsMessagePermission()){
            requestSmsMessagePermission();
        }

        send.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {


                if(checkSmsMessagePermission()){
                    EditText PhoneNumEditText = (EditText) findViewById((R.id.PhoneNum));
                    final TextView text = (TextView) findViewById(R.id.Text);
                    EditText MessageEditText = (EditText) findViewById(R.id.Message);
                    EditText NumberEditText = findViewById(R.id.Num);
                    EditText DelayEditText = findViewById(R.id.Delay);

                    if(PhoneNumEditText.getText().toString().trim().length()>0){
                        final String PhoneNum = PhoneNumEditText.getText().toString();
                        final String message = MessageEditText.getText().toString();
                        if(NumberEditText.getText().toString().trim().length()>0){
                            final int NumOfMessages =Integer.parseInt(NumberEditText.getText().toString());
                            if(DelayEditText.getText().toString().trim().length()>0) {
                                final int Delay = Integer.parseInt(DelayEditText.getText().toString());

                                final Thread thread = new Thread(new Runnable() {
                                    private String error="";
                                    private int NumOfMessages2 = NumOfMessages;
                                    private int Delay2;
                                    private int sent=0;

                                    public void send(String number,String text){
                                        try{
                                            SmsManager smgr = SmsManager.getDefault();
                                            smgr.sendTextMessage(number,null,text.toString(),null,null);
                                        }
                                        catch (Exception e){ }
                                    }

                                    public TextView result;
                                    @Override
                                    public void run() {

                                        long thread_id = Thread.currentThread().getId();
                                        final int thread_ID = ((Number)thread_id).intValue();



                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                final TextView result = addTextView(thread_ID);
                                            }
                                        });

                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {

                                        }

                                        //TextView txt = findViewById(R.id.Text);
                                        if(NumOfMessages2 < 1) NumOfMessages2=1;
                                        if(Delay < 1) Delay2 = 1;
                                        else Delay2=Delay*1000;
                                        // here we send messages
                                        for(int i=0;i<NumOfMessages2;i++) {
                                            send(PhoneNum,message);
                                            sent++;
                                            try {
                                                Thread.sleep(Delay2);
                                            } catch (InterruptedException e) {
                                                error=e.toString();
                                            }

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {


                                                    try{
                                                        TextView t = findViewById(thread_ID);
                                                        if(t!=null){
                                                            t.setText("number "+PhoneNum+" sent: "+sent+"/"+NumOfMessages2);
                                                        }
                                                    }catch (Exception e){}
                                                }
                                            });
                                        }
                                            /*try{
                                                TextView ttt = findViewById(thread_ID);
                                                if(ttt!=null){
                                                    ttt.setText("sent all "+NumOfMessages2+" to "+PhoneNum);
                                                }
                                            }catch (Exception e){}*/
                                    }
                                });
                                try {
                                    thread.start();
                                }catch (Exception e){
                                    text.setText(e.toString());
                                }
                            }else{
                                DelayEditText.requestFocus();
                                text.setText("please specify delay between messages");
                            }
                        }else{
                            NumberEditText.requestFocus();
                            text.setText("please specify number of messages to be sent");
                        }
                    }else{
                        PhoneNumEditText.requestFocus();
                        text.setText("please specify phone number");
                    }
                }else{
                    TextView text = findViewById(R.id.Text);
                    text.setText(Html.fromHtml("<font color='red'>Permission to send sms has been denied or request cancelled Enable app permisions to send sms than try again<font>"));
                }
            }
        });
        ImageButton contactButton = findViewById(R.id.contact);
        contactButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                readContact();
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        EditText PhoneNumField = findViewById(R.id.PhoneNum);
        //pick contact
        if (reqCode == Constants.PICK_CONTACT && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = this.getContentResolver().query(contactUri, projection, null, null, null);

            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String num = cursor.getString(numberIndex);
                num = num.replace("(","");
                num = num.replace("-","");
                num = num.replace(" ","");
                String number = num.replace(")","");

                PhoneNumField.setText(number);
            }
            cursor.close();
        }
    }

    public void ToastMessage(String textMessage){
        Toast.makeText(getApplicationContext(),textMessage,Toast.LENGTH_LONG).show();
    }
    public void readContact(){
        TextView txt = findViewById(R.id.Text);
        requestContactPermission();
        Intent i=new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, Constants.PICK_CONTACT);
    }
    public boolean checkSmsMessagePermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }
    public void requestContactPermission(){
        TextView txt = findViewById(R.id.Text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
    }
    public void requestSmsMessagePermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 123);
    }
}
