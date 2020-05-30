package com.example.mapsactivity;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileController {

    private static final String TAG = "FileController";


    public static void fileWriter(FStore fStore) {
            /////////////////////// 파일 쓰기 ///////////////////////
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/favoriteData"); // 저장 경로
            // 폴더 생성
        try {
            if(!saveFile.exists()){ // 폴더 없을 경우
                boolean isSuccess = saveFile.mkdir(); // 폴더 생성
                Log.e(TAG, "만들었나요???" + Boolean.toString(isSuccess));
            }
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/favoriteData.txt", true));
            buf.append(fStore.code).append("-");
            buf.append(String.valueOf(fStore.favorites)); // 날짜 쓰기
            buf.newLine(); // 개행
            Log.d(TAG, fStore.code);
            buf.close();
        } catch (FileNotFoundException e) {
            assert e != null;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public void fileReader() {
//        /////////////////////// 파일 읽기 ///////////////////////
//        // 파일 생성
//        String line = null; // 한줄씩 읽기
//        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/favoriteData"); // 저장 경로
//        // 폴더 생성
//        if(!saveFile.exists()){ // 폴더 없을 경우
//            saveFile.mkdir(); // 폴더 생성
//        }
//        try {
//            BufferedReader buf = new BufferedReader(new FileReader(saveFile+"/favoriteData.txt"));
//            while((line=buf.readLine())!=null){
////                tv.append(line);
////                tv.append("\n");
//                // FStore
//            }
//            buf.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
