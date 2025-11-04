package net.Yama_Sheep.copywand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CopySavedData extends SavedData {
    private final Map<UUID, CopyDataManager.CopyData> copyMap = new HashMap<>();

    public static final Codec<CopySavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), CopyDataManager.CopyData.CODEC)
                            .fieldOf("copies").forGetter(data -> data.copyMap)
            ).apply(instance, map -> {
                CopySavedData data = new CopySavedData();
                data.copyMap.putAll(map);
                return data;
            })
    );

    public static final SavedDataType<CopySavedData> TYPE = new SavedDataType<>(
            "copywand_data",
            ctx -> new CopySavedData(),
            ctx -> CODEC,
            DataFixTypes.LEVEL
    );

    public static CopySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void saveCopy(UUID playerId, CopyDataManager.CopyData data) {
        copyMap.put(playerId, data);
        this.setDirty();
    }

    public CopyDataManager.CopyData getCopy(UUID playerId) {
        return copyMap.get(playerId);
    }
}
