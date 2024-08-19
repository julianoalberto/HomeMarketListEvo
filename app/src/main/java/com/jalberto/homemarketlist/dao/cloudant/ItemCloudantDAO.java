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
import com.jalberto.homemarketlist.dao.ItemDAO;
import com.jalberto.homemarketlist.model.Category;
import com.jalberto.homemarketlist.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by jalberto on 7/28/16.
 */
public class ItemCloudantDAO extends AbstractCloudantDAO implements ItemDAO
{
    public static final String ITEM_PREFIX = "item.";

    public static final String TYPE = "item";

    public static final String KEY_NAME = "name";

    public static final String KEY_CATEGORY = "category";

    public static final String KEY_IS_OUT_OF = "isOutOf";

    private HashMap<String, Item> itemsMap;

    private CategoryDAO categoryDAO;

    public ItemCloudantDAO(DocumentStore documentStore, CategoryDAO categoryDAO)
    {
        this.documentStore = documentStore;
        this.categoryDAO = categoryDAO;
        //this.populate();
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
        itemsMap.put(item.getName(), item);
        /*DocumentRevision newRevision = itemToRevision(item);
        try {
            delete(item);
            documentStore.database().create(newRevision);
        } catch (DocumentStoreException e) {
            e.printStackTrace();
        }  catch (ConflictException e) {
            e.printStackTrace();
        } catch (AttachmentException e) {
            e.printStackTrace();
        }*/

        return true;
    }

    @Override
    public boolean delete(Item item)
    {
        itemsMap.remove(item.getName());

        DocumentRevision oldRevision = null;
        try
        {
            oldRevision = documentStore.database().read(item.getName());
            documentStore.database().delete(oldRevision);
            return true;
        }
        catch (DocumentNotFoundException e)
        {
            return false;
        } catch (DocumentStoreException e) {
            e.printStackTrace();
        } catch (ConflictException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public Item set(Item oldItem, Item newItem)
    {
        if (!oldItem.getName().equals(newItem.getName()))
        {
            delete(oldItem);
        }
        add(newItem);
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

            itemsMap = new HashMap<String, Item>();
            Query q = documentStore.query();
            Map<String, Object> query = new HashMap<String, Object>();
            query.put(KEY_TYPE, TYPE);
            Item item = null;
            try {
                QueryResult result = q.find(query);
                for (DocumentRevision revision : result) {
                    item = revisionToItem(revision);
                    itemsMap.put(item.getName(), item);
                }
            } catch (QueryException e) {
                e.printStackTrace();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void save() throws Exception
    {
        for (String key : itemsMap.keySet()) {
            Item item = itemsMap.get(key);
            DocumentRevision newRevision = itemToRevision(item);
            if (documentStore.database().contains(newRevision.getId())) {
                DocumentRevision oldRevision = documentStore.database().read(item.getName());

                Map<String, Object> body = oldRevision.getBody().asMap();
                body.put(KEY_NAME, item.getName());
                body.put(KEY_IS_OUT_OF, item.isOutOf());
                body.put(KEY_CATEGORY, generateId(item.getCategory().getName()));
                oldRevision.setBody(DocumentBodyFactory.create(body));

                documentStore.database().update(oldRevision);
            } else {
                documentStore.database().create(newRevision);
            }
        }
    }

    @Override
    public void synchronize() throws Exception
    {
        super.synchronize();
    }

    private void populate() {

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
        for (Item anItem : items) {
            try {
                DocumentRevision revision = itemToRevision(anItem);
                if (documentStore.database().contains(revision.getId())) {
                    //documentStore.database().delete(revision);
                    documentStore.database().update(revision);
                } else {
                    documentStore.database().create(revision);
                }
            } catch (DocumentNotFoundException e) {
                e.printStackTrace();
            } catch (AttachmentException e) {
                e.printStackTrace();
            } catch (ConflictException e) {
                e.printStackTrace();
            } catch (DocumentStoreException e) {
                e.printStackTrace();
            }
        }
    }

    private DocumentRevision itemToRevision(Item item)
    {
        DocumentRevision revision = new DocumentRevision(item.getName());
        Map<String, Object> body = new HashMap<String, Object>();
        body.put(KEY_TYPE, TYPE);
        body.put(KEY_NAME, item.getName());
        body.put(KEY_IS_OUT_OF, item.isOutOf());
        body.put(KEY_CATEGORY, generateId(item.getCategory().getName()));
        revision.setBody(DocumentBodyFactory.create(body));
        return revision;
    }

    private Item revisionToItem(DocumentRevision revision)
    {
        Map<String, Object> body = revision.getBody().asMap();
        Item item = new Item((String) body.get(KEY_NAME),
                categoryDAO.getById((String) body.get(KEY_CATEGORY)));
        item.setIsOutOf((Boolean) body.get(KEY_IS_OUT_OF));
        return item;
    }
}
