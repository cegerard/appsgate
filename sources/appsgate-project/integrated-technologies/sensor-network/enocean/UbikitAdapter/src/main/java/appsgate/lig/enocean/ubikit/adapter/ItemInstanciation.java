package appsgate.lig.enocean.ubikit.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubikit.PhysicalEnvironmentItem;

import java.util.Collection;

/**
 * Inner class for Ubikit items instanciation thread
 *
 * @author Cédric Gérard
 * @since June 25, 2013
 * @version 1.0.0
 */
public class ItemInstanciation implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ItemInstanciation.class);


    private UbikitAdapter adapter;

    Collection<PhysicalEnvironmentItem> itemList;

    public ItemInstanciation(UbikitAdapter adapter,Collection<PhysicalEnvironmentItem> itemList) {
        super();
        this.adapter = adapter;
        this.itemList = itemList;
    }

    public void run() {
        for (PhysicalEnvironmentItem item : itemList) {
            logger.debug("creating instance of : "+item.getUID());
            adapter.instanciateItem(item);
        }
    }

}