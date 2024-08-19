package com.jalberto.homemarketlist.model;

/**
 * Created by jalberto on 6/24/16.
 */
public class Category implements Comparable<Category>
{
    private final String name;

    public Category(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return getName().toLowerCase().replace(" ", "_");
    }

    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append("[Category: [name = ");
        s.append(getName());
        s.append("]]");
        return s.toString();
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (!Category.class.isAssignableFrom(obj.getClass()))
        {
            return false;
        }

        final Category other = (Category) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equalsIgnoreCase(other.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(Category other)
    {
        return this.getName().compareTo(other.getName());
    }
}
