package com.jalberto.homemarketlist.dao;

import com.jalberto.homemarketlist.model.Category;
import com.jalberto.homemarketlist.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jalberto on 7/27/16.
 */
public interface ItemDAO extends BaseDAO
{
    public List<Item> getAll();

    public Item getByName(String name);

    boolean add(Item item);

    boolean delete(Item item);

    Item set(Item oldItem, Item newItem);

    public List<Item> getAllByCategoryName(String categoryName);

    public Map<Category, List<Item>> getAllGroupedByCategory();
}
