package org.makkiato.arcadeclient.data.core;

public enum CommandLanguage {
    SQL("sql"),
    SQLSCRIPT("sqlscript"),
    GRAPHQL("graphql"),
    CYPHER("cypher"),
    GREMLIN("gremlin"),
    MONGO("mongo");

    public final String key;
    CommandLanguage(String key) {
        this.key = key;
    }
}