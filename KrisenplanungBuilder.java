package krisenplanung_Simulation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
/*Mithilfe dieser KLasse wird die Simulation erstellt. Sie wird beim 0. Tick einmal aufgerufen*/
public class KrisenplanungBuilder implements ContextBuilder <Object> {
	private int hilfsvariable = 1;
	public static int zaehler = 0;
	public static int menge = 0;
	private int nummer = 1;
	public static LinkedList<Bevoelkerung> bevListe = new LinkedList<Bevoelkerung>();
	@Override
	public Context build(Context<Object> context) {
		context.setId("Krisenplanung_Simulation");
		
		GeographyParameters geoParams = new GeographyParameters();
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null)
				.createGeography("Geography", context, geoParams);
		/*Die einzelnen Shape-Files werden eingelesen*/
		GeometryFactory fac = new GeometryFactory();
		loadFeatures("data/Towns 6.0.shp", context, geography, 1);
		loadFeatures("data/Sea & Airports 2.0.shp", context, geography, 2);
		loadFeatures("data/Ausgabestellen 2.0.shp", context, geography, 3);
		/*Die Objekte der Klasse Bevoelkerung werden erzeugt und auf die Karte gesetzt*/
		try { 
			LinkedList <Location> listeKoordinaten = EinlesenBevoelkerung.einlesenBevoelkerung();
			Iterator it = listeKoordinaten.iterator();
		while (it.hasNext()) {
			Location koordinaten = (Location) it.next();
			Coordinate coord = new Coordinate(koordinaten.xkoordinate,koordinaten.ykoordinate);
			int zaehler = koordinaten.anzahl;
			while (zaehler > 0) {
			zaehler = zaehler - 1;
			Bevoelkerung bev =new Bevoelkerung();
			context.add(bev);
			Point geomhilfe = fac.createPoint(coord);
			geography.move(bev, geomhilfe);
			bev.setMyPoint(coord);
			bevListe.add(bev);
			menge = menge + 1;
			if (menge == 1) {
			bev.ausgabe = 1;
			} else { bev.ausgabe = 0;}
		}
		}
		System.out.println("Es gibt " + menge + " Personen");
		}
		catch (Exception e ) {
			System.out.println("Fehler beim Einlesen der Koordinaten für die Bevoelkerung");
		}
		/*Die Objekte der Klasse Zulieferer werden erzeugt und auf die Karte gesetzt*/
		try { 
			LinkedList <Location> listeKoordinaten = EinlesenZulieferer.einlesenZulieferer();
			Iterator it = listeKoordinaten.iterator();
		while (it.hasNext()) {
			Location koordinaten = (Location) it.next();
			Coordinate coord = new Coordinate(koordinaten.xkoordinate,koordinaten.ykoordinate);
			int zaehler = koordinaten.anzahl;
			while (zaehler > 0) {
			zaehler = zaehler -1;
			Zulieferer zul =new Zulieferer(6,koordinaten.xkoordinate,koordinaten.ykoordinate);
			context.add(zul);
			Point geomhilfe = fac.createPoint(coord);
			geography.move(zul, geomhilfe);
			}
		}
		}
		catch (Exception e ) {
			System.out.println("Fehler beim Einlesen der Koordinaten für die Zulieferer");
		}
		return context;
	}
	/*Methode zum Laden von Daten aus Shapefiles*/
	private List<SimpleFeature> loadFeaturesFromShapefile (String filename) {
		URL url = null;
		try {
			url = new File(filename).toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Try to load the shapefile
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();

			while(fiter.hasNext()){
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			fiter.close();
			store.dispose();
		}
		
		return features;
	}
	/*Methode welche die Daten des Shapefiles einliest und danach entsprechend das richtige Objekt erzeugt*/
	private void loadFeatures (String filename, Context context, Geography geography, int number) { 
		
		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		Coordinate test = new Coordinate(0,0);
		Coordinate koordinaten = new Coordinate(0,0);
		int hilf = 0;
		int hilfKap = 0;
		LinkedList<Integer> ausgabeOffen = new LinkedList<Integer>();
		LinkedList<Integer> ausgabeKapa = new LinkedList<Integer>();
		ausgabeOffen.add(-1);
		/*Hier werden die Informationen zu den Ausgabeorten eingelesen. Diese werden später in der Methode benötigt*/
		try {
			ausgabeOffen = EinlesenAusgabestelleOffen.ausgabeOffen();
			ausgabeKapa = EinlesenAusgabestelleKapazitaet.ausgabeKapazitaet();
			System.out.println("Die Länge der Liste beträgt: " + ausgabeOffen.size());
		} catch (IOException e) {
			System.out.println("Fehler beim übertragen der Excel-Werte");
			e.printStackTrace();
		}
		/*Je nachdem welche Daten aus dem Shapefile übergeben wurden, wird das entsprechende Objekt erzeugt*/
		for (SimpleFeature feature : features){
			Geometry geom = (Geometry)feature.getDefaultGeometry();
				if (!geom.isValid()){
					System.out.println("Invalid geometry: " + feature.getID());
				}
				else if (number == 1) {
					Informationszentrum agent = new Informationszentrum();
					Bevoelkerung.listeInfo.add(agent);
					if (agent != null){
						context.add(agent);
						geography.move(agent, geom);
						} else {System.out.println("Error creating agent for  " + geom);}
				}
				else if (number == 2) {
					Ankunftsstelle agent = new Ankunftsstelle();
					if (agent != null){
						context.add(agent);
						geography.move(agent, geom);
						} else {System.out.println("Error creating agent for  " + geom);}
				}
				else if (number == 3) {
					if (ausgabeOffen.get(0) != -1) {
							int uebergabe = ausgabeOffen.get(hilf);
							if (uebergabe == 1) {
								int kapazitaet = ausgabeKapa.get(hilfKap);
								hilfKap++;
								Ausgabezentrum agent = new Ausgabezentrum(kapazitaet);
								Informationszentrum.listeAusgabe.add(agent);
								if (agent != null){
									context.add(agent);
									geography.move(agent, geom);
									Coordinate koordinate = geom.getCoordinate();
									agent.setKoordinaten(koordinate.x, koordinate.y);
									agent.setNumber(nummer);
									nummer++;
									} 
								else {System.out.println("Error creating agent for  " + geom);}
							}
							hilf = hilf + 1;
					}
					
				}
				
				
			}
		
	}
}
