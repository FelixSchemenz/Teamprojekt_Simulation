package krisenplanung_Simulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Informationszentrum {

	public static LinkedList<Ausgabezentrum> listeAusgabe = new LinkedList<Ausgabezentrum>();
	private Coordinate infopunkt, point;
	private Coordinate ausgabepunkt = new Coordinate(0, 0);
	double xhilfskoordinate, yhilfskoordinate;
	private double breitengrad = 101695;
	private double hoehengrad = 111226;
	
	public Informationszentrum (){
	}
	/*Diese Methode vermittelt die optimale Ausgabestelle. Dabei werden zuerst alle Ausgabestellen in einem Radius unter 20
	 * in eine Liste listekurz geschrieben. Danach wird die listekurz durchlaufen und die nächste Ausgabestelle mit Wasser
	 * herausgefunden. In der letzten for-schleife wird die Ausgabestelle ausgegeben*/
	public Coordinate auskunft() {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		infopunkt = geom.getCoordinate();
		double dauer = 1000000000;
		for  (Ausgabezentrum ausgabe : listeAusgabe) {
			Geometry geomAusgabe = geography.getGeometry(ausgabe);
			point = geomAusgabe.getCoordinate();
			double distanz = Math.sqrt(2)*Math.sqrt(breitengrad*breitengrad*(point.x - infopunkt.x)*(point.x - infopunkt.x)+(point.y-infopunkt.y)*(point.y-infopunkt.y)*hoehengrad*hoehengrad);
			if (distanz <= 20000.00) {
				if (ausgabe.getReservierung() > 0) {
					if (distanz < dauer) {
						dauer = distanz;
						ausgabepunkt = point;
					}
				}
		}
		}
		for  (Ausgabezentrum ausgabe : listeAusgabe) {
			Geometry geomAusgabe = geography.getGeometry(ausgabe);
			point = geomAusgabe.getCoordinate();
			if (point.x == ausgabepunkt.x && point.y == ausgabepunkt.y) {
				ausgabe.reservierung();
			}
		}
		if (ausgabepunkt.x != 0 || ausgabepunkt.x != 0) {
			return ausgabepunkt;
		} else return null;
	}
}
