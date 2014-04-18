package fiji.plugin.mamut;

import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_COLOR_MAP;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_DRAWING_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_HIGHLIGHT_COLOR;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_LIMIT_DRAWING_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_SPOT_COLOR;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_TRACK_DISPLAY_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_TRACK_DISPLAY_MODE;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_COLOR;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_COLORMAP;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_DISPLAY_SPOT_NAMES;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_DRAWING_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_HIGHLIGHT_COLOR;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_LIMIT_DRAWING_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_SPOTS_VISIBLE;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_SPOT_COLORING;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_SPOT_RADIUS_RATIO;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACKS_VISIBLE;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_COLORING;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_DISPLAY_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_DISPLAY_MODE;
import ij.IJ;
import ij3d.Image3DUniverse;
import ij3d.ImageWindow3D;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;

import loci.formats.FormatException;
import mpicbg.spim.data.SequenceDescription;
import net.imglib2.RealPoint;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;

import org.jdom2.JDOMException;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.xml.sax.SAXException;

import bdv.SequenceViewsLoader;
import bdv.SpimSource;
import bdv.VolatileSpimSource;
import bdv.img.cache.Cache;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.tools.HelpDialog;
import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.BrightnessDialog;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.RealARGBColorConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.state.ViewerState;
import fiji.plugin.mamut.detection.SourceSemiAutoTracker;
import fiji.plugin.mamut.feature.spot.SpotSourceIdAnalyzerFactory;
import fiji.plugin.mamut.gui.AnnotationPanel;
import fiji.plugin.mamut.gui.MamutControlPanel;
import fiji.plugin.mamut.gui.MamutGUI;
import fiji.plugin.mamut.gui.MamutGUIModel;
import fiji.plugin.mamut.gui.MamutKeyboardHandler;
import fiji.plugin.mamut.io.MamutXmlReader;
import fiji.plugin.mamut.io.MamutXmlWriter;
import fiji.plugin.mamut.providers.MamutEdgeAnalyzerProvider;
import fiji.plugin.mamut.providers.MamutSpotAnalyzerProvider;
import fiji.plugin.mamut.providers.MamutTrackAnalyzerProvider;
import fiji.plugin.mamut.providers.MamutViewProvider;
import fiji.plugin.mamut.util.SourceSpotImageUpdater;
import fiji.plugin.mamut.viewer.MamutOverlay;
import fiji.plugin.mamut.viewer.MamutViewer;
import fiji.plugin.mamut.viewer.MamutViewerFactory;
import fiji.plugin.mamut.viewer.MamutViewerPanel;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.ModelChangeEvent;
import fiji.plugin.trackmate.ModelChangeListener;
import fiji.plugin.trackmate.SelectionChangeEvent;
import fiji.plugin.trackmate.SelectionChangeListener;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.features.ModelFeatureUpdater;
import fiji.plugin.trackmate.features.edges.EdgeAnalyzer;
import fiji.plugin.trackmate.features.edges.EdgeTargetAnalyzer;
import fiji.plugin.trackmate.features.spot.SpotAnalyzerFactory;
import fiji.plugin.trackmate.features.track.TrackAnalyzer;
import fiji.plugin.trackmate.features.track.TrackIndexAnalyzer;
import fiji.plugin.trackmate.gui.DisplaySettingsEvent;
import fiji.plugin.trackmate.gui.DisplaySettingsListener;
import fiji.plugin.trackmate.io.IOUtils;
import fiji.plugin.trackmate.providers.EdgeAnalyzerProvider;
import fiji.plugin.trackmate.providers.SpotAnalyzerProvider;
import fiji.plugin.trackmate.providers.TrackAnalyzerProvider;
import fiji.plugin.trackmate.util.ModelTools;
import fiji.plugin.trackmate.visualization.ManualEdgeColorGenerator;
import fiji.plugin.trackmate.visualization.ManualSpotColorGenerator;
import fiji.plugin.trackmate.visualization.PerEdgeFeatureColorGenerator;
import fiji.plugin.trackmate.visualization.PerTrackFeatureColorGenerator;
import fiji.plugin.trackmate.visualization.SpotColorGenerator;
import fiji.plugin.trackmate.visualization.SpotColorGeneratorPerTrackFeature;
import fiji.plugin.trackmate.visualization.TrackMateModelView;
import fiji.plugin.trackmate.visualization.threedviewer.SpotDisplayer3D;
import fiji.plugin.trackmate.visualization.trackscheme.TrackScheme;

public class MaMuT implements ModelChangeListener
{

	public static final ImageIcon MAMUT_ICON = new ImageIcon( MaMuT.class.getResource( "mammouth-256x256.png" ) );

	public static final String PLUGIN_NAME = "MaMuT";

	public static final String PLUGIN_VERSION = "0.12.0-SNAPSHOT";

	private static final double DEFAULT_RADIUS = 10;

	/**
	 * By how portion of the current radius we change this radius for every
	 * change request.
	 */
	private static final double RADIUS_CHANGE_FACTOR = 0.1;

	/** The default width for new image viewers. */
	public static final int DEFAULT_WIDTH = 800;

	/** The default height for new image viewers. */
	public static final int DEFAULT_HEIGHT = 600;

	private final KeyStroke moveSpotKeystroke = KeyStroke.getKeyStroke( KeyEvent.VK_SPACE, 0 );

	private SetupAssignments setupAssignments;

	private BrightnessDialog brightnessDialog;

