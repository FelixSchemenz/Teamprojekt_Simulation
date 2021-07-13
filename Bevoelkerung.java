package krisenplanung_Simulation;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Bevoelkerung {

	
	private int energylevel;
	private int versorgt, iteration;
	private int gerettet = 1;
	public int ausgabe;
	private double durchschnittlicheDistanz, totaleDistanz;
	public static LinkedList<Informationszentrum> listeInfo = new LinkedList<Informationszentrum>();
	private Ausgabezentrum ausgabestelle = new Ausgabezentrum(-10);
	public Coordinate ausgabepunkt = null;
	private Coordinate infopunkt;
	private Coordinate myPoint = new Coordinate(0,0);
	private double breitengrad = 101695;
	private double hoehengrad = 111226;
	/*Hilfsvariable, welche verhindern soll das bei jedem Aufruf von abbau neue Infostelle gewählt wird*/
	private int blocker = 0;
	private int blockerInfo = 0;
	private int blockerAusgabe = 0;
	
	public Bevoelkerung () {
		/*Zu Beginn wird Energielevel auf zufällige ganze Zahl zwischen 1 und 10 gesetzt*/
		Random rn = new Random();
		int nummer = rn.nextInt(9)+1;
		this.energylevel = nummer;
		this.versorgt = 1;
	}
	/*Wichtigste Methode die in jedem Schritt aufgerufen wird*/
	@ScheduledMethod(start = 1, interval = 1)
	public void abbau () {
		if (gerettet != 0) {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		/*Pro Iteration wird Energie abgebaut und Person bewegt sich einmal.*/
		if (energylevel > 0) {
			energylevel = energylevel-1;
			move();
			return;
		}
		/*Zuerst muss die nächste Infostelle gefunden werden*/
		/*Dieser Teil wird nur durchlaufen, falls die nächste Ausgbestelle noch nicht identifiziert wurde*/
		if (blocker != 1) {
			infopunkt = new Coordinate(0,0);
			blocker = 1;
			blockerInfo = 0;
			double kurz = 100000000;
		for (Informationszentrum info : listeInfo) {
			Geometry geomInfo = geography.getGeometry(info);
			Coordinate point = new Coordinate(0,0);
			point = geomInfo.getCoordinate();
			double distanz = Math.sqrt(breitengrad*breitengrad*(point.x - myPoint.x)*(point.x - myPoint.x)+hoehengrad*hoehengrad*(point.y-myPoint.y)*(point.y-myPoint.y));
			if (distanz <= kurz) {
				infopunkt = geomInfo.getCoordinate();
				kurz = distanz;
		}
		}
		}
		/*Jetzt bewegen wir uns zur nächsten Infostelle*/
		if (blockerInfo == 0) {
		if (myPoint.x == infopunkt.x && myPoint.y == infopunkt.y) {
			blockerInfo = 1;} 
		else {
			moveToInfo(infopunkt);
			/*Nun wird überprüft ob durch Interaktion mit einer anderern Person bereits der nächste Ausgabeort bekannt ist*/
			/*Falls ja, muss nichtmehr zur Infostelle gelaufen werden*/
			if (ausgabepunkt != null) {
				double weite = Math.sqrt(breitengrad*breitengrad*(myPoint.x-ausgabepunkt.x)*(myPoint.x-ausgabepunkt.x)+hoehengrad*hoehengrad*(myPoint.y-ausgabepunkt.y)*(myPoint.y-ausgabepunkt.y));
				setDurchschnittlicheDistanz(weite);
				blockerInfo = 1;
			}
			return;}
		}
		
		/*Dann wird in der Infostelle nach der nächsten Ausgabestelle gefragt*/
		/*Der Blocker ist für den Fall da dass durch Interaktion mit einer anderern Person bereits der nächste Ausgabeort bekannt ist*/
		/*Dann muss nicht in der Infostelle nachgefragt werden*/ 
		if (blockerAusgabe == 0) {
		for (Informationszentrum info : listeInfo) {
			blockerAusgabe = 1;
			Geometry geomInfo = geography.getGeometry(info);
			Coordinate point = new Coordinate(0,0);
			point = geomInfo.getCoordinate();
			/*Es muss die auskunft-Methode der richtigen Infostelle abgerufen werden*/
			if (infopunkt.x == point.x && infopunkt.y == point.y) {
				ausgabepunkt = info.auskunft();	}
				}
				/*Falls ausgabepunkt == null, liegt keine Ausgabestelle in der Umgebung*/
				/*Der zaehler wird erhöht und die Person auf den Nullpunkt gesetzt*/
				if (ausgabepunkt == null) {
					System.out.println("Ich wurde nicht versorgt!");
					this.versorgt = 0;
					this.gerettet = 0;
					KrisenplanungBuilder.zaehler = KrisenplanungBuilder.zaehler + 1;
					ausgabepunkt = new Coordinate(0,0);
					GeometryFactory fac = new GeometryFactory();
					Point ende = fac.createPoint(ausgabepunkt);
					geography.move(this, ende);
				} 
				else {
					/*Entfernung zur nächsten Ausgabestelle wird in durchschnittlicher und totaler Distanz aufgenommen*/
					double weite = Math.sqrt(breitengrad*breitengrad*(myPoint.x-ausgabepunkt.x)*(myPoint.x-ausgabepunkt.x)+hoehengrad*hoehengrad*(myPoint.y-ausgabepunkt.y)*(myPoint.y-ausgabepunkt.y));
					setDurchschnittlicheDistanz(weite);
				}
			}
		}
		/*Die Person bewegt sich zur nächsten Ausgabestelle*/
		moveToAusgabe(ausgabepunkt);
		/*Falls Energielevel wieder aufgefüllt wurde, wird werden Blocker wieder zurückgesetzt und Kreislauf beginnt von vorne*/
		if (energylevel > 0) {
			blocker = 0;
			infopunkt = null;
			ausgabepunkt = null;
			blockerAusgabe = 0;
		}
		return;
	}
	
	public void setEnergylevel (int auffüllen) {
		this.energylevel = auffüllen;
	}
	public int getEnergylevel () {
		return energylevel;
	}
	
	public void setAusgabestelle (Ausgabezentrum ausgabestelle) {
		this.ausgabestelle = ausgabestelle;
	}
	public Ausgabezentrum getAusgabestelle() {
		return this.ausgabestelle;
	}
	public Ausgabezentrum interaktion() {
		if (this.ausgabestelle == null) {
			return null;}
			else { return ausgabestelle;
			}		
	}
	public void setMyPoint(Coordinate coord) {
		this.myPoint = coord;
	}
	public Coordinate getMyPoint() {
		return this.myPoint;
	}
	public double getXKoordinate () {
		return myPoint.x;
	}
	public double getYKoordinate () {
		return myPoint.y;
	}
	public int getVersorgt () {
		return this.versorgt;
	}
	public double getDurchschnittlicheDistanz () {
		return durchschnittlicheDistanz;
	}
	/*Durchschnittliche Distanz wird berechnet indem gesamte Distanz durch Kreisdurchläufe geteilt wird*/
	public void setDurchschnittlicheDistanz (double distanz) {
		iteration = iteration + 1;
		if (iteration != 0) {
			totaleDistanz = totaleDistanz + distanz;
		durchschnittlicheDistanz = totaleDistanz / iteration;
		}
	}
	/*Methode gibt alle 100 Iteration den Anteil der Bevölkerung us, welcher versorgt wird. Außerdem wird durschnittliche
	 * Distanz und Anzahl der nicht versorgten Personen angegeben. Methode dient somit zur Auswertung/Bewertung von
	 * Entscheidungen seitens der Optimierung
	 */
	@ScheduledMethod(start = 10, interval = 10)
	public void anteilVersorgt () {
		if (this.ausgabe == 1) {
		if (KrisenplanungBuilder.menge != 0) {
		System.out.println("Anzahl Personen:" + KrisenplanungBuilder.menge);
		System.out.println("Anzahl nicht versorgter Personen: " + KrisenplanungBuilder.zaehler);
		double zahl = KrisenplanungBuilder.zaehler;
		double menge = KrisenplanungBuilder.menge;
		double anteil = (double )(1.0-(zahl / menge))*100;
		System.out.println("Es wurden " + anteil + " % der Bevölkerung ausreichend versorgt!");
		double durchschnittlicherWeg = 0;
		int zaehler = 0;
		/*If-Schleife wurde eingeabut um zu verhindern, dass Personen die kein einziges mal versorgt wurden,
		 * in die durchschnittliche Distanz aufgenommen werden. Dies würde die Ergebnisse verfälschen, da viele
		 * Personen die kein einziges mal versorgt worden sind zu einer niedrigen Distanz führen würden-
		 */
		for (Bevoelkerung bev : KrisenplanungBuilder.bevListe) {
			if (bev.getDurchschnittlicheDistanz() != 0) {
			durchschnittlicherWeg = durchschnittlicherWeg + bev.getDurchschnittlicheDistanz();}
			else { zaehler++;}
		}
		double hilfe = durchschnittlicherWeg / (KrisenplanungBuilder.menge - zaehler);
		System.out.println("Anzahl der Personen die kein einziges mal versorgt wurden: " + zaehler);
		System.out.println("Im Durchschnitt müssen die Personen " + hilfe + " Meter zur nächsten Ausgabestelle laufen!");
		}
		} else return;
	}
	/*Methode, welche den Weg zur Ausgabestelle bestimmt*/
	public void moveToAusgabe (Coordinate ausgabepunkt) {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		myPoint = geom.getCoordinate();
		/*Es wird überprüft ob die Person an der Ausgabestelle angekommen ist*/
		if (myPoint.x == ausgabepunkt.x && myPoint.y == ausgabepunkt.y) {
			/*Falls ja, wird die richtige Ausgabestelle aus der Liste ausgewählt*/
			for  (Ausgabezentrum ausgabe : Informationszentrum.listeAusgabe) {
				Geometry geomAusgabe = geography.getGeometry(ausgabe);
				Coordinate point = new Coordinate(0,0);
				point = geomAusgabe.getCoordinate();
				/*Die richtige Ausgabestelle in der Liste wird gesucht*/
				if (point.x == ausgabepunkt.x && point.y == ausgabepunkt.y) {
					/*Gibt es noch Wasser in der Ausgabestelle?
					 *Falls nein, ist die Wasserversorgung nicht mehr gewährleistet
					 * Versorgt wird auf 0 gesetzt und Agent wird aus Simulation entfernt
					 */
					if (ausgabe.regenaration() == -1) {
						this.versorgt = 0;
						this.gerettet = 0;
						KrisenplanungBuilder.zaehler = KrisenplanungBuilder.zaehler + 1;
						ausgabepunkt = new Coordinate(0,0);
						GeometryFactory fac = new GeometryFactory();
						Point ende = fac.createPoint(ausgabepunkt);
						geography.move(this, ende);
						return;
					} else {
					energylevel = energylevel + ausgabe.regenaration();
					}
				}
			}
			return;
		}
		/*Winkel und Distanz zwischen aktuellem Punkt und Ausgabestelle wird bestimmt*/
		double distanz = Math.sqrt(breitengrad*breitengrad*(myPoint.x-ausgabepunkt.x)*(myPoint.x-ausgabepunkt.x)+hoehengrad*hoehengrad*(myPoint.y-ausgabepunkt.y)*(myPoint.y-ausgabepunkt.y));
		double angle =winkel(myPoint, ausgabepunkt);
		if (distanz>7200) {
			/*Falls die Distanz größer als 2 km ist, bewegt sich die Person auf dem kürzesten Weg 2 km in Richtung der Ausgabestelle*/
			geography.moveByVector(this, 7200, angle);
		}
		else {
			/*Falls die Entfernung geringer als 2 km ist, kommt die Person an der Ausgabestelle an*/
			GeometryFactory fac = new GeometryFactory();
			Point geomtest = fac.createPoint(ausgabepunkt);
			geography.move(this, geomtest);
		}
		myPoint = geom.getCoordinate();
		}
	
	public void moveToInfo (Coordinate infopunkt) {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		myPoint = geom.getCoordinate();
		/*Es wird überprüft ob man auf dem Weg bereits eine Person trifft, welche sich zur 
		 * kürzsten Ausgabestelle bewegt
		 */
		for (Bevoelkerung bev : KrisenplanungBuilder.bevListe) {
			Coordinate secondPoint = bev.getMyPoint();
			if (myPoint.x == secondPoint.x && myPoint.y == secondPoint.y) {
				if (bev.ausgabepunkt != null) {
					this.ausgabepunkt = bev.ausgabepunkt;
				}
			}
		}
		/*Winkel und Distanz zwischen aktuellem Punkt und Infostelle wird bestimmt*/
		double distanz = Math.sqrt(breitengrad*breitengrad*(myPoint.x-infopunkt.x)*(myPoint.x-infopunkt.x)+hoehengrad*hoehengrad*(myPoint.y-infopunkt.y)*(myPoint.y-infopunkt.y));
		double angle = winkel(myPoint, infopunkt);
		if (distanz>7200) {
			/*Falls die Distanz größer als 2 km ist, bewegt sich die Person auf dem kürzesten Weg 2 km in Richtung der Infostelle*/
			geom = geography.moveByVector(this, 7200, angle);
		}
		else {
			/*Falls die Entfernung geringer als 2 km ist, kommt die Person an der Infostelle an*/
			GeometryFactory fac = new GeometryFactory();
			Point geomtest = fac.createPoint(infopunkt);
			geography.move(this, geomtest);
		}
		myPoint = geom.getCoordinate();
	}
	/*Methode, welche aufgerufen wird, falls das Energielevel ungleich 0 ist*/
	public void move () {
		Context context = ContextUtils.getContext(this);
		Geography<Object> geography = (Geography)context.getProjection("Geography");
		Geometry geom = geography.getGeometry(this);
		myPoint = geom.getCoordinate();
		if (energylevel == 0) {
			return;
		}
		/*Beliebiger Winkel zwischen 0 und 359 Grad wird erzeugt. Die Person bewegt sich 2km in die Richtung
		 * Somit hängt die Bewegung der Person in der move Methode vom Zufall ab.
		 */
		Random rn = new Random();
		double angle = (rn.nextInt(359)*Math.PI)/180;
		geom = geography.moveByVector(this, 2000, angle);
		myPoint = geom.getCoordinate();
		}
		
	/*Methode welche den Winkel zwischen 2 Punkten berechnet*/
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
	
