package com.example.mapsactivity;

import android.os.Environment;

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
//        String str = input_text.getText().toString();
            // 파일 생성
            File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/favoriteData"); // 저장 경로
            // 폴더 생성
            if(!saveFile.exists()){ // 폴더 없을 경우
                saveFile.mkdir(); // 폴더 생성
            }
        try {
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTime = sdf.format(date);
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/favoriteData.txt", true));
            buf.append(nowTime + " "); // 날짜 쓰기
//            buf.append(str); // 파일 쓰기
            buf.newLine(); // 개행
            buf.close();
        } catch (FileNotFoundException e) {
            assert e != null;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileReader() {
        /////////////////////// 파일 읽기 ///////////////////////
        // 파일 생성
        String line = null; // 한줄씩 읽기
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/favoriteData"); // 저장 경로
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedReader buf = new BufferedReader(new FileReader(saveFile+"/favoriteData.txt"));
            while((line=buf.readLine())!=null){
//                tv.append(line);
//                tv.append("\n");
                // FStore
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
