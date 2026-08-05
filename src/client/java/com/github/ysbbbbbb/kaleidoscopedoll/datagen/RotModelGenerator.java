package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 生成玩偶 16 方向旋转模型。
 * <p>
 * vanilla 26.2 的 blockstate 旋转只支持 90 度象限，因此这里将每个玩偶模型
 * 按 22.5 度步进预旋转生成 16 个模型（rot_doll/&lt;doll&gt;/rot_&lt;n&gt;.json），
 * blockstate 直接引用对应的旋转模型。
 * <p>
 * 旋转通过每个元素的 "rotation" 复合实现：把模型的整体 Y 轴旋转与元素已有的
 * 单轴旋转复合为一个欧拉旋转（x/y/z），并求出等效旋转原点，保证几何精确。
 */
public class RotModelGenerator implements DataProvider {
    private static final Vector3f CENTER = new Vector3f(8.0f, 8.0f, 8.0f);
    private static final float EPSILON = 1e-4f;

    private final PackOutput.PathProvider modelPathProvider;

    public RotModelGenerator(FabricPackOutput output) {
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block/rot_doll");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        String sourceRoot = System.getProperty("kaleidoscope.sourceRoot", "");
        Path modelRoot = java.nio.file.Path.of(sourceRoot, "src", "main", "resources", "assets", "kaleidoscope_doll", "models", "block", "doll");
        List<CompletableFuture<?>> futures = new ArrayList<>();

        ModRegisterEvent.DOLL_BLOCKS.keySet().forEach(id -> {
            Path modelPath = modelRoot.resolve(id.getPath() + ".json");
            JsonObject baseModel;
            try (var reader = new InputStreamReader(java.nio.file.Files.newInputStream(modelPath), StandardCharsets.UTF_8)) {
                baseModel = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                KaleidoscopeDoll.LOGGER.error("Failed to read doll model: {}", modelPath, e);
                return;
            }

            for (int rotation = 0; rotation < 16; rotation++) {
                float angle = -rotation * 22.5f;
                JsonObject rotated;
                try {
                    rotated = rotateModel(baseModel, angle);
                } catch (Exception e) {
                    KaleidoscopeDoll.LOGGER.error("Failed to rotate doll model {} rot {}", id, rotation, e);
                    continue;
                }
                Path path = this.modelPathProvider.json(
                        Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/rot_" + rotation));
                futures.add(DataProvider.saveStable(cachedOutput, rotated, path));
            }
        });

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "Rotated Doll Models";
    }

    private JsonObject rotateModel(JsonObject baseModel, float angleDegrees) {
        Matrix3f modelRotation = new Matrix3f().rotationY((float) Math.toRadians(angleDegrees));
        JsonObject out = baseModel.deepCopy();
        JsonArray elements = GsonHelper.getAsJsonArray(out, "elements");

        for (JsonElement elementElement : elements) {
            JsonObject element = elementElement.getAsJsonObject();

            // 原始元素变换
            JsonObject rotationJson = element.has("rotation") ? element.getAsJsonObject("rotation") : null;
            Matrix3f elementRotation;
            Vector3f origin;
            if (rotationJson == null) {
                elementRotation = new Matrix3f();
                origin = new Vector3f();
            } else {
                origin = readVector3f(rotationJson, "origin");
                if (rotationJson.has("axis")) {
                    String axis = GsonHelper.getAsString(rotationJson, "axis");
                    float angle = (float) Math.toRadians(GsonHelper.getAsFloat(rotationJson, "angle"));
                    elementRotation = switch (axis) {
                        case "x" -> new Matrix3f().rotationX(angle);
                        case "y" -> new Matrix3f().rotationY(angle);
                        case "z" -> new Matrix3f().rotationZ(angle);
                        default -> throw new IllegalArgumentException("Invalid rotation axis: " + axis);
                    };
                } else {
                    float x = (float) Math.toRadians(GsonHelper.getAsFloat(rotationJson, "x", 0));
                    float y = (float) Math.toRadians(GsonHelper.getAsFloat(rotationJson, "y", 0));
                    float z = (float) Math.toRadians(GsonHelper.getAsFloat(rotationJson, "z", 0));
                    elementRotation = new Matrix3f().rotationZYX(z, y, x);
                }
            }

            // 复合旋转: R = Rm * Re
            Matrix3f composed = modelRotation.mul(elementRotation, new Matrix3f());

            // 平移: t = C + Rm*Oe - R*Oe - Rm*C
            Vector3f rmOe = modelRotation.transform(origin, new Vector3f());
            Vector3f rOe = composed.transform(origin, new Vector3f());
            Vector3f rmC = modelRotation.transform(CENTER, new Vector3f());
            Vector3f t = new Vector3f(CENTER).add(rmOe).sub(rOe).sub(rmC);

            if (isIdentity(composed)) {
                // 纯平移: 直接平移 from/to
                if (rotationJson != null) {
                    element.remove("rotation");
                }
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");
                translate(from, t);
                translate(to, t);
                continue;
            }

            // O' = (I - R)^-1 * t
            Matrix3f ir = new Matrix3f().identity().sub(composed, new Matrix3f());
            Vector3f newOrigin = ir.invert().transform(t, new Vector3f());

            // 欧拉角分解 (vanilla: rotationZYX(z, y, x))
            Vector3f euler = composed.getEulerAnglesZYX(new Vector3f());

            JsonObject newRotation = new JsonObject();
            newRotation.add("origin", writeVector3f(newOrigin));
            newRotation.addProperty("x", (float) Math.toDegrees(euler.x));
            newRotation.addProperty("y", (float) Math.toDegrees(euler.y));
            newRotation.addProperty("z", (float) Math.toDegrees(euler.z));

            if (rotationJson == null) {
                element.add("rotation", newRotation);
            } else {
                element.remove("rotation");
                element.add("rotation", newRotation);
            }
        }
        return out;
    }

    private boolean isIdentity(Matrix3f matrix) {
        Matrix3f diff = new Matrix3f().identity().sub(matrix, new Matrix3f());
        float length = diff.m00() * diff.m00() + diff.m01() * diff.m01() + diff.m02() * diff.m02()
                + diff.m10() * diff.m10() + diff.m11() * diff.m11() + diff.m12() * diff.m12()
                + diff.m20() * diff.m20() + diff.m21() * diff.m21() + diff.m22() * diff.m22();
        return length < EPSILON * EPSILON;
    }

    private Vector3f readVector3f(JsonObject object, String key) {
        JsonArray array = GsonHelper.getAsJsonArray(object, key);
        return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private JsonArray writeVector3f(Vector3f vector) {
        JsonArray array = new JsonArray();
        array.add(new com.google.gson.JsonPrimitive(round(vector.x)));
        array.add(new com.google.gson.JsonPrimitive(round(vector.y)));
        array.add(new com.google.gson.JsonPrimitive(round(vector.z)));
        return array;
    }

    private float round(float value) {
        return Math.round(value * 1000.0f) / 1000.0f;
    }

    private void translate(JsonArray pos, Vector3f t) {
        pos.set(0, new com.google.gson.JsonPrimitive(round(pos.get(0).getAsFloat() + t.x)));
        pos.set(1, new com.google.gson.JsonPrimitive(round(pos.get(1).getAsFloat() + t.y)));
        pos.set(2, new com.google.gson.JsonPrimitive(round(pos.get(2).getAsFloat() + t.z)));
    }
}
