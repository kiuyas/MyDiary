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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
}

/**
 * ダイアログコールバック記述用インタフェース
 */
interface IDialogCallback {
    void execute(String input);
}
