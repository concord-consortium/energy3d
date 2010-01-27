package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private static final float WALL_HEIGHT = 0.5f;
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);

	public Wall() {
		super(2);
		Mesh mesh = new Mesh("Wall");
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("brick_wall.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);
	}

	@Override
	protected void draw(Vector3 p, int i) {
		vertexBuffer.position(i * 2 * 3);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(WALL_HEIGHT);
		
//		vertexBuffer.put(p.getXf()).put(p.getYf()).put((i%2==0) ? p.getZf() : WALL_HEIGHT);
//		vertexBuffer.put(p.getXf()).put(p.getYf()).put((i%2==1) ? p.getZf() : WALL_HEIGHT);		

//		if (i == 0) {
//			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
//			vertexBuffer.put(p.getXf()).put(p.getYf()).put(WALL_HEIGHT);
//		}		
		
		final float TEXTURE_SCALE = (i < 1) ? 0 : (float)p.subtract(points.get(i-1), null).length();

		// texture coords
		textureBuffer.position(i * 2 * 2);
		textureBuffer.put(TEXTURE_SCALE).put(0);
		textureBuffer.put(TEXTURE_SCALE).put(1);
		
		// draw spheres for points
		pointsRoot.getChild(i).setTranslation(p);
		
	}

}
