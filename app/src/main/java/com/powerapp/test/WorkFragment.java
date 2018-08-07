package com.powerapp.test;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;

import com.opencsv.CSVReader;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by lukas on 21.04.2018.
 */

public class WorkFragment extends Fragment {

    //         CSV permissions
    public static final int PERMISSION_ASK = 1001;
    private final static int PERMISSIONS_REQUEST_WRITE_STORAGE = 5;


    private static int SETS_TILL_BIG_BREAK = 4;
    private static int POMODORO_TIME = 15;
    private static int SMALL_BREAK_TIME = 3;
    private static int BIG_BREAK_TIME = 12;
    ;
    private static String SMALL_BREAK_STATE = "small_break";
    private static String BIG_BREAK_STATE = "big_break";
    private static String STUDY_STATE = "study";

    private int counter = POMODORO_TIME;
    private String location;
    private String task;

    private boolean is_on = false;
    private boolean is_pause = true;

    private int pomodoro_step = 0;
    private int completed_pomodoro = 0;

    private Spinner location_spinner;
    private Spinner task_spinner;


    //                BUTTONS AND TEXTS
    private Button button_start;
    private Button button_cancel;
    private Button button_next;
    private Button button_pause;
    private Button button_unpause;
    private TextView time_text;
    private ImageView state_image;
    private LinearLayout pomodoro_list;
    private LinearLayout tomatoes_list;
    private ProgressBar progressBarView;
    public CountDownTimer cdt = null;
    Date end_time;
    Date start_time;


    public String modeFlag = STUDY_STATE; // shows if we have a work (true) or break_image (false)

    private static WorkFragment instance = null;

    private int counterLukasz = 0; // WHAT IS THIS LUKAS?

    // ----------------------------------------------------

    public static WorkFragment getInstance() {
        if (instance == null) {
            instance = new WorkFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Utils.onCreateCSV();

        View workView = inflater.inflate(R.layout.work, container, false);

        //                BUTTONS AND TEXTS
        button_start = workView.findViewById(R.id.button_start);
        button_cancel = workView.findViewById(R.id.button_cancel);
        button_next = workView.findViewById(R.id.button_next);
        button_pause = workView.findViewById(R.id.button_pause);
        button_unpause = workView.findViewById(R.id.button_unpause);
        time_text =  workView.findViewById(R.id.textView);
        state_image = workView.findViewById(R.id.stateImage);
        state_image.setVisibility(View.INVISIBLE);

//        SPINNERS GENERATED
        location_spinner = workView.findViewById(R.id.locationSpinner);
        task_spinner = workView.findViewById(R.id.taskSpinner);

        ArrayAdapter<CharSequence> adapter_location = ArrayAdapter.createFromResource( getActivity().getApplicationContext(),
                R.array.location_array, R.layout.spinner_item_custom);
        adapter_location.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
        location_spinner.setAdapter(adapter_location);

        ArrayAdapter<CharSequence> adapter_task = ArrayAdapter.createFromResource(
                getActivity().getApplicationContext(),
                R.array.task_array, R.layout.spinner_item_custom);
        adapter_task.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
        task_spinner.setAdapter(adapter_task);

        location_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                location = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                location = "None";
            }
        });

        task_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                task = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                task = "None";
            }
        });
        //SPINNERS END

        // TableRow containing the list of pomodoros (the goals for the day)
        final LinearLayout pomodoro_list = workView.findViewById(R.id.pomodoro_list);

        arrangeButtons();

