package com.powerapp.test;

import android.util.Log;

import com.opencsv.CSVReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ahmet on 29.04.2018.
 */

public final class Utils {
    private Utils() { }

    //                  Write to CSV variables
    public static FileWriter file_writer;
    public static BufferedWriter buffer_writer;
    public static File csv_file;
    public static String file_name = "";

    public static String FILE_NAME = "dataPomodoro_02";

    public static  String FILENAME = "dataPomodoro_02";

    public static List<String> fields_list = Arrays.asList("location", "action", "isAccomplished",
            "totalDuration", "expectedDuration", "actualDuration", "startDate", "startHour","endDate", "endHour", "todayGoal", "dumbCounter");



    public static void onCreateCSV()
    {
        //                      Setup CSV writing
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        file_name = FILE_NAME + ".csv";
        String filePath = baseDir + File.separator + file_name;
        csv_file = new File(filePath);
        try {
            if (!csv_file.exists()) {
                Log.d("tag1", "CSV csv_file doesn't exist. Creation of the csv_file with the header.");
                csv_file.createNewFile();
                file_writer = new FileWriter(csv_file.getAbsoluteFile(), true);
                buffer_writer = new BufferedWriter(file_writer);
                buffer_writer.write(String.join(",", fields_list));
                buffer_writer.write("\n");
                buffer_writer.flush();
                buffer_writer.close();
                file_writer.close();
            }else{
                Log.d("tag1", "CSV csv_file do exist.");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        //-----------------------------------------------------
    }

    public static String readCSVtext(){
        String out = "";
        try{
            String uri = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            //String fileName = getIntent().getStringExtra("FILE_NAME");
            String fileName = FILENAME + ".csv";
            uri=uri + File.separator + fileName;
            File file = new File(uri);
            System.out.println("ManualDeb: filename "+uri);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader csvStreamReader = new InputStreamReader(fileInputStream);
            CSVReader reader = new CSVReader(csvStreamReader);
            String [] nextLine;
            nextLine = reader.readNext();

            out+="\"";

            out += String.join(",", nextLine);
            Log.d("tag10", "outside lukasz");
            while ((nextLine = reader.readNext()) != null) {
                out += "\\n";
                out += String.join(",", nextLine);
            }
            out += "\"";
        }catch(Exception e){
            e.printStackTrace();
        }
        return out;
    }


    public static  void closeWritingCSVfile()
    {
        try {
            buffer_writer.flush();
            buffer_writer.close();
            file_writer.close();
            Log.d("tag3", "CSV closed after writing.");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void readCSVfile(){
        try{
            String uri = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            //String file_name = getIntent().getStringExtra("FILE_NAME");
            String fileName = FILE_NAME + ".csv";

            uri=uri + File.separator + fileName;
            File file = new File(uri);
            System.out.println("ManualDeb: filename "+uri);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader csvStreamReader = new InputStreamReader(fileInputStream);
            CSVReader reader = new CSVReader(csvStreamReader);
            String [] nextLine;
            Log.d("tag10", "outside lukasz");
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                System.out.println(String.join(",", nextLine));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void writeCSVfile(String location, String action, Integer is_accomplished, Date start_date, Date end_date, Integer expected_duration, Integer actual_duration, Integer todays_goal, Integer dumbCounter){


        String start_string = start_date.toString();
        String end_string = end_date.toString();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm");
        long diff = (end_date.getTime() - start_date.getTime())/1000;
        if(actual_duration>diff) {
            actual_duration = (int) diff;
        }
        String total_duration = Long.toString(diff);
        try {
            file_writer = new FileWriter(csv_file.getAbsoluteFile(),true);
            buffer_writer = new BufferedWriter(file_writer);
            buffer_writer.write(location + "," + action + "," + is_accomplished + "," + total_duration + "," + expected_duration.toString() + ","
                    + actual_duration.toString() + "," + formatDate.format(start_date) + "," + formatHour.format(start_date) + ","
                    + formatDate.format(end_date) + "," + formatHour.format(end_date) + "," + todays_goal.toString() + "," + dumbCounter.toString() + "\n");

            Log.d("tag3", "New raw appended to the CSV.");
            closeWritingCSVfile();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
