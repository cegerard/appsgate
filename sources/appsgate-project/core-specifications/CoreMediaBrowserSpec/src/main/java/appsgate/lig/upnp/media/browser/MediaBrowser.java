package appsgate.lig.upnp.media.browser;

public interface MediaBrowser {

    /**
     * Flag to ask only for metadata of the specified directory
     */
    public static final String BROWSE_METADATA = "BrowseMetadata";

    /**
     * flag to ask for the list of direct children and the metadata of the
     * specified directory
     */
    public static final String BROWSE_CHILDREN = "BrowseDirectChildren";

    /**
     * browse the contents of the media directory identified by objectId ("0" is
     * the root object of the media server).
     *
     * @param objectID
     * @param browseFlag
     * @param filter
     * @param startingIndex
     * @param requestedCount
     * @param sortCriteria
     * @return
     */
    public String browse(String objectID, String browseFlag, String filter,
            long startingIndex, long requestedCount, String sortCriteria);

}
