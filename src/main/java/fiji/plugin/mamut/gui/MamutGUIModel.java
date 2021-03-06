package fiji.plugin.mamut.gui;

import fiji.plugin.trackmate.gui.TrackMateGUIModel;

public class MamutGUIModel extends TrackMateGUIModel {

	/**
	 * How close must be the new spot found to be accepted, in radius units.
	 */
	public double distanceTolerance = 1.5;

	/**
	 * The fraction of the initial quality above which we keep new spots. The
	 * highest, the more intolerant.
	 */
	public double qualityThreshold = 0d;

	/**
	 * In semi auto-tracking, how many frames to process at max. Make it 0 or
	 * negative to remove this limit.
	 */
	public int maxNFrames = 5;

	/**
	 * By how much we move in time when calling the step in time action.
	 */
	public int timeStep = 5;


}
