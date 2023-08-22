package kebriel.ctf.internal.nms;

import io.netty.util.internal.ConcurrentSet;
import kebriel.ctf.display.scoreboards.ScoreboardLine;
import kebriel.ctf.entity.components.EntityWrapper;
import kebriel.ctf.entity.components.EquipmentSlots;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.CTFLogger;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.util.ReflectionUtil;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore.EnumScoreboardAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.ScoreboardTeamBase.EnumNameTagVisibility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Supplier;

public abstract class GamePacket {

    private final Set<CTFPlayer> receivers;
    private Supplier<Collection<CTFPlayer>> receiversSource;
    private Supplier<Location> nonstaticTarget;
    private Location target;

    private boolean rendered;
    private volatile boolean ubiquitous;
    private final Policy policy;

    {
        receivers = new ConcurrentSet<>();
        ubiquitous = true;
        policy = getClass().isAnnotationPresent(PacketPolicy.class) ? getClass().getAnnotation(PacketPolicy.class).policy() : Policy.SEND_ONLY;
    }

    private GamePacket(Location target) {
        this.target = target;
    }

    private GamePacket(Supplier<Location> nonstaticTarget) {
        this.nonstaticTarget = nonstaticTarget;
    }

    private GamePacket() {}

    public GamePacket addReceivers(Collection<CTFPlayer> receivingPlayers) {
        receivingPlayers.removeIf(Objects::isNull); // Null check failsafe for added safety
        receivers.addAll(receivingPlayers);
        ubiquitous = receivers.isEmpty();
        return this;
    }

    public GamePacket addReceivers(CTFPlayer... receivingPlayers) {
        return addReceivers(Arrays.asList(receivingPlayers));
    }

    public GamePacket setReceivers(Collection<CTFPlayer> receivingPlayers) {
        setReceivers(JavaUtil.typeArray(receivingPlayers, CTFPlayer.class));
        return this;
    }

    public GamePacket setReceivers(CTFPlayer... receivingPlayers) {
        receivers.clear();
        return addReceivers(receivingPlayers);
    }

    public void setReceiverSource(Supplier<Collection<CTFPlayer>> source) {
        receiversSource = source;
    }

    protected boolean isUbiquitous() {
        return ubiquitous;
    }

    private boolean inRenderProximity(CTFPlayer player) {
        return MinecraftUtil.packetIsInProximity(player, this);
    }

    public boolean shouldSend(CTFPlayer player) {
        return player.valid() && (!rendered || inRenderProximity(player)) && (ubiquitous || receivers.contains(player));
    }

    protected abstract WrappedPacket get();

    public void sendFor(CTFPlayer player) {
        if(shouldSend(player)) {
            if(!player.valid() && receivers.contains(player)) {
                removeReceiver(player);
                return;
            }
            get().send(player);
        }
    }

    public void sendToLobby() {
        for(CTFPlayer player : CTFPlayer.getAllOnline()) {
            sendFor(player);
        }
    }

    public void send() {
        ubiquitous = receivers.isEmpty();
        if(ubiquitous) {
            sendToLobby();
        }else{
            for(CTFPlayer player : receivers) {
                sendFor(player);
            }
        }
    }

    public void render() {
        if(policy == Policy.SEND_ONLY || rendered)
            return;

        PacketRegistry.renderThis(this);
        rendered = true;
    }

    public void derender() {
        if(!rendered)
            return;

        PacketRegistry.derenderThis(this);
        rendered = false;
    }

    public void updateRender() {
        if(!rendered)
            return;

        PacketRegistry.updateThis(this);
    }

    public void renderFor(CTFPlayer player) {
        if(policy == Policy.SEND_ONLY)
            return;

        PacketRegistry.render(this, player);
    }

    public Set<CTFPlayer> getReceivers() {
        return receivers;
    }

    public void removeReceiver(CTFPlayer player) {
        receivers.remove(player);
    }

