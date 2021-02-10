package com.epam.deltix.gflog.api;

/**
 * Implement the interface to customize formatting logic.
 */
public interface Loggable {

    /**
     * Appends an object content to the entry.
     */
    void appendTo(final AppendableEntry entry);

}
