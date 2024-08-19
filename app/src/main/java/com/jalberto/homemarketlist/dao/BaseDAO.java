package com.jalberto.homemarketlist.dao;

/**
 * Created by jalberto on 8/1/16.
 */
public interface BaseDAO
{
    public void load() throws Exception;

    public void save() throws Exception;

    public void synchronize () throws Exception;
}
