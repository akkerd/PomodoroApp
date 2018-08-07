package com.powerapp.test;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    //              PERMISSIONS REQUIRED FOR CSV CREATION
    private static final int REQUEST_WRITE_PERMISSION = 786;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //openFilePicker();
        }
    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
    }
    // -------------------------------------------------------------------
    private static final int PERMISSION_REQUEST_CODE = 1;

    public Integer todays_goal;
    public WorkFragment workFragment;
    private BottomNavigationView bottomNav;


    @Override
    public void onBackPressed() {
        Log.i("myTag", "Nothing happens");
        createBackAlertDialog();
    }

    //    Something which creates the alert dialogue
    private void createBackAlertDialog(){
        //DialogInterface dialog_interface;
        //dialog_interface = new DialogInterface();
        new AlertDialog.Builder(this)
                .setTitle("Are you sure about leaving?")
                .setMessage("All progress in this session will be lost.")
                .setPositiveButton("LEAVE", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        workFragment.discardSession();
                        moveTaskToBack(true);
                        finish();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();

    }

    private void createFragmentAlertDialog( String fromFragment ){
        //dialog_interface = new DialogInterface();
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this)
                .setTitle("Are you sure about leaving?")
                .setMessage("If you go away, all progress in this session will be lost.")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bottomNav.setSelectedItemId(R.id.nav_work);
                    }
                });

        if( fromFragment.equals("notification") ){
            dialog_builder.setPositiveButton("LEAVE",  new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //bottomNav.setSelectedItemId(R.id.nav_notification);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new NotificationFragment()).commit();
                    workFragment.discardSession();
                }
            });
        }else{
            dialog_builder.setPositiveButton("LEAVE",  new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //bottomNav.setSelectedItemId(R.id.nav_stats);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new StatisticsFragment()).commit();
                    workFragment.discardSession();
                }
            });
        }
        dialog_builder.show();
    }


    //    What is this ?
    long userInteractionTime = 0;
    @Override
    public void onUserInteraction() {
        userInteractionTime = System.currentTimeMillis();
        super.onUserInteraction();
        Log.i("appname","Interaction");
    }


    @Override
    public void onUserLeaveHint() {
        long uiDelta = (System.currentTimeMillis() - userInteractionTime);

        super.onUserLeaveHint();
        //Log.i("bThere","Last User Interaction = "+uiDelta);
        if (uiDelta < 100) {
            Log.i("appname", "Home Key Pressed");
//            createAlertDialog("back");
        }
        else
            Log.i("appname","We are leaving, but will probably be back shortly!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        workFragment = WorkFragment.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the number of goals assigned from previous Activity
        todays_goal = getIntent().getIntExtra("TODAYS_GOAL", 0);

//      --------------------------------------------------------------------------------
//                    NAVIGATION BAR
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_work);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, workFragment).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                    Fragment selectedFragment = null;
                    switch(item.getItemId()){
                        case R.id.nav_notification:
                            if(workFragment.isBreak() || workFragment.isIs_pause())
                                selectedFragment = new NotificationFragment();
                            else{
                                selectedFragment = workFragment;
                                bottomNav.setSelectedItemId(R.id.nav_work);
                                createFragmentAlertDialog("notification");

                            }
                            break;
                        case R.id.nav_work:
                            selectedFragment = workFragment;
                            break;
                        case R.id.nav_stats:
                            if(workFragment.isBreak() || workFragment.isIs_pause())
                                selectedFragment = new StatisticsFragment();
                            else{
                                selectedFragment = workFragment;
                                bottomNav.setSelectedItemId(R.id.nav_work);
                                createFragmentAlertDialog("stats");
                            }

                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();


                    return true;

                }
            };



}
