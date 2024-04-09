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
public final class NBTMagic {
    static Plugin plugin;

    private static final Map<Class<?>, PersistentDataType<?, ?>> dataTypeMap = new HashMap<>();

    public static void init(Plugin thisPlugin) {
        plugin = thisPlugin;
        dataTypeMap.put(String.class, PersistentDataType.STRING);
        dataTypeMap.put(Integer.class, PersistentDataType.INTEGER);
        dataTypeMap.put(Double.class, PersistentDataType.DOUBLE);
        dataTypeMap.put(Boolean.class, PersistentDataType.BOOLEAN);
    }

    private static NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    public static PersistentDataContainer getNBTHolder(Entity entity) {
        return entity.getPersistentDataContainer();
    }

    public static <T> T getNBT(Entity entity, String key, Class<T> dataType) { // todo: memoization for this? for name, isparent, ischild, & uuid
        if (entity == null || key == null) return null;

        PersistentDataType<?, T> persistentDataType = (PersistentDataType<?, T>) dataTypeMap.get(dataType);
        if (persistentDataType == null) return null;

        NamespacedKey namespacedKey = getNamespacedKey(key);
        T value = getNBTHolder(entity).get(namespacedKey, persistentDataType);
        if (value != null) return value;

        // Set default value based on dataType
        switch(dataType.toString()){ // todo: .getName()?
            case "class java.lang.String":
                setNBT(entity, key, "");
                return (T) "";
            case "class java.lang.Integer":
                setNBT(entity, key, 0);
                return (T) Integer.valueOf(0);
            case "class java.lang.Double":
                setNBT(entity, key, 0.0);
                return (T) Double.valueOf(0.0);
            case "class java.lang.Boolean":
                setNBT(entity, key, false);
                return (T) Boolean.FALSE;
            default:
                System.out.println("invalid data type for getNBT: " + dataType);
        }
        return null;
    }

    public static Boolean isBoolNBTNull(Entity entity, String key) {
        if (entity != null && key != null) {
            return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.BOOLEAN) == null;
        } else {
            return true;
        }
    }

    public static <T> void setNBT(Entity entity, String key, T value) {
        if (entity == null || key == null || value == null) {
            logNullValues(entity, key, value);
            throw new IllegalArgumentException("setNBT(): Entity, key, or value is null");
        }

        NamespacedKey namespacedKey = getNamespacedKey(key);
        switch (value.getClass().getName()) {
            case "java.lang.String":
                getNBTHolder(entity).set(namespacedKey, PersistentDataType.STRING, (String) value);
                break;
            case "java.lang.Integer":
                getNBTHolder(entity).set(namespacedKey, PersistentDataType.INTEGER, (Integer) value);
                break;
            case "java.lang.Double":
                getNBTHolder(entity).set(namespacedKey, PersistentDataType.DOUBLE, (Double) value);
                break;
            case "java.lang.Boolean":
                getNBTHolder(entity).set(namespacedKey, PersistentDataType.BOOLEAN, (Boolean) value);
                break;
            default:
                String errorMessage = "setNBT(): Unsupported data type: " + value.getClass().getName(); // todo: config this
                System.out.println(errorMessage);
                throw new IllegalArgumentException(errorMessage);
        }
    }


    private static void logNullValues(Entity entity, String key, Object value) {
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
