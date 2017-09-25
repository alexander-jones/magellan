package com.magellan.magellan;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collection;

public abstract class SharedPreferencesList<E> extends ArrayList<E>
{
    protected SharedPreferences mSharedPreferences = null;
    protected String mPrefix = null;
    private int mGeneration = 0;

    protected SharedPreferencesList()
    {
    }

    protected SharedPreferencesList(SharedPreferences prefs, String classPrefix, int i)
    {
        mSharedPreferences = prefs;
        mPrefix = classPrefix + Integer.toString(i);
    }

    public int getGeneration()
    {
        return mGeneration;
    }

    public int getNewestGeneration()
    {
        if (mSharedPreferences == null)
            return -1;

        return mSharedPreferences.getInt(mPrefix + "GENERATION", 0);
    }

    public boolean save()
    {
        if (mSharedPreferences == null)
            return false;

        int generation = getNewestGeneration();
        if (generation != mGeneration)
        {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            save(editor);
            editor.commit();
        }
        return true;
    }

    public boolean load()
    {
        if (mSharedPreferences == null)
            return false;

        super.clear();
        mGeneration = mSharedPreferences.getInt(mPrefix + "GENERATION", 0);
        int itemSize = mSharedPreferences.getInt(mPrefix + "COUNT", 0);
        for (int i = 0; i < itemSize; ++i)
            super.add(loadItem(i));
        return true;
    }

    public boolean saveItem(int index)
    {
        if (mSharedPreferences == null)
            return false;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        saveItem(editor, index);
        editor.commit();
        return true;
    }

    @Override
    public boolean add(E e)
    {
        boolean ret = super.add(e);
        if (ret)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    public void add(int i, E e)
    {
        super.add(i, e);
        mGeneration++;
        save();
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> e)
    {
        boolean ret = super.addAll(i, e);
        if (ret)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends E> e)
    {
        boolean ret = super.addAll(e);
        if (ret)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    public void clear()
    {
        super.clear();
        mGeneration++;
        save();
    }

    @Override
    public boolean remove(Object e)
    {
        boolean ret = super.remove(e);
        if (ret)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    public E remove(int i)
    {
        E ret = super.remove(i);
        if (ret != null)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean ret = super.removeAll(c);
        if (ret)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
        super.removeRange(fromIndex, toIndex);
        mGeneration++;
        save();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean ret = super.retainAll(c);
        if (ret)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    @Override
    public E set(int index, E element)
    {
        E ret = super.set(index, element);
        if (ret != null)
        {
            mGeneration++;
            save();
        }
        return ret;
    }

    protected void attach(SharedPreferences prefs, String classPrefix, int i)
    {
        mGeneration++;
        mPrefix = classPrefix + Integer.toString(i);
        mSharedPreferences = prefs;
    }

    protected void save(SharedPreferences.Editor editor)
    {
        editor.putInt(mPrefix + "GENERATION", mGeneration);
        editor.putInt(mPrefix + "COUNT", size());
        for (int i = 0; i < size(); ++i)
            saveItem(editor, i);
    }

    protected abstract void saveItem(SharedPreferences.Editor editor, int position);
    protected abstract E loadItem(int position);
}
