package org.processmining.trafficlightcc.visualization.trafficlight;

public class BaseLayoutInfo {
	
	/**
	 * X-coordinate (given a ({@link this#hDesiredExt} * {@link this#wDesiredExt}) box,
	 * the actual ({@link this#h} * {@link this#w}) box is position w.r.t. this x-coordinate
	 */
	protected float x;
	
	/**
	 * Y-coordinate (given a ({@link this#hDesiredExt} * {@link this#wDesiredExt}) box,
	 * the actual ({@link this#h} * {@link this#w}) box is position w.r.t. this y-coordinate
	 */
	protected float y;
	
	/**
	 * Width
	 */
	protected float w;
	
	/**
	 * Height
	 */
	protected float h;
	
	
	public BaseLayoutInfo() {
		this.x = 0f;
		this.y = 0f;
		this.w = 0f;
		this.h = 0f;
	}

	@Override
	public String toString() {
		return String.format("{x=%f, y=%f, w=%f, h=%f}", 
				this.x, this.y,
				this.w, this.h);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getW() {
		return w;
	}

	public float getH() {
		return h;
	}
	

}
