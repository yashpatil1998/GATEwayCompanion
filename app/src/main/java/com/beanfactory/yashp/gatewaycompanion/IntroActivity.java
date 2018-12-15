package com.beanfactory.yashp.gatewaycompanion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class IntroActivity extends AppCompatActivity {

    ImageView logo;
    Animation fromBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        logo = (ImageView) findViewById(R.id.logoImage);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);

        logo.setAnimation(fromBottom);

        fromBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MyAsyncTask myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });




//        try {
//            Thread.sleep(2000);
//            startActivity(new Intent(IntroActivity.this, MainActivity.class));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }




    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        private MyAsyncTask() {
            dialog = new ProgressDialog(IntroActivity.this);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            startActivity(new Intent(IntroActivity.this, MainActivity.class));
            finish();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading GATEway Companion");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
}
