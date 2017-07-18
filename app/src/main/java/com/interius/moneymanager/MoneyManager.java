package com.interius.moneymanager;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ParseException;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MoneyManager extends AppCompatActivity {

    private static final int CM_DELETE_ID = 1;
    ListView listView;
    MyDatabase database;
    SimpleCursorAdapter cursorAdapter;
    Cursor cursor;
    TextView textTotalMoney;

    SharedPreferences sPref;
    public static final String KEY_MONEY="money";

    public int totalMoney; //общий остаток денег

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money_manager);
        database = new MyDatabase(this);
        database.open();

        cursor = database.getAllData();
        startManagingCursor(cursor);

        String[] form = new String[]{database.COLUMN_NAME, database.COLUMN_MONEY,
                database.COLUMN_DATE};
        int[] to = new int[]{R.id.item_text1, R.id.item_text2, R.id.item_text3};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item, cursor,form,to);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(cursorAdapter);

        textTotalMoney = (TextView) findViewById(R.id.totalMoney) ;
        sPref =  PreferenceManager.getDefaultSharedPreferences(this);
        sPref = getPreferences(MODE_PRIVATE);
        totalMoney= sPref.getInt(KEY_MONEY,0);
        String str = Integer.toString(totalMoney);
        textTotalMoney.setText(str);

        registerForContextMenu(listView);

    }

    //функция выполняющая действия после нажатия на кнопку, передавать true если доход и false если расход,
    public void buttonCLick(boolean add)
    {
        final LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MoneyManager.this);
        if(add)
            alertBuilder.setTitle("Введите доход");
        else
            alertBuilder.setTitle("Введите расход");
        //передаем инфлейтеру созданный xml - файл и присваем это view
        View v = inflater.inflate(R.layout.dialog_items,null);
        //добавляем готовый view в алерт диалог
        alertBuilder.setView(v);

        final EditText textName = (EditText) v.findViewById(R.id.dialogName);
        final EditText textMoney = (EditText) v.findViewById(R.id.dialogMoney);
        //получаем текущую дату и время
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        final String formatedDate = df.format(c.getTime());
        if(add) {
            alertBuilder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!textName.getText().toString().isEmpty() && !textMoney.getText().toString().isEmpty()) {
                        //добавляем новую запись в бд использую данные из алерт диалога
                        database.addRec(textName.getText().toString(), Integer.parseInt(textMoney.getText().toString()), formatedDate);
                        cursor.requery();
                        try {
                            //добовляем новый доход к общему количеству денег
                            totalMoney += Integer.parseInt(textMoney.getText().toString());
                            textTotalMoney.setText(Integer.toString(totalMoney));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        else {
            alertBuilder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!textName.getText().toString().isEmpty() && !textMoney.getText().toString().isEmpty()) {
                        database.addRec(textName.getText().toString(), -1 * Integer.parseInt(textMoney.getText().toString()), formatedDate);
                        cursor.requery();
                        try {
                            totalMoney -= Integer.parseInt(textMoney.getText().toString());
                            textTotalMoney.setText(Integer.toString(totalMoney));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }
    public void onClickAdd(View view)
    {

        buttonCLick(true);
    }

    public  void onClickDec(View view)
    {
        buttonCLick(false);
    }

    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo)
    {
        super.onCreateContextMenu(menu,view,contextMenuInfo);
        menu.add(0,CM_DELETE_ID,0,R.string.deleteRec);
    }

    public boolean onContextItemSelected(MenuItem item)
    {
        Cursor c;

        if(item.getItemId() == CM_DELETE_ID)
        {
            //удаление строки при удерживании на ней, так же высчитывается общее количтво денег
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            c=database.findRec(adapterContextMenuInfo.position);
            totalMoney -= Integer.parseInt(c.getString(c.getColumnIndex("MONEY"))); //при удалении строки высчитывается значение из общего количества денег
            textTotalMoney.setText(Integer.toString(totalMoney));
            database.delRec(adapterContextMenuInfo.id);
            cursor.requery();
            return true;
        }
        return  super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // две кноки меню сверху
        switch (item.getItemId()) {
            // очищает список и бд но отсавляет общее количество денег
            case R.id.action_clean:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("Вы действительно хотите удалить историю счета?");
                alertBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.delAll();
                        cursor.requery();
                    }
                });
                alertBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
                return true;
            // очищает список и бд а также обнуляет количество денег
            case R.id.action_delete:
                AlertDialog.Builder alertBuilderDel = new AlertDialog.Builder(this);
                alertBuilderDel.setTitle("Вы действительно хотите обнулить счет?");
                alertBuilderDel.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.delAll();
                        totalMoney = 0;
                        textTotalMoney.setText(Integer.toString(totalMoney));
                        cursor.requery();
                    }
                });
                alertBuilderDel.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alertDialogDel = alertBuilderDel.create();
                alertDialogDel.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void  onPause()
    {
        super.onPause();
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt(KEY_MONEY,totalMoney);
        editor.apply();
    }
    protected void onDestroy()
    {
        super.onDestroy();
        database.close();
    }

}
