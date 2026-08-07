package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 生成玩偶 16 方向旋转模型。
 *
 * <p>vanilla 26.2 的 blockstate 旋转只支持 90 度象限,因此这里把每个玩偶模型按
 * 22.5 度步进预旋转,生成 rot_doll/&lt;doll&gt;/rot_&lt;n&gt;.json,blockstate 直接引用。
 *
 * <p>旋转烘焙进每个元素:源元素的自身旋转与"绕模型中心 (8,8,8) 旋转 -N*22.5°"复合为
 * 矩阵 R,再把平移量 Δ=Rᵀt 写进 from/to,rotation 统一绕原点 (0,0,0) 以欧拉角
 * (x/y/z,组装顺序 Rz·Ry·Rx,即 vanilla rotationZYX(z,y,x)) 表示。全程双精度计算,
 * 每个元素无条件输出 rotation(包括恒等),不做阈值分支,避免万向锁退化与
 * EPSILON 恒等近似带来的几何误差。
 */
public class RotModelGenerator implements DataProvider {
    private static final double[] CENTER = {8.0, 8.0, 8.0};
    private static final double MIN_EXTENT = -16.0;
    private static final double MAX_EXTENT = 32.0;
    /** 位置/角度输出保留 4 位小数,单值误差 <= 1e-4,远小于几何验证容差 1e-3。 */
    private static final double ROUND = 10000.0;

    private final PackOutput.PathProvider modelPathProvider;

    public RotModelGenerator(FabricPackOutput output) {
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block/rot_doll");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        String sourceRoot = System.getProperty("kaleidoscope.sourceRoot", "");
        Path modelRoot = java.nio.file.Path.of(sourceRoot, "src", "main", "resources", "assets",
                "kaleidoscope_doll", "models", "block", "doll");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        ModRegisterEvent.DOLL_BLOCKS.keySet().forEach(id -> {
            Path modelPath = modelRoot.resolve(id.getPath() + ".json");
            JsonObject baseModel;
            try (var reader = new InputStreamReader(java.nio.file.Files.newInputStream(modelPath), StandardCharsets.UTF_8)) {
                baseModel = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                KaleidoscopeDoll.LOGGER.error("Failed to read doll model: {}", modelPath, e);
                failures.add(id.getPath() + " (read)");
                return;
            }

            int[] textureSize = readTextureSize(modelPath);

            for (int rotation = 0; rotation < 16; rotation++) {
                try {
                    JsonObject rotated = rotateModel(baseModel, -rotation * 22.5, textureSize[0], textureSize[1]);
                    Path path = this.modelPathProvider.json(
                            Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/rot_" + rotation));
                    futures.add(DataProvider.saveStable(cachedOutput, rotated, path));
                } catch (Exception e) {
                    KaleidoscopeDoll.LOGGER.error("Failed to rotate doll model {} rot {}", id, rotation, e);
                    failures.add(id.getPath() + "/rot_" + rotation);
                }
            }
        });

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Rotated doll model generation failed for " + failures.size()
                    + " cases: " + failures.subList(0, Math.min(10, failures.size())));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "Rotated Doll Models";
    }

