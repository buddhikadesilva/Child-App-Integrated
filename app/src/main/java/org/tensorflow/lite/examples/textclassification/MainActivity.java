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
  private NotificationReceiver nReceiver; //variable for get read notification - *notification related things coded in NLService.java file
  DAOAlert daoAlert;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tfe_tc_activity_main);
    Log.v(TAG, "onCreate");
      startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));

      nReceiver = new NotificationReceiver(); // create notification service object
      IntentFilter filter = new IntentFilter();
      filter.addAction("NOTIFICATION_LISTENER_EXAMPLE");
      registerReceiver(nReceiver,filter); // run the notification service

      daoAlert = new DAOAlert(); // database configuration - db related files - Alert.java , DAOAlert.java





    client = new TextClassificationClient(getApplicationContext()); // load TensorFlow modal
    handler = new Handler();
    Button classifyButton = findViewById(R.id.button);
    classifyButton.setOnClickListener(
        (View v) -> {
          classify("8989",inputEditText.getText().toString()); // input a msg from UI and classify using the modal
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
          client.load(); // load TensorFlow modal
        });
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.v(TAG, "onStop");
    handler.post(
        () -> {
          client.unload(); // unload TensorFlow modal
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

    class NotificationReceiver extends BroadcastReceiver { // Receive notifications from background

        @Override
        public void onReceive(Context context, Intent intent) {

            client = new TextClassificationClient(context);
            client.load(); // load TensorFlow modal
            if (intent.getStringExtra("from")!=null){
                String from = intent.getStringExtra("from");
                String message = intent.getStringExtra("message");
                classify(from,message); // classify the notification using the modal

            }


        }
    }





}
