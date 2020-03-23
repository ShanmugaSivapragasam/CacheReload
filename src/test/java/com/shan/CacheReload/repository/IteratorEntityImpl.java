package com.shan.CacheReload.repository;

import com.google.cloud.datastore.*;
import com.google.datastore.v1.QueryResultBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;


public class IteratorEntityImpl<T> implements Iterator<T>, QueryResults<T> {

    List<Entity> entityList = new ArrayList<>();
    Iterator<Entity> entityIterator;

    public IteratorEntityImpl(Long numEnties){

        for (Long i = Long.valueOf(0); i< numEnties; i++){
            FullEntity<IncompleteKey> TEST_FULL_ENTITY = FullEntity.newBuilder().build();
            KeyFactory keyFactory  = new KeyFactory("testproject" , "").setKind("test");
            Key taskKey = keyFactory.newKey("some-arbitrary-key"+i);
            Entity testEntity =  Entity.newBuilder(taskKey, TEST_FULL_ENTITY).build();
            entityList.add(testEntity);
        }
        entityIterator = entityList.listIterator();

    }

    @Override
    public boolean hasNext() {
        return entityIterator.hasNext();
    }

    @Override
    public T next() {

        return (T)entityIterator.next();
    }

    @Override
    public void remove() {
        entityIterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        while (hasNext())
            action.accept(next());
    }

    @Override
    public Class<?> getResultClass() {
        return Entity.class;
    }

    @Override
    public Cursor getCursorAfter() {
        return null;
    }

    @Override
    public int getSkippedResults() {
        return 0;
    }

    @Override
    public QueryResultBatch.MoreResultsType getMoreResults() {
        return null;
    }
}
