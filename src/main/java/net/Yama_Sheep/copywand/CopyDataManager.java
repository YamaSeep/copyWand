package net.Yama_Sheep.copywand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CopyDataManager {

    public record CopyData(List<BlockPos> poses, List<BlockState> states) {

        public static final Codec<CopyData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BlockPos.CODEC.listOf().fieldOf("poses").forGetter(CopyData::poses),
                        BlockState.CODEC.listOf().fieldOf("states").forGetter(CopyData::states)
                ).apply(instance, CopyData::new)
        );
    }
}
