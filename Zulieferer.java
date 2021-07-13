package krisenplanung_Simulation;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Zulieferer {
	private int menge;
	private Coordinate ankunftsstelle = new Coordinate(0,0);
	private Coordinate ausgabepunkt = new Coordinate(0,0);
	private Coordinate myPoint = new Coordinate(0,0);
	private double breitengrad = 101695;
	private double hoehengrad = 111226;
	private int warten;
	private int blocker = 0;
	private int zurueck = 0;
	private int start = 0;
	/*Bei Objekterzeugung wird jedem Zulieferer eine Ankunftsstelle zugeordnet. Außerdem muss er zu Beginn fünf Ticks warten.*/
	public Zulieferer (int menge, double x, double y) {
		this.menge = menge;
		ankunftsstelle.x = x;
		ankunftsstelle.y = y;
		warten = 5;
	}
	public double getAusgabepunkt () {
		return this.ausgabepunkt.x;
	}
	public void ausliefern (Ausgabezentrum ausgabestelle) {
		ausgabestelle.setWater(menge);
	}
	/*Methode zur Entscheidungsfindung des Zulieferers*/
	/*Es wird die Ausgabestelle mit gerinsgtem Wasservorrat angesteuert. Ist dies bei mehreren Ausgabestellen der Fall, wird die nächste genommen*/
	public void entscheidung () {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		for (Ausgabezentrum aus : Informationszentrum.listeAusgabe) {
			Geometry geom = geography.getGeometry(aus);
			if (aus.getWater() == 0) {
				ausgabepunkt = geom.getCoordinate();
				return;}
			}
		int hilfsvariable = 1000000000;
		for (Ausgabezentrum aus : Informationszentrum.listeAusgabe) {
			Geometry geom = geography.getGeometry(aus);
			if (aus.getWater() <= hilfsvariable) {
				ausgabepunkt = geom.getCoordinate();
				hilfsvariable = aus.getWater();
				}
		}
				return;
	}
	/*Methode, welche für den Bewegungsablauf des Zulieferers verantwortlich ist*/
	@ScheduledMethod(start = 0, interval = 1)
	public void lieferung () {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		myPoint = geom.getCoordinate();
		/*Der Zulieferer bewegt sich nicht die gesamte Zeit, sondern pausiert auch bei der Ankunftsstelle*/
		if (warten == 0) {
			if (zurueck != 1) {
			if (blocker !=1) {
				myPoint = geom.getCoordinate();
				entscheidung();
				blocker = 1;
				}
			/*Falls der Zulieferer fertig mit warten ist, bewegt er sich zur Ausgabestelle*/
				if (menge > 0) {
					if (myPoint.x != ausgabepunkt.x || myPoint.y != ausgabepunkt.y) {
						moveLieferung();
						return;
					}
					else 	{
							for (Ausgabezentrum aus : Informationszentrum.listeAusgabe) {
								Coordinate point = new Coordinate(0,0);
								Geometry geomAusgabe = geography.getGeometry(aus);
								point = geomAusgabe.getCoordinate();
								if (point.x == myPoint.x && point.y == myPoint.y) {
									aus.setWater(menge);
									menge = 0;
									zurueck = 1;
									return;
								}
							}
					}
				}
			}
			/*Falls der Zulieferer die Ware abgeliefert hat, bewegt er sich zurück zur Ankunftsstelle*/
				if (myPoint.x != ankunftsstelle.x || myPoint.y != ankunftsstelle.y) {
					moveToAnkunftsstelle();
					return;
				} else {
					menge = 20;
					warten = 6;
					blocker = 0;
					zurueck = 0;
				}
				
			}
		warten = warten-1;
		return;
		
	}
	/*Bewegungsmethode zur ausgewählten Ausgabestelle*/
	public void moveLieferung () {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		myPoint = geom.getCoordinate();
		double distanz = Math.sqrt(breitengrad*breitengrad*(myPoint.x-ausgabepunkt.x)*(myPoint.x-ausgabepunkt.x)+hoehengrad*hoehengrad*(myPoint.y-ausgabepunkt.y)*(myPoint.y-ausgabepunkt.y));
		double angle = winkel(myPoint, ausgabepunkt);
		if (distanz>48000) {
			geography.moveByVector(this, 48000, angle);
		}
		else {
			GeometryFactory fac = new GeometryFactory();
			Point geomtest = fac.createPoint(ausgabepunkt);
			geography.move(this, geomtest);
		}
		myPoint = geom.getCoordinate();
	}
	/*Bewegungsmethode zur ausgewählten Ankunftsstelle*/
	public void moveToAnkunftsstelle () {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		myPoint = geom.getCoordinate();
		double distanz = Math.sqrt(breitengrad*breitengrad*(myPoint.x-ankunftsstelle.x)*(myPoint.x-ankunftsstelle.x)+hoehengrad*hoehengrad*(myPoint.y-ankunftsstelle.y)*(myPoint.y-ankunftsstelle.y));
		double angle = winkel(myPoint, ankunftsstelle);
		if (distanz>48000) {
			geography.moveByVector(this, 48000, angle);
		}
		else {
			GeometryFactory fac = new GeometryFactory();
			Point geomtest = fac.createPoint(ankunftsstelle);
			geography.move(this, geomtest);
		}
		myPoint = geom.getCoordinate();
	}
	/*Methode zur Berechnung des Winkels*/
	public double winkel (Coordinate punkt1, Coordinate punkt2) {
		double winkel = Math.atan((punkt2.y - punkt1.y)/(punkt2.x - punkt1.x));
		if ((punkt2.y - punkt1.y) < 0 && (punkt2.x - punkt1.x) < 0) {
			winkel = winkel + Math.PI;
		}
		if ((punkt2.y - punkt1.y) > 0 && (punkt2.x - punkt1.x) < 0) {
			winkel = winkel + Math.PI;
		}
		while(winkel < 0) {
			winkel = winkel + 2*Math.PI;
		}
		while (winkel >= 2*Math.PI) {
			winkel = winkel - 2*Math.PI;
		}
		return winkel;
	}
}
