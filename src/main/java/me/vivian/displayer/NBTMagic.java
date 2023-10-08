package me.vivian.displayer;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class NBTMagic {
    Plugin plugin;
    public NBTMagic(Plugin thisPlugin){
        plugin = thisPlugin;
    }

    private NamespacedKey getNamespacedKey(String key){
        return new NamespacedKey(plugin, key);
    }
    public PersistentDataContainer getNBTHolder(Entity entity){
        return entity.getPersistentDataContainer();
    }

    public String getStringNBT(Entity entity, String key){
        return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.STRING);
    }
    public Integer getIntNBT(Entity entity, String key){
        return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.INTEGER);
    }
    public Double getDoubleNBT(Entity entity, String key){
        return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.DOUBLE);
    }
    public Boolean getBoolNBT(Entity entity, String key){
        if(getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.BOOLEAN) == null){
            return false;
        }
        return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.BOOLEAN);
    }
    public Boolean isBoolNBTNull(Entity entity, String key){
        return getNBTHolder(entity).get(getNamespacedKey(key), PersistentDataType.BOOLEAN) == null;
    }

    public void setNBT(Entity entity, String key, String value){
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.STRING, value);
    }
    public void setNBT(Entity entity, String key, Integer value){
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.INTEGER, value);
    }
    public void setNBT(Entity entity, String key, Double value){
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.DOUBLE, value);
    }
    public void setNBT(Entity entity, String key, Boolean value){
        getNBTHolder(entity).set(getNamespacedKey(key), PersistentDataType.BOOLEAN, value);
    }
}
