package me.vivian.displayerutils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing NBT, provides getters/setters for NBT
 */
public class NBTMagic {
    Plugin plugin;

    private final Map<Class<?>, PersistentDataType<?, ?>> dataTypeMap = new HashMap<>();

    public NBTMagic(Plugin thisPlugin) {
        plugin = thisPlugin;
        dataTypeMap.put(String.class, PersistentDataType.STRING);
        dataTypeMap.put(Integer.class, PersistentDataType.INTEGER);
        dataTypeMap.put(Double.class, PersistentDataType.DOUBLE);
        dataTypeMap.put(Boolean.class, PersistentDataType.BOOLEAN);
    }

    private NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    public PersistentDataContainer getNBTHolder(Entity entity) {
        return entity.getPersistentDataContainer();
    }

    public <T> T getNBT(Entity entity, String key, Class<T> dataType) {
        if (entity != null && key != null) {
            PersistentDataType<?, T> persistentDataType = (PersistentDataType<?, T>) dataTypeMap.get(dataType);
            if (persistentDataType != null) {
                NamespacedKey namespacedKey = getNamespacedKey(key);
                T value = getNBTHolder(entity).get(namespacedKey, persistentDataType);
                if (value == null) {
                    // Set default value based on dataType
                    if (dataType == String.class) {
                        setNBT(entity, key, "");
                        value = (T) "";
                    } else if (dataType == Boolean.class) {
                        setNBT(entity, key, false);
                        value = (T) Boolean.FALSE;
                    } else if (dataType == Integer.class) {
                        setNBT(entity, key, 0);
                        value = (T) Integer.valueOf(0);
                    } else if (dataType == Double.class) {
                        setNBT(entity, key, 0.0);
                        value = (T) Double.valueOf(0.0);
                    }
                }
                return value;
            }
        }
        return null;
    }




    public Boolean isBoolNBTNull(Entity entity, String key) {
        if (entity != null && key != null) {
            return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.BOOLEAN) == null;
        } else {
            return true;
        }
    }

    private void setNBTInner(Entity entity, String key, String value) {
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.STRING, value);
    }

    private void setNBTInner(Entity entity, String key, Integer value) {
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.INTEGER, value);
    }

    private void setNBTInner(Entity entity, String key, Double value) {
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.DOUBLE, value);
    }

    private void setNBTInner(Entity entity, String key, Boolean value) {
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.BOOLEAN, value);
    }

    public <T> void setNBT(Entity entity, String key, T value) {
        if (entity == null || key == null || value == null) {
            logNullValues(entity, key, value);
            throw new IllegalArgumentException("setNBT(): Entity, key, or value is null");
        }

        if (value instanceof String) {
            setNBTInner(entity, key, (String) value);
        } else if (value instanceof Integer) {
            setNBTInner(entity, key, (Integer) value);
        } else if (value instanceof Double) {
            setNBTInner(entity, key, (Double) value);
        } else if (value instanceof Boolean) {
            setNBTInner(entity, key, (Boolean) value);
        } else {
            String errorMessage = "setNBT(): Unsupported data type: " + value.getClass().getName();
            System.out.println(errorMessage); // Print the error message to the console
            throw new IllegalArgumentException(errorMessage); // Throw an exception
        }
    }

    private void logNullValues(Entity entity, String key, Object value) {
        if (entity == null) {
            System.out.println("setNBT(): Entity is null");
        }
        if (key == null) {
            System.out.println("setNBT(): Key is null");
        }
        if (value == null) {
            System.out.println("setNBT(): Value is null");
        }
    }
}
