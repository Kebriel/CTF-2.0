package kebriel.ctf.entity.entities.game;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.World;

import java.util.UUID;

public class NPCBase extends EntityHuman {

    public NPCBase(World world) {
        super(world, new GameProfile(UUID.randomUUID(), "NPC"));
    }

    @Override
    public boolean isSpectator() {
        return false;
    }
}
