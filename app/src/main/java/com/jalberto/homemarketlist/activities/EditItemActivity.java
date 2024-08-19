//
package com.jalberto.homemarketlist.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.jalberto.homemarketlist.R;
import java.util.ArrayList;

public class EditItemActivity extends AppCompatActivity
{
    String itemName;
    EditText itemNameEditText;
    ImageButton deleteButton;
    RadioGroup categoryListRadioGroup;
    int actionId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit_item);

        itemNameEditText = (EditText) findViewById(R.id.itemNameEditText);
        categoryListRadioGroup = (RadioGroup) findViewById(R.id.addItemCategoryRadioButtonGroup);
        deleteButton = (ImageButton) findViewById(R.id.addItemDeleteButton);

        actionId = getIntent().getIntExtra(getResources().getString(R.string.intent_extra_action_id), MainActivity.TASK_CODE_ADD_ITEM);

        setItemName(actionId);

        loadCategoryList();
    }

    private void setItemName(int actionId)
    {
        itemName = getIntent().getStringExtra(getResources().getString(R.string.intent_extra_item_name));



        if (actionId == MainActivity.TASK_CODE_ADD_ITEM)
        {
            if (itemName != null && itemName.length() > 0)
            {
                itemName = itemName.substring(0,1).toUpperCase().concat(itemName.substring(1)); // changes first letter to uppercase, e.g: orange -> Orange
                itemNameEditText.setText("");
            }
            deleteButton.setVisibility(View.INVISIBLE);
        }
        else if (actionId == MainActivity.TASK_CODE_EDIT_ITEM)
        {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.requestFocus();
        }

        itemNameEditText.append(itemName);
    }

    public void addItemCancel(View view)
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
        this.overridePendingTransition(0, 0);
    }

    public void addItemOK(View view)
    {
        Intent returnIntent = new Intent();
        String newItemName = itemNameEditText.getText().toString();
        int selectedRadioId = categoryListRadioGroup.getCheckedRadioButtonId();

        if (newItemName == null || newItemName.length() == 0)
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_type_name), Toast.LENGTH_SHORT).show();
            itemNameEditText.requestFocus();
        }
        else
        {
            if (selectedRadioId >= 0)
            {
                RadioButton selectedRadioButton = (RadioButton) findViewById(selectedRadioId);
                returnIntent.putExtra(getResources().getString(R.string.intent_extra_item_name), newItemName);
                returnIntent.putExtra(getResources().getString(R.string.intent_extra_old_item_name), itemName);
                returnIntent.putExtra(getResources().getString(R.string.intent_extra_category_name), selectedRadioButton.getText().toString());
                returnIntent.putExtra(getResources().getString(R.string.intent_extra_action_id), actionId);

                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                this.overridePendingTransition(0, 0);
            }
            else
            {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_select_category), Toast.LENGTH_SHORT).show();
                categoryListRadioGroup.requestFocus();
            }
        }
    }

    public void addItemDelete(View view)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(getResources().getString(R.string.intent_extra_item_name), itemName);
        returnIntent.putExtra(getResources().getString(R.string.intent_extra_action_id), MainActivity.TASK_CODE_DELETE_ITEM);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        this.overridePendingTransition(0, 0);
    }

    private void loadCategoryList()
    {
        ArrayList<String> categoryList = getIntent().getStringArrayListExtra(getResources().getString(R.string.intent_extra_category_list));
        String categoryName = getIntent().getStringExtra(getResources().getString(R.string.intent_extra_category_name));
        for (String category : categoryList)
        {
            RadioButton categoryRadioButton = new RadioButton(getApplicationContext());
            categoryRadioButton.setText(category);
            categoryRadioButton.setTextColor(Color.BLACK);
            categoryListRadioGroup.addView(categoryRadioButton);
            if (categoryName != null && categoryName.length() > 0 && categoryName.equalsIgnoreCase(category))
            {
                categoryListRadioGroup.check(categoryRadioButton.getId());
            }
        }
    }
}
