package net.logeryx.simplemagic.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FangsSpiritConsumableItem extends Item {

    public FangsSpiritConsumableItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world instanceof ServerWorld serverWorld) {

            // Altitude Check
            BlockPos pos = user.getBlockPos();
            boolean isGrounded = !serverWorld.getBlockState(pos.down(1)).isAir() ||
                    !serverWorld.getBlockState(pos.down(2)).isAir();

            if (!isGrounded) {
                // play an error fizzle sound
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        SoundCategory.NEUTRAL,
                        0.5F,
                        1.0F
                );
                return ActionResult.FAIL;
            }

            // Calculate direction
            // Minecraft Yaw is in degrees. Convert to radians for math.
            // Adjust by +90 degrees because Minecraft's coordinate system is rotated relative to standard trig.
            float yaw = user.getYaw();
            double radianYaw = Math.toRadians(yaw + 90);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_EVOKER_CAST_SPELL,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
            );
            // if sneaking do a ring attack instead of a line
            if (user.isSneaking()) {
                conjureRingOfFangs(world, user, user.getY(), yaw);
            } else {
                conjureLineOfFangs(world, user, user.getY(), yaw);
            }

            // Consume the item (if not in creative)
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            // cooldown
            user.getItemCooldownManager().set(stack, 5); // 0.25 seconds
        }

        return ActionResult.SUCCESS;
    }

    // Line attack
    private void conjureLineOfFangs(World world, PlayerEntity user, double maxY, float yaw) {
        double radianYaw = Math.toRadians(yaw + 90);
        for (int i = 1; i < 16; i++) {

            // Calculate X and Z offsets based on where player is looking plus user pos
            // The target X/Z coordinates
            double targetX = user.getX() + Math.cos(radianYaw) * i;
            double targetZ = user.getZ() + Math.sin(radianYaw) * i;

            // calculate the delay (warmup) so they pop up in a wave (1 tick)
            conjureFang(world, user, targetX, targetZ, user.getY(), yaw, i);
        }
    }

    // Double ring attack
    private void conjureRingOfFangs(World world, PlayerEntity user, double maxY, float currentYaw) {
        // Convert the player's current yaw to radians for the math
        float f = (float) Math.toRadians(currentYaw + 90);

        // --- Inner Ring (6 Fangs) ---
        for (int i = 0; i < 6; ++i) {
            // Angle math: f (base angle) + i * PI * 1/3
            float angleRadians = f + i * (float) Math.PI * 1.0F / 3.0F;

            // Spawn 1.5 blocks away from the player
            double targetX = user.getX() + MathHelper.cos(angleRadians) * 1.65D;
            double targetZ = user.getZ() + MathHelper.sin(angleRadians) * 1.65D;

            // We convert the angle back to Degrees for the entity rotation
            float rotationDegrees = (float) Math.toDegrees(angleRadians) - 90;

            // Warmup is 0 (instant) for inner ring
            this.conjureFang(world, user, targetX, targetZ, maxY, rotationDegrees, 1);
        }

        // --- Outer Ring (10 Fangs) ---
        for (int i = 0; i < 10; ++i) {
            // Angle math: slightly offset so fangs don't align perfectly with inner ring
            float angleRadians = f + i * (float) Math.PI * 1.0F / 5.0F + 0.15666F; // 1.25.. is (PI * 2 / 5)

            // Spawn 2.5 blocks away
            double targetX = user.getX() + MathHelper.cos(angleRadians) * 2.7D;
            double targetZ = user.getZ() + MathHelper.sin(angleRadians) * 2.7D;

            float rotationDegrees = (float) Math.toDegrees(angleRadians) - 90;

            // Warmup is 3 ticks (slightly delayed) for outer ring
            this.conjureFang(world, user, targetX, targetZ, maxY, rotationDegrees, 3);
        }
    }

    /**
     * Adapted from EvokerEntity.conjureFangs
     * * @param world The world to spawn in
     * @param owner The player who cast it (so they don't take damage)
     * @param x Target X
     * @param z Target Z
     * @param maxY The highest Y level to check for ground (usually player's Y)
     * @param yaw The rotation of the fang
     * @param warmup How many ticks before it bites
     */
    private void conjureFang(World world, LivingEntity owner, double x, double z, double maxY, float yaw, int warmup) {
        BlockPos blockPos = BlockPos.ofFloored(x, maxY, z);
        boolean foundGround = false;
        double yOffset = 0.0;

        // Search downwards from the player's Y level to find the floor
        do {
            BlockPos blockPosBelow = blockPos.down();
            BlockState blockState = world.getBlockState(blockPosBelow);

            // If the block below is solid...
            if (blockState.isSideSolidFullSquare(world, blockPosBelow, Direction.UP)) {
                // ...and the block above isn't suffocating...
                if (!world.isAir(blockPos)) {
                    BlockState blockStateAbove = world.getBlockState(blockPos);
                    VoxelShape voxelShape = blockStateAbove.getCollisionShape(world, blockPos);
                    if (!voxelShape.isEmpty()) {
                        yOffset = voxelShape.getMax(Direction.Axis.Y);
                    }
                }
                foundGround = true;
                break;
            }

            blockPos = blockPos.down();
            // Keep searching as long as we aren't too far below the player (Math.min is implied by loop structure)
            // We limit the search to 1 block down per iteration, stopping if we go too far.
        } while (blockPos.getY() >= MathHelper.floor(maxY) - 3);

        if (foundGround) {
            // Create the fang
            // CRITICAL: We pass 'owner' (the player) as the last argument.
            // The EvokerFangsEntity logic specifically checks "if (target == owner) don't damage".
            EvokerFangsEntity fang = new EvokerFangsEntity(world, x, blockPos.getY() + yOffset, z, yaw, warmup, owner);

            world.spawnEntity(fang);

            // Vanilla doesn't play the sound automatically when spawning manually, so we emit the event
            // Note: Use owner (Player) as the emitter so sculk sensors hear the PLAYER doing it
            world.emitGameEvent(GameEvent.ENTITY_PLACE, new Vec3d(x, blockPos.getY() + yOffset, z), GameEvent.Emitter.of(owner));
        }
    }
}