package com.example.techbgi.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.techbgi.R;
import com.example.techbgi.database.DbHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class SheetActivity extends AppCompatActivity {


    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    final DatabaseReference reference = firebaseDatabase.getReferenceFromUrl("https://techbgi-default-rtdb.firebaseio.com/");
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        Toolbar toolbar = findViewById(R.id.attentoolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText("Attendances Sheet");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);

        showTable();

    }

    private void showTable() {
        DbHelper dbHelper = new DbHelper();
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        String[] idArray = getIntent().getStringArrayExtra("idArray");
        String[] rollNoArray = getIntent().getStringArrayExtra("rollNoArray");
        String[] studentNameArray = getIntent().getStringArrayExtra("studentNameArray");
        String month = getIntent().getStringExtra("month");
        classId = getIntent().getStringExtra("classId");
        int DAY_IN_MONTH = getDayInMonth(month);

        int rowSize = idArray.length+1;

        //row setup..
        TableRow[] rows = new TableRow[rowSize];
        TextView[] roll_tvs = new TextView[rowSize];
        TextView[] name_tvs = new TextView[rowSize];
        TextView[][] status_tvs = new TextView[rowSize][DAY_IN_MONTH+1];

        for(int i = 0; i < rowSize; i++)
        {
            roll_tvs[i] = new TextView(this);
            name_tvs[i] = new TextView(this);
            for(int j = 1; j <= DAY_IN_MONTH; j++)
            {
                status_tvs[i][j] = new TextView(this);
            }
        }
        //header...
        roll_tvs[0].setText("Roll");
        roll_tvs[0].setTypeface(roll_tvs[0].getTypeface(), Typeface.BOLD);
        name_tvs[0].setText("Name");
        name_tvs[0].setTypeface(name_tvs[0].getTypeface(), Typeface.BOLD);

        for(int i = 1; i <= DAY_IN_MONTH; i++)
        {
            status_tvs[0][i].setText(String.valueOf(i));
            status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface(), Typeface.BOLD);
        }
        for(int i = 1; i < rowSize; i++)
        {
            roll_tvs[i].setText(String.valueOf(rollNoArray[i-1]));
            name_tvs[i].setText(studentNameArray[i-1]);

            for(int j = 1; j <= DAY_IN_MONTH; j++)
            {
                String day = String.valueOf(j);
                if(day.length()==1)day = "0"+day;
                int finalI = i;
                String finalDay = day;
                int finalJ = j;
                reference.child("statustable").orderByChild("studentId").startAt(idArray[i - 1]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                String date = finalDay +"-"+month;
                                if(dataSnapshot.getKey().matches(idArray[finalI - 1]+date)){
                                    String status = dataSnapshot.child("status").getValue().toString();
                                    status_tvs[finalI][finalJ].setText(status);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        for(int i = 0; i < rowSize; i++)
        {
            rows[i] = new TableRow(this);

            if(i%2==0)
                rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
            else
                rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));
            roll_tvs[i].setPadding(16,16,16,16);
            name_tvs[i].setPadding(16,16,16,16);

            rows[i].addView(roll_tvs[i]);
            rows[i].addView(name_tvs[i]);

            for(int j = 1; j <= DAY_IN_MONTH; j++){
            status_tvs[i][j].setPadding(16,16,16,16);
                rows[i].addView(status_tvs[i][j]);
            }

            tableLayout.addView(rows[i]);
        }
        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

    }

    private int getDayInMonth(String month) {
        int monthIndex = Integer.parseInt(month.substring(0, 2)) - 1;
        int year = Integer.parseInt(month.substring(3));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthIndex);
        calendar.set(Calendar.YEAR, year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}