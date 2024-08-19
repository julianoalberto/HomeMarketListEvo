package com.jalberto.homemarketlist.dao.sharedpreferences;

import android.content.SharedPreferences;
import android.os.Debug;

import com.jalberto.homemarketlist.dao.CategoryDAO;
import com.jalberto.homemarketlist.dao.ItemDAO;
import com.jalberto.homemarketlist.model.Category;
import com.jalberto.homemarketlist.model.Item;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by jalberto on 7/28/16.
 */
public class ItemSharedPreferencesDAO extends AbstractSharedPreferencesDAO implements ItemDAO
{
    public static final String ITEM_PREFIX = "item.";

    private HashMap<String, Item> itemsMap;

    private CategoryDAO categoryDAO;

    public ItemSharedPreferencesDAO(SharedPreferences sharedPreferences, CategoryDAO categoryDAO)
    {
        this.sharedPreferences = sharedPreferences;
        this.categoryDAO = categoryDAO;
    }

    @Override
    public List<Item> getAll()
    {
        List<Item> itemsList = new ArrayList<Item>();

        Set<String> keys = itemsMap.keySet();

        for (String key : keys)
        {
            itemsList.add(itemsMap.get(key));
        }

        Collections.sort(itemsList);

        return itemsList;
    }

    public Map<Category, List<Item>> getAllGroupedByCategory()
    {
        HashMap<Category, List<Item>> allByCategory = new HashMap<Category, List<Item>>();

        for (Item item : getAll())
        {
            if (allByCategory.containsKey(item.getCategory()))
            {
                List<Item> items = allByCategory.get(item.getCategory());
                items.add(item);
            }
            else
            {
                List<Item> items = new ArrayList<Item>();
                items.add(item);
                allByCategory.put(item.getCategory(), items);
            }
        }
        return allByCategory;
    }

    @Override
    public Item getByName(String name)
    {
        return itemsMap.get(name);
    }

    @Override
    public boolean add(Item item)
    {
        boolean added = false;
        String key = ITEM_PREFIX + generateId(item.getName());
        itemsMap.put(key, item);
        sharedPreferences.edit().putString(ITEM_PREFIX + generateId(item.getName()), itemToRecord(item)).apply();
        return added;
    }

    @Override
    public boolean delete(Item item)
    {
        String key = ITEM_PREFIX + generateId(item.getName());
        itemsMap.remove(key);
        sharedPreferences.edit().remove(key).apply();
        return true;
    }

    @Override
    public Item set(Item oldItem, Item newItem)
    {
        return null;
    }

    @Override
    public List<Item> getAllByCategoryName(String categoryName)
    {
        return null;
    }

    public void load()
    {
        try
        {
            categoryDAO.load();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        itemsMap = new HashMap<String, Item>();

        Map<String, ?> itemRecordsMap = sharedPreferences.getAll();

        for (Map.Entry<String, ?> recordEntry : itemRecordsMap.entrySet())
        {
            String key = recordEntry.getKey();

            if (key.startsWith(ITEM_PREFIX))
            {
                String recordString = recordEntry.getValue().toString();

                itemsMap.put(key, recordToItem(recordString));
            }
        }

    }

    @Override
    public void save() throws Exception
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : itemsMap.keySet()) {
            Item item = itemsMap.get(key);
            editor.putString(ITEM_PREFIX + generateId(item.getName()), itemToRecord(item));
        }

        editor.commit();
    }

    @Override
    public void synchronize() throws Exception
    {

    }

    private void populate()
    {

        ArrayList<Item> items = new ArrayList<Item>();

        Category hortiFruti = new Category("Hortifruti");
        Category frios = new Category("Frios");
        Category carnes = new Category("Carnes");

        Item item;

        item = new Item("Banana", hortiFruti);
        items.add(item);

        item = new Item("Beterraba", hortiFruti);
        items.add(item);

        item = new Item("Laranja", hortiFruti);
        items.add(item);

        item = new Item("Maca", hortiFruti);
        items.add(item);


        item = new Item("Queijo", frios);
        items.add(item);

        item = new Item("Presunto", frios);
        items.add(item);

        item = new Item("Manteiga", frios);
        items.add(item);

        item = new Item("Leite", frios);
        item.setIsOutOf(true);
        items.add(item);

        item = new Item("Carne moida de segunda", carnes);
        items.add(item);

        item = new Item("Carne moida de primeira", carnes);
        items.add(item);

        item = new Item("Figado", carnes);
        item.setIsOutOf(true);
        items.add(item);

        //sharedPreferences.edit().clear().commit();
        for (Item anItem:items)
        {
            sharedPreferences.edit().putString(ITEM_PREFIX + generateId(anItem.getName()), itemToRecord(anItem)).commit();
        }
    }

    private String itemToRecord(Item item)
    {
        StringBuilder recordString = new StringBuilder();

        recordString.append(item.getName());
        recordString.append(RECORD_DELIMITER);
        recordString.append(item.isOutOf());
        recordString.append(RECORD_DELIMITER);
        recordString.append(generateId(item.getCategory().getName()));

        return recordString.toString();
    }

    // <string name="item.id">name;isOutOf;categoryId</string>
    // <string name="item.leite">Leite;true;frios</string>
    private Item recordToItem(String record)
    {
        String[] values = record.split(RECORD_DELIMITER);
        String name = values[0];
        boolean isOutOf = Boolean.valueOf(values[1]);
        Category category = categoryDAO.getById(values[2]);

        Item item = new Item(name, category);
        item.setIsOutOf(isOutOf);

        return item;
    }


}
