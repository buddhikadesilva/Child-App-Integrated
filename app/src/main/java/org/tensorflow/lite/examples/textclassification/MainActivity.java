/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.textclassification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.tensorflow.lite.examples.textclassification.client.Result;
import org.tensorflow.lite.examples.textclassification.client.TextClassificationClient;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

/** The main activity to provide interactions with users. */
public class MainActivity extends AppCompatActivity {
  private static final String TAG = "TextClassificationDemo";

  private TextClassificationClient client;

  private TextView resultTextView;
  private EditText inputEditText;
  private Handler handler;
  private ScrollView scrollView;
  private NotificationReceiver nReceiver;
  DAOAlert daoAlert;
    String[] predatorsWords = {"8", "99", "142", "182","1174","ASL","CD9","FYEO","GNOC","GYPO","HAK","IWSN","KFY","KPC","MIRL","MOS","NIFOC","NSFW","P911","PAW","PAL","PIR","POS","PRON","RUMORF","SWAK","TDTM","WTTO"};
    private TextView txtView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tfe_tc_activity_main);
    Log.v(TAG, "onCreate");
      startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));

      nReceiver = new NotificationReceiver();
      IntentFilter filter = new IntentFilter();
      filter.addAction("NOTIFICATION_LISTENER_EXAMPLE");
      registerReceiver(nReceiver,filter);
      daoAlert = new DAOAlert();





    client = new TextClassificationClient(getApplicationContext());
    handler = new Handler();
    Button classifyButton = findViewById(R.id.button);
    classifyButton.setOnClickListener(
        (View v) -> {
          classify("8989",inputEditText.getText().toString());
        });
    resultTextView = findViewById(R.id.result_text_view);
    inputEditText = findViewById(R.id.input_text);
    scrollView = findViewById(R.id.scroll_view);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.v(TAG, "onStart");
    handler.post(
        () -> {
          client.load();
        });
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.v(TAG, "onStop");
    handler.post(
        () -> {
          client.unload();
        });
  }

  /** Send input text to TextClassificationClient and get the classify messages. */
  private void classify(final String from,final String text) {
    handler.post(
        () -> {
          // Run text classification with TF Lite.

                List<Result> results = client.classify(text);

                // Show classification result on screen
                showResult(from, text, results);

        });
  }

  /** Show classification result on the screen. */
  private void showResult(final String from,final String inputText, final List<Result> results) {
    // Run on UI thread as we'll updating our app UI
    runOnUiThread(
        () -> {
          String textToShow = "Input: " + inputText + "\nOutput:\n";
          for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            if(result.getTitle().equals("Positive")){
                Alert alert;
                if(result.getConfidence()>0.67){
                    textToShow += "Alert: Looks like predator!!!\n";
                     alert=new Alert(from,inputText,"Looks like predator!!");
                }else{
                     alert=new Alert(from,inputText,"looks okay.");
                }

                daoAlert.add(alert).addOnSuccessListener(suc->{
                    Toast.makeText(getApplicationContext(),"Record is inserted",Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(er->
                {
                    Toast.makeText(getApplicationContext(),""+er.getMessage(),Toast.LENGTH_SHORT).show();
                });
            }
            textToShow += String.format("    %s: %s\n", result.getTitle(), result.getConfidence());
          }
          textToShow += "---------\n";

          // Append the result to the UI.
          resultTextView.append(textToShow);

          // Clear the input text.
          inputEditText.getText().clear();

          // Scroll to the bottom to show latest entry's classification result.
          scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
  }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }



    public void buttonClicked(View v){

//        if(v.getId() == R.id.btnCreateNotify){
//            NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
//            ncomp.setContentTitle("My Notification");
//            //  ncomp.setContentText("Notification Listener Service Example");
//            //  ncomp.setTicker("Notification Listener Service Example");
//            // ncomp.setSmallIcon(R.drawable.ic_launcher);
//            ncomp.setAutoCancel(true);
//            nManager.notify((int)System.currentTimeMillis(),ncomp.build());
//        }
//        else if(v.getId() == R.id.btnClearNotify){
//            Intent i = new Intent("NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
//            i.putExtra("command","clearall");
//            sendBroadcast(i);
//        }
//        else if(v.getId() == R.id.btnListNotify){
//            Intent i = new Intent("NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
//            i.putExtra("command","list");
//            sendBroadcast(i);
//        }


    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            client = new TextClassificationClient(context);
            client.load();
         //   handler = new Handler();
//            String temp = intent.getStringExtra("notification_event") + "\n" + txtView.getText();
            if (intent.getStringExtra("from")!=null){
                String from = intent.getStringExtra("from");
                String message = intent.getStringExtra("message");
//                txtView.setText(from+" "+message);
                classify(from,message);


//                Alert alert=new Alert(from,message,checkMessage(message));
//                daoAlert.add(alert).addOnSuccessListener(suc->{
//                    Toast.makeText(getApplicationContext(),"Record is inserted",Toast.LENGTH_SHORT).show();
//                }).addOnFailureListener(er->
//                {
//                    Toast.makeText(getApplicationContext(),""+er.getMessage(),Toast.LENGTH_SHORT).show();
//                });
            }


        }
    }

//    public String checkMessage(String msg){
//
//        String[] words=msg.split("\\s");//splits the string based on string
//
//        Collection<String> coll1 = Arrays.asList(words);
//        Collection<String> coll2 = Arrays.asList(predatorsWords);
//
//        // ArrayList
//        ArrayList<String> list1 = new ArrayList<>();
//        ArrayList<String> list2 = new ArrayList<>();
//
//        list1.addAll(coll1);
//        list2.addAll(coll2);
//
//        list2.retainAll(list1);
//
//        System.out.println(list2);
//
//        // TreeSet
//        TreeSet<String> set1 = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
//        TreeSet<String> set2 = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
//
//        set1.addAll(coll1);
//        set2.addAll(coll2);
//
//        set2.retainAll(set1);
//
//        System.out.println(set2);
//        if(set2.isEmpty()){
//            return "No words found";
//        }else{
//            return String.valueOf(set2);
//        }
//
//    }



}
