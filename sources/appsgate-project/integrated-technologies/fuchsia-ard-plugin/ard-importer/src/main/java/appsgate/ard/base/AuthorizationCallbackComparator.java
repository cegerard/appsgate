package appsgate.ard.base;

import org.osgi.framework.ServiceReference;

import java.util.Comparator;

public class AuthorizationCallbackComparator implements Comparator {

    public int compare(Object o1, Object o2) {

        ServiceReference sr1=(ServiceReference)o1;
        ServiceReference sr2=(ServiceReference)o2;

        Integer id1=(Integer)sr1.getProperty("priority");
        Integer id2=(Integer)sr2.getProperty("priority");

        return id1-id2;
    }

    public boolean equals(Object obj) {
        return false;
    }
}