//      --------------------------------------------------------------------------------
//                                  PROGRESS BAR
        progressBarView = workView.findViewById(R.id.view_progress_bar);

        Log.i("counter",String.valueOf(counter));
        /*Animation*/
        RotateAnimation makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(false);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setProgress(0);
        progressBarView.setMax(POMODORO_TIME);

        setProgress( counter, POMODORO_TIME, progressBarView);
        cdt = createCountDownTimer(counter);
        Log.i("counter","View Created");


        button_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                createCancelDialog();
            }


        });

        button_pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                is_pause = true;
                cdt.cancel();
                arrangeButtons();
            }
        });

        button_unpause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                is_pause = false;
                cdt = createCountDownTimer(counter);
                arrangeButtons();
                cdt.start();
            }
        });

        button_next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                if(modeFlag == STUDY_STATE)
                {
                    changeColorOfOneTomato(pomodoro_step,R.drawable.pomodoro_simple_gray);
                    pomodoro_step++;

                    // write CSV data
                    end_time = Calendar.getInstance().getTime();

                    Utils.writeCSVfile(location,task,0,start_time,end_time,POMODORO_TIME, POMODORO_TIME-counter
                            ,((MainActivity)getActivity()).todays_goal,counterLukasz);
                    Utils.readCSVfile();
                    counterLukasz++;
                }


                cdt.cancel();
                changeMode();
                is_on = true;
                is_pause = false;
                arrangeButtons();
                if(modeFlag == SMALL_BREAK_STATE){
                    setProgress( 0, SMALL_BREAK_TIME, progressBarView);
                    cdt = createCountDownTimer(SMALL_BREAK_TIME);


                }
                else if (modeFlag == STUDY_STATE){
                    cdt = createCountDownTimer(POMODORO_TIME);
                    setProgress( 0, POMODORO_TIME, progressBarView);
                    changeColorOfOneTomato(pomodoro_step,R.drawable.pomodoro_simple_green);
                    start_time = Calendar.getInstance().getTime();
                }
                else if(modeFlag == BIG_BREAK_STATE)
                {
                    cdt = createCountDownTimer(BIG_BREAK_TIME);
                    setProgress(0,BIG_BREAK_TIME,progressBarView);
                    clearTomatoes();
                }
                cdt.start();
            }
        });

        button_start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                is_on = true;
                is_pause = false;

                if(modeFlag == STUDY_STATE)
                {
                    setProgress( 0, POMODORO_TIME, progressBarView);
                    counter = POMODORO_TIME;
                    cdt.cancel();
                    cdt = createCountDownTimer(counter);
                    cdt.start();
                    changeColorOfOneTomato(pomodoro_step,R.drawable.pomodoro_simple_green);

                    start_time = Calendar.getInstance().getTime();
                }
                else if (modeFlag == SMALL_BREAK_STATE)
                {
                    setProgress( 0, SMALL_BREAK_TIME, progressBarView);
                    cdt = createCountDownTimer(SMALL_BREAK_TIME);
                    cdt.start();

                }
                else
                {
                    setProgress( 0, BIG_BREAK_TIME, progressBarView);
                    cdt = createCountDownTimer(BIG_BREAK_TIME);
                    cdt.start();

                }
                arrangeButtons();
            }
        });

        Integer todays_goal = ((MainActivity)getActivity()).todays_goal;
        if( todays_goal != 0 ){
            Integer size = 1;
            LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams (60,60);
            tomatoes_list = new LinearLayout(getContext());
            for(int i=1; i <= SETS_TILL_BIG_BREAK; i++){
                ImageView iv = new ImageView(getContext());
                iv.setLayoutParams( params );
                iv.setImageResource(R.drawable.pomodoro_simple_gray);
                iv.setId(i);
                tomatoes_list.addView(iv);

            }
            TextView n_goals = workView.findViewById(R.id.n_goals);
            n_goals.setText("" + String.valueOf(completed_pomodoro) + "/" + String.valueOf(todays_goal));
            pomodoro_list.addView(tomatoes_list);
        }
        return workView;
    }

    public void setProgress(int startTime, int endTime, ProgressBar progressBarView) {
        if(modeFlag == STUDY_STATE)
            progressBarView.setMax(POMODORO_TIME);
        else if(modeFlag == BIG_BREAK_STATE)
            progressBarView.setMax(BIG_BREAK_TIME);
        else if(modeFlag == SMALL_BREAK_STATE)
            progressBarView.setMax(SMALL_BREAK_TIME);
        progressBarView.setSecondaryProgress(startTime);
        time_text.setText(timeConversion(startTime));
    }

    public void clearTomatoes()
    {
        for(int i = 1; i<=SETS_TILL_BIG_BREAK;i++)
        {
            ImageView ff = tomatoes_list.findViewById(i);
            ff.setImageResource(R.drawable.pomodoro_simple_gray);
        }
    }


