package mich.gwan.ngucici.activities.bolt;

import static mich.gwan.ngucici.sql.DatabaseHelper.COLUMN_BOLT_CATEGORY;
import static mich.gwan.ngucici.sql.DatabaseHelper.COLUMN_BOLT_QUANTIY;
import static mich.gwan.ngucici.sql.DatabaseHelper.TABLE_BOLT;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mich.gwan.ngucici.R;
import mich.gwan.ngucici.databinding.ActivityBoltSaleBinding;
import mich.gwan.ngucici.helpers.InputValidation;
import mich.gwan.ngucici.model.allsales.AllSales;
import mich.gwan.ngucici.model.bolt.BoltSale;
import mich.gwan.ngucici.model.bolt.Bolts;
import mich.gwan.ngucici.sql.DatabaseHelper;

public class BoltActionSale extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout relativeLayout;
    private TextInputLayout layoutQnty;
    private EditText editTextCat;

    private EditText editTextQnty;
    private TextView textViewClear;
    private TextView textViewSale;
    private AppCompatSpinner spinner;
    private ActivityBoltSaleBinding binding;
    private InputValidation inputValidation;
    private DatabaseHelper databaseHelper;
    private BoltSale sale;
    private TextInputLayout layoutCat;
    private List<Bolts> list;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityBoltSaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initObjects();
        initViews();
        initListeners();

        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.sale));
    }

    private void initObjects(){
        inputValidation = new InputValidation(this);
                databaseHelper = new DatabaseHelper(this);
        list = new ArrayList<>();
        sale = new BoltSale();
        getDataFromSQLite();
        }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtBoltSale:
                performOilSale();
                break;
            case R.id.txtBoltSaleClear:
                emptyTextFields();
                break;
        }
    }

    private void initViews(){
        layoutCat = binding.layBoltSaleCategory;
        layoutQnty = binding.layBoltSaleQnty;

        editTextQnty = binding.editBoltSaleQnty;

        textViewClear = binding.txtBoltSaleClear;
        textViewSale = binding.txtBoltSale;

        relativeLayout = binding.relativeLayout;
        //spinner = binding.spinner;
        spinner = binding.spinner;

        List<String> categories = databaseHelper.getAllSaleBolt();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        binding.iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoltActionSale.this.finish();
            }
        });
    }

    private void initListeners(){
        textViewClear.setOnClickListener(this);
        textViewSale.setOnClickListener(this);
    }

    /**
     * Method to get the selling price
     * returns the selling price
     * @return
     */
    public int getSelPrice(){
        String category = spinner.getSelectedItem().toString().trim().toUpperCase(Locale.ROOT);
        SQLiteDatabase dbg = this.databaseHelper.getReadableDatabase();
        String dbSp = "SELECT bolt_sp FROM bolts WHERE bolt_category =?";
        Cursor catCu = dbg.rawQuery(dbSp,new String[]{category});
        int sellPrice = 0;
        while (catCu.moveToNext()){
            sellPrice = catCu.getInt(0);
        }
        catCu.close();
        dbg.close();
        return sellPrice;
    }
    /**
     * Method to get quantity
     * returns the available quantity
     * @return
     */
    public int getQuantity(){
        String category = spinner.getSelectedItem().toString().trim().toUpperCase(Locale.ROOT);
        SQLiteDatabase dbg = this.databaseHelper.getReadableDatabase();
        String dbSp = "SELECT bolt_quantity FROM bolts WHERE bolt_category =?";
        Cursor catCu = dbg.rawQuery(dbSp,new String[]{category});
        int quantity = 0;
        while (catCu.moveToNext()){
            quantity = catCu.getInt(0);
        }
        catCu.close();
        dbg.close();
        return quantity;
    }

    /**
     * method to get the buying price
     * it returns the buying price
     * @return
     */
    public int getBuyPrice(){
        String category = spinner.getSelectedItem().toString().trim().toUpperCase(Locale.ROOT);
        SQLiteDatabase dbg = this.databaseHelper.getReadableDatabase();
        String dbSp = "SELECT bolt_bp FROM bolts WHERE bolt_category =?";
        Cursor catCu = dbg.rawQuery(dbSp,new String[]{category});
        int buyPrice = 0;
        while (catCu.moveToNext()){
            buyPrice = catCu.getInt(0);
        }
        catCu.close();
        dbg.close();
        return buyPrice;
    }

    public void updateQuantity(int difference){
        String category = spinner.getSelectedItem().toString().trim().toUpperCase(Locale.ROOT);
        SQLiteDatabase db = this.databaseHelper.getReadableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_BOLT_QUANTIY, difference);
        db.update(TABLE_BOLT, value, COLUMN_BOLT_CATEGORY + "= ?", new String[] {category});
    }


    private void performOilSale(){
        AllSales allSales = new AllSales();
        //int difference = getBuyPrice() - Integer.parseInt(editTextQnty.getText().toString().trim());

        if (!inputValidation.isInputEditTextOccupied(editTextQnty, layoutQnty, getString(R.string.error_oilsale_qnty))) {
            return;
        }

        if (databaseHelper.checkBoltCategory(spinner.getSelectedItem().toString().trim().toUpperCase())){
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

                int unitPrice = getSelPrice();
                int saleQnty = Integer.parseInt(editTextQnty.getText().toString().trim());
                int saleTotal = saleQnty * unitPrice;
                int buyPrice = getBuyPrice();
                int profit = (unitPrice - buyPrice) * saleQnty;
                try {
                    sale.setBoltSaleDate(dateFormat.format(LocalDate.now()));
                    sale.setBoltSaleTime(timeFormat.format(java.time.LocalDateTime.now()));
                    sale.setBoltSalecategory(spinner.getSelectedItem().toString().toUpperCase(Locale.ROOT).trim());
                    sale.setBoltSaleUprice(unitPrice);
                    sale.setBoltSaleQnty(Integer.parseInt(editTextQnty.getText().toString().trim()));
                    sale.setBoltSaleTotal(saleTotal);
                    sale.setBoltSaleProfit(profit);
                    allSales.setSaleDate(sale.getBoltSaleDate());
                    allSales.setSaleTime(sale.getBoltSaleTime());
                    allSales.setSalecategory(sale.getBoltSalecategory());
                    allSales.setSaleUprice(sale.getBoltSaleUprice());
                    allSales.setSaleQnty(sale.getBoltSaleQnty());
                    allSales.setSaleTotal(sale.getBoltSaleTotal());
                    allSales.setSaleProfit(sale.getBoltSaleProfit());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                databaseHelper.addBoltSale(sale);
                databaseHelper.addAllSale(allSales);
                Snackbar snackbar = Snackbar.make(relativeLayout, getString(R.string.oilsale_transact_success), Snackbar.LENGTH_LONG);
                View view = snackbar.getView();
                view.setBackgroundColor(Color.BLUE);
                FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)view.getLayoutParams();
                params.gravity = Gravity.TOP | Gravity.CENTER;
                params.setMargins(0,0,0,0);
                view.setLayoutParams(params);
                snackbar.setTextColor(Color.WHITE);
                (snackbar.getView()).getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                (snackbar.getView()).getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                snackbar.show();
                emptyTextFields();
        }
        else {
            Snackbar snackbar = Snackbar.make(relativeLayout, getString(R.string.oilsale_transact_error), Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            view.setBackgroundColor(Color.BLUE);
            FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)view.getLayoutParams();
            params.gravity = Gravity.TOP | Gravity.CENTER;
            params.setMargins(0,0,0,0);
            view.setLayoutParams(params);
            snackbar.setTextColor(Color.WHITE);
            (snackbar.getView()).getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            (snackbar.getView()).getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            snackbar.show();

        }


    }

    @SuppressLint("StaticFieldLeak")
    private void getDataFromSQLite() {
        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(@SuppressLint("StaticFieldLeak") Void... params) {
                list.clear();
                list.addAll(databaseHelper.getAllBolts());

                return null;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
              //  oilRecyclerAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void emptyTextFields(){
        editTextQnty.setText(null);
    }
}
