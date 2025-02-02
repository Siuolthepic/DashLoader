package net.quantumfusion.dashloader.cache;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.BitmapFont;
import net.minecraft.client.font.BlankFont;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.UnicodeTextureFont;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.quantumfusion.dashloader.DashLoader;
import net.quantumfusion.dashloader.cache.atlas.DashImage;
import net.quantumfusion.dashloader.cache.atlas.DashSprite;
import net.quantumfusion.dashloader.cache.blockstates.DashBlockState;
import net.quantumfusion.dashloader.cache.blockstates.properties.DashProperty;
import net.quantumfusion.dashloader.cache.blockstates.properties.value.DashPropertyValue;
import net.quantumfusion.dashloader.cache.font.fonts.DashBitmapFont;
import net.quantumfusion.dashloader.cache.font.fonts.DashBlankFont;
import net.quantumfusion.dashloader.cache.font.fonts.DashFont;
import net.quantumfusion.dashloader.cache.font.fonts.DashUnicodeFont;
import net.quantumfusion.dashloader.cache.models.DashModel;
import net.quantumfusion.dashloader.cache.models.DashModelIdentifier;
import net.quantumfusion.dashloader.cache.models.ModelStage;
import net.quantumfusion.dashloader.cache.models.factory.DashModelFactory;
import net.quantumfusion.dashloader.cache.models.predicates.DashPredicate;
import net.quantumfusion.dashloader.cache.models.predicates.PredicateHelper;
import net.quantumfusion.dashloader.mixin.NativeImageAccessor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class DashRegistry {


    @Serialize(order = 0)
    @SerializeNullable(path = {1})
    @SerializeNullable(path = {0})
    public Map<Long, DashBlockState> blockstates;


    @Serialize(order = 1)
    @SerializeNullable(path = {1})
    @SerializeNullable(path = {0})
    public Map<Long, DashSprite> sprites;


    @Serialize(order = 2)
    @SerializeSubclasses(path = {1}, value = {
            DashIdentifier.class,
            DashModelIdentifier.class
    })
    @SerializeNullable(path = {0})
    public Map<Long, DashID> identifiers;

    @Serialize(order = 3)
    @SerializeNullable(path = {0})
    @SerializeSubclasses(path = {1}, extraSubclassesId = "models")
    public Map<Long, DashModel> models;

    @Serialize(order = 4)
    @SerializeNullable(path = {0})
    @SerializeSubclasses(path = {1}, extraSubclassesId = "fonts")
    public Map<Long, DashFont> fonts;

    @Serialize(order = 5)
    @SerializeNullable(path = {0})
    @SerializeNullable(path = {1})
    public Map<Long, DashImage> images;

    @Serialize(order = 6)
    @SerializeNullable(path = {0})
    @SerializeSubclasses(path = {1}, extraSubclassesId = "predicates")
    public Map<Long, DashPredicate> predicates;


    @Serialize(order = 7)
    @SerializeNullable(path = {0})
    @SerializeSubclasses(path = {1}, extraSubclassesId = "properties")
    public Map<Long, DashProperty> properties;


    @Serialize(order = 8)
    @SerializeNullable(path = {0})
    @SerializeSubclasses(path = {1}, extraSubclassesId = "values")
    public Map<Long, DashPropertyValue> propertyValues;

    public List<Integer> failedPredicates = Collections.synchronizedList(new ArrayList<>());


    public Map<Class, Integer> modelsFailed = new ConcurrentHashMap<>();
    public Map<Long, BlockState> blockstatesOut;
    public Map<Long, Predicate<BlockState>> predicateOut;
    public Map<Long, Identifier> identifiersOut;
    public Map<Long, BakedModel> modelsOut;
    public Map<Long, Sprite> spritesOut;
    public Map<Long, Font> fontsOut;
    public Map<Long, NativeImage> imagesOut;
    public Map<Long, Property<?>> propertiesOut;
    public Map<Long, Comparable<?>> propertyValuesOut;
    DashLoader loader;

    public DashRegistry(@Deserialize("blockstates") Map<Long, DashBlockState> blockstates,
                        @Deserialize("sprites") Map<Long, DashSprite> sprites,
                        @Deserialize("identifiers") Map<Long, DashID> identifiers,
                        @Deserialize("models") Map<Long, DashModel> models,
                        @Deserialize("fonts") Map<Long, DashFont> fonts,
                        @Deserialize("images") Map<Long, DashImage> images,
                        @Deserialize("predicates") Map<Long, DashPredicate> predicates,
                        @Deserialize("properties") Map<Long, DashProperty> properties,
                        @Deserialize("propertyValues") Map<Long, DashPropertyValue> propertyValues) {
        this.blockstates = blockstates;
        this.sprites = sprites;
        this.identifiers = identifiers;
        this.models = models;
        this.fonts = fonts;
        this.images = images;
        this.predicates = predicates;
        this.properties = properties;
        this.propertyValues = propertyValues;
    }

    public DashRegistry(DashLoader loader) {
        blockstates = new HashMap<>();
        sprites = new HashMap<>();
        identifiers = new HashMap<>();
        models = new HashMap<>();
        fonts = new HashMap<>();
        predicates = new HashMap<>();
        images = new HashMap<>();
        properties = new HashMap<>();
        propertyValues = new HashMap<>();
        this.loader = loader;
    }


    public long createBlockStatePointer(BlockState blockState) {
        final long hash = blockState.hashCode();
        if (blockstates.get(hash) == null) {
            blockstates.put(hash, new DashBlockState(blockState, this));
        }
        return hash;
    }

    public final <K> Long createModelPointer(final BakedModel bakedModel, @Nullable K var) {
        if (bakedModel == null) {
            return null;
        }
        final long hash = bakedModel.hashCode();
        if (models.get(hash) == null) {
            DashModelFactory model = loader.modelMappings.get(bakedModel.getClass());
            if (model != null) {
                models.put(hash, model.toDash(bakedModel, this, var));
            } else {
                Integer integer = modelsFailed.get(bakedModel.getClass());
                if (integer != null) {
                    integer += 1;
                } else {
                    modelsFailed.put(bakedModel.getClass(), 0);

                }
            }
        }
        return hash;
    }

    public final long createSpritePointer(final Sprite sprite) {
        final long hash = sprite.hashCode();
        if (sprites.get(hash) == null) {
            sprites.put(hash, new DashSprite(sprite, this));
        }
        return hash;
    }

    public final long createIdentifierPointer(final Identifier identifier) {
        final long hash = identifier.hashCode();
        if (identifiers.get(hash) == null) {
            if (identifier instanceof ModelIdentifier) {
                identifiers.put(hash, new DashModelIdentifier((ModelIdentifier) identifier));
            } else {
                identifiers.put(hash, new DashIdentifier(identifier));
            }
        }
        return hash;
    }

    public final long createImagePointer(final NativeImage image) {
        final long hash = ((NativeImageAccessor) (Object) image).getPointer();
        if (images.get(hash) == null) {
            images.put(hash, new DashImage(image));
        }
        return hash;
    }

    public final long createPredicatePointer(final MultipartModelSelector selector, final StateManager<Block, BlockState> stateManager) {
        final long hash = selector.hashCode();
        if (predicates.get(hash) == null) {
            predicates.put(hash, PredicateHelper.getPredicate(selector, stateManager, this));
        }
        return hash;
    }

    public final long createFontPointer(final Font font) {
        final long hash = font.hashCode();
        if (fonts.get(hash) == null) {
            if (font instanceof BitmapFont) {
                fonts.put(hash, new DashBitmapFont((BitmapFont) font, this));
            } else if (font instanceof UnicodeTextureFont) {
                fonts.put(hash, new DashUnicodeFont((UnicodeTextureFont) font, this));
            } else if (font instanceof BlankFont) {
                fonts.put(hash, new DashBlankFont());
            } else {
                DashLoader.LOGGER.warn(font.getClass().getName() + " is not a supported font format, please contact mod developer to add support.");
            }
        }
        return hash;
    }

    public final Pair<Long, Long> createPropertyPointer(final Property<?> property, final Comparable<?> value) {
        final long hashV = value.hashCode();
        final long hashP = property.hashCode();
        propertyValues.putIfAbsent(hashV, PredicateHelper.getPropertyValue(value, hashP));
        properties.putIfAbsent(hashP, PredicateHelper.getProperty(property));
        return Pair.of(hashP, hashV);
    }


    public final BlockState getBlockstate(final Long pointer) {
        final BlockState blockstate = blockstatesOut.get(pointer);
        if (blockstate == null) {
            DashLoader.LOGGER.error("Blockstate not found in data. PINTR: " + pointer);
        }
        return blockstate;
    }

    public final Sprite getSprite(final Long pointer) {
        final Sprite sprite = spritesOut.get(pointer);
        if (sprite == null) {
            DashLoader.LOGGER.error("Sprite not found in data. PINTR: " + pointer);
        }
        return sprite;
    }

    public final Identifier getIdentifier(final Long pointer) {
        final Identifier identifier = identifiersOut.get(pointer);
        if (identifier == null) {
            DashLoader.LOGGER.error("Identifier not found in data. PINTR: " + pointer);
        }
        return identifier;
    }

    public final BakedModel getModel(final Long pointer) {
        final BakedModel bakedModel = modelsOut.get(pointer);
        if (bakedModel == null) {
            DashLoader.LOGGER.error("Model not found in data. PINTR: " + pointer);
        }
        return bakedModel;
    }

    public final Font getFont(final Long pointer) {
        final Font font = fontsOut.get(pointer);
        if (font == null) {
            DashLoader.LOGGER.error("Font not found in data. PINTR: " + pointer);
        }
        return font;
    }

    public final NativeImage getImage(final Long pointer) {
        final NativeImage image = imagesOut.get(pointer);
        if (image == null) {
            DashLoader.LOGGER.error("NativeImage not found in data. PINTR: " + pointer);
        }
        return image;
    }

    public final Predicate<BlockState> getPredicate(final Long pointer) {
        final Predicate<BlockState> predicate = predicateOut.get(pointer);
        if (predicate == null) {
            DashLoader.LOGGER.error("Predicate not found in data. PINTR: " + pointer);
        }
        return predicateOut.get(pointer);
    }

    public final Pair<Property<?>, Comparable<?>> getProperty(final Long propertyPointer, final Long valuePointer) {
        final Property<?> property = propertiesOut.get(propertyPointer);
        final Comparable<?> value = propertyValuesOut.get(valuePointer);
        if (property == null && value == null) {
            DashLoader.LOGGER.error("Property not found in data. PINTR: " + propertyPointer + "/" + valuePointer);
        }
        return Pair.of(property, value);
    }

    public void toUndash() {
        Logger logger = LogManager.getLogger();

        spritesOut = new ConcurrentHashMap<>();
        blockstatesOut = new ConcurrentHashMap<>();
        predicateOut = new ConcurrentHashMap<>();
        identifiersOut = new ConcurrentHashMap<>();
        imagesOut = new ConcurrentHashMap<>();
        modelsOut = new ConcurrentHashMap<>();
        fontsOut = new ConcurrentHashMap<>();
        propertiesOut = new ConcurrentHashMap<>();
        propertyValuesOut = new ConcurrentHashMap<>();

        logger.info("[1/11] Loading Identifiers");
        identifiers.entrySet().parallelStream().forEach(identifierEntry -> identifiersOut.put(identifierEntry.getKey(), identifierEntry.getValue().toUndash()));
        identifiers = null;

        logger.info("[2/11] Loading Images");
        images.entrySet().parallelStream().forEach(imageEntry -> imagesOut.put(imageEntry.getKey(), imageEntry.getValue().toUndash()));
        images = null;

        logger.info("[3/11] Loading Properties");
        properties.entrySet().parallelStream().forEach(entry -> propertiesOut.put(entry.getKey(), entry.getValue().toUndash()));
        propertyValues.entrySet().parallelStream().forEach(entry -> propertyValuesOut.put(entry.getKey(), entry.getValue().toUndash(this)));
        properties = null;
        propertyValues = null;

        logger.info("[4/11] Loading Blockstates");
        blockstates.entrySet().parallelStream().forEach(blockstateEntry -> blockstatesOut.put(blockstateEntry.getKey(), blockstateEntry.getValue().toUndash(this)));
        blockstates = null;

        logger.info("[5/11] Loading Predicates");
        predicates.entrySet().parallelStream().forEach(predicateEntry -> {
            predicateOut.put(predicateEntry.getKey(), predicateEntry.getValue().toUndash(this));
        });
        predicates = null;

        logger.info("[6/11] Loading Sprites");
        sprites.entrySet().parallelStream().forEach(spriteEntry -> spritesOut.put(spriteEntry.getKey(), spriteEntry.getValue().toUndash(this)));
        sprites = null;


        logger.info("[7/11] Loading Fonts");
        fonts.entrySet().parallelStream().forEach(fontEntry -> fontsOut.put(fontEntry.getKey(), fontEntry.getValue().toUndash(this)));
        fonts = null;

        logger.info("[8/11] Loading Simple Models");
        models.entrySet().parallelStream().forEach(modelEntry -> {
            final DashModel value = modelEntry.getValue();
            if (value.getStage() == ModelStage.SIMPLE) {
                modelsOut.put(modelEntry.getKey(), value.toUndash(this));
            }
        });

        logger.info("[9/11] Loading Intermediate Models");
        models.entrySet().parallelStream().forEach(modelEntry -> {
            final DashModel value = modelEntry.getValue();
            if (value.getStage() == ModelStage.INTERMEDIATE) {
                modelsOut.put(modelEntry.getKey(), value.toUndash(this));
            }
        });

        logger.info("[10/11] Loading Advanced Models");
        models.entrySet().parallelStream().forEach(modelEntry -> {
            final DashModel value = modelEntry.getValue();
            if (value.getStage() == ModelStage.ADVANCED) {
                modelsOut.put(modelEntry.getKey(), value.toUndash(this));
            }
        });

        logger.info("[11/11] Applying Model Overrides");
        models.values().forEach((model) -> model.apply(this));
        models = null;
    }

}