//    A FUNCITON TO ARRANGE BUTTONS
    public void arrangeButtons()
    {
        if(!is_on)
        {
            button_start.setVisibility(View.VISIBLE);
            if(modeFlag == STUDY_STATE)
                button_start.setBackgroundResource(R.drawable.work);
            else
                button_start.setBackgroundResource(R.drawable.break_image);

            button_pause.setVisibility(View.INVISIBLE);
            button_unpause.setVisibility(View.INVISIBLE);
            button_cancel.setVisibility(View.INVISIBLE);
            button_next.setVisibility(View.INVISIBLE);

            if(modeFlag == STUDY_STATE)
            {
                task_spinner.setVisibility(View.VISIBLE);
                location_spinner.setVisibility(View.VISIBLE);
            }
            state_image.setVisibility(View.INVISIBLE);

        }
        else{
            button_cancel.setVisibility(View.VISIBLE);
            button_next.setVisibility(View.VISIBLE);
            button_start.setVisibility(View.INVISIBLE);
            task_spinner.setVisibility(View.INVISIBLE);
            location_spinner.setVisibility(View.INVISIBLE);
            state_image.setVisibility(View.VISIBLE);

            if(modeFlag == STUDY_STATE)
                state_image.setImageResource(R.drawable.pomodoro_gray);
            else
                state_image.setImageResource(R.drawable.break_grey);

            if (!is_pause)
            {
                button_pause.setVisibility(View.VISIBLE);
                button_unpause.setVisibility(View.INVISIBLE);
            }
            else{
                button_pause.setVisibility(View.INVISIBLE);
                button_unpause.setVisibility(View.VISIBLE);
            }
        }
    }

    //    COUNTER FUNCTTION
    //----------------------------------------------------------------------------------------------------------------------------------
    public CountDownTimer createCountDownTimer(final int timeToGo)
    {
        counter = timeToGo;
        return new CountDownTimer((timeToGo+1) * 1000, 1000) {
            public void onTick(long millisUntilFinished) {

                setProgress( counter, timeToGo, progressBarView);
                counter--;
            }

            public void onFinish() {

                if(modeFlag == STUDY_STATE)
                {
                    completed_pomodoro++;
                    changeColorOfOneTomato(pomodoro_step, R.drawable.pomodoro_simple);
//                    ImageView ff = tomatoes_list.findViewById(pomodoro_step %SETS_TILL_BIG_BREAK + 1);
//                    ff.setImageResource(R.drawable.pomodoro_simple);
                    pomodoro_step++;
                    Integer todays_goal = getActivity().getIntent().getIntExtra("TODAYS_GOAL", 0);
                    Log.d("myTag", getView().toString());
                    TextView n_goals = getView().findViewById(R.id.n_goals);
                    n_goals.setText("" + String.valueOf(completed_pomodoro) + "/" + String.valueOf(todays_goal));

                    // write CSV data
                    end_time = Calendar.getInstance().getTime();

                    Utils.writeCSVfile(location,task,0,start_time,end_time,POMODORO_TIME, POMODORO_TIME-counter
                            ,((MainActivity)getActivity()).todays_goal,counterLukasz);
                    Utils.readCSVfile();
                    counterLukasz++;
                }


                is_on = false;
                is_pause = true;

                changeMode();
                ring();
                arrangeButtons();

                // Reset progress bar to 0
                progressBarView.setSecondaryProgress(0);

                if(modeFlag == SMALL_BREAK_STATE)
                {
                    time_text.setText("TAKE A BREAK!");
                }
                else if (modeFlag == BIG_BREAK_STATE)
                {
                    time_text.setText("TAKE A LONG BREAK!");
                }
                else
                {
                    time_text.setText("STUDY TIME!");
                }
                counter = 0;

            }
        };
    }

    public void ring()
    {
        try
        {
            long ringDelay = 1000;
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone alarmRingtone = RingtoneManager
                    .getRingtone(getActivity().getApplicationContext(), notification);
            alarmRingtone.play();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    alarmRingtone.stop();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, ringDelay);
        }
        catch (Exception e)
        {

        }
    }

    //    A FUNCTIION TO GET THE NEXT STATE
    public void changeMode()
    {
        if(modeFlag == STUDY_STATE && (pomodoro_step % SETS_TILL_BIG_BREAK != 0) && pomodoro_step !=0)
        {
            modeFlag = SMALL_BREAK_STATE;
        }
        else if(modeFlag == STUDY_STATE && (pomodoro_step % SETS_TILL_BIG_BREAK == 0) && pomodoro_step !=0)
        {
            modeFlag = BIG_BREAK_STATE;
        }
        else if (modeFlag == BIG_BREAK_STATE)
        {
            modeFlag = STUDY_STATE;
            clearTomatoes();
        }
        else if(modeFlag == SMALL_BREAK_STATE)
        {
            modeFlag = STUDY_STATE;
        }
        else
        {
            modeFlag = STUDY_STATE;
        }

    }

    public boolean isBreak(){
        return modeFlag == SMALL_BREAK_STATE || modeFlag == BIG_BREAK_STATE;
    }

    public boolean isIs_pause(){
        return is_pause;
    }


    public void discardSession(){
        is_on = false;
        is_pause = true;
        modeFlag = STUDY_STATE;
        pomodoro_step = 0;
        cdt.cancel();
        arrangeButtons();
        setProgress( POMODORO_TIME, POMODORO_TIME, progressBarView);
        cdt = createCountDownTimer(POMODORO_TIME);
        clearTomatoes();
        pomodoro_step = 0;
    }

    //    Something which creates the alert dialogue
    private void createCancelDialog(){
        DialogInterface dialog_interface;

        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure about that?")
                .setMessage("You will lose the last uncompleted pomodoro")
                .setPositiveButton("QUIT", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        is_on = false;
                        modeFlag = STUDY_STATE;
                        pomodoro_step = 0;
                        cdt.cancel();
                        arrangeButtons();
                        setProgress( POMODORO_TIME, POMODORO_TIME, progressBarView);
                        clearTomatoes();
                        // write CSV data
                        end_time = Calendar.getInstance().getTime();

                        Utils.writeCSVfile(location,task,0,start_time,end_time,POMODORO_TIME, POMODORO_TIME-counter
                                ,((MainActivity)getActivity()).todays_goal,counterLukasz);
                        Utils.readCSVfile();
                        counterLukasz++;
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    public String timeConversion(int totalSecs)
    {
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    public void changeColorOfOneTomato(int step, int imageId)
    {
        ImageView ff = tomatoes_list.findViewById(step %SETS_TILL_BIG_BREAK + 1);
        ff.setImageResource(imageId);

    }
}


