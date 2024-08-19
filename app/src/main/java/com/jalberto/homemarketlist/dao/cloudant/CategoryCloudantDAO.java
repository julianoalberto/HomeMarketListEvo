package com.jalberto.homemarketlist.dao.cloudant;

import com.cloudant.sync.documentstore.AttachmentException;
import com.cloudant.sync.documentstore.ConflictException;
import com.cloudant.sync.documentstore.DocumentBodyFactory;
import com.cloudant.sync.documentstore.DocumentNotFoundException;
import com.cloudant.sync.documentstore.DocumentRevision;
import com.cloudant.sync.documentstore.DocumentStore;
import com.cloudant.sync.documentstore.DocumentStoreException;
import com.cloudant.sync.query.Query;
import com.cloudant.sync.query.QueryException;
import com.cloudant.sync.query.QueryResult;
import com.jalberto.homemarketlist.dao.CategoryDAO;
import com.jalberto.homemarketlist.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jalberto on 7/28/16.
 */
public class CategoryCloudantDAO extends AbstractCloudantDAO implements CategoryDAO
{
    public static final String TYPE = "category";
    public static final String KEY_NAME = "name";

    private List<Category> categoryList;

    public CategoryCloudantDAO(DocumentStore documentStore)
    {
        this.documentStore = documentStore;

        //populate();

        /*try {
            synchronize();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
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
        this.categoryList = new ArrayList<Category>();
        Query q = documentStore.query();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(KEY_TYPE, TYPE);

        try {
            QueryResult result = q.find(query);
            for (DocumentRevision revision : result) {
                categoryList.add(revisionToCategory(revision));
            }
        } catch (QueryException e) {
            e.printStackTrace();
        }

        Collections.sort(categoryList);
    }

    @Override
    public void save() throws Exception
    {

    }

    @Override
    public void synchronize() throws Exception
    {
        super.synchronize();
    }

    private DocumentRevision categoryToRevision(Category category)
    {
        DocumentRevision revision = new DocumentRevision(category.getId());
        Map<String, Object> body = new HashMap<String, Object>();
        body.put(KEY_TYPE, TYPE);
        body.put(KEY_NAME, category.getName());
        revision.setBody(DocumentBodyFactory.create(body));
        return revision;
    }

    private Category revisionToCategory(DocumentRevision revision)
    {
        Map<String, Object> body = revision.getBody().asMap();
        Category category = new Category((String) body.get(KEY_NAME));
        return category;
    }

    private void populate()
    {
        categoryList = new ArrayList<Category>();

        categoryList.add(new Category("Bebidas"));
        categoryList.add(new Category("Cachorros"));
        categoryList.add(new Category("Carnes"));
        categoryList.add(new Category("Casa"));
        categoryList.add(new Category("Comidas"));
        categoryList.add(new Category("Doces"));
        categoryList.add(new Category("Frios"));
        categoryList.add(new Category("Higiene"));
        categoryList.add(new Category("Hortifruti"));
        categoryList.add(new Category("Limpeza"));
        categoryList.add(new Category("Outros"));

        DocumentRevision revision = null;

        for (Category category : categoryList)
        {
            revision = categoryToRevision(category);

            try {
                if (documentStore.database().contains(revision.getId()))
                {
                    documentStore.database().delete(revision);
                    //documentStore.database().update(revision);
                }
                else
                {
                    documentStore.database().create(revision);
                }
            } catch (DocumentStoreException e) {
                e.printStackTrace();
            } catch (ConflictException e) {
                e.printStackTrace();
            } catch (AttachmentException e) {
                e.printStackTrace();
            } catch (DocumentNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


}