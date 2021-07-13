package krisenplanung_Simulation;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Ausgabezentrum {
	/*Water beschreibt den Wasservorrat. Reservieren beschreibt den Wasservorrat minus den bereits reservierten Wassermengen*/
	/*Wassermengen sind reserviert, wenn sich ein Objekt der Klasse Bevölkerung gerade zum Ausgabezentrum bewegt*/
	private int water;
	private int reservieren;
	private int nachfrage = 0;
	private int number = 0;
	private double xkoordinate;
	private double ykoordinate;
	
	public Ausgabezentrum (int kapazitaet) {
		this.water = kapazitaet;
		this.reservieren = kapazitaet;
	}
	public int getWater () {
		return this.water;
	}
	public int getReservierung() {
		return this.reservieren;
	}
	public int getNachfrage() {
		return this.nachfrage;
	}
	public void setWater (int nachschub) {
		this.water = this.water + nachschub;
		this.reservieren = this.reservieren + nachschub;
	}
	public void setKoordinaten (double xkoordinate, double ykoordinate) {
		this.xkoordinate = xkoordinate;
		this.ykoordinate = ykoordinate;
	}
	public double getXKoordinate () {
		return this.xkoordinate;
	}
	public double getYKoordinate () {
		return this.ykoordinate;
	}
	public void setNumber (int number) {
		this.number = number;
	}
	public int getNummer () {
		return number;
	}
	public void reservierung () {
		if (this.reservieren >= 10) {
			this.reservieren = this.reservieren -10;
		}
		else if (this.reservieren < 10) {
			this.reservieren = 0;
		}
	}
	/*Methode welche dass Energielevel eines Objekts der Klasse Bevoelkerung wieder auffüllt*/
	/*Dabei wird der Wasservorrat herabgesetzt*/
	public int regenaration () {
		int hilfsvariable;
		if (water == 0) {
			return -1;
		} else {
		if (water >= 10) {
			hilfsvariable = 10;
			water = water-5;
			nachfrage = nachfrage + hilfsvariable/2;
			}
		else { hilfsvariable = water;
		nachfrage = nachfrage + hilfsvariable/2;
		water = 0;
		}}
		return hilfsvariable;
	}
	
	
}
