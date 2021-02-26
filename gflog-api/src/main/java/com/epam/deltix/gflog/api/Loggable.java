package com.epam.deltix.gflog.api;

/**
 * Provides a way to encapsulate formatting logic.
 */
public interface Loggable {

    /**
     * Appends the object content to the entry.
     *
     * @param entry to append to.
     */
    void appendTo(final AppendableEntry entry);

}
