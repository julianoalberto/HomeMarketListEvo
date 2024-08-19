package com.jalberto.homemarketlist.model;

/**
 * Created by jalberto on 6/24/16.
 */
public class Item implements Comparable<Item>
{
    private String name;
    private Category category;
    private boolean isOutOf;


    public Item(String name, Category category)
    {
        this.name = name;
        this.category = category;
    }

    public Category getCategory()
    {
        return category;
    }

    public void setCategory(final Category category)
    {
        this.category = category;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setIsOutOf(boolean isOutOf)
    {
        this.isOutOf = isOutOf;
    }

    public boolean isOutOf()
    {
        return isOutOf;
    }

    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append("[Item: [name = ");
        s.append(getName());
        s.append("] [isOutOf = ");
        s.append(isOutOf());
        s.append("] [");
        s.append(getCategory());
        s.append("]]");
        return s.toString();
    }

    @Override
    public int compareTo(Item other)
    {
        return this.getName().compareTo(other.getName());
    }
}
