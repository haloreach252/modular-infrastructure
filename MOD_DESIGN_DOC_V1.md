# 🧱 Modular Utility Posts
## Comprehensive Design Document v1.0
*A modular infrastructure mod for Minecraft 1.21.1+ NeoForge*

---

# 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Core Systems](#core-systems)
3. [Dynamic Sizing System](#dynamic-sizing-system)
4. [Wire Network System](#wire-network-system)
5. [Tools & User Interface](#tools--user-interface)
6. [Cross-Mod Integration](#cross-mod-integration)
7. [Technical Architecture](#technical-architecture)
8. [Asset & Content Guidelines](#asset--content-guidelines)
9. [Development Roadmap](#development-roadmap)
10. [Legal & Attribution](#legal--attribution)

---

# 🎯 Project Overview

## Vision Statement
A clean-room implementation of modular utility infrastructure inspired by Immersive Posts, featuring dynamic sizing, universal wire networks, and extensive cross-mod compatibility while maintaining complete creative and technical independence.

## Core Principles
- **Creative Freedom**: No artificial restrictions on pole height, width, or placement
- **Modular Design**: Component-based construction system for maximum flexibility
- **Universal Compatibility**: Seamless integration with major tech/power mods
- **Visual Excellence**: Original assets with consistent, professional aesthetic
- **Performance First**: Efficient rendering and network handling for large-scale builds

## Target Audience
- **Infrastructure Builders**: Players who love creating realistic power/utility networks
- **Modpack Players**: Users wanting better cross-mod integration and aesthetics
- **Creative Builders**: Those needing flexible, scalable utility components
- **Technical Players**: Users who appreciate efficient, well-designed systems

---

# 🏗️ Core Systems

## Component Architecture

### Post Components
```
📍 Foundation Block
- Ground-level anchoring point
- Provides stability visual cues (not mechanical)
- Material variants affect appearance only
- Supports dynamic sizing (4px-16px width)

🏗️ Shaft Segments  
- Vertical pole pieces (wood, metal, concrete, composite)
- Stackable to unlimited height
- Width matches connected foundation
- Texture variants for weathering/age

⚡ Crossarms
- Horizontal/diagonal mounting points
- Scale proportionally with pole width
- Support multiple attachment types
- Rotatable in 45-degree increments

🔧 Bracing Elements
- Structural supports and decorative trusses
- Enhance visual realism without mechanical function
- Optional components for aesthetic variety
- Material-specific designs

👑 Terminal Caps
- Specialized toppers (lightning rods, beacon lights, finials)
- Functional components (lighting, redstone, decoration)
- Scale with pole width
- Material-appropriate designs
```

### Material System
```
🌳 Wood Variants:
- Oak, Birch, Spruce, Dark Oak, Acacia, Cherry, Mangrove
- Weathering effects over time
- Biome-appropriate defaults

🔩 Metal Types:
- Iron (standard), Steel (IE integration), Aluminum (other mods)
- Rust/patina effects
- Industrial aesthetic

🏗️ Concrete:
- Modern, clean appearance
- Reinforced variants for heavy-duty look
- Smooth and textured options

⚗️ Composite Materials:
- Future-tech appearance
- Cross-mod material integration
- Special properties (glowing, energy-conducting)
```

---

# 📏 Dynamic Sizing System

## Size Specifications

### Width Range: 4px → 16px (2px increments)
```
Size Options:
- 4px (Ultra Slim)    - Telephone, cable, decorative
- 6px (Slim)          - Light residential, interior
- 8px (Standard)      - Default residential power
- 10px (Medium)       - Heavy residential, light commercial  
- 12px (Heavy)        - Commercial, light industrial
- 14px (Thick)        - Heavy industrial, transmission
- 16px (Massive)      - Monument, major transmission
```

### Technical Implementation
```java
// Blockstate property
public static final IntegerProperty WIDTH = IntegerProperty.create("width", 4, 16);

// Validation function
public static boolean isValidWidth(int width) {
    return width >= 4 && width <= 16 && width % 2 == 0;
}

// Model generation
public static BakedModel generatePostModel(int width, PostMaterial material) {
    int radius = width / 2;
    int offset = (16 - width) / 2; // Perfect centering
    return buildCuboidModel(offset, 0, offset, 16-offset, 16, 16-offset);
}
```

### Size Adjustment Methods

#### Primary: Scroll Wheel Control
```java
@SubscribeEvent
public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    Player player = Minecraft.getInstance().player;
    ItemStack held = player.getMainHandItem();
    
    if (held.getItem() instanceof PostItem && !player.isShiftKeyDown()) {
        int currentSize = PostItem.getSize(held);
        int newSize = currentSize + (event.getScrollDelta() > 0 ? 2 : -2);
        newSize = Mth.clamp(newSize, 4, 16);
        
        PostItem.setSize(held, newSize);
        displaySizePreview(newSize);
        event.setCanceled(true);
    }
}
```

#### Secondary: Right-Click Cycling
```java
// Cycle through sizes: 4→6→8→10→12→14→16→4
public void cycleSize(ItemStack stack) {
    int current = getSize(stack);
    int next = (current == 16) ? 4 : current + 2;
    setSize(stack, next);
    playClickSound();
}
```

### Visual Feedback
- **Placement Ghost**: Transparent preview showing exact size before placement
- **Size Indicator**: Tool tooltip displays current size (e.g., "Oak Post (10px)")
- **Attachment Preview**: Shows available connection points for current size
- **Ruler Overlay**: Optional precise measurement display

---

# 🔌 Wire Network System

## Core Wire Architecture
*Based on Immersive Engineering's wire system with full attribution*

### Wire Type Registration
```java
public class WireRegistry {
    // Dynamic registration based on loaded mods
    public static void registerWireType(String id, WireType wireType) {
        if (ModList.get().isLoaded(wireType.getModDependency())) {
            WIRE_TYPES.put(id, wireType);
            LOGGER.info("Registered wire type: {} for mod: {}", 
                       id, wireType.getModDependency());
        }
    }
}

// Example registrations
public static void registerModWires() {
    // Applied Energistics 2
    if (ModList.get().isLoaded("appliedenergistics2")) {
        registerWireType("fluix_wire", new WireType()
            .setModDependency("appliedenergistics2")
            .setTransferCapability(AECapabilities.ME_STORAGE)
            .setChannelCapacity(32) // Dense cable equivalent
            .setTexture("modular_posts:wires/fluix")
            .setRenderer(FluixWireRenderer.class)
        );
    }
    
    // Create
    if (ModList.get().isLoaded("create")) {
        registerWireType("mechanical_wire", new WireType()
            .setModDependency("create")
            .setTransferCapability(CreateCapabilities.KINETIC)
            .setStressCapacity(2048) // Configurable
            .setTexture("modular_posts:wires/mechanical")
            .setRenderer(MechanicalWireRenderer.class)
            .setAnimated(true) // Rotating texture for stress transmission
        );
    }
    
    // Mekanism
    if (ModList.get().isLoaded("mekanism")) {
        registerWireType("energy_wire_basic", createMekanismWire("basic", 400));
        registerWireType("energy_wire_advanced", createMekanismWire("advanced", 1600));
        registerWireType("energy_wire_elite", createMekanismWire("elite", 6400));
        registerWireType("energy_wire_ultimate", createMekanismWire("ultimate", 25600));
    }
}
```

### Wire Properties System
```java
public class WireType {
    private String modDependency;
    private Capability<?> transferCapability;
    private int capacity;
    private ResourceLocation texture;
    private Class<? extends WireRenderer> renderer;
    private boolean animated;
    private Color wireColor;
    private float thickness;
    private int maxLength;
    
    // Visual properties
    private boolean glows;
    private ParticleOptions particles;
    private SoundEvent connectionSound;
    
    // Connection logic
    private Predicate<BlockEntity> canConnectTo;
    private Function<Direction, Boolean> validSides;
}
```

### Wire Network Management
```java
public class WireNetwork {
    // Adapted from IE's network system with attribution
    private Set<BlockPos> posts;
    private Map<BlockPos, Set<WireConnection>> connections;
    private WireType wireType;
    private Level level;
    
    public void addConnection(BlockPos from, BlockPos to, WireType type) {
        // Validate connection distance and type compatibility
        // Update network topology
        // Sync to clients for rendering
    }
    
    public void removeConnection(BlockPos from, BlockPos to) {
        // Clean network split handling
        // Update affected segments
    }
    
    // Cross-mod power transfer
    public void tickPowerTransfer() {
        // Handle mod-specific power/data transfer
        // Respect capacity limits
        // Visual feedback for flow direction
    }
}
```

### Wire Rendering System
```java
public abstract class WireRenderer {
    public abstract void renderWire(PoseStack matrices, 
                                   VertexConsumer buffer,
                                   WireConnection connection,
                                   float partialTicks);
    
    // Shared utilities
    protected void renderBasicWire(Vector3f start, Vector3f end, 
                                  Color color, float thickness) {
        // Catenary curve calculation
        // Texture mapping along curve
        // LOD based on distance
    }
    
    protected void renderAnimatedWire(WireConnection connection, 
                                    float animationTime) {
        // Flowing texture animation
        // Particle effects for power flow
        // Dynamic color based on load
    }
}

public class MechanicalWireRenderer extends WireRenderer {
    @Override
    public void renderWire(PoseStack matrices, VertexConsumer buffer,
                          WireConnection connection, float partialTicks) {
        // Rope-like appearance with rotation animation
        // Stress visualization via thickness/color
        // Create-style kinetic energy particles
    }
}
```

---

# 🛠️ Tools & User Interface

## Post Configurator Tool
*Inspired by Mekanism's Configurator with expanded functionality*

### Tool Modes
```java
public enum ConfiguratorMode {
    COPY("Copy settings from target post"),
    PASTE("Apply copied settings to target"),  
    BATCH_SELECT("Select area for batch operations"),
    NETWORK_APPLY("Apply settings to wire network"),
    WIRE_CONNECT("Connect/disconnect wires"),
    ANALYZE("Display network information");
    
    // Cycle through modes with keybind (default: G)
}

public class PostConfiguratorItem extends Item {
    // NBT data storage
    private static final String COPIED_CONFIG = "CopiedConfig";
    private static final String CURRENT_MODE = "CurrentMode";
    private static final String SELECTION_START = "SelectionStart";
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ConfiguratorMode mode = getCurrentMode(context.getItemInHand());
        
        switch (mode) {
            case COPY -> copyPostConfiguration(context);
            case PASTE -> pastePostConfiguration(context);
            case BATCH_SELECT -> handleBatchSelection(context);
            case NETWORK_APPLY -> applyToNetwork(context);
            case WIRE_CONNECT -> handleWireConnection(context);
            case ANALYZE -> displayNetworkInfo(context);
        }
        
        return InteractionResult.SUCCESS;
    }
}
```

### Configuration Operations
```java
public class PostConfiguration {
    private int width;
    private PostMaterial material;
    private Set<AttachmentType> attachments;
    private Map<String, Object> customProperties;
    
    public void applyTo(BlockEntity postEntity) {
        if (postEntity instanceof PostBlockEntity post) {
            post.setWidth(this.width);
            post.setMaterial(this.material);
            post.syncAttachments(this.attachments);
            post.markForUpdate();
        }
    }
    
    public static PostConfiguration copyFrom(BlockEntity postEntity) {
        // Extract all configurable properties
        // Return immutable configuration object
    }
}

// Batch operations
public class BatchConfigurationManager {
    public static void applyToArea(Level level, BoundingBox area, 
                                 PostConfiguration config, Player player) {
        List<BlockPos> affectedPosts = findPostsInArea(level, area);
        
        if (affectedPosts.size() > LARGE_OPERATION_THRESHOLD) {
            showConfirmationDialog(player, affectedPosts.size(), config);
        } else {
            executeConfiguration(level, affectedPosts, config);
        }
    }
    
    public static void applyToNetwork(Level level, BlockPos startPos, 
                                    PostConfiguration config) {
        Set<BlockPos> networkPosts = WireNetworkHelper.getConnectedPosts(level, startPos);
        executeConfiguration(level, new ArrayList<>(networkPosts), config);
    }
}
```

### Undo System
```java
public class ConfiguratorHistory {
    private static final int MAX_HISTORY = 10;
    private static final Map<UUID, Deque<ConfigurationOperation>> PLAYER_HISTORY = new HashMap<>();
    
    public static void recordOperation(Player player, ConfigurationOperation operation) {
        Deque<ConfigurationOperation> history = PLAYER_HISTORY.computeIfAbsent(
            player.getUUID(), k -> new ArrayDeque<>()
        );
        
        if (history.size() >= MAX_HISTORY) {
            history.removeFirst();
        }
        
        history.addLast(operation);
    }
    
    public static boolean undoLastOperation(Player player) {
        Deque<ConfigurationOperation> history = PLAYER_HISTORY.get(player.getUUID());
        if (history != null && !history.isEmpty()) {
            ConfigurationOperation operation = history.removeLast();
            operation.undo(player.level());
            return true;
        }
        return false;
    }
}

public class ConfigurationOperation {
    private final List<BlockPos> affectedPositions;
    private final List<PostConfiguration> previousConfigurations;
    private final PostConfiguration appliedConfiguration;
    
    public void undo(Level level) {
        for (int i = 0; i < affectedPositions.size(); i++) {
            BlockPos pos = affectedPositions.get(i);
            PostConfiguration previousConfig = previousConfigurations.get(i);
            
            if (level.getBlockEntity(pos) instanceof PostBlockEntity post) {
                previousConfig.applyTo(post);
            }
        }
    }
}
```

## Wire Connection Tools

### Wire Cutters
```java
public class WireCuttersItem extends Item {
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        
        if (be instanceof PostBlockEntity post) {
            Set<WireConnection> connections = post.getWireConnections();
            
            if (connections.isEmpty()) {
                displayMessage(context.getPlayer(), "No wires to cut");
                return InteractionResult.FAIL;
            }
            
            // Show UI for selecting which wire to cut
            openWireSelectionGUI(context.getPlayer(), post, connections);
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    public void cutWire(PostBlockEntity post, WireConnection connection) {
        WireNetwork network = WireNetworkManager.getNetwork(post.getLevel(), post.getBlockPos());
        network.removeConnection(connection.getStart(), connection.getEnd());
        
        // Play cutting sound and particle effects
        playCuttingEffects(post.getLevel(), connection);
    }
}
```

### Wire Connector Tool
```java
public class WireConnectorItem extends Item {
    private static final String FIRST_CONNECTION = "FirstConnection";
    private static final String SELECTED_WIRE_TYPE = "SelectedWireType";
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        
        if (hasFirstConnection(stack)) {
            // Complete connection
            BlockPos firstPos = getFirstConnection(stack);
            WireType wireType = getSelectedWireType(stack);
            
            if (canConnect(context.getLevel(), firstPos, clickedPos, wireType)) {
                createWireConnection(context.getLevel(), firstPos, clickedPos, wireType);
                clearFirstConnection(stack);
                return InteractionResult.SUCCESS;
            } else {
                displayConnectionError(context.getPlayer(), firstPos, clickedPos);
                return InteractionResult.FAIL;
            }
        } else {
            // Start connection
            if (canStartConnection(context.getLevel(), clickedPos)) {
                setFirstConnection(stack, clickedPos);
                displayConnectionPreview(context.getPlayer());
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
    
    // Scroll wheel to change wire type
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        ItemStack held = player.getMainHandItem();
        
        if (held.getItem() instanceof WireConnectorItem && player.isShiftKeyDown()) {
            List<WireType> availableTypes = WireRegistry.getAvailableTypes();
            int currentIndex = getCurrentWireTypeIndex(held);
            int newIndex = (currentIndex + (event.getScrollDelta() > 0 ? 1 : -1) + availableTypes.size()) % availableTypes.size();
            
            setSelectedWireType(held, availableTypes.get(newIndex));
            displayWireTypeChange(availableTypes.get(newIndex));
            event.setCanceled(true);
        }
    }
}
```

---

# 🔧 Cross-Mod Integration

## Attachment System

### Attachment Registry
```java
public class PostAttachmentRegistry {
    private static final Map<Block, AttachmentConfig> ATTACHMENTS = new HashMap<>();
    
    public static void register(Block block, AttachmentConfig config) {
        ATTACHMENTS.put(block, config);
        LOGGER.info("Registered post attachment: {}", block.getRegistryName());
    }
    
    public static boolean canAttach(Block block, PostBlockEntity post, Direction side) {
        AttachmentConfig config = ATTACHMENTS.get(block);
        if (config == null) return false;
        
        return config.isValidMountPoint(side) && 
               config.isValidPostSize(post.getWidth()) &&
               config.areRequirementsMetBy(post);
    }
}

public class AttachmentConfig {
    private Set<MountPoint> validMountPoints;
    private IntRange validPostSizes;
    private Set<PostMaterial> compatibleMaterials;
    private Set<String> requiredWireTypes;
    private Predicate<PostBlockEntity> customRequirements;
    
    public static class Builder {
        public Builder mountPoints(MountPoint... points) { /* */ }
        public Builder postSizes(int min, int max) { /* */ }
        public Builder materials(PostMaterial... materials) { /* */ }
        public Builder requiresWires(String... wireTypes) { /* */ }
        public Builder customRequirement(Predicate<PostBlockEntity> req) { /* */ }
    }
}

public enum MountPoint {
    POST_TOP,           // Top of the post shaft
    POST_SIDE_NORTH,    // Cardinal directions on post
    POST_SIDE_SOUTH,
    POST_SIDE_EAST, 
    POST_SIDE_WEST,
    CROSSARM_END,       // End of crossarm
    CROSSARM_TOP,       // Top of crossarm
    CROSSARM_BOTTOM,    // Bottom of crossarm
    BASE_SIDE,          // Side of foundation
    CUSTOM             // Mod-specific attachment points
}
```

### Mod-Specific Integrations

#### Immersive Engineering
```java
public class IEIntegration {
    public static void registerAttachments() {
        // LV Wire Relay
        PostAttachmentRegistry.register(IEBlocks.Connectors.RELAY_LV, 
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.CROSSARM_TOP, MountPoint.CROSSARM_BOTTOM)
                .postSizes(6, 16) // No ultra-slim posts
                .materials(PostMaterial.METAL, PostMaterial.CONCRETE)
                .requiresWires("lv_wire")
                .build()
        );
        
        // HV Wire Relay  
        PostAttachmentRegistry.register(IEBlocks.Connectors.RELAY_HV,
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.CROSSARM_TOP)
                .postSizes(10, 16) // Heavy-duty only
                .materials(PostMaterial.METAL, PostMaterial.CONCRETE)
                .requiresWires("hv_wire")
                .build()
        );
        
        // Transformer
        PostAttachmentRegistry.register(IEBlocks.Connectors.TRANSFORMER,
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.POST_SIDE_NORTH, MountPoint.POST_SIDE_SOUTH)
                .postSizes(12, 16) // Large posts only
                .materials(PostMaterial.METAL, PostMaterial.CONCRETE)
                .customRequirement(post -> post.hasGroundConnection())
                .build()
        );
    }
}
```

#### Create Integration
```java
public class CreateIntegration {
    public static void registerAttachments() {
        // Rope Pulley
        PostAttachmentRegistry.register(AllBlocks.ROPE_PULLEY.get(),
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.CROSSARM_END, MountPoint.POST_TOP)
                .postSizes(8, 16)
                .materials(PostMaterial.WOOD, PostMaterial.METAL)
                .build()
        );
        
        // Mechanical Relay (custom attachment for our wire system)
        PostAttachmentRegistry.register(ModBlocks.CREATE_MECHANICAL_RELAY,
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.CROSSARM_TOP, MountPoint.CROSSARM_BOTTOM)
                .postSizes(6, 14)
                .requiresWires("mechanical_wire")
                .customRequirement(post -> post.hasStressCapacity())
                .build()
        );
    }
}
```

#### Applied Energistics 2
```java
public class AE2Integration {
    public static void registerAttachments() {
        // ME Cable Anchor (custom block for our system)
        PostAttachmentRegistry.register(ModBlocks.ME_CABLE_ANCHOR,
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.values()) // Very flexible
                .postSizes(4, 16) // All sizes
                .requiresWires("fluix_wire", "covered_wire", "smart_wire")
                .build()
        );
        
        // Wireless Access Point
        PostAttachmentRegistry.register(AEBlocks.WIRELESS_ACCESS_POINT.block(),
            new AttachmentConfig.Builder()
                .mountPoints(MountPoint.POST_TOP, MountPoint.CROSSARM_TOP)
                .postSizes(8, 16)
                .materials(PostMaterial.METAL, PostMaterial.COMPOSITE)
                .customRequirement(post -> post.hasWirelessCapability())
                .build()
        );
    }
}
```

### Data Pack Support
```json
{
  "type": "modular_posts:attachment",
  "block": "thermal:energy_cell",
  "config": {
    "mount_points": ["post_side_north", "post_side_south", "post_side_east", "post_side_west"],
    "post_sizes": {"min": 10, "max": 16},
    "materials": ["metal", "concrete"],
    "required_wires": ["rf_wire"],
    "placement_sound": "thermal:block_place",
    "removal_sound": "thermal:block_break"
  }
}
```

---

# 🏛️ Technical Architecture

## Performance Optimization

### Rendering System
```java
public class PostRenderingManager {
    // LOD (Level of Detail) system
    private static final double LOD_DISTANCE_1 = 32.0; // Full detail
    private static final double LOD_DISTANCE_2 = 64.0; // Simplified
    private static final double LOD_DISTANCE_3 = 128.0; // Minimal
    
    public static LODLevel calculateLOD(BlockPos postPos, Vec3 cameraPos) {
        double distance = postPos.distToCenterSqr(cameraPos);
        
        if (distance < LOD_DISTANCE_1 * LOD_DISTANCE_1) return LODLevel.FULL;
        if (distance < LOD_DISTANCE_2 * LOD_DISTANCE_2) return LODLevel.SIMPLIFIED;
        if (distance < LOD_DISTANCE_3 * LOD_DISTANCE_3) return LODLevel.MINIMAL;
        return LODLevel.HIDDEN;
    }
    
    // Batch rendering for multiple posts
    public static void renderPostBatch(PoseStack matrices, VertexConsumer buffer,
                                     List<PostBlockEntity> posts, LODLevel lod) {
        for (PostBlockEntity post : posts) {
            switch (lod) {
                case FULL -> renderFullPost(matrices, buffer, post);
                case SIMPLIFIED -> renderSimplifiedPost(matrices, buffer, post);
                case MINIMAL -> renderMinimalPost(matrices, buffer, post);
            }
        }
    }
}

public enum LODLevel {
    FULL,       // All details, attachments, wire connections
    SIMPLIFIED, // Basic post shape, major attachments only
    MINIMAL,    // Simple cylinder, no attachments
    HIDDEN      // Not rendered
}
```

### Network Synchronization
```java
public class PostNetworkHandler {
    // Efficient delta updates for multiplayer
    public static void sendPostUpdate(ServerPlayer player, PostBlockEntity post, 
                                    Set<UpdateType> changes) {
        PostUpdatePacket packet = new PostUpdatePacket(
            post.getBlockPos(),
            changes.contains(UpdateType.SIZE) ? post.getWidth() : null,
            changes.contains(UpdateType.MATERIAL) ? post.getMaterial() : null,
            changes.contains(UpdateType.ATTACHMENTS) ? post.getAttachments() : null
        );
        
        PacketDistributor.PLAYER.with(() -> player).send(packet);
    }
    
    // Bulk updates for configurator operations
    public static void sendBulkUpdate(ServerPlayer player, 
                                    Map<BlockPos, PostConfiguration> changes) {
        BulkPostUpdatePacket packet = new BulkPostUpdatePacket(changes);
        PacketDistributor.PLAYER.with(() -> player).send(packet);
    }
}

public enum UpdateType {
    SIZE, MATERIAL, ATTACHMENTS, WIRE_CONNECTIONS, VISUAL_STATE
}
```

### Memory Management
```java
public class PostModelCache {
    // Cache models for each size/material combination
    private static final LoadingCache<ModelKey, BakedModel> MODEL_CACHE = 
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<ModelKey, BakedModel>() {
                @Override
                public BakedModel load(ModelKey key) {
                    return generatePostModel(key.width, key.material, key.components);
                }
            });
    
    public static BakedModel getModel(int width, PostMaterial material, 
                                    Set<ComponentType> components) {
        ModelKey key = new ModelKey(width, material, components);
        return MODEL_CACHE.getUnchecked(key);
    }
    
    record ModelKey(int width, PostMaterial material, Set<ComponentType> components) {}
}
```

## Block Entity Design
```java
public class PostBlockEntity extends BlockEntity implements MenuProvider {
    // Core properties
    private int width = 8; // Default size
    private PostMaterial material = PostMaterial.WOOD_OAK;
    private Set<AttachmentInstance> attachments = new HashSet<>();
    private Map<Direction, WireConnection> wireConnections = new HashMap<>();
    
    // Network integration
    private final LazyOptional<IEnergyStorage> energyHandler;
    private final LazyOptional<IStressCapability> stressHandler;
    private final LazyOptional<IMENetworkProvider> meHandler;
    
    // Rendering data
    private AABB renderBounds;
    private boolean needsRenderUpdate = true;
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.width = tag.getInt("Width");
        this.material = PostMaterial.valueOf(tag.getString("Material"));
        
        // Load attachments
        ListTag attachmentList = tag.getList("Attachments", Tag.TAG_COMPOUND);
        this.attachments.clear();
        for (Tag attachmentTag : attachmentList) {
            AttachmentInstance attachment = AttachmentInstance.load((CompoundTag) attachmentTag);
            this.attachments.add(attachment);
        }
        
        // Load wire connections
        // ... wire loading logic
        
        updateRenderBounds();
    }
    
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Width", this.width);
        tag.putString("Material", this.material.name());
        
        // Save attachments
        ListTag attachmentList = new ListTag();
        for (AttachmentInstance attachment : this.attachments) {
            attachmentList.add(attachment.save());
        }
        tag.put("Attachments", attachmentList);
        
        // Save wire connections
        // ... wire saving logic
    }
    
    // Network capabilities based on attached components and wires
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        // Energy capability (RF, FE, etc.)
        if (cap == ForgeCapabilities.ENERGY && hasEnergyWires()) {
            return energyHandler.cast();
        }
        
        // Create stress capability
        if (cap == CreateCapabilities.KINETIC && hasMechanicalWires()) {
            return stressHandler.cast();
        }
        
        // AE2 ME network capability
        if (cap == AECapabilities.ME_STORAGE && hasMEWires()) {
            return meHandler.cast();
        }
        
        return super.getCapability(cap, side);
    }
    
    // Configuration methods
    public void setWidth(int newWidth) {
        if (isValidWidth(newWidth) && this.width != newWidth) {
            this.width = newWidth;
            this.needsRenderUpdate = true;
            updateRenderBounds();
            markForSync();
        }
    }
    
    public void setMaterial(PostMaterial newMaterial) {
        if (this.material != newMaterial) {
            this.material = newMaterial;
            this.needsRenderUpdate = true;
            markForSync();
        }
    }
    
    // Attachment management
    public boolean addAttachment(AttachmentInstance attachment) {
        if (canAddAttachment(attachment)) {
            this.attachments.add(attachment);
            updateCapabilities();
            markForSync();
            return true;
        }
        return false;
    }
    
    public boolean removeAttachment(AttachmentInstance attachment) {
        if (this.attachments.remove(attachment)) {
            updateCapabilities();
            markForSync();
            return true;
        }
        return false;
    }
    
    private void updateRenderBounds() {
        double radius = this.width / 2.0 / 16.0; // Convert to world units
        this.renderBounds = new AABB(-radius, 0, -radius, radius, 1, radius);
    }
    
    private void markForSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            PostNetworkHandler.sendPostUpdate((ServerPlayer) null, this, 
                EnumSet.allOf(UpdateType.class));
        }
    }
}
```

---

# 🎨 Asset & Content Guidelines

## Visual Style Guide

### Design Principles
- **Consistent Proportions**: All components maintain proper scale relationships
- **Material Authenticity**: Each material type has distinctive, realistic appearance
- **Modular Compatibility**: Components visually integrate seamlessly regardless of mix
- **Performance Conscious**: Textures optimized for both quality and memory usage

### Texture Specifications
```
📏 Texture Resolution:
- Base Textures: 16x16 pixels (Minecraft standard)
- Detail Overlays: 16x16 pixels with alpha channel
- Animated Textures: 16x256 pixels (16 frames max)

🎨 Color Palette Guidelines:
Wood Materials:
- Oak: #B8945F → #8B6914 (warm brown range)
- Birch: #F4E4BC → #D4C59A (light cream range)  
- Spruce: #7A5C3F → #5D4A2F (dark brown range)

Metal Materials:
- Iron: #D8D8D8 → #5F5F5F (neutral gray range)
- Steel: #B8C5D1 → #6B7B8C (blue-gray range)
- Weathered: Add rust/patina overlays

Concrete Materials:
- Clean: #E6E6E6 → #CCCCCC (light gray range)
- Industrial: #B3B3B3 → #999999 (medium gray range)
- Weathered: Darker base with stain patterns
```

### Model Architecture
```java
// Dynamic model generation for different sizes
public class PostModelGenerator {
    public static UnbakedModel generatePostModel(int width, PostMaterial material) {
        BlockModel.Builder builder = new BlockModel.Builder();
        
        // Calculate dimensions
        int radius = width / 2;
        int offset = (16 - width) / 2;
        
        // Main shaft element
        builder.element()
            .from(offset, 0, offset)
            .to(16 - offset, 16, 16 - offset)
            .face(Direction.NORTH).texture("#side").end()
            .face(Direction.SOUTH).texture("#side").end()
            .face(Direction.EAST).texture("#side").end()
            .face(Direction.WEST).texture("#side").end()
            .face(Direction.UP).texture("#top").end()
            .face(Direction.DOWN).texture("#bottom").end();
        
        // Material-specific textures
        String textureBase = "modular_posts:blocks/" + material.getTexturePath();
        builder.texture("side", textureBase + "_side");
        builder.texture("top", textureBase + "_top");
        builder.texture("bottom", textureBase + "_bottom");
        
        return builder.build();
    }
}
```

### Animation System
```java
public class PostAnimationManager {
    // Wire sag animation based on weather
    public static float calculateWireSag(WireConnection connection, Level level) {
        WeatherType weather = level.getRainLevel(1.0f);
        float baselineSag = 0.1f; // 10% droop
        float weatherMultiplier = 1.0f + (weather * 0.3f); // Up to 30% more sag in rain
        
        return baselineSag * weatherMultiplier;
    }
    
    // Attachment animation (swinging lanterns, rotating equipment)
    public static void updateAttachmentAnimations(PostBlockEntity post, float partialTicks) {
        for (AttachmentInstance attachment : post.getAttachments()) {
            if (attachment.hasAnimation()) {
                attachment.updateAnimation(partialTicks);
            }
        }
    }
    
    // Seasonal weathering effects
    public static float getWeatheringLevel(PostBlockEntity post) {
        long worldTime = post.getLevel().getGameTime();
        BiomeManager.BiomeType biome = post.getLevel().getBiome(post.getBlockPos()).value().getBiomeCategory();
        
        float baseWeathering = (worldTime / 24000f) * 0.001f; // Very slow aging
        float biomeMultiplier = switch (biome) {
            case SWAMP, OCEAN -> 1.5f; // Faster weathering in wet biomes
            case DESERT -> 0.7f; // Slower weathering in dry biomes  
            default -> 1.0f;
        };
        
        return Math.min(1.0f, baseWeathering * biomeMultiplier);
    }
}
```

## Sound Design
```java
public class PostSounds {
    // Material-specific placement sounds
    public static final SoundEvent PLACE_WOOD_POST = createSound("place_wood_post");
    public static final SoundEvent PLACE_METAL_POST = createSound("place_metal_post");
    public static final SoundEvent PLACE_CONCRETE_POST = createSound("place_concrete_post");
    
    // Interaction sounds
    public static final SoundEvent CONFIGURATOR_COPY = createSound("configurator_copy");
    public static final SoundEvent CONFIGURATOR_PASTE = createSound("configurator_paste");
    public static final SoundEvent WIRE_CONNECT = createSound("wire_connect");
    public static final SoundEvent WIRE_DISCONNECT = createSound("wire_disconnect");
    
    // Ambient sounds
    public static final SoundEvent WIRE_HUM_ELECTRICAL = createSound("wire_hum_electrical");
    public static final SoundEvent WIRE_CREAK_MECHANICAL = createSound("wire_creak_mechanical");
    public static final SoundEvent POST_WIND_WHISTLE = createSound("post_wind_whistle");
    
    private static SoundEvent createSound(String name) {
        ResourceLocation location = new ResourceLocation("modular_posts", name);
        return SoundEvent.createVariableRangeEvent(location);
    }
}
```

---

# 🗓️ Development Roadmap

## Phase 1: Foundation (Weeks 1-8)
**Goal: Functional MVP with core systems**

### Week 1-2: Project Setup
- [ ] Mod structure and build system (NeoForge 1.21.1)
- [ ] Basic block registration and item system
- [ ] Initial texture placeholders and Blockbench models
- [ ] Core data structures (PostBlockEntity, size system)

### Week 3-4: Dynamic Sizing System
- [ ] Blockstate properties for width (4-16px, 2px increments)
- [ ] Model generation for different sizes
- [ ] Scroll wheel size adjustment
- [ ] Right-click cycling implementation
- [ ] Visual feedback and placement preview

### Week 5-6: Basic Wire System
- [ ] IE wire system integration with proper attribution
- [ ] WireType registration system
- [ ] Basic energy wire implementation (RF/FE)
- [ ] Wire rendering and connection logic
- [ ] Simple wire connection tools

### Week 7-8: Post Configurator Tool
- [ ] Copy/paste functionality for post settings
- [ ] Mode switching (Copy/Paste/Batch/Network)
- [ ] Basic undo system
- [ ] Visual feedback for operations
- [ ] Safety confirmations for bulk operations

**Deliverable**: Working mod with resizable posts, basic wires, and configuration tools

## Phase 2: Integration & Enhancement (Weeks 9-16)
**Goal: Cross-mod compatibility and advanced features**

### Week 9-10: Cross-Mod Wire Types
- [ ] Create mechanical wire integration
- [ ] Applied Energistics 2 fluix wire implementation
- [ ] Mekanism energy tier wires
- [ ] Thermal Expansion RF wires
- [ ] Dynamic wire type registration based on loaded mods

### Week 11-12: Attachment System
- [ ] PostAttachmentRegistry implementation
- [ ] Immersive Engineering attachment compatibility
- [ ] Create mod attachment support
- [ ] Basic data pack attachment definitions
- [ ] Attachment placement and removal logic

### Week 13-14: Advanced Tools
- [ ] Wire connector tool with type selection
- [ ] Wire cutters with selection GUI
- [ ] Network analyzer for debugging
- [ ] Batch selection area tool
- [ ] Network-wide configuration application

### Week 15-16: Performance Optimization
- [ ] LOD system for distant posts
- [ ] Model caching and optimization
- [ ] Efficient network synchronization
- [ ] Memory usage optimization
- [ ] Multiplayer testing and fixes

**Deliverable**: Feature-complete mod with full cross-mod integration

## Phase 3: Polish & Release (Weeks 17-24)
**Goal: Production-ready release with documentation**

### Week 17-18: Visual Polish
- [ ] Final texture pass (original artwork)
- [ ] Animation system implementation
- [ ] Seasonal and weathering effects
- [ ] Sound effects and ambient audio
- [ ] Particle effects for power flow

### Week 19-20: Advanced Features
- [ ] Blueprint system for saving/loading designs
- [ ] Template sharing between players
- [ ] Advanced network analysis tools
- [ ] Cross-system power conversion
- [ ] Smart load balancing visualization

### Week 21-22: Documentation & Tutorials
- [ ] Comprehensive mod wiki
- [ ] Video tutorials for key features
- [ ] API documentation for addon developers
- [ ] Data pack creation guide
- [ ] Integration guide for modpack creators

### Week 23-24: Testing & Release
- [ ] Extensive multiplayer testing
- [ ] Performance benchmarking
- [ ] Compatibility testing with popular modpacks
- [ ] Community beta testing
- [ ] CurseForge and Modrinth release preparation

**Deliverable**: Polished, documented mod ready for public release

## Post-Release: Maintenance & Expansion
### Version 1.1 Goals
- [ ] Additional material types based on community feedback
- [ ] More cross-mod integrations (Botania, Thermal, etc.)
- [ ] Advanced automation features
- [ ] Quality of life improvements
- [ ] Performance enhancements based on real-world usage

### Version 1.2 Goals
- [ ] Custom post shapes (triangular, hexagonal bases)
- [ ] Advanced wire physics simulation
- [ ] Integration with structure generation mods
- [ ] Multi-block post structures (towers, complex frames)

---

# ⚖️ Legal & Attribution

## License Framework
**Custom Open-Source License** based on discussion requirements

### Permitted Uses
- ✅ **Learning and Education**: Study code and concepts for educational purposes
- ✅ **Modpack Inclusion**: Include in public and private modpack distributions
- ✅ **Addon Development**: Create compatible addons and extensions
- ✅ **Community Contributions**: Submit improvements, translations, documentation
- ✅ **Non-Commercial Use**: Personal and server use without monetization
- ✅ **Asset Derivatives**: Create texture packs and resource pack variants

### Restricted Uses
- ❌ **Direct Redistribution**: Reupload or mirror without explicit permission
- ❌ **Commercial Forks**: Create competing commercial versions
- ❌ **Asset Extraction**: Use textures/models in other unrelated projects
- ❌ **Trademark Usage**: Use mod name or branding for derivative works
- ❌ **Closed Source Derivatives**: All modifications must maintain open licensing

### Attribution Requirements

#### Immersive Engineering Wire System
```java
/*
 * Wire network system adapted from Immersive Engineering
 * Original code by BluSunrize and Mr_Hazard
 * Licensed under BluSunrize's License (https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/LICENSE)
 * 
 * Adaptations and modifications for Modular Utility Posts by [Author]
 * Integration with cross-mod power systems and dynamic wire types
 */
```

#### In-Game Attribution
- Credits screen entry: "Wire system adapted from Immersive Engineering by BluSunrize"
- Tool tooltips: Wire-related tools acknowledge IE system where appropriate
- Mod description: Clear statement of IE integration and attribution

#### Documentation Attribution
- README.md: Prominent attribution section
- CurseForge/Modrinth: Acknowledgment in mod description
- Wiki: Dedicated attribution page with links to original projects

### Distribution Guidelines

#### Primary Platforms
- **CurseForge**: Primary distribution with full feature description
- **Modrinth**: Secondary platform with identical content
- **GitHub**: Source code, issue tracking, development builds

#### Revenue Sharing
- **Creator**: Retains 100% of CurseForge/Modrinth reward points
- **Contributors**: Recognition in credits, optional donation links
- **No Monetization Barriers**: No premium features or donation-locked content

#### Modpack Guidelines
```
Modpack Permission: OPEN
- Include in any modpack without requesting permission
- Modify configs and recipes as needed for pack balance
- Credit mod in modpack description/credits
- Link to official mod pages when possible

Redistribution Rules:
- Must maintain original mod files without modification
- Include attribution in pack documentation
- Do not claim creation or ownership of the mod
- Report issues to original mod, not modpack creators
```

### Community Guidelines

#### Contribution Standards
- **Code Quality**: Follow established coding patterns and documentation
- **Testing**: Ensure changes don't break existing functionality
- **Licensing**: All contributions subject to same license terms
- **Attribution**: Contributors credited in mod credits and documentation

#### Issue Reporting
- **GitHub Issues**: Primary bug tracking and feature requests
- **Discord Support**: Community help and discussion
- **No Guarantee**: Best-effort support, no SLA for fixes or features

#### Addon Development
- **API Stability**: Public APIs maintained across minor versions
- **Documentation**: Comprehensive javadocs for public interfaces
- **Examples**: Sample addon projects and tutorials provided
- **Compatibility**: Best effort to maintain addon compatibility

---

# 📞 Contact & Resources

## Development Resources
- **GitHub Repository**: `https://github.com/[username]/modular-utility-posts`
- **Issue Tracker**: GitHub Issues for bugs and feature requests
- **Wiki**: Comprehensive documentation and tutorials
- **API Docs**: Javadoc generation for public interfaces

## Community
- **Discord Server**: Real-time support and discussion
- **Reddit**: r/feedthebeast for broader community engagement
- **ModMaker**: Development community and collaboration

## External Dependencies
- **NeoForge**: 1.21.1 minimum version
- **Immersive Engineering**: Wire system integration (optional but recommended)
- **JEI/REI**: Recipe integration support
- **Waila/TOP/Jade**: Information display integration

---

*This document serves as the definitive reference for the Modular Utility Posts mod development. All implementation details should align with the specifications outlined here, while allowing for reasonable technical adaptations as development progresses.*

**Document Version**: 1.0  
**Last Updated**: [Current Date]  
**Next Review**: Phase 1 completion