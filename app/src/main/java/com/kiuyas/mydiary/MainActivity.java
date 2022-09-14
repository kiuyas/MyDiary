package com.kiuyas.mydiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /** 本体Documentsフォルダのパス */
    private static final String DOCUMENT_FOLDER = "/storage/self/Documents";

    /** 保存データのフィールドセパレータ */
    private static final String FIELD_SEPARATOR = ",";

    /** 保存データの行セパレータ */
    private static final String LINE_SEPARATOR = "\n";

    /** 曜日文字列 */
    private static final String[] DAY_OF_WEEK_NAMES = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    /** 画像ファイル名リスト */
    private final String[] members = new String[31];

    /** 日付文字列リスト */
    private final String[] dates = new String[31];

    /** 内容リスト */
    private final String[] contents = new String[31];

    /** Resource IDを格納するarray */
    private final List<Integer> imgList = new ArrayList<>();

    /** 表示中の年 */
    private int year;

    /** 表示中の月(0-11) */
    private int month;

    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 画面準備
        GridView gridview = findViewById(R.id.gridview);
        gridview.setAdapter(new GridAdapter(this.getApplicationContext(),
                R.layout.grid_items,
                imgList,
                dates,
                contents
        ));

        // クリックイベント設定
        setEvents();

        // 表示日付の初期化
        initDisplayDate();

        // カレンダー生成＆表示
        showCalendar();
    }

    // ============================================================

    /**
     * 表示日付の初期化
     */
    private void initDisplayDate() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
    }

    /**
     * カレンダー生成＆表示
     */
    private void showCalendar() {
        // 日付文字列リストセット
        makeDates();

        // 内容セット
        makeContents();

        // 画像リストセット
        makeImgList();

        // 年月表示
        showYearMonth();

        // グリッド再表示
        ((GridView)findViewById(R.id.gridview)).invalidateViews();
    }

    /**
     * 年月の表示
     */
    private void showYearMonth() {
        TextView txt1 = findViewById(R.id.textView);
        txt1.setText(String.format("%s/%s", year, month + 1));
    }

    // ============================================================

    /**
     * クリックイベント設定
     */
    private void setEvents() {
        GridView gridview = findViewById(R.id.gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * カレンダーのセルクリック時処理
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = String.format("%d/%d/%s の内容:", year, month + 1, dates[position]);
                openDialog(title, contents[position], new IDialogCallback() {
                    @Override
                    public void execute(String input) {
                        contents[position] = input;
                        ((GridView)findViewById(R.id.gridview)).invalidateViews();
                        saveText();
                    }
                });
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevMonth();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonth();
            }
        });
    }

    /**
     * PREVボタンクリック時処理
     */
    private void prevMonth() {
        month--;
        if (month == -1) {
            month = 11;
            year--;
        }
        showCalendar();
    }

    /**
     * NEXTボタンクリック時処理
     */
    private void nextMonth() {
        month++;
        if (month == 12) {
            month = 0;
            year++;
        }
        showCalendar();
    }

    // ============================================================

    /**
     * 日付文字列リストの生成
     */
    private void makeDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, 1);

        for(int i = 0; i < 31; i++) {
            dates[i] = String.format("%d %s", cal.get(Calendar.DATE), DAY_OF_WEEK_NAMES[cal.get(Calendar.DAY_OF_WEEK) - 1]);
            cal.add(Calendar.DATE, 1);
        }
    }

    /**
     * 内容リストの生成
     */
    private void makeContents() {
        for(int i = 0; i < 31; i++) {
            contents[i] = "";
        }

        loadText();
    }

    /**
     * 画像リストの生成
     */
    private void makeImgList() {
        imgList.clear();
        for (String member: members){
            if (member != null) {
                int imageId = getResources().getIdentifier(
                        member, "drawable", getPackageName());
                imgList.add(imageId);
            } else {
                imgList.add(0);
            }
        }
    }

    // ============================================================

    /**
     * 文字列入力ダイアログを開く
     * @param title
     * @param content
     * @param cb
     */
    private void openDialog(String title, String content, IDialogCallback cb) {
        LayoutInflater inflater
                = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.dialog, null);
        final EditText editText
                = (EditText)view.findViewById(R.id.editText1);
        editText.setText(content);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setIcon(R.drawable.icon)
                .setView(view)
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cb.execute(editText.getText().toString());
                            }
                        })
                .setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .show();
    }

    // ============================================================

    private String getFileName() {
        return String.format("diary_save_%d_%02d.txt", year, month + 1);
    }

    private void saveText(){
        String message = null;

        String fileName = getFileName();
        String inputText = makeSaveData();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE), "UTF-8");
            writer.write(inputText);
        } catch (FileNotFoundException e) {
            message = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            message = e.getMessage();
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    writer.close();
                } catch (Exception e) {
                }
            }
        }

        if (message != null) {
            message = "保存に失敗しました。" + message;
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private String makeSaveData() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 31; i++) {
            sb.append(String.format("%d%s%s%s", i + 1, FIELD_SEPARATOR,
                    contents[i], // TODO 入力内容に半角カンマや改行が含まれる場合の処理
                    LINE_SEPARATOR));
        }
        return sb.toString();
    }

    private void loadText() {
        String message = null;
        String fileName = getFileName();
        BufferedReader reader = null;
        try {
            String text;
            FileInputStream inputStream = openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while( (text = reader.readLine()) != null ){
                String[] s = text.split(FIELD_SEPARATOR);
                try {
                    int d = Integer.parseInt(s[0]);
                    if (d >= 1 && d <= 31) {
                        contents[d - 1] = s.length == 2 ? s[1] : "";
                    }
                } catch (NumberFormatException e) {
                }
            }
        } catch (FileNotFoundException e) {
            message = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            message = e.getMessage();
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }

        if (message != null) {
            message = "保存に失敗しました。" + message;
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}

/**
 * ダイアログコールバック記述用インタフェース
 */
interface IDialogCallback {
    void execute(String input);
}
