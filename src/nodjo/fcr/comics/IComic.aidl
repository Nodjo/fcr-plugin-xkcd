package nodjo.fcr.comics;



interface IComic {
	

    // Ordered list of strip identifiers
    // All identifiers must be longs with a value > 0
    // Try to make it fast, but do NOT cache for further calls.
    // This will be cached inside the main app.
    // When this method is called, it is expected to do its full
    // process every time, in case there's a new comic available
    long[] getStripIdentifiers();

	// The strip title, or the date for a daily strip, or
	// at least an ID if nothing else (this info will be displayed
	// to the user)
    String getStripTitle(long stripId);
    
    String getStripUrl(long stripId);
    
    // The online store to allow the user to buy comic-related products
    // If no online store is available, return the website url
    String getWebsiteUrl();
}