package com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock;

import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.model.BedrockCube;
import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.model.BedrockCubeBox;
import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.model.BedrockCubePerFace;
import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.model.BedrockPart;
import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.pojo.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 将基岩版实体模型文件读取为 Java 版的 net.minecraft.client.model.Model 模型，此类和 AbstractBedrockEntityModel 一样
 * <p>
 * 但由于 net.minecraft.client.model.EntityModel 和 net.minecraft.client.model.Model 是继承关系，无法复用，故重复代码
 */
public class BedrockModel {
    /**
     * 存储 BedrockPart 的 HashMap
     */
    protected final HashMap<String, BedrockPart> modelMap = new HashMap<>();
    /**
     * 存储 Bones 的 HashMap，主要是给后面寻找父骨骼进行坐标转换用的
     */
    protected final HashMap<String, BonesItem> indexBones = new HashMap<>();
    /**
     * 哪些模型需要渲染。加载进父骨骼的子骨骼是不需要渲染的
     */
    protected final List<BedrockPart> shouldRender = new LinkedList<>();
    /**
     * 模型的 AABB
     */
    protected AABB renderBoundingBox;
    /**
     * 模型实际渲染范围（懒计算缓存），见 {@link #getActualRenderBoundingBox()}
     */
    private AABB actualRenderBoundingBox;
    /**
     * 模型的 identifier，用于区分不同模型
     */
    protected String identifier;