    public void removeReceivers(CTFPlayer... players) {
        Arrays.asList(players).forEach(receivers::remove);
    }

    public void addReceiver(CTFPlayer player) {
        receivers.add(player);
    }

    public void clearReceivers() {
        receivers.clear();
    }

    public Supplier<Location> getNonstaticTarget() {
        return nonstaticTarget;
    }

    public Policy getPolicy() {
        return policy;
    }

    public boolean isRendered() {
        return rendered;
    }

    public boolean shouldCheckReceivers() {
        return receiversSource != null;
    }

    public void updateReceivers() {
        setReceivers(receiversSource.get());
    }

    @PacketPolicy(policy = Policy.LOCATIONAL)
    public static class SpawnEntity extends GamePacket implements Revertable {

        private final Entity entity;

        public SpawnEntity(Entity entity) {
            super(() -> entity.getBukkitEntity().getLocation());
            this.entity = entity;
        }

        public SpawnEntity(EntityWrapper<?> entity) {
            this(entity.getEntity());
        }

        @Override
        protected WrappedPacket get() {
            return WrappedPacket.wrap(entity instanceof LivingEntity ? new PacketPlayOutSpawnEntityLiving((EntityLiving) entity) : new PacketPlayOutSpawnEntity(entity, MinecraftUtil.getEntityTypeID(entity)));
        }

        @Override
        public GamePacket getRevertedPacket() {
            return new RemoveEntity(entity);
        }
    }

    @PacketPolicy(policy = Policy.LOCATIONAL)
    public static class RotateEntity extends GamePacket implements Revertable {

        private final Entity entity;
        private final float oldYaw;
        private final float oldPitch;
        private final float newYaw;
        private final float newPitch;

        /*
         * Shows the entity's rotation as being in accordance with the given yaw & pitch values
         */
        public RotateEntity(Entity entity, float newYaw, float newPitch) {
            super(() -> entity.getBukkitEntity().getLocation());
            this.entity = entity;
            this.newYaw = newYaw;
            this.newPitch = newPitch;
            oldYaw = entity.yaw;
            oldPitch = entity.pitch;
        }

