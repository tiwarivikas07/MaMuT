package fiji.plugin.mamut.providers;

import fiji.plugin.mamut.feature.spot.CellDivisionTimeAnalyzerSpotFactory;
import fiji.plugin.mamut.feature.spot.SpotSourceIdAnalyzerFactory;
import fiji.plugin.trackmate.features.manual.ManualSpotColorAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotAnalyzerFactory;
import fiji.plugin.trackmate.providers.SpotAnalyzerProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A provider for the spot analyzer factories for MaMuT only.
 */
@SuppressWarnings( "rawtypes" )
public class MamutSpotAnalyzerProvider extends SpotAnalyzerProvider
{
	private static final List< String > KEYS;

	static
	{
		KEYS = new ArrayList< String >( 3 );
		KEYS.add( SpotSourceIdAnalyzerFactory.KEY );
		KEYS.add( CellDivisionTimeAnalyzerSpotFactory.KEY );
		KEYS.add( ManualSpotColorAnalyzerFactory.KEY );
	}

	private final SpotSourceIdAnalyzerFactory spotSourceIdAnalyzerFactory;

	private final CellDivisionTimeAnalyzerSpotFactory cellDivisionTimeAnalyzerSpotFactory;

	public MamutSpotAnalyzerProvider()
	{
		super();
		this.spotSourceIdAnalyzerFactory = new SpotSourceIdAnalyzerFactory();
		this.cellDivisionTimeAnalyzerSpotFactory = new CellDivisionTimeAnalyzerSpotFactory();
	}

	@Override
	public List< String > getKeys()
	{
		return KEYS;
	}

	@Override
	public SpotAnalyzerFactory getFactory( final String key )
	{
		if ( key.equals( SpotSourceIdAnalyzerFactory.KEY ) )
		{
			return spotSourceIdAnalyzerFactory;
		}
		else if ( key.equals( CellDivisionTimeAnalyzerSpotFactory.KEY ) )
		{
			return cellDivisionTimeAnalyzerSpotFactory;
		}
		else
		{
			return super.getFactory( key );
		}
	}
}
