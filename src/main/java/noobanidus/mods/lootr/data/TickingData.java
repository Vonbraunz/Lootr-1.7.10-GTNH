package noobanidus.mods.lootr.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TickingData extends WorldSavedData {
    private final Map<UUID, Integer> tickMap = new HashMap<>();

    public TickingData(String id) {
        super(id);
    }

    public boolean isDone(UUID id) {
        Integer val = tickMap.get(id);
        return val != null && (val == 0 || val == 1);
    }

    public int getValue(UUID id) {
        Integer val = tickMap.get(id);
        return val != null ? val : -1;
    }

    public boolean setValue(UUID id, int value) {
        return tickMap.put(id, value) == null;
    }

    public void removeDone(UUID id) {
        tickMap.remove(id);
    }

    public boolean tick() {
        if (tickMap.isEmpty()) return false;
        boolean changed = false;
        Map<UUID, Integer> newMap = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : tickMap.entrySet()) {
            int value = entry.getValue();
            if (value > 0) {
                value--;
                changed = true;
            }
            newMap.put(entry.getKey(), value);
        }
        if (changed) {
            tickMap.clear();
            tickMap.putAll(newMap);
            return true;
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        tickMap.clear();
        NBTTagCompound data = compound.getCompoundTag("result");
        for (Object key : data.func_150296_c()) {
            String k = (String) key;
            try {
                UUID uuid = UUID.fromString(k);
                tickMap.put(uuid, data.getInteger(k));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagCompound data = new NBTTagCompound();
        for (Map.Entry<UUID, Integer> entry : tickMap.entrySet()) {
            data.setInteger(entry.getKey().toString(), entry.getValue());
        }
        compound.setTag("result", data);
    }
}