        @Override
        protected WrappedPacket get() {
            return WrappedPacket.wrap(new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), JavaUtil.encodeAngle(newYaw), JavaUtil.encodeAngle(newPitch), entity.onGround));
        }

        @Override
        public GamePacket getRevertedPacket() {
            return new RotateEntity(entity, oldYaw, oldPitch);
        }
    }

    @PacketPolicy(policy = Policy.LOCATIONAL)
    public static class RemoveEntity extends GamePacket implements Revertable {

        private final Entity entity;

        public RemoveEntity(EntityWrapper<?> entity) {
            this(entity.getEntity());
        }

        public RemoveEntity(Entity entity) {
            super(() -> entity.getBukkitEntity().getLocation());
            this.entity = entity;
        }

        @Override
        protected WrappedPacket get() {
            return WrappedPacket.wrap(new PacketPlayOutEntityDestroy(entity.getId()));
        }

        @Override
        public GamePacket getRevertedPacket() {
            return new SpawnEntity(entity);
        }
    }

    @PacketPolicy(policy = Policy.LOCATIONAL)
    public static class MoveEntity extends GamePacket implements Revertable {

        private final Entity entity;
        private final byte[] deltas;
        private final byte[] oldDeltas;

        public MoveEntity(Entity entity, Vector move) {
            this(entity, parseVector(move));
        }

        private MoveEntity(Entity entity, byte[] deltas) {
            super(() -> entity.getBukkitEntity().getLocation());
            this.entity = entity;
            this.deltas = deltas;
            oldDeltas = new byte[3];
            for(int i = 0; i < deltas.length; i++) { // Invert the values, so this packet can be positionally reversed
                oldDeltas[i] = (byte) (-deltas[i]);
            }
        }

        private static byte[] parseVector(Vector vec) {
            byte[] bytes = new byte[3];
            if(!(JavaUtil.withinByteRange(vec.getX(), vec.getY(), vec.getZ()))) {
                Arrays.fill(bytes, (byte) 0); // Failsafe to prevent errors if too large or small of a value is given
            }
            bytes[0] = (byte)(vec.getX() * 32.0);
            bytes[1] = (byte)(vec.getY() * 32.0);
            bytes[2] = (byte)(vec.getZ() * 32.0);
            return bytes;
        }

        @Override
        protected WrappedPacket get() {
            return WrappedPacket.wrap(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entity.getId(),
                    deltas[0], deltas[1], deltas[2], false));
        }

        @Override
        public GamePacket getRevertedPacket() {
            return new MoveEntity(entity, oldDeltas);
        }
    }

    @PacketPolicy(policy = Policy.LOCATIONAL)
    public static class SetEntityEquipment extends GamePacket implements Revertable {

        private final Entity entity;
        private final ItemStack baseEquipment;
        private final EquipmentSlots slot;
        private final ItemStack item;

        public SetEntityEquipment(Entity entity, EquipmentSlots slot, ItemStack item) {
            super(() -> entity.getBukkitEntity().getLocation());
            baseEquipment = entity.getEquipment()[slot.getSlotID()];
            this.entity = entity;
            this.slot = slot;
            this.item = item;
        }

        @Override
        protected WrappedPacket get() {
            return WrappedPacket.wrap(new PacketPlayOutEntityEquipment(entity.getId(), slot.getSlotID(), item));
        }

        /**
         * Reverts the entity to appearing to wear whatever they're actually wearing, so not a
         * full technical reversion, as that would require tracking whatever possible
         * equipment packet their appearance had been affected by before.
         */
        @Override
        public GamePacket getRevertedPacket() {
            return new SetEntityEquipment(entity, slot, baseEquipment);
        }
    }

    @PacketPolicy(policy = Policy.SEND_ONLY)
    public static class DisplayTitle extends GamePacket {

        private String title;
        private String subtitle;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        public static DisplayTitle displayFullTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            return new DisplayTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

        public static DisplayTitle displayOnlyTitle(String text, int fadeIn, int stay, int fadeOut) {
            return new DisplayTitle(text, true, fadeIn, stay, fadeOut);
        }

        public static DisplayTitle displayOnlySubtitle(String text, int fadeIn, int stay, int fadeOut) {
            return new DisplayTitle(text, false, fadeIn, stay, fadeOut);
        }

        private DisplayTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this(fadeIn, stay, fadeOut);
            this.title = title;
            this.subtitle = subtitle;
        }

        private DisplayTitle(String line, boolean title, int fadeIn, int stay, int fadeOut) {
            this(fadeIn, stay, fadeOut);
            if(title)
                this.title = line;
            else
                subtitle = line;
        }

        private DisplayTitle(int fadeIn, int stay, int fadeOut) {
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        @Override
        protected WrappedPacket get() {
            // Invokes the constructor with EnumTitleAction.TIMES
            WrappedPacket packet = WrappedPacket.wrap(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
            if(title != null)
                packet.addPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText(title)));
            if(subtitle != null)
                packet.addPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle)));

            return packet;
        }
    }

    @PacketPolicy(policy = Policy.STATIC)
    public static class CreateScoreboard extends GamePacket implements Revertable {

        private final List<ScoreboardLine> lines;
        private final String id;
        private final String title;

        public CreateScoreboard(String id, String title, List<ScoreboardLine> lines) {
            this.id = id;
            this.title = title;
            this.lines = lines;
        }

        @Override
        protected WrappedPacket get() {
            PacketPlayOutScoreboardObjective createScoreboard = new PacketPlayOutScoreboardObjective();
            ReflectionUtil.setField(createScoreboard, "a", id);
            ReflectionUtil.setField(createScoreboard, "b", title);
            ReflectionUtil.setField(createScoreboard, "c", EnumScoreboardHealthDisplay.INTEGER);
            ReflectionUtil.setField(createScoreboard, "d", 0);

            PacketPlayOutScoreboardDisplayObjective displayScoreboard = new PacketPlayOutScoreboardDisplayObjective();
            ReflectionUtil.setField(displayScoreboard, "a", 1);
            ReflectionUtil.setField(displayScoreboard, "b", id);

            return WrappedPacket.wrap(createScoreboard).addPacket(displayScoreboard);
        }

        @Override
        public void sendFor(CTFPlayer player) {
            super.sendFor(player);
            if(shouldSend(player))
                for(ScoreboardLine line : lines) {
                    line.fillValues(player);
                    line.updateForPlayer(player);
                }
        }

        @Override
        public GamePacket getRevertedPacket() {
            return new ClearScoreboard(id);
        }
    }

    @PacketPolicy(policy = Policy.SEND_ONLY)
    public static class ClearScoreboard extends GamePacket {

        private final String id;

        public ClearScoreboard(String id) {
            this.id = id;
        }

        @Override
        protected WrappedPacket get() {
            PacketPlayOutScoreboardObjective remove = new PacketPlayOutScoreboardObjective();
            ReflectionUtil.setField(remove, "a", id);
            ReflectionUtil.setField(remove, "d", 1);
            return WrappedPacket.wrap(remove);
        }
    }

    @PacketPolicy(policy = Policy.SEND_ONLY)
    public static class UpdateScoreboardLine extends GamePacket {

        private final String id;
        private final int score;
        private final String line;

        /**
         * The syntax for adjusting the score vs. text contents of a scoreboard
         * line is virtually identical. As a result, the procedure currently is as follows:
         *
         * If you want to change the text contents of a line, ensure that the
         * number score you enter is identical to what it already is -- as that
         * will be how the client knows which line you're referring to
         *
         * If you want to change the numeric score of a line, ensure that the
         * text contents you enter are identical, as that instead will be
         * how the client knows which line is being edited
         *
         * The only current workaround I'm aware of is automatically running
         * a packet using EnumScoreboardAction.REMOVE automatically before
         * each update, but sending extra packets unnecessarily can affect
         * latency
         */
        public UpdateScoreboardLine(String id, int score, String line) {
            this.id = id;
            this.score = score;
            this.line = line;
        }

        @Override
        protected WrappedPacket get() {
            PacketPlayOutScoreboardScore addLine = new PacketPlayOutScoreboardScore();
            ReflectionUtil.setField(addLine, "a", line);
            ReflectionUtil.setField(addLine, "b", id);
            ReflectionUtil.setField(addLine, "c", score);
            ReflectionUtil.setField(addLine, "d", EnumScoreboardAction.CHANGE);
            return WrappedPacket.wrap(addLine);
        }
    }

    @PacketPolicy(policy = Policy.STATIC)
    public static class AlterPlayerNametag extends GamePacket implements Revertable {

        public enum NametagVisibilityAction {
            SET(EnumNameTagVisibility.NEVER, NametagAction.CREATE), REMOVE(EnumNameTagVisibility.ALWAYS, NametagAction.DELETE);

            private final String tag;
            private final NametagAction action;

            NametagVisibilityAction(EnumNameTagVisibility tag, NametagAction action) {
                this.tag = tag.e;
                this.action = action;
            }

            private String getTag() {
                return tag;
            }

            private NametagAction getAction() {
                return action;
            }
        }

        public enum NametagAction {
            CREATE(0), DELETE(1);

            private final int action;

            NametagAction(int action) {
                this.action = action;
            }

            private int getActionID() {
                return action;
            }
        }

        private final Set<CTFPlayer> targets;
        private final NametagAction action;
        private NametagVisibilityAction visibilityAction;
        private final String id;
        private ChatColor color;

        {
            targets = new HashSet<>();
        }

        /**
         * Creates a new color
         */
        public static AlterPlayerNametag setColor(Collection<CTFPlayer> targets, String teamID, ChatColor color) {
            return new AlterPlayerNametag(targets, NametagAction.CREATE, teamID, color);
        }

        public static AlterPlayerNametag setVisibility(CTFPlayer target, NametagVisibilityAction action) {
            return new AlterPlayerNametag(target, action, target.getNameRaw());
        }

        /**
         * This is meant to be used for a bulk collection of players, primarily
         * the action of coloring a whole team's nametags
         * @param color the ChatColor that will be used
         *              as a 'prefix' to color nametags
         * @param targets CTFPlayers varargs representing
         *               any/all players that should be
         *               affected by this packet
         */


        private AlterPlayerNametag(Collection<CTFPlayer> targets, NametagAction action, String id, ChatColor color) {
            this.targets.addAll(targets);
            this.action = action;
            this.id = id;
            this.color = color;
        }

        private AlterPlayerNametag(Collection<CTFPlayer> targets, NametagVisibilityAction visibilityAction, String id) {
            this.targets.addAll(targets);
            this.id = id;
            this.action = visibilityAction.getAction();
            this.visibilityAction = visibilityAction;
        }

        private AlterPlayerNametag(CTFPlayer target, NametagVisibilityAction visibilityAction, String id) {
            targets.add(target);
            this.id = id;
            this.action = visibilityAction.getAction();
            this.visibilityAction = visibilityAction;
        }

        @Override
        protected WrappedPacket get() {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

            // Set the team that's being edited
            ReflectionUtil.setField(packet, "a", id);

            // Set the action being taken
            // 0 = create, 1 = delete, 2 = edit, 3 = add player(s), 4 = remove player(s)
            ReflectionUtil.setField(packet, "h", action.getActionID());

            // If modifying visibility, set proper field
            if(visibilityAction != null)
                ReflectionUtil.setField(packet, "e", visibilityAction.getTag());

            // If setting color, set the team's prefix as being that color
            if(color != null)
                ReflectionUtil.setField(packet, "d", String.valueOf(color));

            // Set the targets involved in this operation, if provided
            if(!targets.isEmpty()) {
                List<String> names = new ArrayList<>();
                for(CTFPlayer player : targets)
                    names.add(player.getNameRaw());
                // Set players involved in this operation (by String list of names)
                ((Collection<String>)ReflectionUtil.getField(packet, "g")).addAll(names);
            }

            return WrappedPacket.wrap(packet);
        }

        /**
         * Adds a new player to be affected by this packet. Not
         * to be confused with a new player who *sees* this packet,
         * aka a 'receiver'
         *
         * Automatically updates, if rendered
         */
        public void addTarget(CTFPlayer player) {
            targets.add(player);
            updateRender(); // Method only does anything if this packet is already rendered
        }

        /**
         * Removes a player from being affected by this packet
         *
         * Automatically updates, if rendered
         */
        public void removeTarget(CTFPlayer player) {
            targets.remove(player);
            updateRender();
        }

        @Override
        public GamePacket getRevertedPacket() {
            return switch(action) {
                case CREATE -> new AlterPlayerNametag(targets, NametagAction.DELETE, id, null);
                case DELETE -> visibilityAction == NametagVisibilityAction.REMOVE ? new AlterPlayerNametag(targets, NametagVisibilityAction.SET, id)
                : new AlterPlayerNametag(targets, NametagAction.CREATE, id, color);
            };
        }
    }

    @PacketPolicy(policy = Policy.LOCATIONAL)
    public static class PlayParticles extends GamePacket {

        public enum ParticleField {
            PARTICLE_TYPE(EnumParticle.class),
            OFFSET_X(Float.class), OFFSET_Y(Float.class), OFFSET_Z(Float.class),
            DATA(Float.class), SPEED(Float.class),
            COUNT(Integer.class),
            LONG_DISTANCE(Boolean.class),
            EXTRA_DATA(Integer.class);

            private final Class<?> type;

            ParticleField(Class<?> type) {
                this.type = type;
            }

            private boolean matchesTypeOf(Object obj) {
                return obj.getClass().equals(type);
            }
        }

        private EnumParticle particle;
        private float xOffset;
        private float yOffset;
        private float zOffset;
        // Is data or speed, depending on the particle type
        private float data;
        private int count;
        private boolean longDistance;
        private int[] extraData;

        public PlayParticles(EnumParticle particle, Location loc, double xOffset, double yOffset, double zOffset, double data, int count, boolean longDistance, int... extraData) {
            this(particle, (float) xOffset, (float) yOffset, (float) zOffset, (float) data, count, longDistance, extraData);
            super.target = loc;
        }

        public PlayParticles(EnumParticle particle, Supplier<Location> nonstaticLoc, double xOffset, double yOffset, double zOffset, double data, int count, boolean longDistance, int... extraData) {
            this(particle, (float) xOffset, (float) yOffset, (float) zOffset, (float) data, count, longDistance, extraData);
            super.nonstaticTarget = nonstaticLoc;
        }

        private PlayParticles(EnumParticle particle, float xOffset, float yOffset, float zOffset, float data, int count, boolean longDistance, int... extraData) {
            this.particle = particle;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.data = data;
            this.count = count;
            this.longDistance = longDistance;
            this.extraData = extraData;
        }

        public GamePacket setField(ParticleField field, Object value) {
            if(!field.matchesTypeOf(value))
                throw new IllegalArgumentException("Type mismatch between value and value type of ParticleField '" + field + "'");

            switch(field) {
                case PARTICLE_TYPE -> particle = (EnumParticle) value;
                case OFFSET_X -> xOffset = (float) value;
                case OFFSET_Y -> yOffset = (float) value;
                case OFFSET_Z -> zOffset = (float) value;
                case DATA, SPEED -> data = (float) value;
                case COUNT -> count = (int) value;
                case LONG_DISTANCE -> longDistance = (boolean) value;
                case EXTRA_DATA -> extraData = (int[]) value;
            }
            return this;
        }

        public <T> T getField(ParticleField field) {
            Object value = switch(field) {
                case PARTICLE_TYPE -> particle;
                case OFFSET_X -> xOffset;
                case OFFSET_Y -> yOffset;
                case OFFSET_Z -> zOffset;
                case DATA, SPEED -> data;
                case COUNT -> count;
                case LONG_DISTANCE -> longDistance;
                case EXTRA_DATA -> extraData;
            };
            try {
                return (T) value;
            } catch(ClassCastException e) {
                CTFLogger.logError("Improper type expected from method");
            }
            return null;
        }

        public GamePacket modField(ParticleField field, char operator, float mod) {
            setField(field, JavaUtil.performAmbiguousMath(getField(field), mod, operator));
            return this;
        }

        @Override
        protected WrappedPacket get() {
            Location loc = super.getNonstaticTarget() == null ? super.target : super.nonstaticTarget.get();
            return WrappedPacket.wrap(new PacketPlayOutWorldParticles(
                    particle,
                    longDistance,
                    (float) loc.getX(),
                    (float) loc.getY(),
                    (float) loc.getZ(),
                    xOffset, yOffset, zOffset, data, count, extraData));
        }
    }

    @PacketPolicy(policy = Policy.SEND_ONLY)
    public static class SetXP extends GamePacket {

        private int level;
        private float progress;

        public SetXP(int level, float progress) {
            this.level = level;
            this.progress = progress;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setProgress(float progress) {
            this.progress = progress;
        }

        @Override
        protected WrappedPacket get() {
            return WrappedPacket.wrap(new PacketPlayOutExperience(progress, 0, level));
        }
    }

}