    private JsonObject rotateModel(JsonObject baseModel, double angleDegrees, int textureWidth, int textureHeight) {
        double[][] modelRotation = rotationY(Math.toRadians(angleDegrees));
        JsonObject out = baseModel.deepCopy();
        JsonArray elements = GsonHelper.getAsJsonArray(out, "elements");

        String mainTextureKey = "#1";
        if (out.has("textures")) {
            for (String key : out.getAsJsonObject("textures").keySet()) {
                if (!"particle".equals(key)) {
                    mainTextureKey = "#" + key;
                    break;
                }
            }
        }

        for (JsonElement elementElement : elements) {
            JsonObject element = elementElement.getAsJsonObject();

            // 清理脏数据: #missing 引用替换为主纹理, UV clamp 到贴图尺寸
            if (element.has("faces")) {
                for (Map.Entry<String, JsonElement> faceEntry : element.getAsJsonObject("faces").entrySet()) {
                    JsonObject face = faceEntry.getValue().getAsJsonObject();
                    if ("#missing".equals(GsonHelper.getAsString(face, "texture", ""))) {
                        face.addProperty("texture", mainTextureKey);
                    }
                    if (face.has("uv")) {
                        JsonArray uv = face.getAsJsonArray("uv");
                        for (int i = 0; i < uv.size(); i++) {
                            double max = (i % 2 == 0) ? textureWidth : textureHeight;
                            uv.set(i, new JsonPrimitive((float) clamp(uv.get(i).getAsDouble(), 0.0, max)));
                        }
                    }
                }
            }

            // 源元素旋转 Re 与旋转原点 Oe
            JsonObject rotationJson = element.has("rotation") ? element.getAsJsonObject("rotation") : null;
            double[][] elementRotation;
            double[] origin;
            if (rotationJson == null) {
                elementRotation = identity();
                origin = new double[]{0, 0, 0};
            } else {
                origin = readVector(rotationJson, "origin");
                if (rotationJson.has("axis")) {
                    String axis = GsonHelper.getAsString(rotationJson, "axis");
                    double angle = Math.toRadians(GsonHelper.getAsFloat(rotationJson, "angle"));
                    elementRotation = switch (axis) {
                        case "x" -> rotationX(angle);
                        case "y" -> rotationY(angle);
                        case "z" -> rotationZ(angle);
                        default -> throw new IllegalArgumentException("Invalid rotation axis: " + axis);
                    };
                } else {
                    double x = Math.toRadians(GsonHelper.getAsFloat(rotationJson, "x", 0));
                    double y = Math.toRadians(GsonHelper.getAsFloat(rotationJson, "y", 0));
                    double z = Math.toRadians(GsonHelper.getAsFloat(rotationJson, "z", 0));
                    elementRotation = rotationZYX(z, y, x);
                }
            }

            // 复合旋转 R = Rm * Re
            double[][] composed = mul(modelRotation, elementRotation);

            // t = C + Rm*Oe - R*Oe - Rm*C; Δ = Rᵀ*t; 渲染 p' = R*(p+Δ) 绕原点
            double[] rmOe = matVec(modelRotation, origin);
            double[] rOe = matVec(composed, origin);
            double[] rmC = matVec(modelRotation, CENTER);
            double[] t = add3(sub3(add3(CENTER, rmOe), rOe), neg3(rmC));
            double[] delta = matVec(transpose(composed), t);

            double[] euler = decomposeZYX(composed);
            JsonObject newRotation = new JsonObject();
            newRotation.add("origin", writeVector(new double[]{0, 0, 0}));
            newRotation.add("x", new JsonPrimitive((float) round4(Math.toDegrees(euler[0]))));
            newRotation.add("y", new JsonPrimitive((float) round4(Math.toDegrees(euler[1]))));
            newRotation.add("z", new JsonPrimitive((float) round4(Math.toDegrees(euler[2]))));
            if (rotationJson == null) {
                element.add("rotation", newRotation);
            } else {
                element.remove("rotation");
                element.add("rotation", newRotation);
            }

            translate(element.getAsJsonArray("from"), delta);
            translate(element.getAsJsonArray("to"), delta);
        }

        translateIntoBounds(elements);
        return out;
    }

    private int[] readTextureSize(Path modelPath) {
        String name = modelPath.getFileName().toString().replace(".json", ".png");
        Path texturePath = modelPath.resolveSibling("../../../../textures/block/doll/" + name).normalize();
        try {
            byte[] header = new byte[24];
            try (var in = java.nio.file.Files.newInputStream(texturePath)) {
                int read = in.readNBytes(header, 0, 24);
                if (read == 24 && header[0] == (byte) 0x89 && header[12] == 'I' && header[13] == 'H' && header[14] == 'D' && header[15] == 'R') {
                    int width = ((header[16] & 0xFF) << 24) | ((header[17] & 0xFF) << 16) | ((header[18] & 0xFF) << 8) | (header[19] & 0xFF);
                    int height = ((header[20] & 0xFF) << 24) | ((header[21] & 0xFF) << 16) | ((header[22] & 0xFF) << 8) | (header[23] & 0xFF);
                    return new int[]{width, height};
                }
            }
        } catch (Exception ignored) {
        }
        return new int[]{64, 64};
    }

    /**
     * 把全部元素平移到 [-16, 32] 内(from/to 硬性约束,越界会让 CuboidModel 解析失败)。
     * from/to 与 rotation origin 施加同一平移,因此渲染结果只多一个全局位移,形状不变。
     */
    private void translateIntoBounds(JsonArray elements) {
        double[] min = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (JsonElement elementElement : elements) {
            JsonObject element = elementElement.getAsJsonObject();
            minMax(element.getAsJsonArray("from"), min, max);
            minMax(element.getAsJsonArray("to"), min, max);
        }

        double[] shift = new double[3];
        for (int i = 0; i < 3; i++) {
            if (min[i] < MIN_EXTENT) {
                shift[i] = MIN_EXTENT - min[i];
            } else if (max[i] > MAX_EXTENT) {
                shift[i] = MAX_EXTENT - max[i];
            }
        }
        if (shift[0] == 0 && shift[1] == 0 && shift[2] == 0) {
            return;
        }

        for (JsonElement elementElement : elements) {
            JsonObject element = elementElement.getAsJsonObject();
            translate(element.getAsJsonArray("from"), shift);
            translate(element.getAsJsonArray("to"), shift);
            if (element.has("rotation")) {
                JsonObject rotation = element.getAsJsonObject("rotation");
                double[] origin = readVector(rotation, "origin");
                for (int i = 0; i < 3; i++) {
                    origin[i] += shift[i];
                }
                rotation.add("origin", writeVector(origin));
            }
        }

        // 平移后仍越界(同轴跨度 > 48)无法修复,直接失败,由 run() 汇总报错
        for (JsonElement elementElement : elements) {
            JsonObject element = elementElement.getAsJsonObject();
            for (String key : List.of("from", "to")) {
                double[] v = readVector(element, key);
                for (int i = 0; i < 3; i++) {
                    if (v[i] < MIN_EXTENT || v[i] > MAX_EXTENT) {
                        throw new IllegalArgumentException(
                                "Element still out of bounds [-16, 32] after recentering: " + key + "=" + Arrays.toString(v));
                    }
                }
            }
        }
    }

