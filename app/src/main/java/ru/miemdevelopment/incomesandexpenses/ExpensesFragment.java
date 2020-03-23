package ru.miemdevelopment.incomesandexpenses;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class ExpensesFragment extends Fragment {
    private static final String TAG = "ExpensesFragment";

    PieChart pieChart;

    ListView eventList;
    TextView header;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Cursor dbCursor;
    Button addButtonExpenses;
    String[]  dbGraphArrayX, dbGraphArrayY, dbGraphArrayDate;
    SimpleCursorAdapter dbCursorAdapter;

    String graphArrayForCategories[];
    float graphArrayForExpenses[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_expenses, container, false);

        header = (TextView) view.findViewById(R.id.headerExpenses);

        eventList = (ListView) view.findViewById(R.id.listExpenses);
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity().getApplicationContext(), AddEventActivity.class);
                intent.putExtra("fragment_name", "expenses");
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
        dbHelper = new DatabaseHelper(getActivity().getApplicationContext());

        Log.d(TAG, "onCreate: starting to create chart");

        pieChart = (PieChart) view.findViewById(R.id.chartExpenses);

        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(13);

        // открываем подключение
        db = dbHelper.getReadableDatabase();

        //получаем данные из бд
        dbCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE_EXPENSES, null);
        String[] headers = new String[]{DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_EXPENSE};

        dbCursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.two_line_list_item,
                dbCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        header.setText("Elements found: " + String.valueOf(dbCursor.getCount()));
        eventList.setAdapter(dbCursorAdapter);

        //массив для графика
        dbGraphArrayY = dbHelper.getTableRaw(db, "expenses", "expense");
        dbGraphArrayX = dbHelper.getTableRaw(db, "expenses", "category");
        //dbGraphArrayDate = dbHelper.getTableRaw(db, "expenses", "date");

        final String categories[] = getResources().getStringArray(R.array.categoriesExpenses);

        graphArrayForCategories = new String[categories.length];
        graphArrayForExpenses = new float[categories.length];

        for(int i = 0; i < categories.length; i++) {
            graphArrayForCategories[i] = categories[i];
            graphArrayForExpenses[i] = 0;
        }

        for(int i = 0; i < graphArrayForCategories.length; i++) {
            for (int j = 0; j < dbGraphArrayX.length; j++) {
                if (graphArrayForCategories[i].equals(categories[Integer.parseInt(dbGraphArrayX[j])])) {
                    graphArrayForExpenses[i] += Float.parseFloat(dbGraphArrayY[j]);
                }
            }
        }

        for(int i = 0; i < graphArrayForCategories.length; i++)
            Log.d(TAG, graphArrayForCategories[i]);

        for(int i = 0; i < graphArrayForExpenses.length; i++)
            Log.d(TAG, String.valueOf(graphArrayForExpenses[i]));

        Log.d(TAG, "graphCursors are created");

        addDataSet();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: Value select from chart.");
                Log.d(TAG, "onValueSelected: " + e.toString());
                Log.d(TAG, "onValueSelected: " + h.toString());

                String position = e.toString().substring(e.toString().indexOf("y: ") + 3);
                Log.d(TAG, "position: " + position);

                int arrayIndex = 0;

                for (int i = 0; i < graphArrayForExpenses.length; i++) {
                    if (graphArrayForExpenses[i] == Float.parseFloat(position)) {
                        arrayIndex = i;
                        break;
                    }
                }

                Toast.makeText(getActivity(), "Category: " + graphArrayForCategories[arrayIndex]
                        + "\n" + "Expense: RUB" + graphArrayForExpenses[arrayIndex], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        addButtonExpenses = (Button) view.findViewById(R.id.addButtonExpenses);

        addButtonExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddEventActivity.class); //было this
                intent.putExtra("fragment_name", "expenses");
                startActivity(intent);
            }
        });

        return view;
    }

    private void addDataSet() {
        Log.d(TAG, "addDataSet started");

        ArrayList<PieEntry> yEntries = new ArrayList<>();
        ArrayList<String> xEntries = new ArrayList<>();

        for (int i = 0; i < graphArrayForExpenses.length; i++) {
            if (graphArrayForExpenses[i] != 0) {
                yEntries.add(new PieEntry(graphArrayForExpenses[i]));
                xEntries.add(graphArrayForCategories[i]);
            }
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntries, "ExpenseCategory");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to data set
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        Log.d(TAG, "onCreate: all shit with chart is done");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключения
        db.close();
        dbCursor.close();
    }

}