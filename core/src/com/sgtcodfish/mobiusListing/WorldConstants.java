package com.sgtcodfish.mobiusListing;

/**
 * <p>
 * Holds constants that are globally true.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class WorldConstants {
	public static final float	GRAVITY							= 1.0f;

	/**
	 * <p>
	 * If a value is less than GLOBAL_SOLID_OPACITY it is not solid and is drawn
	 * with an opacity of GLOBAL_PASSTHROUGH_OPACITY.
	 * </p>
	 * <p>
	 * If a value is greater than GLOBAL_SOLID_OPACITY it is drawn as opaque (a
	 * = 1.0f) and is solid (i.e. collidable).
	 * </p>
	 */
	public static final float	GLOBAL_SOLID_OPACITY_THRESHOLD	= 0.9f;

	public static final float	GLOBAL_SOLID_OPACITY			= 1.0f;
	public static final float	GLOBAL_PASSTHROUGH_OPACITY		= 0.25f;

	public enum InteractableLayerTypes {
		DX, DY, FADABLE;

		public static String layerPropertyOf(InteractableLayerTypes type) {
			switch (type) {
			case DX:
				return "dx";
			case DY:
				return "dy";
			case FADABLE:
				return "minOpacity";
			default:
				throw new IllegalStateException("Invalid state in layerPropertyOf");
			}
		}

		public static InteractableLayerTypes fromProperty(String property) {
			if ("dx".equals(property)) {
				return DX;
			} else if ("dy".equals(property)) {
				return DY;
			} else if ("minOpacity".equals(property)) {
				return FADABLE;
			} else {
				return null;
			}
		}
	}

	public static final String[]	interactableLayersProperties	= { "dx", "dy", "minOpacity" };
}
