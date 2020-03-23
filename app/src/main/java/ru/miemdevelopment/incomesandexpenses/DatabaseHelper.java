package ru.miemdevelopment.incomesandexpenses;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "IncomesAndExpenses.db"; // название бд
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE_INCOMES = "incomes"; // название таблиц в бд
    static final String TABLE_EXPENSES = "expenses";

    // названия столбцов
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_EVENT = "event";
    public static final String COLUMN_INCOME = "income";
    public static final String COLUMN_EXPENSE = "expense";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //создание таблиц
        db.execSQL("CREATE TABLE incomes (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TITLE
                + " TEXT, " + COLUMN_EVENT + " TEXT," + COLUMN_INCOME
                + " FLOAT, " + COLUMN_CATEGORY + " INTEGER, " + COLUMN_DATE
                + " TEXT);");
        db.execSQL("CREATE TABLE expenses (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TITLE
                + " TEXT, " + COLUMN_EVENT + " TEXT," + COLUMN_EXPENSE
                + " FLOAT, " + COLUMN_CATEGORY + " INTEGER, " + COLUMN_DATE
                + " TEXT);");

        // добавление начальных данных
        db.execSQL("INSERT INTO "+ TABLE_INCOMES +" (" + COLUMN_TITLE
                + ", " + COLUMN_EVENT + ", " + COLUMN_INCOME
                + ", " + COLUMN_CATEGORY + ", " + COLUMN_DATE
                + ") VALUES ('Job', 'Dvornik', 2000.5, 2, '06.05.2017');");
        db.execSQL("INSERT INTO "+ TABLE_EXPENSES +" (" + COLUMN_TITLE
                + ", " + COLUMN_EVENT + ", " + COLUMN_EXPENSE
                + ", " + COLUMN_CATEGORY + ", " + COLUMN_DATE
                + ") VALUES ('Shop', 'Dixy', 1566.29, 1, '23.04.2017');");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }

    public String[] getTableRaw(SQLiteDatabase db, String table, String raw){
        int iterator = 0;

        Cursor cursor = db.query(table,
                new String[] {raw},
                null, null, null, null, null);

        String[] rawFinalArray = new String[cursor.getCount()];

        while (cursor.moveToNext()) {
            rawFinalArray[iterator] = cursor.getString(cursor.getColumnIndex(raw));
            iterator++;
        }
        cursor.close();
        return rawFinalArray;
    }

    public String[] getTableRawWithCondition(SQLiteDatabase db, String table, String raw,
                                             String rawForCondition, String rawForDate1,
                                             String rawForDate2, String condition){
        int iterator = 0;

        Cursor cursor = db.query(table,
                new String[] {raw, rawForCondition},
                rawForCondition + " = ?"/* + " AND date = ?" + "WHERE `date` BETWEEN '" + 2011-09-01 + "' AND '" +2011-09-30 + "'\n"*/,
                new String[] {condition},
                null, null, null);

        //db.rawQuery("");

        String[] rawFinalArray = new String[cursor.getCount()];

        while (cursor.moveToNext()) {
            rawFinalArray[iterator] = cursor.getString(cursor.getColumnIndex(raw));
            iterator++;
        }
        cursor.close();
        return rawFinalArray;
    }
}