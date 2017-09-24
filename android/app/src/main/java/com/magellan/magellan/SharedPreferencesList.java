package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collection;

public abstract class SharedPreferencesList<E> extends ArrayList<E>
{
    private SharedPreferences mSharedPreferences;
    private int mGeneration = 0;

    public SharedPreferencesList(Context context, String tag)
    {
        mSharedPreferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
    }

    public int getGeneration()
    {
        return mGeneration;
    }

    public int getNewestGeneration()
    {
       return mSharedPreferences.getInt("GENERATION", 0);
    }

    public void save()
    {
        mGeneration++;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.putInt("GENERATION", mGeneration);
        editor.putInt("COUNT", size());
        for (int i = 0; i < size(); ++i)
            saveElement(editor, i);
        editor.commit();
    }

    public void load()
    {
        super.clear();
        mGeneration = mSharedPreferences.getInt("GENERATION", 0);
        int itemSize = mSharedPreferences.getInt("COUNT", 0);
        for (int i = 0; i < itemSize; ++i)
            super.add(loadElement(mSharedPreferences, i));
    }

    @Override
    public boolean add(E e)
    {
        boolean ret = super.add(e);
        save();
        return ret;
    }

    @Override
    public void add(int i, E e)
    {
        super.add(i, e);
        save();
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> e)
    {
        boolean ret = super.addAll(i, e);
        save();
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends E> e)
    {
        boolean ret = super.addAll(e);
        save();
        return ret;
    }

    @Override
    public void clear()
    {
        super.clear();
        save();
    }

    @Override
    public Object clone()
    {
        Object ret = super.clone();
        save();
        return ret;
    }

    @Override
    public boolean remove(Object e)
    {
        boolean ret = super.remove(e);
        save();
        return ret;
    }

    @Override
    public E remove(int i)
    {
        E ret = super.remove(i);
        save();
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean ret = super.removeAll(c);
        save();
        return ret;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
        super.removeRange(fromIndex, toIndex);
        save();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean ret = super.retainAll(c);
        save();
        return ret;
    }

    @Override
    public E set(int index, E element)
    {
        E ret = super.set(index, element);
        save();
        return ret;
    }

    protected abstract void saveElement(SharedPreferences.Editor editor, int position);
    protected abstract E loadElement(SharedPreferences sp, int position);
}