	private HelpDialog helpDialog;

	/** The model shown and edited by this plugin. */
	private Model model;

	/** The next created spot will be set with this radius. */
	private double radius = DEFAULT_RADIUS;

	/** The radius below which a spot cannot go. */
	private final double minRadius = 2; // TODO change this when we have a

	// physical calibration

	/** The spot currently moved under the mouse. */
	private Spot movedSpot = null;

	/** The image data sources to be displayed in the views. */
	private List< SourceAndConverter< ? >> sources;

	private Cache cache;

	/** The number of timepoints in the image sources. */
	private int nTimepoints;

	/**
	 * If true, the next added spot will be automatically linked to the
	 * previously created one, given that the new spot is created in a
	 * subsequent frame.
	 */
	private boolean isLinkingMode = false;

	/**
	 * The color map for painting the spots. It is centralized here and is used
	 * in the {@link MamutOverlay}s.
	 */
	private SpotColorGenerator spotColorProvider;

	private PerTrackFeatureColorGenerator trackColorProvider;

	private PerEdgeFeatureColorGenerator edgeColorProvider;

	private SpotColorGeneratorPerTrackFeature spotColorPerTrackFeatureProvider;

	private ManualSpotColorGenerator manualSpotColorGenerator;

	private ManualEdgeColorGenerator manualEdgeColorGenerator;

	private TrackMate trackmate;

	private SourceSettings settings;

	private SelectionModel selectionModel;

	private MamutGUIModel guimodel;

	private SourceSpotImageUpdater< ? > thumbnailUpdater;

	private Logger logger;

	/**
	 * If <code>true</code>, then each time a spot is manually created, we will
	 * first test if it is not within the radius of an existing spot. If so,
	 * then the new spot will not be added to the model.
	 */
	private boolean testWithinSpot = true;

	private MamutGUI gui;

	private static File mamutFile;

	private static File imageFile;

