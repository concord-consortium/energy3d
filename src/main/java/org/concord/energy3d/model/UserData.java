package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.util.geom.BufferUtils;

public class UserData {
	private final HousePart housePart;
	private final int editPointIndex;
	private final boolean isEditPoint;
	private Vector3 printCenter = new Vector3();
	private ReadOnlyVector3 normal; // if this mesh is imported, save its original normal here
	private ReadOnlyVector3 rotatedNormal; // store the result of current rotation to avoid recalculation, null if there is no need to rotate the original normal
	private RenderState renderState;
	private FloatBuffer textureBuffer;
	private int nodeIndex = -1;
	private int meshIndex = -1;
	private boolean reachable = true;
	private boolean imported; // flag that indicates this as an imported mesh

	public UserData(final HousePart housePart) {
		this(housePart, -1, false);
	}

	/** call only when this UserData serves an imported mesh */
	public UserData(final Foundation foundation, final int nodeIndex, final int meshIndex) {
		this(foundation, -1, false);
		this.nodeIndex = nodeIndex;
		this.meshIndex = meshIndex;
		imported = true;
	}

	public UserData(final HousePart housePart, final int editPointIndex, final boolean isEditPoint) {
		this.housePart = housePart;
		this.editPointIndex = editPointIndex;
		this.isEditPoint = isEditPoint;
	}

	public boolean isImported() {
		return imported;
	}

	public HousePart getHousePart() {
		return housePart;
	}

	public int getEditPointIndex() {
		return editPointIndex;
	}

	public boolean isEditPoint() {
		return isEditPoint;
	}

	public void setPrintCenter(final Vector3 printCenter) {
		this.printCenter = printCenter;
	}

	public Vector3 getPrintCenter() {
		return printCenter;
	}

	@Override
	public String toString() {
		return housePart + "  index=" + editPointIndex + "  PrintCenter=" + Util.toString(printCenter);
	}

	public ReadOnlyVector3 getNormal() {
		return normal == null ? null : normal.clone();
	}

	public void setNormal(final ReadOnlyVector3 normal) {
		this.normal = normal;
	}

	public RenderState getRenderState() {
		return renderState;
	}

	public void setRenderState(final RenderState renderState) {
		this.renderState = renderState;
	}

	public FloatBuffer getTextureBuffer() {
		if (textureBuffer == null) {
			return null;
		}
		return BufferUtils.clone(textureBuffer); // must clone as the texture buffer will be modified later
	}

	public void setTextureBuffer(final FloatBuffer textureBuffer) {
		if (textureBuffer != null) {
			textureBuffer.rewind();
			this.textureBuffer = BufferUtils.clone(textureBuffer); // must clone as the texture buffer will be modified later
		} else {
			this.textureBuffer = null;
		}
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public int getMeshIndex() {
		return meshIndex;
	}

	public void setReachable(final boolean reachable) {
		this.reachable = reachable;
	}

	public boolean isReachable() {
		return reachable;
	}

	public void setRotatedNormal(final ReadOnlyVector3 n) {
		rotatedNormal = n;
	}

	public ReadOnlyVector3 getRotatedNormal() {
		return rotatedNormal;
	}

}
