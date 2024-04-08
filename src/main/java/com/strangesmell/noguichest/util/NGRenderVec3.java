package com.strangesmell.noguichest.util;

import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;

public record NGRenderVec3(Vec3 translate,Vec3 scale,Vec4 selection,Vec3 onPointScale,Axis rotationAxis,float rotationTime ) {

}
