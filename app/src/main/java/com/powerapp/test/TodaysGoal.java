package com.powerapp.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.content.Intent;

public class TodaysGoal extends AppCompatActivity {
    Button btn_set_goals;
    NumberPicker np;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todays_goal);


        np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMaxValue(60);
        np.setMinValue(1);
        np.setFocusable(true);
        np.setFocusableInTouchMode(true);
        np.setValue(4);

        btn_set_goals = (Button) findViewById(R.id.button_set_goals);
        btn_set_goals.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("TODAYS_GOAL", np.getValue());
                startActivity(intent);
            }
        });
    }
}
