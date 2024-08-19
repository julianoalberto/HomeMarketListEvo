package com.jalberto.homemarketlist.dao.sharedpreferences;

import android.content.SharedPreferences;

import com.jalberto.homemarketlist.dao.BaseDAO;

/**
 * Created by jalberto on 7/27/16.
 */
public abstract class AbstractSharedPreferencesDAO implements BaseDAO
{
    public static final String RECORD_DELIMITER = ";";
    public static final String ID_SEPARATOR = "_";
    protected SharedPreferences sharedPreferences;

    protected String generateId(String name)
    {
        if (name != null)
        {
            return name.toLowerCase().replace(" ", ID_SEPARATOR);
        }
        return "";
    }
}
