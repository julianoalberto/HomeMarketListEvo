package com.jalberto.homemarketlist.dao;

import com.jalberto.homemarketlist.model.Category;

import java.util.List;

/**
 * Created by jalberto on 7/27/16.
 */
public interface CategoryDAO extends BaseDAO
{
    public List<Category> getAll();

    public Category getByName(String name);

    public boolean add(Category category);

    public boolean delete(Category category);

    public Category set(Category oldCategory, Category newCategory);

    public Category getById(String name);
}