	public MaMuT()
	{

		// I can't stand the metal look. If this is a problem, contact me
		// (jeanyves.tinevez@gmail.com)
		if ( IJ.isMacOSX() || IJ.isWindows() )
		{
			try
			{
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			catch ( final ClassNotFoundException e )
			{
				e.printStackTrace();
			}
			catch ( final InstantiationException e )
			{
				e.printStackTrace();
			}
			catch ( final IllegalAccessException e )
			{
				e.printStackTrace();
			}
			catch ( final UnsupportedLookAndFeelException e )
			{
				e.printStackTrace();
			}
		}

	}

	/*
	 * PUBLIC METHODS
	 */

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public void load( final File mamutfile ) throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{

		MaMuT.mamutFile = mamutfile;

		final MamutXmlReader reader = new MamutXmlReader( mamutfile );

		/*
		 * Read model
		 */

		model = reader.getModel();
		model.addModelChangeListener( this );

		/*
		 * Selection model
		 */

		selectionModel = new SelectionModel( model );
		selectionModel.addSelectionChangeListener( new SelectionChangeListener()
		{
			@Override
			public void selectionChanged( final SelectionChangeEvent event )
			{
				refresh();
				if ( selectionModel.getSpotSelection().size() == 1 )
				{
					centerOnSpot( selectionModel.getSpotSelection().iterator().next() );
				}
			}
		} );

		/*
		 * Read settings
		 */

		settings = new SourceSettings();
		reader.readSettings( settings, null, null, new MamutSpotAnalyzerProvider(), new MamutEdgeAnalyzerProvider(), new MamutTrackAnalyzerProvider() );

		/*
		 * Read image source
		 */

		imageFile = new File( settings.imageFolder, settings.imageFileName );
		if ( !imageFile.exists() )
		{
			// Then try relative path
			imageFile = new File( mamutfile.getParent(), settings.imageFileName );
		}

		try
		{
			prepareSources( imageFile );
		}
		catch ( final JDOMException e )
		{
			e.printStackTrace();
		}
		reader.getSetupAssignments( setupAssignments );

		/*
		 * Update settings
		 */

		settings.setFrom( sources, imageFile, nTimepoints, cache );

		/*
		 * Configure settings object with spot, edge and track analyzers as
		 * specified in the providers.
		 */

		prepareSettingsObject();

		/*
		 * Autoupdate features & declare them
		 */

		new ModelFeatureUpdater( model, settings );

		trackmate = new TrackMate( model, settings );
		trackmate.computeSpotFeatures( true );
		trackmate.computeEdgeFeatures( true );
		trackmate.computeTrackFeatures( true );

		/*
		 * Thumbnail updater
		 */

		thumbnailUpdater = new SourceSpotImageUpdater( settings, sources );

		/*
		 * Color provider
		 */

		prepareColorProviders();

		/*
		 * GUI model
		 */

		guimodel = new MamutGUIModel();
		guimodel.setDisplaySettings( createDisplaySettings( model ) );

		/*
		 * Control Panel
		 */

		gui = launchGUI();

		/*
		 * Brightness
		 */

		brightnessDialog = new BrightnessDialog( gui, setupAssignments );

		/*
		 * Help
		 */

		helpDialog = new HelpDialog( gui, MaMuT.class.getResource( "Help.html" ) );

		/*
		 * Read and render views
		 */

		final MamutViewProvider provider = new MamutViewProvider();
		final Collection< TrackMateModelView > views = reader.getViews( provider, model, settings, selectionModel );
		for ( final TrackMateModelView view : views )
		{
			for ( final String key : guimodel.getDisplaySettings().keySet() )
			{
				view.setDisplaySettings( key, guimodel.getDisplaySettings().get( key ) );
			}

			if ( view.getKey().equals( MamutViewerFactory.KEY ) )
			{
				final MamutViewer viewer = ( MamutViewer ) view;
				installKeyBindings( viewer );
				installMouseListeners( viewer );

				viewer.addWindowListener( new WindowAdapter()
				{
					@Override
					public void windowClosed( final WindowEvent arg0 )
					{
						guimodel.getViews().remove( viewer );
					}
				} );

				InitializeViewerState.initTransform( viewer.getViewerPanel() );
			}
			else if ( view instanceof TrackScheme )
			{
				final TrackScheme trackscheme = ( TrackScheme ) view;
				trackscheme.setSpotImageUpdater( thumbnailUpdater );
			}

			view.render();
			guimodel.addView( view );
		}

	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void launch( final File file ) throws FormatException, IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		MaMuT.imageFile = file;

		/*
		 * Load image source
		 */

		try
		{
			prepareSources( file );
		}
		catch ( final JDOMException e )
		{
			e.printStackTrace();
		}

		/*
		 * Instantiate model
		 */

		model = new Model();
		model.addModelChangeListener( this );

		/*
		 * Thumbnail updater
		 */

		thumbnailUpdater = new SourceSpotImageUpdater( settings, sources );

		/*
		 * Settings
		 */

		settings = new SourceSettings();
		settings.setFrom( sources, file, nTimepoints, cache );

		/*
		 * Configure settings object with spot, edge and track analyzers as
		 * specified in the providers.
		 */

		prepareSettingsObject();

		/*
		 * Autoupdate features & declare them
		 */

		new ModelFeatureUpdater( model, settings );

		trackmate = new TrackMate( model, settings );
		trackmate.computeSpotFeatures( true );
		trackmate.computeEdgeFeatures( true );
		trackmate.computeTrackFeatures( true );

		/*
		 * Selection model
		 */

		selectionModel = new SelectionModel( model );
		selectionModel.addSelectionChangeListener( new SelectionChangeListener()
		{
			@Override
			public void selectionChanged( final SelectionChangeEvent event )
			{
				refresh();
				if ( selectionModel.getSpotSelection().size() == 1 )
				{
					centerOnSpot( selectionModel.getSpotSelection().iterator().next() );
				}
			}
		} );

		/*
		 * Color provider
		 */

		prepareColorProviders();

		/*
		 * GUI model
		 */

		guimodel = new MamutGUIModel();
		guimodel.setDisplaySettings( createDisplaySettings( model ) );

		/*
		 * Control Panel
		 */

		gui = launchGUI();

		/*
		 * Brightness
		 */

		brightnessDialog = new BrightnessDialog( gui, setupAssignments );

		/*
		 * Help
		 */

		helpDialog = new HelpDialog( gui, MaMuT.class.getResource( "Help.html" ) );
	}

	@Override
	public void modelChanged( final ModelChangeEvent event )
	{
		refresh();
	}

	/*
	 * PRIVATE METHODS
	 */

	private MamutGUI launchGUI()
	{

		final MamutGUI mamutGUI = new MamutGUI( trackmate, this );

		final MamutControlPanel viewPanel = mamutGUI.getViewPanel();
		viewPanel.setSpotColorGenerator( spotColorProvider );
		viewPanel.setSpotColorGeneratorPerTrackFeature( spotColorPerTrackFeatureProvider );
		viewPanel.setManualSpotColorGenerator( manualSpotColorGenerator );
		viewPanel.setEdgeColorGenerator( edgeColorProvider );
		viewPanel.setManualEdgeColorGenerator( manualEdgeColorGenerator );
		viewPanel.setTrackColorGenerator( trackColorProvider );

		viewPanel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent event )
			{
				if ( event == viewPanel.TRACK_SCHEME_BUTTON_PRESSED )
				{
					launchTrackScheme( viewPanel.getTrackSchemeButton() );

				}
				else if ( event == viewPanel.DO_ANALYSIS_BUTTON_PRESSED )
				{
					launch3DViewer( viewPanel.getDoAnalysisButton() );

				}
				else if ( event == viewPanel.MAMUT_VIEWER_BUTTON_PRESSED )
				{
					newViewer();

				}
				else if ( event == viewPanel.MAMUT_SAVE_BUTTON_PRESSED )
				{
					save();

				}
				else
				{
					System.out.println( "[MaMuT] Caught unknown event: " + event );
				}
			}

		} );
		viewPanel.addDisplaySettingsChangeListener( new DisplaySettingsListener()
		{
			@Override
			public void displaySettingsChanged( final DisplaySettingsEvent event )
			{
				guimodel.getDisplaySettings().put( event.getKey(), event.getNewValue() );
				for ( final TrackMateModelView view : guimodel.getViews() )
				{
					view.setDisplaySettings( event.getKey(), event.getNewValue() );
					view.refresh();
				}
			}
		} );

		final AnnotationPanel annotationPanel = mamutGUI.getAnnotationPanel();
		logger = annotationPanel.getLogger();
		annotationPanel.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				if ( event == annotationPanel.SEMI_AUTO_TRACKING_BUTTON_PRESSED )
				{
					semiAutoDetectSpot();

				}
				else if ( event == annotationPanel.SELECT_TRACK_BUTTON_PRESSED )
				{
					ModelTools.selectTrack( selectionModel );

				}
				else if ( event == annotationPanel.SELECT_TRACK_DOWNWARD_BUTTON_PRESSED )
				{
					ModelTools.selectTrackDownward( selectionModel );

				}
				else if ( event == annotationPanel.SELECT_TRACK_UPWARD_BUTTON_PRESSED )
				{
					ModelTools.selectTrackUpward( selectionModel );

				}
				else
				{
					System.out.println( "[MaMuT] Caught unknown event: " + event );
				}
			}
		} );

		return mamutGUI;

	}

	/**
	 * Sets the {@link #settings} field with the analyzers configured for MaMuT.
	 */
	private void prepareSettingsObject()
	{
		settings.clearSpotAnalyzerFactories();
		final SpotAnalyzerProvider spotAnalyzerProvider = new MamutSpotAnalyzerProvider();
		final List< String > spotAnalyzerKeys = spotAnalyzerProvider.getKeys();
		for ( final String key : spotAnalyzerKeys )
		{
			final SpotAnalyzerFactory< ? > spotFeatureAnalyzer = spotAnalyzerProvider.getFactory( key );
			settings.addSpotAnalyzerFactory( spotFeatureAnalyzer );
		}

		settings.clearEdgeAnalyzers();
		final EdgeAnalyzerProvider edgeAnalyzerProvider = new MamutEdgeAnalyzerProvider();
		final List< String > edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
		for ( final String key : edgeAnalyzerKeys )
		{
			final EdgeAnalyzer edgeAnalyzer = edgeAnalyzerProvider.getFactory( key );
			settings.addEdgeAnalyzer( edgeAnalyzer );
		}

		settings.clearTrackAnalyzers();
		final TrackAnalyzerProvider trackAnalyzerProvider = new MamutTrackAnalyzerProvider();
		final List< String > trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
		for ( final String key : trackAnalyzerKeys )
		{
			final TrackAnalyzer trackAnalyzer = trackAnalyzerProvider.getFactory( key );
			settings.addTrackAnalyzer( trackAnalyzer );
		}
	}

	private void prepareColorProviders()
	{
		spotColorProvider = new SpotColorGenerator( model );
		trackColorProvider = new PerTrackFeatureColorGenerator( model, TrackIndexAnalyzer.TRACK_ID );
		edgeColorProvider = new PerEdgeFeatureColorGenerator( model, EdgeTargetAnalyzer.EDGE_COST );
		spotColorPerTrackFeatureProvider = new SpotColorGeneratorPerTrackFeature( model, TrackIndexAnalyzer.TRACK_ID );
		manualSpotColorGenerator = new ManualSpotColorGenerator();
		manualEdgeColorGenerator = new ManualEdgeColorGenerator( model );
	}

	private void prepareSources( final File dataFile ) throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, JDOMException
	{
		final SequenceViewsLoader loader = new SequenceViewsLoader( dataFile.getAbsolutePath() );
		final SequenceDescription seq = loader.getSequenceDescription();
		nTimepoints = seq.numTimepoints();
		sources = new ArrayList< SourceAndConverter< ? >>();
		cache = ( ( Hdf5ImageLoader ) seq.imgLoader ).getCache();
		final ArrayList< ConverterSetup > converterSetups = new ArrayList< ConverterSetup >();
		for ( int setup = 0; setup < seq.numViewSetups(); ++setup )
		{
			final RealARGBColorConverter< VolatileUnsignedShortType > vconverter = new RealARGBColorConverter< VolatileUnsignedShortType >( 0, 65535 );
			vconverter.setColor( new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ) ) );
			final RealARGBColorConverter< UnsignedShortType > converter = new RealARGBColorConverter< UnsignedShortType >( 0, 65535 );
			converter.setColor( new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ) ) );
			@SuppressWarnings( "rawtypes" )
			final VolatileSpimSource vs = new VolatileSpimSource( loader, setup, "angle " + seq.setups.get( setup ).getAngle() );
			@SuppressWarnings( "rawtypes" )
			final SpimSource s = vs.nonVolatile();
			@SuppressWarnings( "unchecked" )
			final SourceAndConverter< VolatileUnsignedShortType > vsoc = new SourceAndConverter< VolatileUnsignedShortType >( vs, vconverter );
			@SuppressWarnings( "unchecked" )
			final SourceAndConverter< UnsignedShortType > soc = new SourceAndConverter< UnsignedShortType >( s, converter, vsoc );
			sources.add( soc );
			converterSetups.add( new RealARGBColorConverterSetup( setup, converter, vconverter )
			{
				@Override
				public void setDisplayRange( final int min, final int max )
				{
					super.setDisplayRange( min, max );
					requestRepaintAllViewers();
				}

				@Override
				public void setColor( final ARGBType color )
				{
					super.setColor( color );
					requestRepaintAllViewers();
				}
			} );
		}

		/*
		 * Create setup assignments (for managing brightness and color).
		 */

		setupAssignments = new SetupAssignments( converterSetups, 0, 65535 );
		final MinMaxGroup group = setupAssignments.getMinMaxGroups().get( 0 );
		for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
		{
			setupAssignments.moveSetupToGroup( setup, group );
		}
	}

	private MamutViewer newViewer()
	{
		final MamutViewer viewer = new MamutViewer( DEFAULT_WIDTH, DEFAULT_HEIGHT, sources, nTimepoints, cache, model, selectionModel );

		for ( final String key : guimodel.getDisplaySettings().keySet() )
		{
			viewer.setDisplaySettings( key, guimodel.getDisplaySettings().get( key ) );
		}

		installKeyBindings( viewer );
		installMouseListeners( viewer );

		viewer.addWindowListener( new DeregisterWindowListener( viewer ) );

		InitializeViewerState.initTransform( viewer.getViewerPanel() );

		// TODO: only initBrightness if it hasn't been done before.
		InitializeViewerState.initBrightness( 0.001, 0.999, viewer.getViewerPanel(), setupAssignments );

		viewer.render();
		guimodel.addView( viewer );

		viewer.refresh();

		return viewer;

	}

	public void toggleBrightnessDialog()
	{
		brightnessDialog.setVisible( !brightnessDialog.isVisible() );
	}

	public void toggleHelpDialog()
	{
		helpDialog.setVisible( !helpDialog.isVisible() );
	}

	private void save()
	{

		final Logger logger = Logger.IJ_LOGGER;
		if ( null == imageFile )
		{
			final File folder = new File( System.getProperty( "user.dir" ) ).getParentFile().getParentFile();
			mamutFile = new File( folder.getPath() + File.separator + "MamutAnnotation.xml" );
		}
		else
		{
			final String pf = imageFile.getParent();
			String lf = imageFile.getName();
			lf = lf.split( "\\." )[ 0 ] + "-mamut.xml";
			mamutFile = new File( pf, lf );
		}

		mamutFile = IOUtils.askForFileForSaving( mamutFile, IJ.getInstance(), logger );
		if ( null == mamutFile )
		{
			logger.log( "Saving canceled.\n" );
			return;
		}

		logger.log( "Saving to " + mamutFile + '\n' );

		final MamutXmlWriter writer = new MamutXmlWriter( mamutFile, logger );
		writer.appendModel( model );
		writer.appendSettings( settings );
		writer.appendMamutState( guimodel, setupAssignments );
		try
		{
			writer.writeToFile();
			logger.log( "Done.\n" );
		}
		catch ( final FileNotFoundException e )
		{
			logger.error( "Could not find file " + mamutFile + ";\n" + e.getMessage() );
		}
		catch ( final IOException e )
		{
			logger.error( "Could not write to " + mamutFile + ";\n" + e.getMessage() );
		}

	}

	private void requestRepaintAllViewers()
	{
		if ( guimodel != null )
		{
			for ( final TrackMateModelView view : guimodel.getViews() )
			{
				if ( view instanceof MamutViewer )
				{
					( ( MamutViewer ) view ).getViewerPanel().requestRepaint();
				}
			}
		}
	}

	/**
	 * Configures the specified {@link MamutViewer} with key bindings.
	 *
	 * @param the
	 *            {@link MamutViewer} to configure.
	 */
	private void installKeyBindings( final MamutViewer viewer )
	{

		new MamutKeyboardHandler( this, viewer );

		/*
		 * Custom key presses
		 */

		viewer.addHandler( new KeyListener()
		{

			@Override
			public void keyTyped( final KeyEvent event )
			{}

			@Override
			public void keyReleased( final KeyEvent event )
			{
				if ( event.getKeyCode() == moveSpotKeystroke.getKeyCode() )
				{
					if ( null != movedSpot )
					{
						model.beginUpdate();
						try
						{
							model.updateFeatures( movedSpot );
						}
						finally
						{
							model.endUpdate();
							final String str = String.format( "Moved spot " + movedSpot + " to location X = %.1f, Y = %.1f, Z = %.1f.", movedSpot.getFeature( Spot.POSITION_X ), movedSpot.getFeature( Spot.POSITION_Y ), movedSpot.getFeature( Spot.POSITION_Z ) );
							viewer.getLogger().log( str );
							movedSpot = null;
						}
						refresh();
					}
				}
			}

			@Override
			public void keyPressed( final KeyEvent event )
			{
				if ( event.getKeyCode() == moveSpotKeystroke.getKeyCode() )
				{
					movedSpot = getSpotWithinRadius( viewer.getViewerPanel() );
				}

			}
		} );

	}

	/**
	 * Configures the specified {@link MamutViewer} with mouse listeners.
	 *
	 * @param viewer
	 *            the {@link MamutViewer} to configure.
	 */
	private void installMouseListeners( final MamutViewer viewer )
	{
		viewer.addHandler( new MouseMotionListener()
		{

			@Override
			public void mouseMoved( final MouseEvent arg0 )
			{
				if ( null != movedSpot )
				{
					final RealPoint gPos = new RealPoint( 3 );
					viewer.getViewerPanel().getGlobalMouseCoordinates( gPos );
					final double[] coordinates = new double[ 3 ];
					gPos.localize( coordinates );
					movedSpot.putFeature( Spot.POSITION_X, coordinates[ 0 ] );
					movedSpot.putFeature( Spot.POSITION_Y, coordinates[ 1 ] );
					movedSpot.putFeature( Spot.POSITION_Z, coordinates[ 2 ] );
				}
			}

			@Override
			public void mouseDragged( final MouseEvent arg0 )
			{}
		} );

		viewer.addHandler( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent event )
			{

				if ( event.getClickCount() < 2 )
				{

					final Spot spot = getSpotWithinRadius( viewer.getViewerPanel() );
					if ( null != spot )
					{
						// Center view on it
						centerOnSpot( spot );
						if ( !event.isShiftDown() )
						{
							// Replace selection
							selectionModel.clearSpotSelection();
						}
						// Toggle it to selection
						if ( selectionModel.getSpotSelection().contains( spot ) )
						{
							selectionModel.removeSpotFromSelection( spot );
						}
						else
						{
							selectionModel.addSpotToSelection( spot );
						}

					}
					else
					{
						// Clear selection
						selectionModel.clearSelection();
					}

				}
				else
				{

					final Spot spot = getSpotWithinRadius( viewer.getViewerPanel() );
					if ( null == spot )
					{
						// Create a new spot
						addSpot( viewer );
					}

				}
				refresh();
			}
		} );

	}

	private void launchTrackScheme( final JButton button )
	{
		button.setEnabled( false );
		new Thread( "Launching TrackScheme thread" )
		{

			@Override
			public void run()
			{
				final TrackScheme trackscheme = new TrackScheme( model, selectionModel );
				trackscheme.getGUI().addWindowListener( new DeregisterWindowListener( trackscheme ) );
				trackscheme.setSpotImageUpdater( thumbnailUpdater );
				for ( final String settingKey : guimodel.getDisplaySettings().keySet() )
				{
					trackscheme.setDisplaySettings( settingKey, guimodel.getDisplaySettings().get( settingKey ) );
				}
				selectionModel.addSelectionChangeListener( trackscheme );
				trackscheme.render();
				guimodel.addView( trackscheme );
				button.setEnabled( true );
			};
		}.start();
	}

	private void launch3DViewer( final JButton button )
	{
		button.setEnabled( false );
		new Thread( "MaMuT new 3D viewer thread" )
		{
			@Override
			public void run()
			{
				final Image3DUniverse universe = new Image3DUniverse();
				final ImageWindow3D win = new ImageWindow3D( "MaMuT 3D Viewer", universe );
				win.setIconImage( MamutControlPanel.THREEDVIEWER_ICON.getImage() );
				universe.init( win );
				win.pack();
				win.setVisible( true );

				// universe.show();
				final SpotDisplayer3D newDisplayer = new SpotDisplayer3D( model, selectionModel, universe );
				for ( final String key : guimodel.getDisplaySettings().keySet() )
				{
					newDisplayer.setDisplaySettings( key, guimodel.getDisplaySettings().get( key ) );
				}
				guimodel.addView( newDisplayer );
				newDisplayer.render();
				button.setEnabled( true );
			}
		}.start();
	}

	private void refresh()
	{
		// Just ask to repaint the TrackMate overlay
		for ( final TrackMateModelView viewer : guimodel.getViews() )
		{
			viewer.refresh();
		}
	}

	private void centerOnSpot( final Spot spot )
	{
		for ( final TrackMateModelView otherView : guimodel.getViews() )
		{
			otherView.centerViewOn( spot );
		}
	}

	/**
	 * Performs the semi-automatic detection of subsequent spots. For this to
	 * work, exactly one spot must be in the selection.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public void semiAutoDetectSpot()
	{
		final SourceSemiAutoTracker autotracker = new SourceSemiAutoTracker( model, selectionModel, sources, logger );
		autotracker.setNumThreads( 4 );
		autotracker.setParameters( guimodel.qualityThreshold, guimodel.distanceTolerance );

		new Thread( "MaMuT semi-automated tracking thread" )
		{
			@Override
			public void run()
			{
				final boolean ok = autotracker.checkInput() && autotracker.process();
				if ( !ok )
				{
					logger.error( autotracker.getErrorMessage() );
				}
			}
		}.start();
	}

	/**
	 * Adds a new spot at the mouse current location.
	 *
	 * @param viewer
	 *            the viewer in which the add spot request was made.
	 */
	public void addSpot( final MamutViewer viewer )
	{

		// Check if the mouse is not off-screen
		final Point mouseScreenLocation = MouseInfo.getPointerInfo().getLocation();
		final Point viewerPosition = viewer.getLocationOnScreen();
		final Dimension viewerSize = viewer.getSize();
		if ( mouseScreenLocation.x < viewerPosition.x || mouseScreenLocation.y < viewerPosition.y || mouseScreenLocation.x > viewerPosition.x + viewerSize.width || mouseScreenLocation.y > viewerPosition.y + viewerSize.height ) { return; }

		final ViewerState state = viewer.getViewerPanel().getState();
		final int frame = state.getCurrentTimepoint();
		final int sourceId = state.getCurrentSource();

		// Ok, then create this spot, wherever it is.
		final double[] coordinates = new double[ 3 ];
		viewer.getViewerPanel().getGlobalMouseCoordinates( RealPoint.wrap( coordinates ) );
		final Spot spot = new Spot( coordinates[ 0 ], coordinates[ 1 ], coordinates[ 2 ], radius, -1d );

		if ( testWithinSpot )
		{
			final Spot closestSpot = model.getSpots().getClosestSpot( spot, frame, true );
			if ( null != closestSpot )
			{
				final double closestRadius = closestSpot.getFeature( Spot.RADIUS ).doubleValue();
				if ( closestSpot.squareDistanceTo( spot ) < closestRadius * closestRadius )
				{
					final String message = "Cannot create spot: it is too close to spot " + closestSpot + ".\n";
					viewer.getLogger().log( message );
					return;
				}
			}
		}

		spot.putFeature( Spot.QUALITY, -1d );
		spot.putFeature( Spot.POSITION_T, Double.valueOf( frame ) );
		spot.putFeature( SpotSourceIdAnalyzerFactory.SOURCE_ID, Double.valueOf( sourceId ) );

		model.beginUpdate();
		try
		{
			model.addSpotTo( spot, frame );
		}
		finally
		{
			model.endUpdate();
		}

		String message = String.format( "Added spot " + spot + " at location X = %.1f, Y = %.1f, Z = %.1f, T = %.0f", spot.getFeature( Spot.POSITION_X ), spot.getFeature( Spot.POSITION_Y ), spot.getFeature( Spot.POSITION_Z ), spot.getFeature( Spot.FRAME ) );

		// Then, possibly, the edge. We must do it in a subsequent update,
		// otherwise the model gets confused.
		final Set< Spot > spotSelection = selectionModel.getSpotSelection();

		if ( isLinkingMode && spotSelection.size() == 1 )
		{ // if we are in the
			// right mode & if
			// there is only one
			// spot in
			// selection
			final Spot targetSpot = spotSelection.iterator().next();
			if ( targetSpot.getFeature( Spot.FRAME ).intValue() != spot.getFeature( Spot.FRAME ).intValue() )
			{ // & if they are on different
				// frames
				model.beginUpdate();
				final DefaultWeightedEdge newedge;
				try
				{

					// Create link
					newedge = model.addEdge( targetSpot, spot, -1 );
				}
				finally
				{
					model.endUpdate();
				}
				message += ", linked to spot " + targetSpot + ".";
				selectionModel.clearEdgeSelection();
				selectionModel.addEdgeToSelection( newedge );
			}
			else
			{
				message += ".";
			}
		}
		else
		{
			message += ".";
		}
		viewer.getLogger().log( message );

		// Store new spot as the sole selection for this model
		selectionModel.clearSpotSelection();
		selectionModel.addSpotToSelection( spot );
	}

	/**
	 * Remove spot at the mouse current location (if there is one).
	 *
	 * @param viewer
	 *            the viewer in which the delete spot request was made.
	 */
	public void deleteSpot( final MamutViewer viewer )
	{
		final Spot spot = getSpotWithinRadius( viewer.getViewerPanel() );
		if ( null != spot )
		{
			// We can delete it
			model.beginUpdate();
			try
			{
				model.removeSpot( spot );
			}
			finally
			{
				model.endUpdate();
				final String str = "Removed spot " + spot + ".";
				viewer.getLogger().log( str );
			}
		}

	}

	/**
	 * Increases (or decreases) the neighbor spot radius.
	 *
	 * @param viewer
	 *            the viewer in which the change radius was made.
	 * @param factor
	 *            the factor by which to change the radius. Negative value are
	 *            used to decrease the radius.
	 */
	public void increaseSpotRadius( final MamutViewer viewer, final double factor )
	{
		final Spot spot = getSpotWithinRadius( viewer.getViewerPanel() );
		if ( null != spot )
		{
			// Change the spot radius
			double rad = spot.getFeature( Spot.RADIUS );
			rad += factor * RADIUS_CHANGE_FACTOR * rad;

			if ( rad < minRadius ) { return; }

			radius = rad;
			spot.putFeature( Spot.RADIUS, rad );
			// Mark the spot for model update;
			model.beginUpdate();
			try
			{
				model.updateFeatures( spot );
			}
			finally
			{
				model.endUpdate();
				final String str = String.format( "Changed spot " + spot + " radius to R = %.1f.", spot.getFeature( Spot.RADIUS ) );
				viewer.getLogger().log( str );
			}
			refresh();
		}
	}

	/**
	 * Returns the closest {@link Spot} with respect to the current mouse
	 * location, and for which the current location is within its radius, or
	 * <code>null</code> if there is no such spot. In other words: returns the
	 * spot in which the mouse pointer is.
	 *
	 * @param viewer
	 *            the viewer to inspect.
	 * @return the closest spot within radius.
	 */
	private Spot getSpotWithinRadius( final MamutViewerPanel viewer )
	{
		/*
		 * Get the closest spot
		 */
		final int frame = viewer.getState().getCurrentTimepoint();
		final RealPoint gPos = new RealPoint( 3 );
		viewer.getGlobalMouseCoordinates( gPos );
		final double[] coordinates = new double[ 3 ];
		gPos.localize( coordinates );
		final Spot location = new Spot( coordinates[ 0 ], coordinates[ 1 ], coordinates[ 2 ], radius, -1d );
		final Spot closestSpot = model.getSpots().getClosestSpot( location, frame, true );
		if ( null == closestSpot ) { return null; }
		/*
		 * Determine if we are inside the spot
		 */
		final double d2 = closestSpot.squareDistanceTo( location );
		final double r = closestSpot.getFeature( Spot.RADIUS );
		if ( d2 < r * r )
		{
			return closestSpot;
		}
		else
		{
			return null;
		}

	}

	/**
	 * Toggle the spot creation test on/off. If on, MaMuT will prevent a new
	 * spot to be created within another one.
	 */
	public void toggleSpotCreationTest()
	{
		testWithinSpot = !testWithinSpot;
	}

	public void toggleLinkingMode( final Logger logger )
	{
		this.isLinkingMode = !isLinkingMode;
		final String str = "Switched auto-linking mode " + ( isLinkingMode ? "on." : "off." );
		logger.log( str );
	}

	/**
	 * Toggles a link between two spots.
	 * <p>
	 * The two spots are taken from the selection, which must have exactly two
	 * spots in it
	 *
	 * @param logger
	 *            the {@link Logger} to echo linking messages.
	 */
	public void toggleLink( final Logger logger )
	{
		/*
		 * Toggle a link between two spots.
		 */
		final Set< Spot > selectedSpots = selectionModel.getSpotSelection();
		if ( selectedSpots.size() == 2 )
		{
			final Iterator< Spot > it = selectedSpots.iterator();
			final Spot source = it.next();
			final Spot target = it.next();

			if ( model.getTrackModel().containsEdge( source, target ) )
			{
				/*
				 * Remove it
				 */
				model.beginUpdate();
				try
				{
					model.removeEdge( source, target );
					logger.log( "Removed the link between " + source + " and " + target + ".\n" );
				}
				finally
				{
					model.endUpdate();
				}

			}
			else
			{
				/*
				 * Create a new link
				 */
				final int ts = source.getFeature( Spot.FRAME ).intValue();
				final int tt = target.getFeature( Spot.FRAME ).intValue();

				if ( tt != ts )
				{
					model.beginUpdate();
					try
					{
						model.addEdge( source, target, -1 );
						logger.log( "Created a link between " + source + " and " + target + ".\n" );
					}
					finally
					{
						model.endUpdate();
					}
					/*
					 * To emulate a kind of automatic linking, we put the last
					 * spot to the selection, so several spots can be tracked in
					 * a row without having to de-select one
					 */
					Spot single;
					if ( tt > ts )
					{
						single = target;
					}
					else
					{
						single = source;
					}
					selectionModel.clearSpotSelection();
					selectionModel.addSpotToSelection( single );

				}
				else
				{
					logger.error( "Cannot create a link between two spots belonging in the same frame." );
				}
			}

		}
		else
		{
			logger.error( "Expected selection to contain 2 spots, found " + selectedSpots.size() + ".\n" );
		}
	}

	/**
	 * Returns the starting display settings that will be passed to any new view
	 * registered within this GUI.
	 *
	 * @param model
	 *            the model this GUI will configure; might be required by some
	 *            display settings.
	 * @return a map of display settings mappings.
	 */
	protected Map< String, Object > createDisplaySettings( final Model model )
	{
		final Map< String, Object > displaySettings = new HashMap< String, Object >();
		displaySettings.put( KEY_COLOR, DEFAULT_SPOT_COLOR );
		displaySettings.put( KEY_HIGHLIGHT_COLOR, DEFAULT_HIGHLIGHT_COLOR );
		displaySettings.put( KEY_SPOTS_VISIBLE, true );
		displaySettings.put( KEY_DISPLAY_SPOT_NAMES, false );
		displaySettings.put( KEY_SPOT_RADIUS_RATIO, 1.0f );
		displaySettings.put( KEY_TRACKS_VISIBLE, true );
		displaySettings.put( KEY_TRACK_DISPLAY_MODE, DEFAULT_TRACK_DISPLAY_MODE );
		displaySettings.put( KEY_TRACK_DISPLAY_DEPTH, DEFAULT_TRACK_DISPLAY_DEPTH );
		displaySettings.put( KEY_TRACK_COLORING, trackColorProvider );
		displaySettings.put( KEY_SPOT_COLORING, spotColorProvider );
		displaySettings.put( KEY_COLORMAP, DEFAULT_COLOR_MAP );
		displaySettings.put( KEY_LIMIT_DRAWING_DEPTH, DEFAULT_LIMIT_DRAWING_DEPTH );
		displaySettings.put( KEY_DRAWING_DEPTH, DEFAULT_DRAWING_DEPTH );
		return displaySettings;
	}

	/**
	 * Exposes the GUI model that stores this GUI states.
	 *
	 * @return the {@link MamutGUIModel}.
	 */
	public MamutGUIModel getGuimodel()
	{
		return guimodel;
	}

	/**
	 * Exposes the {@link SelectionModel} shared amongst all views in the MaMuT
	 * session.
	 *
	 * @return the {@link SelectionModel}.
	 */
	public SelectionModel getSelectionModel()
	{
		return selectionModel;
	}

	/**
	 * Exposes the GUI frame that fosters user interface with this MaMuT
	 * session.
	 *
	 * @return the {@link MamutGUI}.
	 */
	public MamutGUI getGUI()
	{
		return gui;
	}

	/**
	 * Exposes the TrackMate object that controls this MaMuT session.
	 * 
	 * @return the {@link TrackMate} object.
	 */
	public TrackMate getTrackMate()
	{
		return trackmate;
	}

	private class DeregisterWindowListener implements WindowListener
	{

		private final TrackMateModelView view;

		public DeregisterWindowListener( final TrackMateModelView view )
		{
			this.view = view;
		}

		@Override
		public void windowActivated( final WindowEvent arg0 )
		{}

		@Override
		public void windowClosed( final WindowEvent arg0 )
		{}

		@Override
		public void windowClosing( final WindowEvent arg0 )
		{
			guimodel.removeView( view );
		}

		@Override
		public void windowDeactivated( final WindowEvent arg0 )
		{}

		@Override
		public void windowDeiconified( final WindowEvent arg0 )
		{}

		@Override
		public void windowIconified( final WindowEvent arg0 )
		{}

		@Override
		public void windowOpened( final WindowEvent arg0 )
		{}

	}
}