    // ---------- 双精度矩阵数学 ----------

    /** Rz·Ry·Rx(与 vanilla rotationZYX(z,y,x) 一致,列向量先 X 后 Y 后 Z)。 */
    private static double[][] rotationZYX(double z, double y, double x) {
        double cx = Math.cos(x), sx = Math.sin(x);
        double cy = Math.cos(y), sy = Math.sin(y);
        double cz = Math.cos(z), sz = Math.sin(z);
        return new double[][]{
                {cy * cz, sx * sy * cz - cx * sz, cx * sy * cz + sx * sz},
                {cy * sz, sx * sy * sz + cx * cz, cx * sy * sz - sx * cz},
                {-sy, sx * cy, cx * cy}
        };
    }

    private static double[][] rotationX(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{{1, 0, 0}, {0, c, -s}, {0, s, c}};
    }

    private static double[][] rotationY(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{{c, 0, s}, {0, 1, 0}, {-s, 0, c}};
    }

    private static double[][] rotationZ(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[][]{{c, -s, 0}, {s, c, 0}, {0, 0, 1}};
    }

    private static double[][] identity() {
        return new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    }

    private static double[][] mul(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double s = 0;
                for (int k = 0; k < 3; k++) {
                    s += a[i][k] * b[k][j];
                }
                r[i][j] = s;
            }
        }
        return r;
    }

    private static double[][] transpose(double[][] m) {
        return new double[][]{
                {m[0][0], m[1][0], m[2][0]},
                {m[0][1], m[1][1], m[2][1]},
                {m[0][2], m[1][2], m[2][2]}
        };
    }

    private static double[] matVec(double[][] m, double[] v) {
        return new double[]{
                m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
                m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
                m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]
        };
    }

    private static double[] add3(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double[] sub3(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[] neg3(double[] a) {
        return new double[]{-a[0], -a[1], -a[2]};
    }

    /**
     * 与 Rz·Ry·Rx 组装匹配的欧拉分解(双精度):常规 y=asin(-R20), x=atan2(R21,R22),
     * z=atan2(R10,R00);万向锁 |y|=90° 时设 z=0, x=atan2(±R01,R11)(y>0 取 +)。
     */
    private static double[] decomposeZYX(double[][] r) {
        double sy = -r[2][0];
        double y = Math.asin(clamp(sy, -1.0, 1.0));
        double x;
        double z;
        if (Math.abs(sy) < 1.0 - 1e-12) {
            x = Math.atan2(r[2][1], r[2][2]);
            z = Math.atan2(r[1][0], r[0][0]);
        } else {
            z = 0;
            x = sy > 0 ? Math.atan2(r[0][1], r[1][1]) : Math.atan2(-r[0][1], r[1][1]);
        }
        return new double[]{x, y, z};
    }

    // ---------- JSON 辅助 ----------

    private static void minMax(JsonArray pos, double[] min, double[] max) {
        for (int i = 0; i < 3; i++) {
            double v = pos.get(i).getAsDouble();
            if (v < min[i]) min[i] = v;
            if (v > max[i]) max[i] = v;
        }
    }

    private static double[] readVector(JsonObject object, String key) {
        JsonArray array = GsonHelper.getAsJsonArray(object, key);
        return new double[]{
                array.get(0).getAsDouble(),
                array.get(1).getAsDouble(),
                array.get(2).getAsDouble()
        };
    }

    private static JsonArray writeVector(double[] vector) {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive((float) round4(vector[0])));
        array.add(new JsonPrimitive((float) round4(vector[1])));
        array.add(new JsonPrimitive((float) round4(vector[2])));
        return array;
    }

    private static void translate(JsonArray pos, double[] t) {
        for (int i = 0; i < 3; i++) {
            pos.set(i, new JsonPrimitive((float) round4(pos.get(i).getAsDouble() + t[i])));
        }
    }

    private static double round4(double value) {
        return Math.round(value * ROUND) / ROUND;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
