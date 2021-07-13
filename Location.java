package krisenplanung_Simulation;
/*Hilfsklasse zum Speichern von Koordinaten*/
public class Location {
	double xkoordinate;
	double ykoordinate;
	int anzahl;
	
	public Location (double xkoordinate, double ykoordinate, int anzahl) {
		this.xkoordinate = xkoordinate;
		this.ykoordinate = ykoordinate;
		this.anzahl = anzahl;
	}
}
