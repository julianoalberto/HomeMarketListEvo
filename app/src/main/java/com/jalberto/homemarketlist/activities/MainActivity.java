package com.jalberto.homemarketlist.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudant.sync.documentstore.DocumentStore;
import com.cloudant.sync.documentstore.DocumentStoreNotOpenedException;
import com.jalberto.homemarketlist.R;
import com.jalberto.homemarketlist.dao.CategoryDAO;
import com.jalberto.homemarketlist.dao.ItemDAO;
import com.jalberto.homemarketlist.dao.cloudant.CategoryCloudantDAO;
import com.jalberto.homemarketlist.dao.cloudant.ItemCloudantDAO;
import com.jalberto.homemarketlist.dao.sharedpreferences.CategorySharedPreferencesDAO;
import com.jalberto.homemarketlist.dao.sharedpreferences.ItemSharedPreferencesDAO;
import com.jalberto.homemarketlist.model.Category;
import com.jalberto.homemarketlist.model.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    // start all application related preferences with PREF_ / preferences.
    public static final String PREF_AT = "preferences.at";

    // flags to indicate in which tab the user is in
    public static final int AT_HOME = 0;
    public static final int AT_MARKET = 1;

    // call / return codes for edit item activity
    public static final int TASK_CODE_EDIT_ITEM = 0;
    public static final int TASK_CODE_ADD_ITEM = 1;
    public static final int TASK_CODE_DELETE_ITEM = 2;

    private LinearLayout itemsLayout;

    private EditText filterEditText;

    private ImageButton clearFilterButton;

    private ArrayList<CheckBox> itemCheckBoxList = new ArrayList<CheckBox>();

    private int at;

    private ItemDAO itemDAO;

    private CategoryDAO categoryDAO;

    private SharedPreferences applicationPreferences;

    private SharedPreferences applicationDataSharedPreferences;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        itemsLayout = (LinearLayout) findViewById(R.id.itemsLayout);

        loadPreferences();

        // Shared preferences storage
        /*applicationDataSharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.application_data_shared_preferences_file_name), Context.MODE_PRIVATE);
        categoryDAO = new CategorySharedPreferencesDAO(applicationDataSharedPreferences);
        itemDAO = new ItemSharedPreferencesDAO(applicationDataSharedPreferences, categoryDAO); //change to factory pattern (DAOFactory.getItemDAO(Configuration.DAOSource)*/

        // Cloudant storage

        DocumentStore ds = null;
        File path = getApplicationContext().getDir("documentstores", Context.MODE_PRIVATE);
        try {
            ds = DocumentStore.getInstance(new File(path, "home_market_list_database"));
        } catch (DocumentStoreNotOpenedException e) {
            Log.d("ERROR", "Error creating document store.");
            e.printStackTrace();
        }
        categoryDAO = new CategoryCloudantDAO(ds);
        itemDAO = new ItemCloudantDAO(ds, categoryDAO);

        loadData();

        createFilterEditText();
        setActiveTab();
        displayItemsCheckBoxList();
        createClearFilterButton();
        createToast();
        setHomeButtonEvent();
    }

    private void createToast()
    {
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
    }

    private void createFilterEditText()
    {
        filterEditText = (EditText) findViewById(R.id.filterEditText);

        filterEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String filterString = filterEditText.getText().toString();
                if (filterString != null || !filterString.equals(""))
                {
                    showClearFilterButton();
                    displayItemsCheckBoxList();
                }
                else
                {
                    hideClearFilterButton();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String filterString = filterEditText.getText().toString();
                if (filterString == null || filterString.equals(""))
                {
                    hideClearFilterButton();
                }
            }
        });
    }

    private void createClearFilterButton()
    {
        clearFilterButton = (ImageButton) findViewById(R.id.clearFilterButton);
    }

    private void showClearFilterButton()
    {
        clearFilterButton.setVisibility(View.VISIBLE);
    }

    private void hideClearFilterButton()
    {
        clearFilterButton.setVisibility(View.INVISIBLE);
    }

    public void atHome(View view)
    {
        at = AT_HOME;
        displayItemsCheckBoxList();
    }

    public void atMarket(View view)
    {
        at = AT_MARKET;
        displayItemsCheckBoxList();
    }

    private void displayItemsCheckBoxList()
    {
        setActiveTab();

        if (at == AT_HOME)
        {
            displayAtHomeItemsCheckBoxList();
        } else if (at == AT_MARKET)
        {
            displayAtMarketItemsCheckBoxList();
        }
    }

    private void displayAtHomeItemsCheckBoxList()
    {
        itemsLayout.removeAllViews();
        itemCheckBoxList.clear();
        for (final Item item : itemDAO.getAll())
        {
            if (!item.isOutOf())
            {
                CheckBox itemCheckBox = new CheckBox(getApplicationContext());
                itemCheckBox.setText(item.getName());
                itemCheckBox.setTextColor(Color.BLACK);
                itemsLayout.addView(itemCheckBox);
                itemCheckBoxList.add(itemCheckBox);

                itemCheckBox.setButtonDrawable(getResources().getDrawable(R.drawable.custom_checkbox_home));

                // TODO
                itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        item.setIsOutOf(true);
                        itemDAO.add(item);
                        showToast(item.getName());
                        displayAtHomeItemsCheckBoxList();
                    }
                });

                itemCheckBox.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        editItem(item);
                        displayAtHomeItemsCheckBoxList();
                        return true;

                    }
                });
            }
        }

        filter();
    }

    private void displayAtMarketItemsCheckBoxList()
    {
        Map<Category, List<Item>> allByCategoryMap = itemDAO.getAllGroupedByCategory();
        itemsLayout.removeAllViews();
        itemCheckBoxList.clear();
        for (Category category : allByCategoryMap.keySet())
        {
            List<Item> itemsList = allByCategoryMap.get(category);
            boolean displayCategoryHeader = false;

            TextView categoryHeaderTextView = new TextView(getApplicationContext());
            categoryHeaderTextView.setText(category.getName());
            categoryHeaderTextView.setTextSize(16);
            categoryHeaderTextView.setTextColor(Color.GRAY);
            itemsLayout.addView(categoryHeaderTextView);

            for (final Item item : itemsList)
            {
                if (item.isOutOf())
                {
                    CheckBox itemCheckBox = new CheckBox(getApplicationContext());
                    itemCheckBox.setText(item.getName());
                    itemCheckBox.setTextColor(Color.BLACK);
                    itemsLayout.addView(itemCheckBox);
                    itemCheckBoxList.add(itemCheckBox);
                    itemCheckBox.setButtonDrawable(getResources().getDrawable(R.drawable.custom_checkbox_market));

                    //TODO
                    itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                    {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                        {
                            item.setIsOutOf(false);
                            itemDAO.add(item);
                            showToast(item.getName());
                            displayAtMarketItemsCheckBoxList();
                        }
                    });

                    itemCheckBox.setOnLongClickListener(new View.OnLongClickListener()
                    {
                        @Override
                        public boolean onLongClick(View v)
                        {
                            editItem(item);
                            displayAtMarketItemsCheckBoxList();
                            return true;

                        }
                    });

                    displayCategoryHeader = true;
                }
            }

            if (!displayCategoryHeader)
            {
                itemsLayout.removeView(categoryHeaderTextView);
            }
        }

        filter();
    }

    private void setActiveTab()
    {
        ImageButton homeButton = (ImageButton) findViewById(R.id.buttonHome);
        ImageButton marketButton = (ImageButton) findViewById(R.id.buttonMarket);
        if (at == AT_HOME)
        {
            homeButton.setBackgroundColor(Color.WHITE);
            marketButton.setBackgroundColor(Color.LTGRAY);
        }
        if (at == AT_MARKET)
        {
            marketButton.setBackgroundColor(Color.WHITE);
            homeButton.setBackgroundColor(Color.LTGRAY);
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        saveData();
        savePreferences();
    }

    public void clearFilter(View v)
    {
        clearFilterText();
    }

    private void clearFilterText()
    {
        filterEditText.setText("");
        hideClearFilterButton();
    }

    private void filter() // TODO improve filter mechanism to filter on both tabs
    {
        String filterString = filterEditText.getText().toString();

        if (filterString != null || !filterString.equals(""))
        {
            for (CheckBox itemCheckBox : itemCheckBoxList)
            {
                String text = itemCheckBox.getText().toString();

                if (!text.toUpperCase().contains(filterString.toUpperCase()))
                {
                    itemsLayout.removeView(itemCheckBox);
                }
            }
        } else
        {
            displayItemsCheckBoxList();
        }
    }

    /**
     * Calls EditItemActivity activity
     * @param v
     */
    public void addItem(View v)
    {
        Intent addItem = new Intent(this, EditItemActivity.class);
        addItem.putExtra(getResources().getString(R.string.intent_extra_item_name), filterEditText.getText().toString());

        ArrayList<String> categoryList = new ArrayList<String>();
        for (Category category : categoryDAO.getAll()) //TODO: find a more elegant solution
        {
            categoryList.add(category.getName());
        }
        addItem.putStringArrayListExtra(getResources().getString(R.string.intent_extra_category_list), categoryList);

        addItem.putExtra(getResources().getString(R.string.intent_extra_action_id), TASK_CODE_ADD_ITEM);
        startActivityForResult(addItem, TASK_CODE_ADD_ITEM);

        this.overridePendingTransition(0, 0); // prevents transition animation when returning opening activity
    }

    /**
     * Calls EditItemActivity activity
     * @param item
     */
    protected void editItem(Item item)
    {
        Intent editItem = new Intent(this, EditItemActivity.class);
        editItem.putExtra(getResources().getString(R.string.intent_extra_item_name), item.getName());
        editItem.putExtra(getResources().getString(R.string.intent_extra_category_name), item.getCategory().getName());
        ArrayList<String> categoryList = new ArrayList<String>();
        for (Category category : categoryDAO.getAll()) { //TODO: find more elegant solution
            categoryList.add(category.getName());
        }
        editItem.putStringArrayListExtra(getResources().getString(R.string.intent_extra_category_list), categoryList);

        editItem.putExtra(getResources().getString(R.string.intent_extra_action_id), TASK_CODE_EDIT_ITEM);
        startActivityForResult(editItem, TASK_CODE_EDIT_ITEM);

        this.overridePendingTransition(0, 0); // prevents transition animation when returning opening activity
    }


    /**
     * Handles the return of the call to EditItemActivity activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            switch (data.getIntExtra(getResources().getString(R.string.intent_extra_action_id), -1))
            {
                case -1:
                {
                    break;
                }
                case TASK_CODE_ADD_ITEM:
                {
                    String categoryName = data.getStringExtra(getResources().getString(R.string.intent_extra_category_name));
                    String itemName = data.getStringExtra(getResources().getString(R.string.intent_extra_item_name));

                    Category category = categoryDAO.getByName(categoryName);
                    Item item = new Item(itemName, category);
                    if (at == AT_MARKET)
                    {
                        item.setIsOutOf(true);
                    }

                    itemDAO.add(item);

                    displayItemsCheckBoxList();

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_added_item), Toast.LENGTH_SHORT).show();

                    break;
                }
                case TASK_CODE_EDIT_ITEM:
                {
                    String categoryName = data.getStringExtra(getResources().getString(R.string.intent_extra_category_name));
                    String newItemName = data.getStringExtra(getResources().getString(R.string.intent_extra_item_name));
                    String oldItemName = data.getStringExtra(getResources().getString(R.string.intent_extra_old_item_name));

                    Category category = categoryDAO.getByName(categoryName);
                    Item item = new Item(newItemName, category);
                    if (at == AT_MARKET)
                    {
                        item.setIsOutOf(true);
                    }

                    Item oldItem = new Item(oldItemName, item.getCategory());
                    itemDAO.set(oldItem, item);
                    //itemDAO.add(item);

                    displayItemsCheckBoxList();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_edited_item), Toast.LENGTH_SHORT).show();
                    break;
                }
                case TASK_CODE_DELETE_ITEM:
                {
                    String itemName = data.getStringExtra(getResources().getString(R.string.intent_extra_item_name));
                    itemDAO.delete(itemDAO.getByName(itemName));

                    displayItemsCheckBoxList();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_deleted_item), Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            filterEditText.selectAll();
        }
    }

    private void loadData()
    {
        try
        {
            categoryDAO.load();
            itemDAO.load();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void saveData()
    {
        try
        {
            itemDAO.save();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void loadPreferences()
    {
        applicationPreferences = getPreferences(Context.MODE_PRIVATE);

        at = applicationPreferences.getInt(PREF_AT, AT_HOME);
    }

    private void savePreferences()
    {
        applicationPreferences.edit().putInt(PREF_AT, at).commit();
    }

    private void showToast(String message)
    {
        toast.setText(message);
        toast.show();
    }

    private void setHomeButtonEvent() {
        ImageButton homeButton = (ImageButton) findViewById(R.id.buttonHome);
        homeButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                toast.setText("Synchronyzing...");
                toast.show();
                try {
                    itemDAO.synchronize();
                    displayItemsCheckBoxList();
                } catch (Exception e) {
                    e.printStackTrace();
                    toast.setText("Error synchronizing: " + e.getMessage());
                    toast.show();
                }
                toast.setText("Synchronyzed.");
                toast.show();
                return true;
            }
        });
    }
}