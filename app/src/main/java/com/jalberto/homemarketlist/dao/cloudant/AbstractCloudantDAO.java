package com.jalberto.homemarketlist.dao.cloudant;

import android.content.SharedPreferences;

import com.cloudant.sync.documentstore.DocumentStore;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;
import com.jalberto.homemarketlist.dao.BaseDAO;

import java.net.URI;
import java.util.concurrent.CountDownLatch;


/**
 * Created by jalberto on 7/27/16.
 */
public abstract class AbstractCloudantDAO implements BaseDAO
{
    public static final String ID_SEPARATOR = "_";
    public static final String KEY_TYPE = "type";


    protected DocumentStore documentStore;

    protected String generateId(String name)
    {
        if (name != null)
        {
            return name.toLowerCase().replace(" ", ID_SEPARATOR);
        }
        return "";
    }

    public void synchronize() throws Exception
    {
        push();
        pull();
    }

    private void push() throws Exception
    {
        URI uri = new URI("https://almosichertionnotherable:a905cfba5bd8e167d727df7f570fd347a086e38e@23c99e3f-de9d-47a2-ad36-718bbba8696c-bluemix.cloudant.com/homemarketlist");
        // Create a replicator that replicates changes from the local
        // DocumentStore to the remote database.
        Replicator replicator = ReplicatorBuilder.push().to(uri).from(documentStore).build();

        // Use a CountDownLatch to provide a lightweight way to wait for completion
        CountDownLatch latch = new CountDownLatch(1);
        Listener listener = new Listener(latch);
        replicator.getEventBus().register(listener);
        replicator.start();
        latch.await();
        replicator.getEventBus().unregister(listener);
        if (replicator.getState() != Replicator.State.COMPLETE) {
            System.err.println("Error replicating TO remote");
            System.err.println(listener.errors);
        } else {
            System.out.println(String.format("Replicated %d documents in %d batches",
                    listener.documentsReplicated, listener.batchesReplicated));
        }
    }

    private void pull() throws Exception
    {
        URI uri = new URI("https://almosichertionnotherable:a905cfba5bd8e167d727df7f570fd347a086e38e@23c99e3f-de9d-47a2-ad36-718bbba8696c-bluemix.cloudant.com/homemarketlist");
        // Create a replicator that replicates changes from the local
        // DocumentStore to the remote database.
        Replicator replicator = ReplicatorBuilder.pull().from(uri).to(documentStore).build();

        // Use a CountDownLatch to provide a lightweight way to wait for completion
        CountDownLatch latch = new CountDownLatch(1);
        Listener listener = new Listener(latch);
        replicator.getEventBus().register(listener);
        replicator.start();
        latch.await();
        replicator.getEventBus().unregister(listener);
        if (replicator.getState() != Replicator.State.COMPLETE) {
            System.err.println("Error replicating FROM remote");
            System.err.println(listener.errors);
        } else {
            System.out.println(String.format("Replicated %d documents in %d batches",
                    listener.documentsReplicated, listener.batchesReplicated));
        }
    }
}
