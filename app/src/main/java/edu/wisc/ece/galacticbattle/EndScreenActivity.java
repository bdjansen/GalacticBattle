package edu.wisc.ece.galacticbattle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Blake on 10/24/2016.
 */
public class EndScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_options);
        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }

        Intent intent = getIntent();
        String message = intent.getStringExtra(GameActivity.EXTRA_OUTCOME);
        String endTitle = "YOU " + message;

        TextView label = (TextView) findViewById(R.id.endOutcome);
        label.setText(endTitle);
    }
}
