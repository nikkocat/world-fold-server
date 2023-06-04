package nikkocat.worldfold;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

public abstract class CodeStorage {

    // Extracted from TeleportCommand.class
    private static void teleport(Entity target, ServerWorld world, double x, double y, double z, Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch) {
        BlockPos blockPos = new BlockPos(x, y, z);
        float f = MathHelper.wrapDegrees(yaw);
        float g = MathHelper.wrapDegrees(pitch);
        if (target instanceof ServerPlayerEntity) {
            ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
            world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, target.getId());
            target.stopRiding();
            if (((ServerPlayerEntity) target).isSleeping()) {
                ((ServerPlayerEntity) target).wakeUp(true, true);
            }

            if (world == target.world) {
                ((ServerPlayerEntity) target).networkHandler.requestTeleport(x, y, z, f, g, movementFlags);
            } else {
                ((ServerPlayerEntity) target).teleport(world, x, y, z, f, g);
            }

            target.setHeadYaw(f);
        } else {
            float h = MathHelper.clamp(g, -90.0F, 90.0F);
            if (world == target.world) {
                target.refreshPositionAndAngles(x, y, z, f, h);
                target.setHeadYaw(f);
            } else {
                target.detach();
                Entity entity = target;
                target = target.getType().create(world);
                if (target == null) {
                    return;
                }

                target.copyFrom(entity);
                target.refreshPositionAndAngles(x, y, z, f, h);
                target.setHeadYaw(f);
                entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                world.onDimensionChanged(target);
            }
        }

        if (!(target instanceof LivingEntity) || !((LivingEntity) target).isFallFlying()) {
            target.setVelocity(target.getVelocity().multiply(1.0, 0.0, 1.0));
            target.setOnGround(true);
        }

        if (target instanceof PathAwareEntity) {
            ((PathAwareEntity) target).getNavigation().stop();
        }
    }
}
