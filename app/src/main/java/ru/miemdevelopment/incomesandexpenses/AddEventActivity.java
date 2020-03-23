package ru.miemdevelopment.incomesandexpenses;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private static String TAG = "AddEventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        long eventId = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventId = extras.getLong("id");
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, PlaceholderFragment.newInstance(eventId))
                    .commit();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        EditText titleBox, eventBox, incomeExpenseBox;
        Button dateBox;
        Button delButton, saveButton;

        DatabaseHelper dbHelper;
        SQLiteDatabase db;
        Cursor dbCursor;

        private DatePickerDialog datePicker;

        String incomeOrExpense;

        public static PlaceholderFragment newInstance(long id) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putLong("id", id);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);

            dbHelper = new DatabaseHelper(getActivity());

            initDateBirthdayDatePicker();

            incomeOrExpense = (String) getActivity().getIntent().getSerializableExtra("fragment_name");
            Log.d(TAG, incomeOrExpense);
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_event, container, false);
            titleBox = (EditText) rootView.findViewById(R.id.etTitle);
            eventBox = (EditText) rootView.findViewById(R.id.etEvent);
            incomeExpenseBox = (EditText) rootView.findViewById(R.id.etIncomeExpense);
            final Spinner categoryBox = (Spinner) rootView.findViewById(R.id.sCategory);
            dateBox = (Button) rootView.findViewById(R.id.etDate);

            saveButton = (Button) rootView.findViewById(R.id.saveButton);
            delButton = (Button) rootView.findViewById(R.id.deleteButton);

            final long id = getArguments() != null ? getArguments().getLong("id") : 0;

            Log.d(TAG, "onCreate: starting to create spinner");

            SpinnerHelper.flag = id > 0;

            SpinnerHelper adapter;
            if (incomeOrExpense.equals("expenses")){
                adapter = new SpinnerHelper(getActivity(),
                        android.R.layout.simple_spinner_item, getActivity().getResources().getStringArray(R.array.categoriesExpenses));
            } else {
                adapter = new SpinnerHelper(getActivity(),
                        android.R.layout.simple_spinner_item, getActivity().getResources().getStringArray(R.array.categoriesIncomes));
            }

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categoryBox.setAdapter(adapter);
            categoryBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    // Set adapter flag that something has been chosen
                    SpinnerHelper.flag = true;
                }
            });

            dateBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    datePicker.show();
                }
            });

            dateBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        datePicker.show();
                    }
                }
            });

            db = dbHelper.getWritableDatabase();

            // кнопка удаления
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (incomeOrExpense.equals("incomes"))
                        db.delete(DatabaseHelper.TABLE_INCOMES, "_id = ?", new String[]{String.valueOf(id)});
                    else db.delete(DatabaseHelper.TABLE_EXPENSES, "_id = ?", new String[]{String.valueOf(id)});
                    goHome();
                }
            });

            // кнопка сохранения
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_TITLE, titleBox.getText().toString());
                    cv.put(DatabaseHelper.COLUMN_EVENT, eventBox.getText().toString());

                    if (incomeOrExpense.equals("incomes"))
                        cv.put(DatabaseHelper.COLUMN_INCOME, Float.parseFloat(incomeExpenseBox.getText().toString()));
                    else cv.put(DatabaseHelper.COLUMN_EXPENSE, Float.parseFloat(incomeExpenseBox.getText().toString()));

                    cv.put(DatabaseHelper.COLUMN_CATEGORY, categoryBox.getSelectedItemPosition());
                    cv.put(DatabaseHelper.COLUMN_DATE, dateBox.getText().toString());

                    if (incomeOrExpense.equals("incomes")) {
                        if (id > 0) {
                            db.update(DatabaseHelper.TABLE_INCOMES, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(id), null);
                        } else {
                            db.insert(DatabaseHelper.TABLE_INCOMES, null, cv);
                        }
                    }
                    else {
                        if (id > 0) {
                            db.update(DatabaseHelper.TABLE_EXPENSES, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(id), null);
                        } else {
                            db.insert(DatabaseHelper.TABLE_EXPENSES, null, cv);
                        }
                    }
                    goHome();
                }
            });

            // если 0, то добавление
            if (id > 0) {

                // получаем элемент по id из бд
                if (incomeOrExpense.equals("incomes")) {
                    dbCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE_INCOMES + " where " +
                            DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                    dbCursor.moveToFirst();
                }
                else {
                    dbCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE_EXPENSES + " where " +
                            DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                    dbCursor.moveToFirst();
                }

                titleBox.setText(dbCursor.getString(1));
                eventBox.setText(dbCursor.getString(2));
                incomeExpenseBox.setText(dbCursor.getString(3));
                categoryBox.setSelection(dbCursor.getInt((4)));
                dateBox.setText(dbCursor.getString(5));

                dbCursor.close();
            } else {

                // скрываем кнопку удаления
                delButton.setVisibility(View.GONE);
            }

            return rootView;
        }

        public void goHome() {
            // закрываем подключение
            db.close();
            // переход к главной activity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
        }

        private void initDateBirthdayDatePicker() {
            Calendar newCalendar = Calendar.getInstance(); // объект типа Calendar мы будем использовать для получения даты
            final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); // это строка нужна для дальнейшего преобразования даты в строку

            //создаем объект типа DatePickerDialog и инициализируем его конструктор обработчиком события выбора даты и данными для даты по умолчанию
            datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                // функция onDateSet обрабатывает шаг 2: отображает выбранные нами данные в элементе EditText
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newCal = Calendar.getInstance();
                    newCal.set(year, monthOfYear, dayOfMonth);
                    dateBox.setText(dateFormat.format(newCal.getTime()));

                    Context context = getActivity().getApplicationContext();
                    InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(dateBox.getWindowToken(), 1);
                }
            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        }
    }
}