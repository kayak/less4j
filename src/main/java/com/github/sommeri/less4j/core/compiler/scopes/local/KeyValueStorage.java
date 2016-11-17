package com.github.sommeri.less4j.core.compiler.scopes.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.sommeri.less4j.utils.ArraysUtils;
import com.github.sommeri.less4j.utils.PubliclyCloneable;

public class KeyValueStorage<M, T> implements Cloneable {

  private List<Level<M, T>> levels = new ArrayList<Level<M, T>>();
  private List<ValuePlaceholder<M, T>> placeholders = new ArrayList<ValuePlaceholder<M, T>>();

  public int size() {
    return levels.size();
  }

  public void add(M key, T thing) {
    Level<M, T> lastLevel = getLastLevel();
    lastLevel.add(key, thing);
  }

  public void add(KeyValueStorage<M, T> otherStorage) {
    levels.addAll(otherStorage.levels);
    placeholders.addAll(otherStorage.placeholders);
  }

  public boolean contains(M key) {
    for (Level<M, T> level : levels) {
      if (level.contains(key))
        return true;
    }
    return false;
  }

  public T getValue(M key) {
    for (int i = levels.size() - 1; i >= 0; i--) {
        Level<M, T> level = levels.get(i);
        if (level.contains(key))
            return level.getValue(key);
    }
    return null;
  }

  public void remove(M key) {
      for (int i = levels.size() - 1; i >= 0; i--) {
          Level<M, T> level = levels.get(i);
          if (level.contains(key))
              level.remove(key);
      }
  }

  public Set<Entry<M, T>> getAllEntries() {
    Set<Entry<M, T>> result = new HashSet<Entry<M,T>>();
    for (int i = levels.size() - 1; i >= 0; i--) {
        Level<M, T> level = levels.get(i);
        result.addAll(level.getAllEntries());
    }
    return result;
  }


  public ValuePlaceholder<M, T> createPlaceholder() {
    Level<M, T> addLevel = addLevel();
    ValuePlaceholder<M, T> placeholder = new ValuePlaceholder<M, T>(addLevel);
    placeholders.add(placeholder);
    // add level that will be on top of placeholder
    addLevel();
    return placeholder;
  }

  public void addDataToFirstPlaceholder(KeyValueStorage<M, T> otherStorage) { // used to be called addToPlaceholder
    ValuePlaceholder<M, T> placeholder = placeholders.size() > 0 ? placeholders.get(0) : null;
    addDataOnly(placeholder, otherStorage);
  }

  private void addDataOnly(ValuePlaceholder<M, T> placeholder, KeyValueStorage<M, T> otherStorage) {
    for (Level<M, T> level : otherStorage.levels) {
      placeholder.level.addAll(level);
    }
  }

  public void addToFirstPlaceholder(M key, T value) {
    ValuePlaceholder<M, T> placeholder = placeholders.size() > 0 ? placeholders.get(0) : null;
    placeholder.level.add(key, value);
  }

  public void closeFirstPlaceholder() { // used to be called closePlaceholder
      if (!placeholders.isEmpty()) {
          placeholders.remove(0);
      }
  }

  //REPLACE whatever was stored in placeholder
  public void replacePlaceholder(ValuePlaceholder<M, T> placeholder, KeyValueStorage<M, T> otherStorage) {
    //replace in data
    ArraysUtils.replace(levels, placeholder.level, otherStorage.levels);
    ArraysUtils.replace(placeholders, placeholder, otherStorage.placeholders);
  }

  private Level<M, T> getLastLevel() {
    if (levels.isEmpty()) {
      addLevel();
    }

    Level<M, T> lastLevel = levels.get(levels.size() - 1);
    return lastLevel;
  }

  private Level<M, T> addLevel() {
    levels.add(new Level<M, T>());
    return levels.get(levels.size() - 1);
  }

  @Override
  public KeyValueStorage<M, T> clone() {
    try {
      @SuppressWarnings("unchecked")
      KeyValueStorage<M, T> clone = (KeyValueStorage<M, T>) super.clone();
      clone.levels = ArraysUtils.deeplyClonedList(levels);
      clone.placeholders = new LinkedList<ValuePlaceholder<M, T>>();
      for (ValuePlaceholder<M, T> placeholder : placeholders) {
        int index = levels.indexOf(placeholder.level);
        Level<M, T> levelClone = clone.levels.get(index);
        clone.placeholders.add(new ValuePlaceholder<M, T>(levelClone));
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Impossible state.");
    }
  }

  @Override
  public String toString() {
    return levels.toString();
  }
  
  private static class Level<M, T> implements PubliclyCloneable {

    private HashMap<M, T> storage;
    
    private Map<M, T> getStorage() {
        if (storage == null) {
            storage = new HashMap<M, T>();
        }
        return storage;
    }

    public void add(M key, T thing) {
      getStorage().put(key, thing);
    }

    @SuppressWarnings("unchecked")
    public Collection<Entry<M, T>> getAllEntries() {
      return storage != null ? storage.entrySet() : Collections.EMPTY_LIST;
    }

    public T getValue(M key) {
      return storage != null ? storage.get(key) : null;
    }

    public void remove(M key) {
      if (storage != null) {
          storage.remove(key);
      }
    }

    public boolean contains(M key) {
      return storage != null ? storage.containsKey(key) : false;
    }

    public void addAll(Level<M, T> otherLevel) {
      Map<M, T> s = getStorage();
      for (Entry<M, T> entry : otherLevel.storage.entrySet()) {
        s.put(entry.getKey(), entry.getValue());
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Level<M, T> clone() {
      try {
        Level<M, T> clone = (Level<M, T>) super.clone();
        if (storage != null) {
            clone.storage = (HashMap<M,T>)storage.clone();
        }
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new IllegalStateException("Impossible state.");
      }
    }

    
    @Override
    public String toString() {
      return "Level: " + storage != null ? storage.toString() : "{}";
    }
  }

  public static class ValuePlaceholder<M, T> {
    private final Level<M, T> level;

    public ValuePlaceholder(Level<M, T> level) {
      super();
      this.level = level;
    }

  }

}