    public BedrockModel(InputStream stream)  {
        BedrockModelPOJO pojo = BedrockModelUtil.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), BedrockModelPOJO.class);
        if (BedrockVersion.isLegacyVersion(pojo)) {
            throw new IllegalArgumentException("Legacy bedrock model version is not supported!");
        }
        if (BedrockVersion.isNewVersion(pojo)) {
            readModel(pojo);
        }
    }

    public BedrockModel(BedrockModelPOJO pojo)  {
        this(pojo, BedrockVersion.getVersion(pojo));
    }

    public BedrockModel(BedrockModelPOJO pojo, BedrockVersion version)  {
        if (version == BedrockVersion.LEGACY) {
            throw new IllegalArgumentException("Legacy bedrock model version is not supported!");
        }
        if (version == BedrockVersion.NEW) {
            readModel(pojo);
        }
    }

    public BedrockModel() {
        renderBoundingBox = new AABB(-1, 0, -1, 1, 2, 1);
    }

    protected void readModel(BedrockModelPOJO pojo) {
        assert pojo.getFirstGeometryModel() != null;
        pojo.getFirstGeometryModel().deco();

        Description description = pojo.getFirstGeometryModel().getDescription();
        // 模型的 identifier
        identifier = description.getIdentifier();
        // 材质的长度、宽度
        int texWidth = description.getTextureWidth();
        int texHeight = description.getTextureHeight();

        float[] offset = description.getVisibleBoundsOffset();
        float offsetX = offset[0];
        float offsetY = offset[1];
        float offsetZ = offset[2];
        float width = description.getVisibleBoundsWidth() / 2.0f;
        float height = description.getVisibleBoundsHeight() / 2.0f;
        renderBoundingBox = new AABB(offsetX - width, offsetY - height, offsetZ - width, offsetX + width, offsetY + height, offsetZ + width);

        // 往 indexBones 里面注入数据，为后续坐标转换做参考
        for (BonesItem bones : pojo.getFirstGeometryModel().getBones()) {
            // 塞索引，这是给后面坐标转换用的
            indexBones.put(bones.getName(), bones);
            // 塞入新建的空 BedrockPart 实例
            // 因为后面添加 parent 需要，所以先塞空对象，然后二次遍历再进行数据存储
            modelMap.put(bones.getName(), new BedrockPart());
        }

        // 开始往 ModelRenderer 实例里面塞数据
        for (BonesItem bones : pojo.getFirstGeometryModel().getBones()) {
            // 骨骼名称，注意因为后面动画的需要，头部、手部、腿部等骨骼命名必须是固定死的
            String name = bones.getName();
            // 旋转点，可能为空
            @Nullable float[] rotation = bones.getRotation();
            // 父骨骼的名称，可能为空
            @Nullable String parent = bones.getParent();
            // 塞进 HashMap 里面的模型对象
            BedrockPart model = modelMap.get(name);

            // 镜像参数
            model.mirror = bones.isMirror();

            // 旋转点
            model.setPos(convertPivot(bones, 0), convertPivot(bones, 1), convertPivot(bones, 2));

            // Nullable 检查，设置旋转角度
            if (rotation != null) {
                setRotationAngle(model, convertRotation(rotation[0]), convertRotation(rotation[1]), convertRotation(rotation[2]));
            }

            // Null 检查，进行父骨骼绑定
            if (parent != null) {
                modelMap.get(parent).addChild(model);
            } else {
                // 没有父骨骼的模型才进行渲染
                shouldRender.add(model);
            }

            // 我的天，Cubes 还能为空……
            if (bones.getCubes() == null) {
                continue;
            }

            // 塞入 Cube List
            for (CubesItem cube : bones.getCubes()) {
                float[] uv = cube.getUv();
                @Nullable FaceUVsItem faceUv = cube.getFaceUv();
                float[] size = cube.getSize();
                @Nullable float[] cubeRotation = cube.getRotation();
                boolean mirror = cube.isMirror();
                float inflate = cube.getInflate();

                // 当做普通 cube 存入
                if (cubeRotation == null) {
                    if (faceUv == null) {
                        model.cubes.add(new BedrockCubeBox(uv[0], uv[1],
                                convertOrigin(bones, cube, 0), convertOrigin(bones, cube, 1), convertOrigin(bones, cube, 2),
                                size[0], size[1], size[2], inflate, mirror,
                                texWidth, texHeight));
                    } else {
                        model.cubes.add(new BedrockCubePerFace(
                                convertOrigin(bones, cube, 0), convertOrigin(bones, cube, 1), convertOrigin(bones, cube, 2),
                                size[0], size[1], size[2], inflate,
                                texWidth, texHeight, faceUv));
                    }
                }
                // 创建 Cube ModelRender
                else {
                    BedrockPart cubeRenderer = new BedrockPart();
                    cubeRenderer.setPos(convertPivot(bones, cube, 0), convertPivot(bones, cube, 1), convertPivot(bones, cube, 2));
                    setRotationAngle(cubeRenderer, convertRotation(cubeRotation[0]), convertRotation(cubeRotation[1]), convertRotation(cubeRotation[2]));
                    if (faceUv == null) {
                        cubeRenderer.cubes.add(new BedrockCubeBox(uv[0], uv[1],
                                convertOrigin(cube, 0), convertOrigin(cube, 1), convertOrigin(cube, 2),
                                size[0], size[1], size[2], inflate, mirror,
                                texWidth, texHeight));
                    } else {
                        cubeRenderer.cubes.add(new BedrockCubePerFace(
                                convertOrigin(cube, 0), convertOrigin(cube, 1), convertOrigin(cube, 2),
                                size[0], size[1], size[2], inflate,
                                texWidth, texHeight, faceUv));
                    }

                    // 添加进父骨骼中
                    model.addChild(cubeRenderer);
                }
            }
        }
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        // 读取阶段的整体修正：基岩模型坐标是 y 向下、正面朝 -Z 的约定，
        // 在这里统一翻转 Y/Z，使模型正面朝 +Z 且竖直方向与渲染空间一致。
        // 这样各渲染路径（物品/实体/方块）都不再需要手写的 180° 补偿。
        poseStack.scale(1.0F, -1.0F, -1.0F);
        for (BedrockPart model : shouldRender) {
            model.render(poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public AABB getRenderBoundingBox() {
        return renderBoundingBox;
    }

    /**
     * 计算模型实际渲染时的世界空间包围盒（模型单位，16=1 格）。
     * <p>
     * 与 {@link #getRenderBoundingBox()}（visible bounds，单位不一致且通常留有很大余量）不同，
     * 这里直接遍历骨骼/方块网格并应用与渲染一致的平移/旋转/缩放，
     * 得到的是 {@link BedrockModel#render} 翻转 Y/Z 之前、与
     * DollSpecialModelRenderer 中 translate(0.5, 1.5, 0.5) 同空间的真实范围。
     */
    public AABB getActualRenderBoundingBox() {
        if (actualRenderBoundingBox == null) {
            actualRenderBoundingBox = computeActualRenderBoundingBox();
        }
        return actualRenderBoundingBox;
    }

    private AABB computeActualRenderBoundingBox() {
        float[] min = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY};
        float[] max = {Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        Matrix4f identity = new Matrix4f();
        for (BedrockPart root : shouldRender) {
            accumulateRenderBox(root, identity, min, max);
        }
        if (min[0] > max[0] || min[1] > max[1] || min[2] > max[2]) {
            // 没有任何可渲染网格时回退到 visible bounds
            return renderBoundingBox;
        }
        return new AABB(min[0] * 16.0F, min[1] * 16.0F, min[2] * 16.0F,
                max[0] * 16.0F, max[1] * 16.0F, max[2] * 16.0F);
    }

    private void accumulateRenderBox(BedrockPart part, Matrix4f matrix, float[] min, float[] max) {
        if (!part.visible) {
            return;
        }
        // 与 BedrockPart.render 一致：缩放过小直接退出
        boolean xNearZero = -1E-5F < part.xScale && part.xScale < 1E-5F;
        boolean yNearZero = -1E-5F < part.yScale && part.yScale < 1E-5F;
        boolean zNearZero = -1E-5F < part.zScale && part.zScale < 1E-5F;
        if ((xNearZero && yNearZero) || (xNearZero && zNearZero) || (yNearZero && zNearZero)) {
            return;
        }

        Matrix4f current = new Matrix4f(matrix);
        current.translate(part.x / 16.0F + part.offsetX, part.y / 16.0F + part.offsetY, part.z / 16.0F + part.offsetZ);
        if (part.xRot != 0.0F || part.yRot != 0.0F || part.zRot != 0.0F) {
            current.rotateZYX(part.zRot, part.yRot, part.xRot);
        }
        current.rotate(part.additionalQuaternion);
        current.scale(part.xScale, part.yScale, part.zScale);

        Vector3f corner = new Vector3f();
        for (BedrockCube cube : part.cubes) {
            float x = cube.getX();
            float y = cube.getY();
            float z = cube.getZ();
            float w = cube.getWidth();
            float h = cube.getHeight();
            float d = cube.getDepth();
            for (int i = 0; i < 8; i++) {
                corner.set(x + ((i & 1) != 0 ? w : 0.0F),
                        y + ((i & 2) != 0 ? h : 0.0F),
                        z + ((i & 4) != 0 ? d : 0.0F));
                corner.mulPosition(current);
                min[0] = Math.min(min[0], corner.x);
                min[1] = Math.min(min[1], corner.y);
                min[2] = Math.min(min[2], corner.z);
                max[0] = Math.max(max[0], corner.x);
                max[1] = Math.max(max[1], corner.y);
                max[2] = Math.max(max[2], corner.z);
            }
        }

        for (BedrockPart child : part.children) {
            accumulateRenderBox(child, current, min, max);
        }
    }

    public HashMap<String, BedrockPart> getModelMap() {
        return modelMap;
    }

    public String getIdentifier() {
        return identifier;
    }

    protected void setRotationAngle(BedrockPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
        modelRenderer.setInitRotationAngle(x, y, z);
    }

    /**
     * 基岩版的旋转中心计算方式和 Java 版不太一样，需要进行转换
     * <p>
     * 如果有父模型
     * <li>x，z 方向：本模型坐标 - 父模型坐标
     * <li>y 方向：父模型坐标 - 本模型坐标
     * <p>
     * 如果没有父模型
     * <li>x，z 方向不变
     * <li>y 方向：24 - 本模型坐标
     *
     * @param index 是 xyz 的哪一个，x 是 0，y 是 1，z 是 2
     */
    protected float convertPivot(BonesItem bones, int index) {
        if (bones.getParent() != null) {
            if (index == 1) {
                return indexBones.get(bones.getParent()).getPivot()[index] - bones.getPivot()[index];
            } else {
                return bones.getPivot()[index] - indexBones.get(bones.getParent()).getPivot()[index];
            }
        } else {
            if (index == 1) {
                return 24 - bones.getPivot()[index];
            } else {
                return bones.getPivot()[index];
            }
        }
    }

    protected float convertPivot(BonesItem parent, CubesItem cube, int index) {
        assert cube.getPivot() != null;
        if (index == 1) {
            return parent.getPivot()[index] - cube.getPivot()[index];
        } else {
            return cube.getPivot()[index] - parent.getPivot()[index];
        }
    }

    /**
     * 基岩版和 Java 版本的方块起始坐标也不一致，Java 是相对坐标，而且 y 值方向不一致。
     * 基岩版是绝对坐标，而且 y 方向朝上。
     * 其实两者规律很简单，但是我找了一下午，才明白咋回事。
     * <li>如果是 x，z 轴，那么只需要方块起始坐标减去旋转点坐标
     * <li>如果是 y 轴，旋转点坐标减去方块起始坐标，再减去方块的 y 长度
     *
     * @param index 是 xyz 的哪一个，x 是 0，y 是 1，z 是 2
     */
    protected float convertOrigin(BonesItem bone, CubesItem cube, int index) {
        if (index == 1) {
            return bone.getPivot()[index] - cube.getOrigin()[index] - cube.getSize()[index];
        } else {
            return cube.getOrigin()[index] - bone.getPivot()[index];
        }
    }

    protected float convertOrigin(CubesItem cube, int index) {
        assert cube.getPivot() != null;
        if (index == 1) {
            return cube.getPivot()[index] - cube.getOrigin()[index] - cube.getSize()[index];
        } else {
            return cube.getOrigin()[index] - cube.getPivot()[index];
        }
    }

    /**
     * 基岩版用的是度，Java 版用的是弧度，这个转换很简单
     */
    protected float convertRotation(float degree) {
        return (float) (degree * Math.PI / 180);
    }
}
