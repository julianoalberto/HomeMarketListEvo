package com.jalberto.homemarketlist.dao.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.jalberto.homemarketlist.dao.CategoryDAO;
import com.jalberto.homemarketlist.model.Category;
import com.jalberto.homemarketlist.model.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jalberto on 7/28/16.
 */
public class CategorySharedPreferencesDAO extends AbstractSharedPreferencesDAO implements CategoryDAO
{
    public static final String CATEGORY_LIST_PREFIX = "category";

    public static final String PREF_POPULATE = "preferences.populate";

    private List<Category> categoryList;

    public CategorySharedPreferencesDAO(SharedPreferences sharedPreferences)
    {
        this.sharedPreferences = sharedPreferences;

        boolean populate = this.sharedPreferences.getBoolean(PREF_POPULATE, true);

        if(populate) {
            populate();
        }

        this.sharedPreferences.edit().putBoolean(PREF_POPULATE, false).commit();
    }

    @Override
    public List<Category> getAll()
    {
        return categoryList;
    }

    @Override
    public Category getByName(String name)
    {
        for (Category cat: categoryList)
        {
            if (cat.getName().equalsIgnoreCase(name))
            {
                return cat;
            }
        }
        return null;
    }

    public Category getById(String id)
    {
        for (Category cat: categoryList)
        {
            if (id.equalsIgnoreCase(generateId(cat.getName())))
            {
                return cat;
            }
        }
        return null;
    }

    @Override
    public boolean add(Category category)
    {
        return false;
    }

    @Override
    public boolean delete(Category category)
    {
        return false;
    }

    @Override
    public Category set(Category oldCategory, Category newCategory)
    {
        return null;
    }

    public void load()
    {
        Set<String> categorySet = sharedPreferences.getStringSet(CATEGORY_LIST_PREFIX, new HashSet<String>());

        this.categoryList = new ArrayList<Category>();
        for (String record : categorySet)
        {
            this.categoryList.add(recordToCategory(record));
        }

        Collections.sort(categoryList);
    }

    private void populate()
    {
        categoryList = new ArrayList<Category>();

       categoryList.add(new Category("Bebidas"));
        categoryList.add(new Category("Carnes"));
        categoryList.add(new Category("Comidas"));
        categoryList.add(new Category("Doces"));
        categoryList.add(new Category("Frios"));
        categoryList.add(new Category("Higiene"));
        categoryList.add(new Category("Hortifruti"));
        categoryList.add(new Category("Limpeza"));
        categoryList.add(new Category("Pães"));
        categoryList.add(new Category("Peixes"));
        categoryList.add(new Category("Outros"));

        HashSet<String> categorySet = new HashSet<String>();
        for (Category category : categoryList)
        {
            categorySet.add(categoryToRecord(category));
        }

        sharedPreferences.edit().putStringSet(CATEGORY_LIST_PREFIX, categorySet).commit();
    }

    @Override
    public void save() throws Exception
    {

    }

    @Override
    public void synchronize() throws Exception
    {

    }

    private String categoryToRecord(Category category)
    {
        StringBuilder recordString = new StringBuilder();
        recordString.append(category.getName());

        return recordString.toString();
    }

    private Category recordToCategory(String record)
    {
        String name = record;
        Category category = new Category(name);

        return category;
    }
}
